package com.stayhub.property.controller;

import com.stayhub.property.dto.Room.CreateRoomRequest;
import com.stayhub.property.dto.Room.RoomResponse;
import com.stayhub.property.entity.RoomStatus;
import com.stayhub.property.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomResponse>> findByPropertyId(@PathVariable UUID propertyId) {
        List<RoomResponse> rooms = roomService.findByPropertyId(propertyId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> findAvailableRooms(
            @PathVariable UUID propertyId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut) {
        List<RoomResponse> rooms = roomService.findAvailableRooms(propertyId, checkIn, checkOut);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(
            @PathVariable UUID propertyId,
            @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.create(propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{roomId}/status")
    public ResponseEntity<RoomResponse> updateStatus(
            @PathVariable UUID propertyId,
            @PathVariable UUID roomId,
            @RequestParam RoomStatus status) {
        RoomResponse response = roomService.updateStatus(roomId, status);
        return ResponseEntity.ok(response);
    }
}