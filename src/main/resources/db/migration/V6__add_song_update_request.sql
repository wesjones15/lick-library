CREATE TABLE song_update_request (
    id                UUID PRIMARY KEY,
    song_id           UUID NOT NULL,
    submitter_user_id BIGINT NOT NULL,
    request_type      VARCHAR(32) NOT NULL,
    payload           TEXT NOT NULL,
    status            VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
