package com.stayhub.property.dto.Property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePropertyRequest(
    @NotBlank
    String name,
    @NotBlank
    String address,
    @NotBlank
    String city,
    @NotBlank
    String country,
    @NotBlank
    String description,
    @NotNull
    UUID ownerId
) {}
