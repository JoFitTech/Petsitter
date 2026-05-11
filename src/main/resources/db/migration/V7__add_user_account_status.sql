ALTER TABLE users
    ADD COLUMN account_status VARCHAR(32) NOT NULL DEFAULT 'VERIFIED',
    ADD COLUMN delete_after DATETIME(6) NULL,
    ADD CONSTRAINT chk_users_account_status
        CHECK (account_status IN ('PENDING', 'VERIFIED', 'BLOCKED'));
