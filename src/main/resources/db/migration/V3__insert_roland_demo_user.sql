-- Demo password for Roland: Test123
-- Hashing strategy: Spring Security BCryptPasswordEncoder, strength 10.
SET @roland_password_hash = '$2a$10$0FpC3mVvo/nO/iq/VPSOOOtVV7zq58v8w07jOBryhCUyp6CPmYVxm';

INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    street, house_number, postal_code, city, address_addition,
    account_role, phone, created_at
) SELECT
    UUID_TO_BIN(UUID()),
    'roland.becker@petsitter.local',
    @roland_password_hash,
    'Roland', 'Becker',
    'Ahornstrasse', '27', '40210', 'Duesseldorf', NULL,
    'SIGNED_IN_USER', '+49 211 777888', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'roland.becker@petsitter.local'
);
