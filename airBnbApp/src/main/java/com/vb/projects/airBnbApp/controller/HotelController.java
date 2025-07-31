package com.vb.projects.airBnbApp.controller;

import com.vb.projects.airBnbApp.dto.BookingDto;
import com.vb.projects.airBnbApp.dto.HotelDto;
import com.vb.projects.airBnbApp.dto.HotelReportDto;
import com.vb.projects.airBnbApp.dto.ResponseMessage;
import com.vb.projects.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotel")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDto> createHotel(@RequestBody HotelDto hotelDto) {
       HotelDto hotel = hotelService.createHotel(hotelDto);
       return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long id) {
        HotelDto hotelDto = hotelService.getHotelById(id);
        return new ResponseEntity<>(hotelDto, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getHotelBookings(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelBookings(hotelId));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<HotelDto> updateHotel(@PathVariable Long id, @RequestBody HotelDto hotelDto) {
        HotelDto hotel = hotelService.updateHotel(id, hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<ResponseMessage> deleteHotel(@PathVariable Long id) {
        Boolean isDeleted = hotelService.deleteHotel(id);
        if (isDeleted) {
            return new ResponseEntity<>(new ResponseMessage("Hotel deleted successfully!"),HttpStatus.OK);
        }
        return new ResponseEntity<>(new ResponseMessage("Something went wrong!"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/id/{id}/activate")
    public ResponseEntity<ResponseMessage> activateHotel(@PathVariable Long id) {
        String msg = hotelService.activateHotel(id);
        return new ResponseEntity<>(new ResponseMessage(msg), HttpStatus.OK);
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId, @RequestParam(required = false)LocalDate startDate, @RequestParam(required = false)LocalDate endDate) {
        if(startDate == null) startDate = LocalDate.now().minusMonths(1);
        if(endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(hotelService.getHotelReports(hotelId, startDate, endDate));
    }

}
