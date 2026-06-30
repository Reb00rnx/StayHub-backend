package com.stayhub.user.dto;

import com.stayhub.user.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        Instant createdAt
) {}
