package com.stayhub.property.dto.Room;


import com.stayhub.property.entity.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRoomRequest(

        @NotBlank
        String roomNumber,
        @NotNull
        RoomType type,
        @NotNull
        BigDecimal pricePerNight,
        @NotNull
        Integer maxGuests


) {}
