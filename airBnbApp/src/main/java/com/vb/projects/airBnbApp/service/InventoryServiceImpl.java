package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.*;
import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.Inventory;
import com.vb.projects.airBnbApp.entity.Room;
import com.vb.projects.airBnbApp.entity.User;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.exception.UnauthorisedException;
import com.vb.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.vb.projects.airBnbApp.repository.InventoryRepository;
import com.vb.projects.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.vb.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusYears(1);
        for(;!today.isAfter(end); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteFutureInventories(Room room) {
        inventoryRepository.deleteByDateAfterAndRoom(LocalDate.now(), room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getPageSize());
        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

        //Business logic - 90 days
        Page<HotelPriceDto> hotels = hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),
                hotelSearchRequest.getRoomsCount(),
                dateCount,
                pageable
        );

        return hotels;
    }

    @Override
    public List<InventoryDto> getAllInventories(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found"));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) {
            throw new UnauthorisedException("You are not authorized to view this inventory");
        }
        List<Inventory> inventories = inventoryRepository.findByRoomOrderByDate(room);
        return inventories.stream().map(inventory -> modelMapper.map(inventory, InventoryDto.class)).toList();
    }

    @Override
    @Transactional
    public void updateInventories(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found"));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) {
            throw new UnauthorisedException("You are not authorized to view this inventory");
        }
        inventoryRepository.findAndLockUpdateInventories(roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());
        inventoryRepository.updateInventories(roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),
                updateInventoryRequestDto.getSurgeFactor(),
                updateInventoryRequestDto.getClosed());
    }
}
