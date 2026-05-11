-- Migration: Make password_hash nullable for passwortlose Auth
-- Version: V6
-- Description: Anpassung für passwortlose Authentifizierung (Email-Code-Login)
-- HINWEIS: Nur ein ALTER TABLE Statement – MODIFY + CHANGE gleichzeitig scheitert in MySQL!

ALTER TABLE users
    MODIFY COLUMN password_hash VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '';
