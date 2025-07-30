package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.BookingDto;
import com.vb.projects.airBnbApp.dto.BookingRequest;
import com.vb.projects.airBnbApp.dto.GuestDto;
import com.vb.projects.airBnbApp.entity.*;
import com.vb.projects.airBnbApp.enums.BookingStatus;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.exception.UnauthorisedException;
import com.vb.projects.airBnbApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        for(Inventory inventory : inventories) {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }
        inventoryRepository.saveAll(inventories);

        // create booking
        Booking booking = Booking.builder()
                .hotel(hotel)
                .room(room)
                .bookingStatus(BookingStatus.RESERVED)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .roomsCount(bookingRequest.getRoomsCount())
                .user(getCurrentUser())
                .amount(BigDecimal.TEN)
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

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
       return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
