package com.stayhub.user.dto;

public record UpdateUserRequest(
        String email,
        String firstName,
        String lastName
) {
}
