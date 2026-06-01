CREATE TABLE IF NOT EXISTS password_reset_code (
    id BINARY(16) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6),
    attempts INT NOT NULL DEFAULT 0,
    request_ip VARCHAR(45),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_password_reset_code_email (email),
    INDEX idx_password_reset_code_expires_at (expires_at)
);
