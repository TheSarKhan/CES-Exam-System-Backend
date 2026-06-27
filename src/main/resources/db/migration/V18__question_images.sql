-- Optional images for image-based questions (IMAGE_QUESTION) and image choices
-- (IMAGE_CHOICE). Stores a public URL served by the backend from the uploads volume.
ALTER TABLE questions ADD COLUMN image_url VARCHAR(512);
ALTER TABLE question_options ADD COLUMN image_url VARCHAR(512);
