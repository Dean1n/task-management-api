package ru.dean1n.task_management_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.dean1n.task_management_api.dto.CreateTaskRequest;
import ru.dean1n.task_management_api.dto.TaskResponse;
import ru.dean1n.task_management_api.model.Task;
import ru.dean1n.task_management_api.model.TaskPriority;
import ru.dean1n.task_management_api.model.TaskStatus;
import ru.dean1n.task_management_api.model.User;
import ru.dean1n.task_management_api.repository.TaskRepository;
import ru.dean1n.task_management_api.repository.UserRepository;
import ru.dean1n.task_management_api.service.TaskService;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository,
                userRepository
        );
    }

    @Test
    void createShouldCreateTaskForUser() {
        User user = createUser(1L);

        CreateTaskRequest request = new CreateTaskRequest(
                "  Изучить Mockito  ",
                "Написать unit-тесты",
                TaskPriority.HIGH,
                Instant.parse("2030-01-01T12:00:00Z")
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(10L);
                    task.beforeInsert();
                    return task;
                });

        TaskResponse response = taskService.create(1L, request);

        assertEquals(10L, response.id());
        assertEquals("Изучить Mockito", response.title());
        assertEquals("Написать unit-тесты", response.description());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals(1L, response.userId());

        verify(userRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createShouldUseMediumPriorityByDefault() {
        User user = createUser(1L);

        CreateTaskRequest request = new CreateTaskRequest(
                "Новая задача",
                null,
                null,
                null
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(11L);
                    task.beforeInsert();
                    return task;
                });

        TaskResponse response = taskService.create(1L, request);

        assertEquals(TaskPriority.MEDIUM, response.priority());
        assertEquals(TaskStatus.TODO, response.status());
    }

    @Test
    void createShouldPassCorrectTaskToRepository() {
        User user = createUser(5L);

        CreateTaskRequest request = new CreateTaskRequest(
                "  Проверить задачу  ",
                "Описание",
                TaskPriority.LOW,
                null
        );

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(12L);
                    task.beforeInsert();
                    return task;
                });

        taskService.create(5L, request);

        ArgumentCaptor<Task> captor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(captor.capture());

        Task savedTask = captor.getValue();

        assertEquals("Проверить задачу", savedTask.getTitle());
        assertEquals("Описание", savedTask.getDescription());
        assertEquals(TaskPriority.LOW, savedTask.getPriority());
        assertEquals(user, savedTask.getUser());
    }

    @Test
    void createShouldReturnNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        CreateTaskRequest request = new CreateTaskRequest(
                "Задача",
                null,
                null,
                null
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.create(999L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Пользователь не найден", exception.getReason());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void findByIdShouldNotReturnAnotherUsersTask() {
        when(taskRepository.findByIdAndUserId(10L, 2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.findById(2L, 10L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Задача не найдена", exception.getReason());
    }

    @Test
    void deleteShouldDeleteUsersTask() {
        User user = createUser(1L);
        Task task = createTask(10L, user);

        when(taskRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(task));

        taskService.delete(1L, 10L);

        verify(taskRepository).delete(task);
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("denis");
        user.setEmail("denis@example.com");
        user.setPasswordHash("hash");
        return user;
    }

    private Task createTask(Long id, User user) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Задача");
        task.setDescription("Описание");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUser(user);
        task.beforeInsert();
        return task;
    }
}