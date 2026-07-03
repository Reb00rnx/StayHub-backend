package com.stayhub.user.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.common.exception.ValidationException;
import com.stayhub.user.dto.CreateUserRequest;
import com.stayhub.user.dto.UpdateUserRequest;
import com.stayhub.user.dto.UserResponse;
import com.stayhub.user.entity.User;
import com.stayhub.user.entity.UserRole;
import com.stayhub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true, timeout = 10)
    public UserResponse findById(UUID id) {
        return mapToResponse(findUserEntity(id));
    }

    @Transactional(readOnly = true, timeout = 10)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Transactional(timeout = 10)
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("User creation failed - email already exists: {}", request.email());
            throw new ValidationException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role() != null ? request.role() : UserRole.GUEST);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return mapToResponse(savedUser);
    }

    @Transactional(timeout = 10)
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUserEntity(id);

        if (request.email() != null) {
            log.info("User email updated: {}", request.email());
            user.setEmail(request.email());
        }
        if (request.firstName() != null) {
            log.info("User first name updated: {}", request.firstName());
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            log.info("User last name updated: {}", request.lastName());
            user.setLastName(request.lastName());
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    private User findUserEntity(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getCreatedAt());
    }
}