package com.vb.projects.airBnbApp.controller;

import com.vb.projects.airBnbApp.dto.HotelDto;
import com.vb.projects.airBnbApp.dto.HotelInfoDto;
import com.vb.projects.airBnbApp.dto.HotelPriceDto;
import com.vb.projects.airBnbApp.dto.HotelSearchRequest;
import com.vb.projects.airBnbApp.service.HotelService;
import com.vb.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @PostMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest) {
       Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchRequest);
       return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/details")
    public ResponseEntity<HotelInfoDto> getHotel(@PathVariable Long hotelId) {
       return ResponseEntity.ok(hotelService.getHotelDetailsById(hotelId));
    }
}
