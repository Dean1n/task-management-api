package ru.dean1n.task_management_api.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}