package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.HotelDto;

public interface HotelService {

    HotelDto createHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);
    HotelDto updateHotel(Long id, HotelDto hotelDto);
    Boolean deleteHotel(Long id);
    String activateHotel(Long id);
}
