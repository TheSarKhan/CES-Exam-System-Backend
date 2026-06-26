-- Exams can now hold a concrete, ordered list of questions:
--   * picked from the question bank (referenced), or
--   * authored inline during exam building (owned by the exam).

-- Inline/ad-hoc exam questions don't belong to a bank topic.
ALTER TABLE questions ALTER COLUMN topic_id DROP NOT NULL;

-- Inline questions are owned by their exam and removed together with it.
ALTER TABLE questions ADD COLUMN owner_exam_id BIGINT REFERENCES exams(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_questions_owner_exam ON questions(owner_exam_id);

-- Ordered question list for an exam.
CREATE TABLE exam_questions (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    UNIQUE (exam_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_questions_exam ON exam_questions(exam_id);
