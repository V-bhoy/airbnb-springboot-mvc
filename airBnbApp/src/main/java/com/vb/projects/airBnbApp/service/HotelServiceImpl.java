package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.*;
import com.vb.projects.airBnbApp.entity.Booking;
import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.Room;
import com.vb.projects.airBnbApp.entity.User;
import com.vb.projects.airBnbApp.enums.BookingStatus;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.exception.UnauthorisedException;
import com.vb.projects.airBnbApp.repository.BookingRepository;
import com.vb.projects.airBnbApp.repository.HotelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.vb.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final RoomService roomService;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        log.info("Creating a new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false); // initially it is inactive and has no inventories
        User user = getCurrentUser();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Hotel created with hotel id: {}", hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Creating a new hotel with Id: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        log.info("Updating a hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        modelMapper.map(hotelDto, hotel );
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public Boolean deleteHotel(Long id) {
        log.info("Deleting a hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        for (Room room : hotel.getRooms()) {
            roomService.deleteRoom(room.getId());
        }
        hotelRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public String activateHotel(Long id) {
        log.info("Activating hotel with Id: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        if (hotel.getActive()) {
            return "The hotel is already activated!";
        }
        hotel.setActive(true);
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
        hotelRepository.save(hotel);
        return "Hotel activated successfully!";
    }

    @Override
    public HotelInfoDto getHotelDetailsById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        List<RoomDto> rooms = hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .toList();
        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels.stream().map(hotel -> modelMapper.map(hotel, HotelDto.class)).toList();
    }

    @Override
    public List<BookingDto> getHotelBookings(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not allowed to access the hotel resources!");
        }
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return bookings.stream().map(booking -> modelMapper.map(booking, BookingDto.class)).toList();
    }

    @Override
    public HotelReportDto getHotelReports(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not allowed to access the hotel resources!");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);
        Long totalConfirmedBookings = bookings.stream().filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED).count();
        BigDecimal totalRevenue = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);
        return new HotelReportDto(totalConfirmedBookings, totalRevenue, avgRevenue);
    }

}
