package ru.dean1n.task_management_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Имя пользователя обязательно")
        @Size(
                min = 3,
                max = 50,
                message = "Имя пользователя должно содержать от 3 до 50 символов"
        )
        String username,

        @NotBlank(message = "Электронная почта обязательна")
        @Email(message = "Некорректная электронная почта")
        @Size(
                max = 255,
                message = "Электронная почта слишком длинная"
        )
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(
                min = 8,
                max = 100,
                message = "Пароль должен содержать от 8 до 100 символов"
        )
        String password

) {
}