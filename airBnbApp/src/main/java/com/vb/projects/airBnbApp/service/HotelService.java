package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.BookingDto;
import com.vb.projects.airBnbApp.dto.HotelDto;
import com.vb.projects.airBnbApp.dto.HotelInfoDto;
import com.vb.projects.airBnbApp.dto.HotelReportDto;

import java.time.LocalDate;
import java.util.List;

public interface HotelService {

    HotelDto createHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);
    HotelDto updateHotel(Long id, HotelDto hotelDto);
    Boolean deleteHotel(Long id);
    String activateHotel(Long id);

    HotelInfoDto getHotelDetailsById(Long hotelId);

    List<HotelDto> getAllHotels();

    List<BookingDto> getHotelBookings(Long hotelId);

    HotelReportDto getHotelReports(Long hotelId, LocalDate startDate, LocalDate endDate);
}
