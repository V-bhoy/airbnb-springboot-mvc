package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.HotelDto;
import com.vb.projects.airBnbApp.dto.HotelInfoDto;
import com.vb.projects.airBnbApp.dto.RoomDto;
import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.Room;
import com.vb.projects.airBnbApp.entity.User;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.exception.UnauthorisedException;
import com.vb.projects.airBnbApp.repository.HotelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final RoomService roomService;
    private final ModelMapper modelMapper;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        log.info("Creating a new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false); // initially it is inactive and has no inventories
        User user = getCurrentUser();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Hotel created with hotel id: {}", hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Creating a new hotel with Id: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        log.info("Updating a hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        modelMapper.map(hotelDto, hotel );
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public Boolean deleteHotel(Long id) {
        log.info("Deleting a hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        for (Room room : hotel.getRooms()) {
            roomService.deleteRoom(room.getId());
        }
        hotelRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public String activateHotel(Long id) {
        log.info("Activating hotel with Id: {}", id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnauthorisedException("You are not the owner of this hotel! Unable to access this resource.");
        }
        if (hotel.getActive()) {
            return "The hotel is already activated!";
        }
        hotel.setActive(true);
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
        hotelRepository.save(hotel);
        return "Hotel activated successfully!";
    }

    @Override
    public HotelInfoDto getHotelDetailsById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        List<RoomDto> rooms = hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .toList();
        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

}
