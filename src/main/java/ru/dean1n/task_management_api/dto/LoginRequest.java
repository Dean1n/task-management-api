package ru.dean1n.task_management_api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Имя пользователя обязательно")
        String username,

        @NotBlank(message = "Пароль обязателен")
        String password
) {
}