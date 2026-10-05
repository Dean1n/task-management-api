package ru.dean1n.task_management_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.dean1n.task_management_api.model.TaskPriority;

import java.time.Instant;

public record CreateTaskRequest (
    @NotBlank(message = "Название задачи")
    @Size(min = 1, max = 200, message = "Название должно содержать от 1 до 200 символов")
    String title,
    @Size(max=5000,message = "Описание должно содержать не более 5000 символов")
    String description,
    TaskPriority priority,
    @Future(message = "later")
    Instant deadline
){}
