-- Migration: Make password_hash nullable and set default for passwortlose Auth
-- Version: V6
-- Description: Anpassung für passwortlose Authentifizierung (Email-Code-Login)

ALTER TABLE users
MODIFY password_hash VARCHAR(255) NULL DEFAULT '',
CHANGE password_hash password_hash VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '';

-- Update existierende Passwort-Hashes: diese behalten ihren Wert
-- Neue User mit passwortlosen Logins bekommen leeren String

