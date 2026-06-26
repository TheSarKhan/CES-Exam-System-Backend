-- Exam links are single-use: once started, the link is consumed and can't start a new session.
ALTER TABLE exam_assignments ADD COLUMN consumed BOOLEAN NOT NULL DEFAULT FALSE;
