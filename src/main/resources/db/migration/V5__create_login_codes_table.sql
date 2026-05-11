-- Migration: Create login_codes table for passwortlose Authentifizierung
-- Version: V5
-- Description: Tabelle für temporäre Login-Codes (Email-basiert, ~10 Min gültig)

CREATE TABLE IF NOT EXISTS login_codes (
    id BINARY(16) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6),
    attempts INT NOT NULL DEFAULT 0,
    request_ip VARCHAR(45),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_login_codes_email (email),
    INDEX idx_login_codes_expires_at (expires_at)
);

-- Optional: Trigger/Job für automatisches Löschen abgelaufener Codes
-- (in Production würde man das über einen Task/Schedule machen)

