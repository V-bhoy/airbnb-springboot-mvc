package com.vb.projects.airBnbApp.repository;

import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);
}
