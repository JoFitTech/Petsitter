ALTER TABLE users
    ADD COLUMN display_name VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN birth_date DATE NULL,
    ADD COLUMN nationality VARCHAR(100) NULL,
    ADD COLUMN language VARCHAR(80) NOT NULL DEFAULT 'deutsch',
    ADD COLUMN bio VARCHAR(1000) NULL,
    ADD COLUMN country VARCHAR(100) NOT NULL DEFAULT 'Deutschland',
    ADD COLUMN pending_email VARCHAR(255) NULL,
    ADD COLUMN pending_email_requested_at DATETIME(6) NULL;

UPDATE users
SET display_name = first_name
WHERE display_name = '';
