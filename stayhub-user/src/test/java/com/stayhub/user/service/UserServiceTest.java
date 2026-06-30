package com.stayhub.user.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.common.exception.ValidationException;
import com.stayhub.user.dto.CreateUserRequest;
import com.stayhub.user.dto.UpdateUserRequest;
import com.stayhub.user.dto.UserResponse;
import com.stayhub.user.entity.User;
import com.stayhub.user.entity.UserRole;
import com.stayhub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_shouldReturnUser_whenUserExists() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = createTestUser();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.findById(id);

        // then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.firstName()).isEqualTo(user.getFirstName());
        assertThat(response.role()).isEqualTo(user.getRole());
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: " + id);

        verify(userRepository).findById(id);
    }

    @Test
    void findByEmail_shouldReturnUser_whenUserExists() {
        // given
        String email = "test@example.com";
        User user = createTestUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.findByEmail(email);

        // then
        assertThat(response.email()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        // given
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_shouldCreateUser_whenEmailIsUnique() {
        // given
        CreateUserRequest request = new CreateUserRequest("new@example.com", "rawPassword", "Jane", "Doe");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", UUID.fromString("22222222-2222-2222-2222-222222222222"));
            return savedUser;
        });

        // when
        UserResponse response = userService.createUser(request);

        // then
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.firstName()).isEqualTo(request.firstName());
        assertThat(response.role()).isEqualTo(UserRole.GUEST);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowValidationException_whenEmailAlreadyExists() {
        // given
        CreateUserRequest request = new CreateUserRequest("existing@example.com", "rawPassword", "Jane", "Doe");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateOnlyProvidedFields_whenRequestIsPartial() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = createTestUser();
        UpdateUserRequest request = new UpdateUserRequest(null, "Updated", null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserResponse response = userService.updateUser(id, request);

        // then
        assertThat(response.firstName()).isEqualTo("Updated");
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.lastName()).isEqualTo(user.getLastName());
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