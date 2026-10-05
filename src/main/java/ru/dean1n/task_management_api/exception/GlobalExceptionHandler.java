package ru.dean1n.task_management_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        int statusCode = exception.getStatusCode().value();
        HttpStatus status = HttpStatus.valueOf(statusCode);

        ApiError error = new ApiError(
                Instant.now(),
                statusCode,
                status.getReasonPhrase(),
                exception.getReason() == null
                        ? status.getReasonPhrase()
                        : exception.getReason(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(statusCode)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {
            validationErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Ошибка валидации",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Некорректный JSON или значение поля",
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }
}