-- 1. Multiple Choice Support
CREATE TABLE exam_session_question_selected_options (
    session_question_id BIGINT NOT NULL REFERENCES exam_session_questions(id) ON DELETE CASCADE,
    option_id BIGINT NOT NULL REFERENCES question_options(id) ON DELETE CASCADE,
    PRIMARY KEY (session_question_id, option_id)
);

-- We can drop the single selected_option_id from exam_session_questions later if we strictly use the mapping table,
-- but for backward compatibility or simplicity with SINGLE_CHOICE, we can leave it.

-- 2. Seed Data: Default Admin User (password: admin123 assuming BCrypt)
-- Hash for 'admin123' is '$2a$10$iyXqz6utK9Ap8e0q55Dz0OXxWBkZoW5Mmu5yfpvLDI4jyYfCmRZcC'
INSERT INTO departments (name) VALUES ('IT Department'), ('Human Resources');

INSERT INTO users (email, password_hash, first_name, last_name, department_id, status)
VALUES ('admin@ces.com', '$2a$10$iyXqz6utK9Ap8e0q55Dz0OXxWBkZoW5Mmu5yfpvLDI4jyYfCmRZcC', 'System', 'Admin', 1, 'ACTIVE');

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'admin@ces.com'),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);

-- 3. Seed Data: Sample Question Bank Categories and Topics
INSERT INTO categories (name, description) VALUES ('Programming', 'Software development topics'), ('Soft Skills', 'General workplace skills');

INSERT INTO topics (category_id, name) VALUES 
    ((SELECT id FROM categories WHERE name = 'Programming'), 'Java Basics'),
    ((SELECT id FROM categories WHERE name = 'Programming'), 'Spring Boot'),
    ((SELECT id FROM categories WHERE name = 'Soft Skills'), 'Communication');

-- 4. Seed Data: A few sample questions
INSERT INTO questions (topic_id, type, text, score) VALUES 
    ((SELECT id FROM topics WHERE name = 'Java Basics'), 'SINGLE_CHOICE', 'What is the default value of a boolean variable in Java?', 10.0),
    ((SELECT id FROM topics WHERE name = 'Java Basics'), 'MULTIPLE_CHOICE', 'Which of the following are Java access modifiers?', 10.0);

INSERT INTO question_options (question_id, text, is_correct, sort_order) VALUES
    ((SELECT id FROM questions WHERE text = 'What is the default value of a boolean variable in Java?'), 'true', false, 1),
    ((SELECT id FROM questions WHERE text = 'What is the default value of a boolean variable in Java?'), 'false', true, 2),
    ((SELECT id FROM questions WHERE text = 'What is the default value of a boolean variable in Java?'), 'null', false, 3),
    ((SELECT id FROM questions WHERE text = 'Which of the following are Java access modifiers?'), 'public', true, 1),
    ((SELECT id FROM questions WHERE text = 'Which of the following are Java access modifiers?'), 'private', true, 2),
    ((SELECT id FROM questions WHERE text = 'Which of the following are Java access modifiers?'), 'hidden', false, 3),
    ((SELECT id FROM questions WHERE text = 'Which of the following are Java access modifiers?'), 'protected', true, 4);
