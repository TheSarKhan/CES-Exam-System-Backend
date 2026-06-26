-- Proctoring / anti-cheat events captured during an exam session and submitted
-- together with the answers. Cascade-deleted with the session.
CREATE TABLE session_violations (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT       NOT NULL REFERENCES exam_sessions(id) ON DELETE CASCADE,
    type        VARCHAR(50)  NOT NULL,
    label       VARCHAR(255),
    severity    VARCHAR(20)  NOT NULL,
    occurred_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_session_violations_session ON session_violations(session_id);
