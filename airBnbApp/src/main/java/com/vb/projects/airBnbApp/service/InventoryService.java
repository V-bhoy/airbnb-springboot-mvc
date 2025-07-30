package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.HotelDto;
import com.vb.projects.airBnbApp.dto.HotelSearchRequest;
import com.vb.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
    void initializeRoomForAYear(Room room);
    void deleteFutureInventories(Room room);

    Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
