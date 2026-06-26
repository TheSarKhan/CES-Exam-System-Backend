-- Platform-wide settings as flexible key/value rows.
CREATE TABLE app_settings (
    setting_key   VARCHAR(64) PRIMARY KEY,
    setting_value TEXT,
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO app_settings (setting_key, setting_value) VALUES
    ('org_name', 'CES Assessment'),
    ('support_email', ''),
    ('default_pass_mark', '60'),
    ('default_duration_minutes', '30'),
    ('default_link_validity_days', '7'),
    ('proctoring_enabled', 'true');
