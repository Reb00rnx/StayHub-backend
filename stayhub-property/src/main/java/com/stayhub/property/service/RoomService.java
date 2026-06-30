package com.stayhub.property.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.common.exception.ValidationException;
import com.stayhub.property.dto.Room.CreateRoomRequest;
import com.stayhub.property.dto.Room.RoomResponse;
import com.stayhub.property.entity.Property;
import com.stayhub.property.entity.Room;
import com.stayhub.property.entity.RoomStatus;
import com.stayhub.property.repository.PropertyRepository;
import com.stayhub.property.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true, timeout = 10)
    public List<RoomResponse> findByPropertyId(UUID propertyId) {
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
        log.info("List of property rooms provided");
        return roomRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true, timeout = 10)
    public List<RoomResponse> findAvailableRooms(UUID propertyId, LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            throw new ValidationException("Select proper dates");
        }

        List<Room> rooms = roomRepository.findAvailableRooms(propertyId, checkIn, checkOut);
        log.info("List of available rooms provided");

        return rooms.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(timeout = 10)
    public RoomResponse create(UUID propertyId, CreateRoomRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        Room room = new Room();
        room.setProperty(property);
        room.setRoomNumber(request.roomNumber());
        room.setType(request.type());
        room.setPricePerNight(request.pricePerNight());
        room.setMaxGuests(request.maxGuests());
        room.setStatus(RoomStatus.AVAILABLE);

        Room savedRoom = roomRepository.save(room);
        log.info("Room {} created for property {}", savedRoom.getId(), propertyId);
        return mapToResponse(savedRoom);
    }

    @Transactional(timeout = 10)
    public RoomResponse updateStatus(UUID roomId, RoomStatus newStatus) {
        Room room = findRoomEntity(roomId);
        log.info("Room {} status changed from {} to {}", roomId, room.getStatus(), newStatus);
        room.setStatus(newStatus);

        Room savedRoom = roomRepository.save(room);
        return mapToResponse(savedRoom);
    }

    private Room findRoomEntity(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
    }

    private RoomResponse mapToResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getType(),
                room.getPricePerNight(),
                room.getMaxGuests(),
                room.getStatus(),
                room.getProperty().getId());
    }
}