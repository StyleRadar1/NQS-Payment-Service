-- V8 · transactional outbox for reliable event publication (SR-RNF-15)
CREATE TABLE event_outbox (
    id              BIGSERIAL PRIMARY KEY,
    event_id        UUID         NOT NULL UNIQUE,
    event_type      VARCHAR(100) NOT NULL,
    routing_key     VARCHAR(120) NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    attempts        INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_attempt_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    TIMESTAMP,
    last_error      VARCHAR(500),
    CONSTRAINT ck_event_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT ck_event_outbox_attempts CHECK (attempts >= 0)
);

CREATE INDEX idx_event_outbox_status_next_attempt
    ON event_outbox (status, next_attempt_at);
