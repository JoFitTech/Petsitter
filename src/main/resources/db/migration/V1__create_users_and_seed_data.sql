CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    street VARCHAR(120) NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address_addition VARCHAR(120),
    account_role VARCHAR(32) NOT NULL DEFAULT 'SIGNED_IN_USER',
    phone VARCHAR(50),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_users_account_role
        CHECK (account_role IN ('ADMIN', 'SIGNED_IN_USER'))
);

-- Demo password for all seeded users: localpass
-- Hashing strategy: Spring Security BCryptPasswordEncoder, strength 10.
-- Regenerate in Java with: new BCryptPasswordEncoder().encode("localpass")
-- The application verifies this with PasswordEncoder.matches(rawPassword, passwordHash).
SET @demo_password_hash = '$2a$10$UOZxLvqYU1FexMe0TPJvReNDMtNw2563YcJbbs9NJNxSELPhp1DAa';

INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    street, house_number, postal_code, city, address_addition,
    account_role, phone, created_at
) SELECT
    UUID_TO_BIN(UUID()),
    'admin@petsitter.local',
    @demo_password_hash,
    'Admin', 'User',
    'Hauptstrasse', '1', '10115', 'Berlin', NULL,
    'ADMIN', '+49 30 123456', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@petsitter.local'
);

INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    street, house_number, postal_code, city, address_addition,
    account_role, phone, created_at
) SELECT
    UUID_TO_BIN(UUID()),
    'anna.mueller@petsitter.local',
    @demo_password_hash,
    'Anna', 'Mueller',
    'Rosenweg', '14', '50667', 'Koeln', '2. OG links',
    'SIGNED_IN_USER', '+49 221 111222', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'anna.mueller@petsitter.local'
);

INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    street, house_number, postal_code, city, address_addition,
    account_role, phone, created_at
) SELECT
    UUID_TO_BIN(UUID()),
    'ben.schmidt@petsitter.local',
    @demo_password_hash,
    'Ben', 'Schmidt',
    'Lindenallee', '8a', '80331', 'Muenchen', NULL,
    'SIGNED_IN_USER', '+49 89 333444', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'ben.schmidt@petsitter.local'
);

INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    street, house_number, postal_code, city, address_addition,
    account_role, phone, created_at
) SELECT
    UUID_TO_BIN(UUID()),
    'lara.weber@petsitter.local',
    @demo_password_hash,
    'Lara', 'Weber',
    'Marktplatz', '5', '20095', 'Hamburg', 'Hinterhaus',
    'SIGNED_IN_USER', '+49 40 555666', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'lara.weber@petsitter.local'
);
