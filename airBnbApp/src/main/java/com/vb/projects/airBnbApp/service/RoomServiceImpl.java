package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.dto.RoomDto;
import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.Room;
import com.vb.projects.airBnbApp.entity.User;
import com.vb.projects.airBnbApp.exception.ResourceNotFoundException;
import com.vb.projects.airBnbApp.exception.UnauthorisedException;
import com.vb.projects.airBnbApp.repository.HotelRepository;
import com.vb.projects.airBnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public RoomDto createRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorisedException("You are not authorized to perform this action. Please contact the admin for this hotel");
        }
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);
        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsByHotelId(Long hotelId) {
        log.info("Fetching all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+hotelId));
        List<Room> rooms = hotel.getRooms();
        return rooms.stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long id) {
        log.info("Fetching room with ID: {}", id);
        Room room = roomRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+id));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        return null;
    }

    @Override
    public String deleteRoom(Long id) {
      log.info("Deleting room with ID: {}", id);
      Room room = roomRepository
              .findById(id)
              .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+id));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnauthorisedException("You are not authorized to perform this action. Please contact the admin for this hotel");
        }
      inventoryService.deleteFutureInventories(room);
      roomRepository.deleteById(id);
      return "Room deleted successfully!";
    }
}
