package com.stayhub.booking.controller;

import com.stayhub.booking.dto.BookingResponse;
import com.stayhub.booking.dto.CreateBookingRequest;
import com.stayhub.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(@PathVariable UUID id) {
        BookingResponse response = bookingService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<BookingResponse>> findByGuestId(@PathVariable UUID guestId) {
        List<BookingResponse> response = bookingService.findByGuestId(guestId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable UUID id) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(response);
    }
}
