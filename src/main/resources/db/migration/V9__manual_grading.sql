-- Manual grading for open-ended answers (short/long text). Store the points an
-- admin awards per answer so a session score can be (re)computed to include
-- text questions the system can't auto-grade.
ALTER TABLE exam_session_questions ADD COLUMN awarded_score NUMERIC(5, 2);

-- Backfill already-graded answers so historical session scores stay consistent.
UPDATE exam_session_questions sq
SET awarded_score = COALESCE((SELECT q.score FROM questions q WHERE q.id = sq.question_id), 1)
WHERE sq.is_correct = TRUE;

UPDATE exam_session_questions
SET awarded_score = 0
WHERE is_correct = FALSE;
-- Rows with is_correct IS NULL stay NULL → flagged as "needs manual grading".
