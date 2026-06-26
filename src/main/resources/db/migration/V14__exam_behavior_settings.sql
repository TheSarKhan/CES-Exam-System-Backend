-- Exam behaviour & security settings (key/value rows; code defaults also cover these).
-- updated_at is set explicitly because this DB lost the column's DEFAULT now().
INSERT INTO app_settings (setting_key, setting_value, updated_at) VALUES
    ('shuffle_questions', 'false', now()),
    ('shuffle_options', 'false', now()),
    ('show_result_to_candidate', 'true', now()),
    ('tab_switch_limit', '3', now())
ON CONFLICT (setting_key) DO NOTHING;
