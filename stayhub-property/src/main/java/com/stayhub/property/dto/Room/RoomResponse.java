package com.stayhub.property.dto.Room;

import com.stayhub.property.entity.RoomStatus;
import com.stayhub.property.entity.RoomType;
import java.math.BigDecimal;
import java.util.UUID;

public record RoomResponse(

        UUID id,
        String roomNumber,
        RoomType type,
        BigDecimal pricePerNight,
        Integer maxGuests,
        RoomStatus status,
        UUID propertyId
) {}
