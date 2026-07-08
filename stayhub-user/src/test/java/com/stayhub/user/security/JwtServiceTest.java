package com.stayhub.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                86400000L
        );
        jwtService = new JwtService(properties);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "GUEST");
        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "GUEST");
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "HOST");
        assertThat(jwtService.extractRole(token)).isEqualTo("HOST");
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {
        String token = jwtService.generateToken(UUID.randomUUID(), "test@example.com", "GUEST");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        JwtProperties expiredProperties = new JwtProperties(
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                -1000L
        );
        JwtService expiredJwtService = new JwtService(expiredProperties);
        String token = expiredJwtService.generateToken(UUID.randomUUID(), "test@example.com", "GUEST");
        assertThat(expiredJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forTamperedToken() {
        String token = jwtService.generateToken(UUID.randomUUID(), "test@example.com", "GUEST");
        assertThat(jwtService.isTokenValid(token + "tampered")).isFalse();
    }
}
