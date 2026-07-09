package com.stayhub.property.dto.Property;

import com.stayhub.property.entity.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PropertySearchCriteria(
        String city,
        String country,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        RoomType roomType,
        LocalDate checkIn,
        LocalDate checkOut
) {}