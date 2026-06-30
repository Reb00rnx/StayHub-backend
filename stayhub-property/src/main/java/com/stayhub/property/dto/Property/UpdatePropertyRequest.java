package com.stayhub.property.dto.Property;

public record UpdatePropertyRequest(
    String name,
    String address,
    String city,
    String country,
    String description
)
{}
