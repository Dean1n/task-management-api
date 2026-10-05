package ru.dean1n.task_management_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dean1n.task_management_api.dto.AuthResponse;
import ru.dean1n.task_management_api.dto.LoginRequest;
import ru.dean1n.task_management_api.dto.RegisterRequest;
import ru.dean1n.task_management_api.dto.UserResponse;
import ru.dean1n.task_management_api.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response =
                userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return userService.login(request);
    }
}