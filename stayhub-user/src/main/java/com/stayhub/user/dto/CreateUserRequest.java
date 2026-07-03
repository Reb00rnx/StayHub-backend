package com.stayhub.user.dto;

import com.stayhub.user.entity.UserRole;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        UserRole role
) {}
