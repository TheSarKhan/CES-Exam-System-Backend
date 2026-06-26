-- Restores the default roles and an admin login.
-- Login after running:  admin@ces.com / admin123
-- The hash below is BCrypt for 'admin123' (verified).

INSERT INTO roles (name) VALUES ('ROLE_ADMIN'),('ROLE_EMPLOYEE'),('ROLE_CANDIDATE')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO users (email, password_hash, first_name, last_name, status, created_at, updated_at)
VALUES ('admin@ces.com', '$2a$10$iyXqz6utK9Ap8e0q55Dz0OXxWBkZoW5Mmu5yfpvLDI4jyYfCmRZcC', 'System', 'Admin', 'ACTIVE', now(), now())
  ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, status = 'ACTIVE';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@ces.com'
  ON CONFLICT (user_id, role_id) DO NOTHING;
