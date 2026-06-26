-- Department-scoped question bank + richer question metadata

-- 1. Each category now belongs to a department
ALTER TABLE categories ADD COLUMN department_id BIGINT REFERENCES departments(id) ON DELETE CASCADE;

-- Backfill existing categories to the first available department
UPDATE categories SET department_id = (SELECT MIN(id) FROM departments) WHERE department_id IS NULL;

ALTER TABLE categories ALTER COLUMN department_id SET NOT NULL;

-- Category names are unique within a department, not globally
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;
ALTER TABLE categories ADD CONSTRAINT categories_department_name_unique UNIQUE (department_id, name);

CREATE INDEX IF NOT EXISTS idx_categories_department ON categories(department_id);

-- 2. Richer question metadata
ALTER TABLE questions ADD COLUMN difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
