CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    google_id   VARCHAR(255) UNIQUE,
    email       VARCHAR(255) UNIQUE,
    username    VARCHAR(100),
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    creation_ts TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed admin account as id=1
INSERT INTO users (id, google_id, email, username, role, status, creation_ts)
VALUES (1, NULL, '${app.admin.email}', 'admin', 'ADMIN', 'APPROVED', CURRENT_TIMESTAMP);

-- Force next auto-generated user id to start at 2
SELECT setval('users_id_seq', 1, true);
