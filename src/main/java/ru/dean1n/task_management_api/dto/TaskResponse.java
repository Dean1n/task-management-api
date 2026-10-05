package ru.dean1n.task_management_api.dto;

import ru.dean1n.task_management_api.model.Task;
import ru.dean1n.task_management_api.model.TaskPriority;
import ru.dean1n.task_management_api.model.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant deadline,
        Instant createdAt,
        Instant updatedAt,
        Long userId
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDeadline(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getUser().getId()
        );
    }
}