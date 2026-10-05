package ru.dean1n.task_management_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.dean1n.task_management_api.dto.CreateTaskRequest;
import ru.dean1n.task_management_api.dto.TaskResponse;
import ru.dean1n.task_management_api.dto.UpdateTaskRequest;
import ru.dean1n.task_management_api.model.Task;
import ru.dean1n.task_management_api.model.TaskPriority;
import ru.dean1n.task_management_api.model.TaskStatus;
import ru.dean1n.task_management_api.model.User;
import ru.dean1n.task_management_api.repository.TaskRepository;
import ru.dean1n.task_management_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.dean1n.task_management_api.dto.TaskPageResponse;

import java.util.Set;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "title",
            "status",
            "priority",
            "deadline",
            "createdAt",
            "updatedAt"
    );
    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse create(
            Long userId,
            CreateTaskRequest request
    ) {
        User user = findUser(userId);

        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(
                request.priority() == null
                        ? TaskPriority.MEDIUM
                        : request.priority()
        );
        task.setDeadline(request.deadline());
        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        return TaskResponse.from(savedTask);
    }


    @Transactional(readOnly = true)
    public TaskResponse findById(
            Long userId,
            Long taskId
    ) {
        Task task = findTask(userId, taskId);

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(
            Long userId,
            Long taskId,
            UpdateTaskRequest request
    ) {
        Task task = findTask(userId, taskId);

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Название задачи не может быть пустым"
                );
            }

            task.setTitle(request.title().trim());
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.status() != null) {
            task.setStatus(request.status());
        }

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        if (request.deadline() != null) {
            task.setDeadline(request.deadline());
        }

        Task savedTask = taskRepository.save(task);

        return TaskResponse.from(savedTask);
    }

    @Transactional
    public void delete(
            Long userId,
            Long taskId
    ) {
        Task task = findTask(userId, taskId);
        taskRepository.delete(task);
    }

    private User findUser(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));
    }

    private Task findTask(
            Long userId,
            Long taskId
    ) {
        return taskRepository
                .findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Задача не найдена"
                ));
    }
    @Transactional(readOnly = true)
    public TaskPageResponse findAll(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        findUser(userId);

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Номер страницы не может быть отрицательным"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Размер страницы должен быть от 1 до 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимое поле сортировки"
            );
        }

        Sort.Direction sortDirection;

        if (direction.equalsIgnoreCase("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Направление сортировки должно быть asc или desc"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Specification<Task> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("user").get("id"),
                                userId
                        );

        if (status != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (priority != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("priority"),
                                    priority
                            )
            );
        }

        Page<Task> taskPage = taskRepository.findAll(
                specification,
                pageable
        );

        List<TaskResponse> tasks = taskPage
                .getContent()
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new TaskPageResponse(
                tasks,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isFirst(),
                taskPage.isLast()
        );
    }
}