package ru.dean1n.task_management_api.dto;

import java.util.List;

public record TaskPageResponse(
        List<TaskResponse> tasks,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}