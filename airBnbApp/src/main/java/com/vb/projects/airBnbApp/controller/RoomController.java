package com.vb.projects.airBnbApp.controller;

import com.vb.projects.airBnbApp.dto.ResponseMessage;
import com.vb.projects.airBnbApp.dto.RoomDto;
import com.vb.projects.airBnbApp.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotel/{hotelId}/room")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto) {
        RoomDto newRoom = roomService.createRoom(hotelId, roomDto);
        return new ResponseEntity<>(newRoom, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoomDto>> getAllRooms(@PathVariable Long hotelId) {
        List<RoomDto> rooms = roomService.getAllRoomsByHotelId(hotelId);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        RoomDto roomDto = roomService.getRoomById(id);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<ResponseMessage> deleteRoomById(@PathVariable Long id) {
        String msg = roomService.deleteRoom(id);
        return new ResponseEntity<>(new ResponseMessage(msg), HttpStatus.OK);
    }

    @PutMapping("/id/{roomId}")
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable Long hotelId, @PathVariable Long roomId, @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoom(hotelId, roomId, roomDto));
    }
}
