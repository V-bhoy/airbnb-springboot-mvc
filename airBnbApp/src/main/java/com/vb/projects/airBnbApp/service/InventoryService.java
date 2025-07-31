package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.*;
import com.vb.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    void initializeRoomForAYear(Room room);
    void deleteFutureInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventories(Long roomId);

    void updateInventories(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
