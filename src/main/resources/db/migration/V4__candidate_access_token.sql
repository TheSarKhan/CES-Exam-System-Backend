ALTER TABLE exam_assignments
    ADD COLUMN access_token VARCHAR(36) UNIQUE;

CREATE INDEX idx_exam_assignments_access_token ON exam_assignments (access_token);
