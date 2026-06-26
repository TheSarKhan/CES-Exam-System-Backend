-- Why a session ended automatically (vs. the candidate submitting normally).
-- NULL = normal submission. 'PROCTORING' = auto-terminated by anti-cheat (limit reached).
ALTER TABLE exam_sessions ADD COLUMN termination_reason VARCHAR(30);
