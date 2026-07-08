package com.stayhub.user.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.user.dto.AuthResponse;
import com.stayhub.user.dto.CreateUserRequest;
import com.stayhub.user.dto.LoginRequest;
import com.stayhub.user.dto.UserResponse;
import com.stayhub.user.entity.User;
import com.stayhub.user.repository.UserRepository;
import com.stayhub.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", request.email());
            throw new ResourceNotFoundException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        log.info("User {} logged in successfully", user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse register(CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        String token = jwtService.generateToken(created.id(), created.email(), created.role().name());
        log.info("User {} registered successfully", created.email());
        return new AuthResponse(token, created.email(), created.role().name());
    }
}
