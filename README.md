# Task Management API
[![CI](https://github.com/Dean1n/task-management-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Dean1n/task-management-api/actions/workflows/ci.yml)
REST API для управления персональными задачами с регистрацией, JWT-авторизацией, фильтрацией и пагинацией.

## Возможности

- регистрация пользователей;
- вход по имени пользователя и паролю;
- JWT-авторизация;
- создание, просмотр, изменение и удаление задач;
- статусы и приоритеты задач;
- дедлайны;
- фильтрация по статусу и приоритету;
- сортировка и пагинация;
- валидация входных данных;
- единый формат ошибок;
- Swagger UI;
- unit- и HTTP-тесты;
- запуск приложения и PostgreSQL через Docker Compose.

## Технологии

- Java 21
- Spring Boot 4
- Spring MVC
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- PostgreSQL 16
- Flyway
- Maven
- Docker и Docker Compose
- JUnit 6
- Mockito
- MockMvc
- OpenAPI / Swagger

## Запуск через Docker

Для запуска необходим Docker.

```
docker compose up -d --build
```

Проверить состояние контейнеров:

```
docker compose ps
```

Посмотреть логи приложения:

```
docker compose logs -f api
```

После запуска API доступен по адресу:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health check:

```
curl "http://localhost:8080/api/ping"
```

Ожидаемый ответ:

```json
{
  "status": "ok"
}
```

Остановить приложение:

```
docker compose down
```

## Локальный запуск

Запустить только PostgreSQL:

```
docker compose up -d postgres
```

Запустить приложение:

```
./mvnw spring-boot:run
```

## Переменные окружения

| Переменная | Значение по умолчанию | Описание |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/task_manager` | URL PostgreSQL |
| `DB_USERNAME` | `task_user` | Пользователь базы данных |
| `DB_PASSWORD` | `task_password` | Пароль базы данных |
| `JWT_SECRET` | development secret | Ключ подписи JWT |

Для production необходимо передавать собственный длинный `JWT_SECRET`.

## API

### Регистрация

```http
POST /api/auth/register
```

Пример запроса:

```json
{
  "username": "denis",
  "email": "denis@example.com",
  "password": "strong-password-123"
}
```

### Вход

```http
POST /api/auth/login
```

```json
{
  "username": "denis",
  "password": "strong-password-123"
}
```

Пример ответа:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Для защищённых запросов токен передаётся в заголовке:

```http
Authorization: Bearer eyJ...
```

### Задачи

| Метод | URL | Описание |
|---|---|---|
| `POST` | `/api/tasks` | Создать задачу |
| `GET` | `/api/tasks` | Получить задачи пользователя |
| `GET` | `/api/tasks/{taskId}` | Получить задачу |
| `PATCH` | `/api/tasks/{taskId}` | Изменить задачу |
| `DELETE` | `/api/tasks/{taskId}` | Удалить задачу |

### Создание задачи

```json
{
  "title": "Изучить Spring Security",
  "description": "Завершить настройку JWT",
  "priority": "HIGH",
  "deadline": "2030-01-01T18:00:00Z"
}
```

Статусы:

```text
TODO
IN_PROGRESS
DONE
```

Приоритеты:

```text
LOW
MEDIUM
HIGH
```

### Фильтрация и пагинация

```http
GET /api/tasks?status=TODO&priority=HIGH&page=0&size=10&sortBy=createdAt&direction=desc
```

Допустимые поля сортировки:

```text
title
status
priority
deadline
createdAt
updatedAt
```

## Формат ошибки

```json
{
  "timestamp": "2026-10-05T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Ошибка валидации",
  "path": "/api/tasks",
  "validationErrors": {
    "title": "Название задачи"
  }
}
```

## Тестирование

Запустить все тесты:

```
./mvnw clean test
```

Проект содержит:

- unit-тесты `TaskService`;
- unit-тесты `UserService`;
- HTTP-тесты `AuthController`;
- тесты защищённых эндпоинтов `TaskController`;
- проверку JWT-аутентификации;
- проверку валидации.

## Структура проекта

```text
src/main/java/ru/dean1n/task_management_api
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── security
└── service
```

## База данных

Структура базы создаётся и проверяется миграциями Flyway:

```text
src/main/resources/db/migration
```

Основные таблицы:

- `users`;
- `tasks`.

Каждая задача принадлежит одному пользователю. Идентификатор пользователя извлекается из JWT, поэтому его нельзя подменить через URL.