package ru.dean1n.task_management_api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ru.dean1n.task_management_api.exception.ApiError;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Недостаточно прав для выполнения операции",
                request.getRequestURI(),
                Map.of()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}