package ru.dean1n.task_management_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.dean1n.task_management_api.dto.CreateTaskRequest;
import ru.dean1n.task_management_api.dto.TaskPageResponse;
import ru.dean1n.task_management_api.dto.TaskResponse;
import ru.dean1n.task_management_api.dto.UpdateTaskRequest;
import ru.dean1n.task_management_api.model.TaskPriority;
import ru.dean1n.task_management_api.model.TaskStatus;
import ru.dean1n.task_management_api.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response =
                taskService.create(userId(jwt), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public TaskPageResponse findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return taskService.findAll(
                userId(jwt),
                status,
                priority,
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/{taskId}")
    public TaskResponse findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long taskId
    ) {
        return taskService.findById(
                userId(jwt),
                taskId
        );
    }

    @PatchMapping("/{taskId}")
    public TaskResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.update(
                userId(jwt),
                taskId,
                request
        );
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long taskId
    ) {
        taskService.delete(
                userId(jwt),
                taskId
        );
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(
                jwt.getClaimAsString("userId")
        );
    }
}