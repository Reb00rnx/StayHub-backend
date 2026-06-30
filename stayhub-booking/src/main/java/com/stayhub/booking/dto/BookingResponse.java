package com.stayhub.booking.dto;

import com.stayhub.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    UUID roomId,
    UUID guestId,
    LocalDate checkIn,
    LocalDate checkOut,
    BookingStatus status,
    BigDecimal totalPrice,
    Instant createdAt
){}
