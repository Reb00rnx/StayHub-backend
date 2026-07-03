package com.stayhub.user.dto;

public record AuthResponse(
        String token,
        String email,
        String role
) {}
