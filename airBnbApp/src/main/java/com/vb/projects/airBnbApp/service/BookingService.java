package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.BookingDto;
import com.vb.projects.airBnbApp.dto.BookingRequest;
import com.vb.projects.airBnbApp.dto.GuestDto;

import java.util.List;

public interface BookingService {
    BookingDto initiateBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guests);

    Object initiatePayments(Long bookingId);

    void cancelBooking(Long bookingId);
}
