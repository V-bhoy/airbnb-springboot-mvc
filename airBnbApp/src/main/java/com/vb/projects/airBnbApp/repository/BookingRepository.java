package com.vb.projects.airBnbApp.repository;

import com.vb.projects.airBnbApp.entity.Booking;
import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String paymentSessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime from, LocalDateTime to);

    List<Booking> findByUser(User user);
}
