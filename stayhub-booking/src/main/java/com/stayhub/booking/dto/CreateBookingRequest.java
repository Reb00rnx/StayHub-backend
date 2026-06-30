package com.stayhub.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull
    UUID roomId,
    @NotNull
    UUID guestId,
    @NotNull
    LocalDate checkIn,
    @NotNull
    LocalDate checkOut
)
{}
