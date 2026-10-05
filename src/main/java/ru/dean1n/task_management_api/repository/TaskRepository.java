package ru.dean1n.task_management_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.dean1n.task_management_api.model.Task;

import java.util.Optional;

public interface TaskRepository
        extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndUserId(
            Long taskId,
            Long userId
    );
}