package ru.dean1n.task_management_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.dean1n.task_management_api.dto.AuthResponse;
import ru.dean1n.task_management_api.dto.LoginRequest;
import ru.dean1n.task_management_api.dto.RegisterRequest;
import ru.dean1n.task_management_api.dto.UserResponse;
import ru.dean1n.task_management_api.model.User;
import ru.dean1n.task_management_api.repository.UserRepository;

@Service
public class UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Неверное имя пользователя или пароль"
                ));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Неверное имя пользователя или пароль"
            );
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Имя пользователя уже занято"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Электронная почта уже зарегистрирована"
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}