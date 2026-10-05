CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tasks
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(30)  NOT NULL,
    priority    VARCHAR(30)  NOT NULL,
    deadline    TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     BIGINT NOT NULL,

    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_priority ON tasks (priority);
CREATE INDEX idx_tasks_deadline ON tasks (deadline);