package ru.dean1n.task_management_api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.dean1n.task_management_api.dto.TaskPageResponse;
import ru.dean1n.task_management_api.dto.TaskResponse;
import ru.dean1n.task_management_api.model.TaskPriority;
import ru.dean1n.task_management_api.model.TaskStatus;
import ru.dean1n.task_management_api.service.TaskService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(TaskControllerTest.TestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void findAllShouldReturnUnauthorizedWithoutJwt()
            throws Exception {

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findAllShouldUseUserIdFromJwt() throws Exception {
        TaskResponse task = new TaskResponse(
                10L,
                "Изучить тестирование",
                "Написать MockMvc-тест",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.parse("2030-01-01T12:00:00Z"),
                Instant.parse("2026-10-05T10:00:00Z"),
                Instant.parse("2026-10-05T10:00:00Z"),
                2L
        );

        TaskPageResponse response = new TaskPageResponse(
                List.of(task),
                0,
                10,
                1,
                1,
                true,
                true
        );

        when(taskService.findAll(
                eq(2L),
                isNull(),
                isNull(),
                eq(0),
                eq(10),
                eq("createdAt"),
                eq("desc")
        )).thenReturn(response);

        mockMvc.perform(get("/api/tasks")
                        .with(jwt().jwt(token -> token
                                .subject("denis")
                                .claim("userId", "2")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.length()")
                        .value(1))
                .andExpect(jsonPath("$.tasks[0].id")
                        .value(10))
                .andExpect(jsonPath("$.tasks[0].title")
                        .value("Изучить тестирование"))
                .andExpect(jsonPath("$.tasks[0].userId")
                        .value(2))
                .andExpect(jsonPath("$.page")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10))
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.totalPages")
                        .value(1))
                .andExpect(jsonPath("$.first")
                        .value(true))
                .andExpect(jsonPath("$.last")
                        .value(true));

        verify(taskService).findAll(
                2L,
                null,
                null,
                0,
                10,
                "createdAt",
                "desc"
        );
    }

    @Test
    void findAllShouldPassFiltersToService()
            throws Exception {

        TaskPageResponse response = new TaskPageResponse(
                List.of(),
                1,
                5,
                0,
                0,
                false,
                true
        );

        when(taskService.findAll(
                eq(7L),
                eq(TaskStatus.IN_PROGRESS),
                eq(TaskPriority.HIGH),
                eq(1),
                eq(5),
                eq("deadline"),
                eq("asc")
        )).thenReturn(response);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "IN_PROGRESS")
                        .param("priority", "HIGH")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "deadline")
                        .param("direction", "asc")
                        .with(jwt().jwt(token -> token
                                .subject("denis")
                                .claim("userId", "7")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.length()")
                        .value(0))
                .andExpect(jsonPath("$.page")
                        .value(1))
                .andExpect(jsonPath("$.size")
                        .value(5));

        verify(taskService).findAll(
                7L,
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                1,
                5,
                "deadline",
                "asc"
        );
    }

    @Test
    void createShouldUseUserIdFromJwt() throws Exception {
        TaskResponse response = new TaskResponse(
                15L,
                "Новая задача",
                "Описание",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                Instant.parse("2026-10-05T10:00:00Z"),
                Instant.parse("2026-10-05T10:00:00Z"),
                2L
        );

        when(taskService.create(
                eq(2L),
                any()
        )).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .with(jwt().jwt(token -> token
                                .subject("denis")
                                .claim("userId", "2")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Новая задача",
                                  "description": "Описание",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(15))
                .andExpect(jsonPath("$.title")
                        .value("Новая задача"))
                .andExpect(jsonPath("$.description")
                        .value("Описание"))
                .andExpect(jsonPath("$.status")
                        .value("TODO"))
                .andExpect(jsonPath("$.priority")
                        .value("MEDIUM"))
                .andExpect(jsonPath("$.userId")
                        .value(2));

        verify(taskService).create(
                eq(2L),
                any()
        );
    }

    @Test
    void createShouldRejectEmptyTitle() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(jwt().jwt(token -> token
                                .subject("denis")
                                .claim("userId", "2")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Описание"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http
        ) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oauth2 ->
                            oauth2.jwt(Customizer.withDefaults())
                    );

            return http.build();
        }
    }
}