package com.stayhub.user.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.user.dto.AuthResponse;
import com.stayhub.user.dto.CreateUserRequest;
import com.stayhub.user.dto.LoginRequest;
import com.stayhub.user.dto.UserResponse;
import com.stayhub.user.entity.User;
import com.stayhub.user.entity.UserRole;
import com.stayhub.user.repository.UserRepository;
import com.stayhub.user.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // given
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("mocked-token");

        // when
        AuthResponse response = authService.login(new LoginRequest("test@example.com", "rawPassword"));

        // then
        assertThat(response.token()).isEqualTo("mocked-token");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo("GUEST");
    }

    @Test
    void login_shouldThrowResourceNotFoundException_whenEmailNotFound() {
        // given
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@example.com", "password")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldThrowResourceNotFoundException_whenPasswordIsWrong() {
        // given
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashed_password")).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrongPassword")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void register_shouldReturnToken_whenRegistrationIsSuccessful() {
        // given
        CreateUserRequest request = new CreateUserRequest("new@example.com", "password", "Jane", "Doe", UserRole.GUEST);
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserResponse userResponse = new UserResponse(userId, "new@example.com", "Jane", "Doe", UserRole.GUEST, Instant.now());

        when(userService.createUser(request)).thenReturn(userResponse);
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("mocked-token");

        // when
        AuthResponse response = authService.register(request);

        // then
        assertThat(response.token()).isEqualTo("mocked-token");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.role()).isEqualTo("GUEST");
    }

    private User createTestUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setFirstName("John");
        user.setLastName("Test");
        user.setRole(UserRole.GUEST);
        return user;
    }
}
