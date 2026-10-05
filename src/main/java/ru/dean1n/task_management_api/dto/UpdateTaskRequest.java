package ru.dean1n.task_management_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import ru.dean1n.task_management_api.model.TaskPriority;
import ru.dean1n.task_management_api.model.TaskStatus;

import java.time.Instant;

public record UpdateTaskRequest (
        @Size(min = 1, max = 200, message = "Название должно содержать от 1 до 200 символов")
        String title,

        @Size(max = 5000, message = "Описание должно содержать не более 5000 символов")
        String description,

        TaskStatus status,

        TaskPriority priority,

        @Future(message = "later")
        Instant deadline
){}
