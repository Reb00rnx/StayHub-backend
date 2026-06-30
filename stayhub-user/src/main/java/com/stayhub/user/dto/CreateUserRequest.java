package com.stayhub.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank
        String email,
        @NotBlank
        String password ,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName
){}
