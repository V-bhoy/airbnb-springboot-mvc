package com.vb.projects.airBnbApp.controller;

import com.vb.projects.airBnbApp.dto.BookingDto;
import com.vb.projects.airBnbApp.dto.BookingRequest;
import com.vb.projects.airBnbApp.dto.GuestDto;
import com.vb.projects.airBnbApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/initiate")
    public ResponseEntity<BookingDto> initiateBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initiateBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guests) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guests));
    }
}
