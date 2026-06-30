CREATE TABLE users
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE  NOT NULL,
    password_hash VARCHAR(255)         NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    role          VARCHAR(50)          NOT NULL,
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
