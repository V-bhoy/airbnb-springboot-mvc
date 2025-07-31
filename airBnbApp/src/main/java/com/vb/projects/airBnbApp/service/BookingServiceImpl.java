package com.vb.projects.airBnbApp.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.vb.projects.airBnbApp.dto.BookingDto;
import com.vb.projects.airBnbApp.dto.BookingRequest;
import com.vb.projects.airBnbApp.dto.GuestDto;
import com.vb.projects.airBnbApp.entity.*;
import com.vb.projects.airBnbApp.enums.BookingStatus;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.exception.UnauthorisedException;
import com.vb.projects.airBnbApp.repository.*;
import com.vb.projects.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.vb.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initiateBooking(BookingRequest bookingRequest) {
        log.info("Initializing booking request: {}", bookingRequest);
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+bookingRequest.getHotelId()));
        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(()->new ResourceNotFoundException("Room not found with ID: "+bookingRequest.getRoomId()));
        List<Inventory> inventories = inventoryRepository.findAndLockAvailableInventory(
                bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );
        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;
        if(inventories.size() != daysCount) {
            throw new IllegalStateException("Rooms not available for the given days!");
        }
        // reserve rooms
        inventoryRepository.initBooking(room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount());

        // calculate dynamic amount
        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventories);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        // create booking
        Booking booking = Booking.builder()
                .hotel(hotel)
                .room(room)
                .bookingStatus(BookingStatus.RESERVED)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .roomsCount(bookingRequest.getRoomsCount())
                .user(getCurrentUser())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guests) {
        log.info("Adding guests to the requested booking Id: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with ID: "+bookingId));
        User user = getCurrentUser();

        if(!user.equals(booking.getUser())) {
           throw new UnauthorisedException("You are not authorized to add guests to the requested booking");
        }
        if(hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking expired!");
        }
        if(booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests!");
        }

        for (GuestDto guestDto : guests) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        };
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public Object initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with ID: "+bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())) {
            throw new UnauthorisedException("You are not authorized to make payment to the requested booking");
        }
        if(hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking expired!");
        }
        if(booking.getBookingStatus() != BookingStatus.GUESTS_ADDED) {
            throw new IllegalStateException("Found no guests for booking!");
        }
        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"/payment/success", frontendUrl+"/payment/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with ID: "+bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())) {
            throw new UnauthorisedException("You are not authorized to access the requested booking");
        }
        if(booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled!");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        // handle refund
        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
        }catch(StripeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream().map(booking -> modelMapper.map(booking, BookingDto.class)).toList();
    }

    @Transactional
    public void capturePayment(Booking booking){
        Booking existingBooking = bookingRepository.findByPaymentSessionId(booking.getPaymentSessionId())
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with session ID: "+booking.getPaymentSessionId()));
        existingBooking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(existingBooking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());
        inventoryRepository.confirmBooking(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());
        log.info("Booking confirmed successfully for booking ID: {}", booking.getId());

    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
