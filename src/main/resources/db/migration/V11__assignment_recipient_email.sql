-- Optional recipient e-mail for link-mode assignments, so invites can be sent / resent.
ALTER TABLE exam_assignments ADD COLUMN recipient_email VARCHAR(255);
