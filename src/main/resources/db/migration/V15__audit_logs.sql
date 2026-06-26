-- Platform-wide audit trail. One row per state-changing request.
CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT,                 -- null for anonymous / failed login
    user_name    VARCHAR(255),           -- snapshot, survives user deletion
    user_role    VARCHAR(100),
    module       VARCHAR(120),
    action       VARCHAR(120),
    http_method  VARCHAR(10),
    path         VARCHAR(512),
    status_code  INTEGER,
    ip_address   VARCHAR(64),
    created_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);
