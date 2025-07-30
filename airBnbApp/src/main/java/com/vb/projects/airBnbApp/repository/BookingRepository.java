package com.vb.projects.airBnbApp.repository;

import com.vb.projects.airBnbApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String paymentSessionId);
}
