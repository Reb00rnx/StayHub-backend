package com.stayhub.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingEvent(
        UUID bookingId,
        UUID guestId,
        UUID roomId,
        String status,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        Instant occurredAt
) {}
