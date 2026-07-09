package com.stayhub.property.dto.Property;

import com.stayhub.property.entity.Property;

import java.time.Instant;
import java.util.UUID;

public record PropertyResponse(
    UUID id,
    String name,
    String address,
    String city,
    String country,
    String description,
    UUID ownerId,
    Instant createdAt
) {
    public static PropertyResponse from(Property p) {
        return new PropertyResponse(
            p.getId(), p.getName(), p.getAddress(),
            p.getCity(), p.getCountry(), p.getDescription(),
            p.getOwnerId(), p.getCreatedAt()
        );
    }
}
