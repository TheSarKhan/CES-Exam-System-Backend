-- Per-admin "notifications seen" watermark. Unread = events newer than last_read_at.
CREATE TABLE notification_reads (
    user_id      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    last_read_at TIMESTAMP
);
