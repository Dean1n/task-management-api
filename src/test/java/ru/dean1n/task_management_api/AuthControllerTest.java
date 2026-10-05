package ru.dean1n.task_management_api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.dean1n.task_management_api.dto.AuthResponse;
import ru.dean1n.task_management_api.dto.RegisterRequest;
import ru.dean1n.task_management_api.dto.UserResponse;
import ru.dean1n.task_management_api.exception.GlobalExceptionHandler;
import ru.dean1n.task_management_api.service.UserService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        AuthController.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registerShouldReturnCreatedUser() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "denis",
                "denis@example.com",
                Instant.parse("2026-10-05T10:00:00Z")
        );

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "denis",
                                  "email": "denis@example.com",
                                  "password": "strong-password-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        ))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("denis"))
                .andExpect(jsonPath("$.email")
                        .value("denis@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerShouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "incorrect-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Ошибка валидации"))
                .andExpect(jsonPath(
                        "$.validationErrors.username"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.email"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.password"
                ).exists());
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        AuthResponse response = new AuthResponse(
                "jwt-token",
                "Bearer",
                3600L
        );

        when(userService.login(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "denis",
                                  "password": "strong-password-123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("jwt-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(3600));
    }

    @Test
    void loginShouldReturnUnauthorizedForWrongCredentials()
            throws Exception {

        when(userService.login(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Неверное имя пользователя или пароль"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "denis",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Неверное имя пользователя или пароль"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/login"));
    }

    @Test
    void loginShouldRejectEmptyBodyFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.validationErrors.username"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.password"
                ).exists());
    }
}
