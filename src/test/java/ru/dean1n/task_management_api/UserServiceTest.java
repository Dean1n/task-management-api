package ru.dean1n.task_management_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.dean1n.task_management_api.dto.AuthResponse;
import ru.dean1n.task_management_api.dto.LoginRequest;
import ru.dean1n.task_management_api.dto.RegisterRequest;
import ru.dean1n.task_management_api.dto.UserResponse;
import ru.dean1n.task_management_api.model.User;
import ru.dean1n.task_management_api.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void registerShouldCreateUser() {
        RegisterRequest request = new RegisterRequest(
                "  denis  ",
                "  DENIS@EXAMPLE.COM  ",
                "strong-password-123"
        );

        when(userRepository.existsByUsername("denis"))
                .thenReturn(false);

        when(userRepository.existsByEmail("denis@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("strong-password-123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    user.beforeInsert();
                    return user;
                });

        UserResponse response = userService.register(request);

        assertEquals(1L, response.id());
        assertEquals("denis", response.username());
        assertEquals("denis@example.com", response.email());
        assertNotNull(response.createdAt());

        verify(passwordEncoder)
                .encode("strong-password-123");

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("denis")
                        && user.getEmail().equals("denis@example.com")
                        && user.getPasswordHash().equals("encoded-password")
        ));
    }

    @Test
    void registerShouldRejectExistingUsername() {
        RegisterRequest request = new RegisterRequest(
                "denis",
                "new@example.com",
                "strong-password-123"
        );

        when(userRepository.existsByUsername("denis"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "Имя пользователя уже занято",
                exception.getReason()
        );

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerShouldRejectExistingEmail() {
        RegisterRequest request = new RegisterRequest(
                "new-user",
                "denis@example.com",
                "strong-password-123"
        );

        when(userRepository.existsByUsername("new-user"))
                .thenReturn(false);

        when(userRepository.existsByEmail("denis@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "Электронная почта уже зарегистрирована",
                exception.getReason()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginShouldReturnJwtForCorrectPassword() {
        User user = createUser();

        when(userRepository.findByUsername("denis"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "strong-password-123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(3600L);

        AuthResponse response = userService.login(
                new LoginRequest(
                        "denis",
                        "strong-password-123"
                )
        );

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());

        verify(jwtService).generateToken(user);
    }

    @Test
    void loginShouldRejectIncorrectPassword() {
        User user = createUser();

        when(userRepository.findByUsername("denis"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(
                        new LoginRequest(
                                "denis",
                                "wrong-password"
                        )
                )
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        assertEquals(
                "Неверное имя пользователя или пароль",
                exception.getReason()
        );

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginShouldRejectUnknownUser() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(
                        new LoginRequest(
                                "unknown",
                                "some-password"
                        )
                )
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(any());
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("denis");
        user.setEmail("denis@example.com");
        user.setPasswordHash("encoded-password");
        user.beforeInsert();
        return user;
    }
}
