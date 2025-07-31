package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.RoomDto;

import java.util.List;

public interface RoomService {
    RoomDto createRoom(Long hotelId, RoomDto roomDto);
    List<RoomDto> getAllRoomsByHotelId(Long hotelId);
    RoomDto getRoomById(Long id);
    RoomDto updateRoom(Long hotelId, Long roomId, RoomDto roomDto);
    String deleteRoom(Long id);
}
