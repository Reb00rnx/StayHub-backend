package com.stayhub.property.dto.Property;

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
) {}
