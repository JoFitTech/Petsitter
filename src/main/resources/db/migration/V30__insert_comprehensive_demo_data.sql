-- Comprehensive demo data with Nominatim-verified postal-code coordinates.
-- Demo password for newly seeded users: localpass
SET @demo_password_hash = '$2a$10$UOZxLvqYU1FexMe0TPJvReNDMtNw2563YcJbbs9NJNxSELPhp1DAa';

-- Coordinates were checked against the project Nominatim query:
-- /search?country=Germany&postalcode=<PLZ>&format=jsonv2&addressdetails=1&limit=1
INSERT INTO postal_code_location (
    country_code, postal_code, primary_place_name, place_names, latitude, longitude, fetched_at
) VALUES
    ('DE', '10115', 'Berlin',
     CONCAT('Berlin', CHAR(10), 'Mitte', CHAR(10), '10115, Mitte, Berlin, Deutschland'),
     52.532191, 13.384557, CURRENT_TIMESTAMP(6)),
    ('DE', '50667', 'Köln',
     CONCAT('Köln', CHAR(10), 'Altstadt-Nord', CHAR(10), '50667, Altstadt-Nord, Innenstadt, Köln, Nordrhein-Westfalen, Deutschland'),
     50.938640, 6.955030, CURRENT_TIMESTAMP(6)),
    ('DE', '80331', 'München',
     CONCAT('München', CHAR(10), 'Altstadt-Lehel', CHAR(10), '80331, Altstadt-Lehel, München, Bayern, Deutschland'),
     48.135915, 11.573098, CURRENT_TIMESTAMP(6)),
    ('DE', '20095', 'Hamburg',
     CONCAT('Hamburg', CHAR(10), 'Altstadt', CHAR(10), '20095, Altstadt, Hamburg-Mitte, Hamburg, Deutschland'),
     53.551425, 10.000386, CURRENT_TIMESTAMP(6)),
    ('DE', '40210', 'Düsseldorf',
     CONCAT('Düsseldorf', CHAR(10), 'Stadtmitte', CHAR(10), '40210, Stadtmitte, Stadtbezirk 1, Düsseldorf, Nordrhein-Westfalen, Deutschland'),
     51.221355, 6.789850, CURRENT_TIMESTAMP(6)),
    ('DE', '04109', 'Leipzig',
     CONCAT('Leipzig', CHAR(10), 'Zentrum-West', CHAR(10), '04109, Zentrum-West, Mitte, Leipzig, Sachsen, Deutschland'),
     51.338655, 12.365381, CURRENT_TIMESTAMP(6)),
    ('DE', '60311', 'Frankfurt am Main',
     CONCAT('Frankfurt am Main', CHAR(10), 'Altstadt', CHAR(10), '60311, Altstadt, Innenstadt 1, Frankfurt am Main, Hessen, Deutschland'),
     50.110660, 8.682885, CURRENT_TIMESTAMP(6)),
    ('DE', '70173', 'Stuttgart',
     CONCAT('Stuttgart', CHAR(10), 'Stuttgart-Mitte', CHAR(10), '70173, Stuttgart-Mitte, Stuttgart, Baden-Württemberg, Deutschland'),
     48.781406, 9.181830, CURRENT_TIMESTAMP(6)),
    ('DE', '01067', 'Dresden',
     CONCAT('Dresden', CHAR(10), 'Friedrichstadt', CHAR(10), '01067, Friedrichstadt, Altstadt, Dresden, Sachsen, Deutschland'),
     51.060226, 13.717947, CURRENT_TIMESTAMP(6)),
    ('DE', '28195', 'Bremen',
     CONCAT('Bremen', CHAR(10), 'Stadtgebiet Bremen', CHAR(10), 'Mitte', CHAR(10), '28195, Mitte, Bremen-Mitte, Stadtgebiet Bremen, Bremen, Deutschland'),
     53.080418, 8.805176, CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    primary_place_name = VALUES(primary_place_name),
    place_names = VALUES(place_names),
    latitude = VALUES(latitude),
    longitude = VALUES(longitude),
    fetched_at = VALUES(fetched_at);

SET @anna_id = (SELECT id FROM `user` WHERE email = 'anna.mueller@petsitter.local' LIMIT 1);
SET @ben_id = (SELECT id FROM `user` WHERE email = 'ben.schmidt@petsitter.local' LIMIT 1);
SET @lara_id = (SELECT id FROM `user` WHERE email = 'lara.weber@petsitter.local' LIMIT 1);
SET @roland_id = (SELECT id FROM `user` WHERE email = 'roland.becker@petsitter.local' LIMIT 1);

SET @mia_id = COALESCE((SELECT id FROM `user` WHERE email = 'mia.schneider@petsitter.local' LIMIT 1), UUID_TO_BIN('30000000-0000-0000-0000-000000000001'));
INSERT INTO `user` (
    id, email, password_hash, first_name, last_name, display_name,
    street, house_number, postal_code, city, address_addition,
    account_role, account_status, phone, birth_date, nationality, language, bio, country, created_at
) SELECT
    @mia_id, 'mia.schneider@petsitter.local', @demo_password_hash,
    'Mia', 'Schneider', 'Mia',
    'Zentralstrasse', '12', '04109', 'Leipzig', NULL,
    'SIGNED_IN_USER', 'VERIFIED', '+49 341 222333', '1994-03-18',
    'Deutsch', 'deutsch, englisch',
    'Ruhige Tiersitterin mit viel Erfahrung bei Katzen und kleinen Hunden.',
    'Deutschland', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE email = 'mia.schneider@petsitter.local');

SET @jonas_id = COALESCE((SELECT id FROM `user` WHERE email = 'jonas.fischer@petsitter.local' LIMIT 1), UUID_TO_BIN('30000000-0000-0000-0000-000000000002'));
INSERT INTO `user` (
    id, email, password_hash, first_name, last_name, display_name,
    street, house_number, postal_code, city, address_addition,
    account_role, account_status, phone, birth_date, nationality, language, bio, country, created_at
) SELECT
    @jonas_id, 'jonas.fischer@petsitter.local', @demo_password_hash,
    'Jonas', 'Fischer', 'Jonas',
    'Neue Kraeme', '6', '60311', 'Frankfurt am Main', '3. OG',
    'SIGNED_IN_USER', 'VERIFIED', '+49 69 445566', '1989-11-02',
    'Deutsch', 'deutsch',
    'Halter von Reptilien und Aquarienfischen, sucht punktuelle Hilfe fuer Spezialpflege.',
    'Deutschland', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE email = 'jonas.fischer@petsitter.local');

SET @sofia_id = COALESCE((SELECT id FROM `user` WHERE email = 'sofia.wagner@petsitter.local' LIMIT 1), UUID_TO_BIN('30000000-0000-0000-0000-000000000003'));
INSERT INTO `user` (
    id, email, password_hash, first_name, last_name, display_name,
    street, house_number, postal_code, city, address_addition,
    account_role, account_status, phone, birth_date, nationality, language, bio, country, created_at
) SELECT
    @sofia_id, 'sofia.wagner@petsitter.local', @demo_password_hash,
    'Sofia', 'Wagner', 'Sofia',
    'Koenigstrasse', '18', '70173', 'Stuttgart', NULL,
    'SIGNED_IN_USER', 'VERIFIED', '+49 711 889900', '1991-07-09',
    'Deutsch', 'deutsch, italienisch',
    'Tierhalterin mit Katze und Kaninchen, bevorzugt klare Absprachen und kurze Updates.',
    'Deutschland', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE email = 'sofia.wagner@petsitter.local');

SET @tim_id = COALESCE((SELECT id FROM `user` WHERE email = 'tim.neumann@petsitter.local' LIMIT 1), UUID_TO_BIN('30000000-0000-0000-0000-000000000004'));
INSERT INTO `user` (
    id, email, password_hash, first_name, last_name, display_name,
    street, house_number, postal_code, city, address_addition,
    account_role, account_status, phone, birth_date, nationality, language, bio, country, created_at
) SELECT
    @tim_id, 'tim.neumann@petsitter.local', @demo_password_hash,
    'Tim', 'Neumann', 'Tim',
    'Wilsdruffer Strasse', '21', '01067', 'Dresden', NULL,
    'SIGNED_IN_USER', 'VERIFIED', '+49 351 778899', '1986-01-27',
    'Deutsch', 'deutsch, englisch',
    'Bietet flexible Gassi-Runden und Haussitting mit ruhiger, verlaesslicher Betreuung.',
    'Deutschland', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE email = 'tim.neumann@petsitter.local');

SET @clara_id = COALESCE((SELECT id FROM `user` WHERE email = 'clara.hoffmann@petsitter.local' LIMIT 1), UUID_TO_BIN('30000000-0000-0000-0000-000000000005'));
INSERT INTO `user` (
    id, email, password_hash, first_name, last_name, display_name,
    street, house_number, postal_code, city, address_addition,
    account_role, account_status, phone, birth_date, nationality, language, bio, country, created_at
) SELECT
    @clara_id, 'clara.hoffmann@petsitter.local', @demo_password_hash,
    'Clara', 'Hoffmann', 'Clara',
    'Knochenhauerstrasse', '26', '28195', 'Bremen', 'Hinterhaus',
    'SIGNED_IN_USER', 'VERIFIED', '+49 421 112233', '1990-09-21',
    'Deutsch', 'deutsch',
    'Lebt mit zwei Hunden in Bremen und bucht gerne regelmaessige Unterstuetzung.',
    'Deutschland', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE email = 'clara.hoffmann@petsitter.local');

SET @wallet_mia_id = COALESCE((SELECT id FROM wallet_account WHERE user_id = @mia_id LIMIT 1), UUID_TO_BIN('31000000-0000-0000-0000-000000000001'));
INSERT INTO wallet_account (id, user_id, available_balance)
SELECT @wallet_mia_id, @mia_id, 0.00
WHERE NOT EXISTS (SELECT 1 FROM wallet_account WHERE user_id = @mia_id);

SET @wallet_jonas_id = COALESCE((SELECT id FROM wallet_account WHERE user_id = @jonas_id LIMIT 1), UUID_TO_BIN('31000000-0000-0000-0000-000000000002'));
INSERT INTO wallet_account (id, user_id, available_balance)
SELECT @wallet_jonas_id, @jonas_id, 0.00
WHERE NOT EXISTS (SELECT 1 FROM wallet_account WHERE user_id = @jonas_id);

SET @wallet_sofia_id = COALESCE((SELECT id FROM wallet_account WHERE user_id = @sofia_id LIMIT 1), UUID_TO_BIN('31000000-0000-0000-0000-000000000003'));
INSERT INTO wallet_account (id, user_id, available_balance)
SELECT @wallet_sofia_id, @sofia_id, 0.00
WHERE NOT EXISTS (SELECT 1 FROM wallet_account WHERE user_id = @sofia_id);

SET @wallet_tim_id = COALESCE((SELECT id FROM wallet_account WHERE user_id = @tim_id LIMIT 1), UUID_TO_BIN('31000000-0000-0000-0000-000000000004'));
INSERT INTO wallet_account (id, user_id, available_balance)
SELECT @wallet_tim_id, @tim_id, 0.00
WHERE NOT EXISTS (SELECT 1 FROM wallet_account WHERE user_id = @tim_id);

SET @wallet_clara_id = COALESCE((SELECT id FROM wallet_account WHERE user_id = @clara_id LIMIT 1), UUID_TO_BIN('31000000-0000-0000-0000-000000000005'));
INSERT INTO wallet_account (id, user_id, available_balance)
SELECT @wallet_clara_id, @clara_id, 0.00
WHERE NOT EXISTS (SELECT 1 FROM wallet_account WHERE user_id = @clara_id);

SET @pet_anna_mila_id = (SELECT p.id FROM pet p WHERE p.owner_id = @anna_id AND p.name = 'Mila' LIMIT 1);
SET @pet_ben_nino_id = (SELECT p.id FROM pet p WHERE p.owner_id = @ben_id AND p.name = 'Nino' LIMIT 1);
SET @pet_lara_coco_id = (SELECT p.id FROM pet p WHERE p.owner_id = @lara_id AND p.name = 'Coco' LIMIT 1);
SET @pet_lara_luna_id = (SELECT p.id FROM pet p WHERE p.owner_id = @lara_id AND p.name = 'Luna' LIMIT 1);

SET @pet_mia_fips_id = COALESCE((SELECT id FROM pet WHERE owner_id = @mia_id AND name = 'Fips' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000001'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_mia_fips_id, @mia_id, 'Fips', 'DOG', 'Beagle-Mix',
       'Freundlich, braucht klare Leinenfuehrung und kurze Suchspiele.',
       '2020-05-14', NULL, 'GEIMPFT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @mia_id AND name = 'Fips');

SET @pet_mia_kira_id = COALESCE((SELECT id FROM pet WHERE owner_id = @mia_id AND name = 'Kira' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000002'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_mia_kira_id, @mia_id, 'Kira', 'CAT', 'Maine Coon',
       'Sehr menschenbezogen, mag feste Futterzeiten und Kammrituale.',
       '2019-09-03', NULL, 'GEIMPFT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @mia_id AND name = 'Kira');

SET @pet_jonas_rango_id = COALESCE((SELECT id FROM pet WHERE owner_id = @jonas_id AND name = 'Rango' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000003'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_jonas_rango_id, @jonas_id, 'Rango', 'REPTILE', 'Bartagame',
       'Terrariumtemperatur und Lichtzeiten muessen kontrolliert werden.',
       '2021-04-22', NULL, 'UNBEKANNT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @jonas_id AND name = 'Rango');

SET @pet_jonas_nemo_id = COALESCE((SELECT id FROM pet WHERE owner_id = @jonas_id AND name = 'Nemo' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000004'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_jonas_nemo_id, @jonas_id, 'Nemo', 'FISH', 'Kampffisch',
       'Nur vorportioniertes Futter geben, Wasserstand kurz pruefen.',
       '2023-01-10', NULL, 'UNBEKANNT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @jonas_id AND name = 'Nemo');

SET @pet_sofia_momo_id = COALESCE((SELECT id FROM pet WHERE owner_id = @sofia_id AND name = 'Momo' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000005'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_sofia_momo_id, @sofia_id, 'Momo', 'RABBIT', 'Loewenkopfkaninchen',
       'Braucht frisches Heu, ruhige Ansprache und taeglich gereinigte Ecke.',
       '2022-06-12', NULL, 'GEIMPFT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @sofia_id AND name = 'Momo');

SET @pet_sofia_pepper_id = COALESCE((SELECT id FROM pet WHERE owner_id = @sofia_id AND name = 'Pepper' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000006'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_sofia_pepper_id, @sofia_id, 'Pepper', 'CAT', 'Europaeisch Kurzhaar',
       'Neugierig, darf aber nicht auf den Balkon.',
       '2020-10-01', NULL, 'GEIMPFT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @sofia_id AND name = 'Pepper');

SET @pet_clara_bruno_id = COALESCE((SELECT id FROM pet WHERE owner_id = @clara_id AND name = 'Bruno' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000007'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_clara_bruno_id, @clara_id, 'Bruno', 'DOG', 'Labrador',
       'Sehr freundlich, stark an der Leine, vertraegt Kinder.',
       '2018-02-19', NULL, 'GEIMPFT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @clara_id AND name = 'Bruno');

SET @pet_clara_lotta_id = COALESCE((SELECT id FROM pet WHERE owner_id = @clara_id AND name = 'Lotta' LIMIT 1), UUID_TO_BIN('32000000-0000-0000-0000-000000000008'));
INSERT INTO pet (id, owner_id, name, species, breed, notes, birth_date, custom_species, vaccination_status)
SELECT @pet_clara_lotta_id, @clara_id, 'Lotta', 'DOG', 'Mischling',
       'Anfangs vorsichtig, bleibt mit Bruno entspannt allein.',
       '2021-12-07', NULL, 'GEIMPFT'
WHERE NOT EXISTS (SELECT 1 FROM pet WHERE owner_id = @clara_id AND name = 'Lotta');

INSERT IGNORE INTO pet_tag (pet_id, tag) VALUES
    (@pet_mia_fips_id, 'VERSPIELT'),
    (@pet_mia_fips_id, 'LEINENFUEHRIG'),
    (@pet_mia_kira_id, 'KANN_ALLEIN_BLEIBEN'),
    (@pet_mia_kira_id, 'KASTRIERT'),
    (@pet_jonas_rango_id, 'KANN_ALLEIN_BLEIBEN'),
    (@pet_jonas_nemo_id, 'FUTTERSENSIBEL'),
    (@pet_sofia_momo_id, 'AENGSTLICH'),
    (@pet_sofia_momo_id, 'STUBENREIN'),
    (@pet_sofia_pepper_id, 'VERSPIELT'),
    (@pet_clara_bruno_id, 'VERTRAEGLICH_MIT_KINDERN'),
    (@pet_clara_bruno_id, 'LEINENFUEHRIG'),
    (@pet_clara_lotta_id, 'AENGSTLICH'),
    (@pet_clara_lotta_id, 'VERTRAEGLICH_MIT_ANDEREN_TIEREN');

DROP TEMPORARY TABLE IF EXISTS demo_image_seed;
CREATE TEMPORARY TABLE demo_image_seed (
    asset_id BINARY(16) NOT NULL,
    user_id BINARY(16) NULL,
    pet_id BINARY(16) NULL,
    label VARCHAR(20) NOT NULL,
    caption VARCHAR(80) NOT NULL,
    bg CHAR(7) NOT NULL,
    fg CHAR(7) NOT NULL
);

INSERT INTO demo_image_seed (asset_id, user_id, pet_id, label, caption, bg, fg) VALUES
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000001'), @mia_id, NULL, 'Mia', 'Sitter Leipzig', '#2F7D6E', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000002'), @tim_id, NULL, 'Tim', 'Sitter Dresden', '#4457A5', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000003'), @roland_id, NULL, 'Roland', 'Sitter Duesseldorf', '#7A4E8A', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000004'), NULL, @pet_clara_bruno_id, 'Bruno', 'Labrador', '#C06B3E', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000005'), NULL, @pet_clara_lotta_id, 'Lotta', 'Hund', '#9F5F80', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000006'), NULL, @pet_mia_fips_id, 'Fips', 'Beagle-Mix', '#3F8F5B', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000007'), NULL, @pet_mia_kira_id, 'Kira', 'Maine Coon', '#6E6A3D', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000008'), NULL, @pet_jonas_rango_id, 'Rango', 'Bartagame', '#B66A2C', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000009'), NULL, @pet_jonas_nemo_id, 'Nemo', 'Fisch', '#2F77A5', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000010'), NULL, @pet_sofia_momo_id, 'Momo', 'Kaninchen', '#6D8A3D', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000011'), NULL, @pet_sofia_pepper_id, 'Pepper', 'Katze', '#7B6A5A', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000012'), NULL, @pet_anna_mila_id, 'Mila', 'Katze', '#85625A', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000013'), NULL, @pet_ben_nino_id, 'Nino', 'Kaninchen', '#7D7D4A', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000014'), NULL, @pet_lara_luna_id, 'Luna', 'Dackel', '#A55C45', '#FFFFFF'),
    (UUID_TO_BIN('3b000000-0000-0000-0000-000000000015'), NULL, @pet_lara_coco_id, 'Coco', 'Vogel', '#3B8796', '#FFFFFF');

INSERT IGNORE INTO image_asset (id, user_id, pet_id, created_at)
SELECT asset_id, user_id, pet_id, CURRENT_TIMESTAMP(6)
FROM demo_image_seed
WHERE (user_id IS NOT NULL AND pet_id IS NULL)
   OR (user_id IS NULL AND pet_id IS NOT NULL);

INSERT IGNORE INTO image_asset_variant (
    asset_id, variant, mime_type, width, height, file_size, content
)
SELECT
    image_rows.asset_id,
    image_rows.variant,
    'image/svg+xml',
    image_rows.size,
    image_rows.size,
    LENGTH(image_rows.svg_content),
    CAST(image_rows.svg_content AS BINARY)
FROM (
    SELECT
        ia.id AS asset_id,
        sizes.variant,
        sizes.size,
        CONCAT(
            '<svg xmlns="http://www.w3.org/2000/svg" width="', sizes.size, '" height="', sizes.size, '" viewBox="0 0 768 768">',
            '<rect width="768" height="768" fill="', seed.bg, '"/>',
            '<circle cx="602" cy="154" r="172" fill="#ffffff" opacity="0.18"/>',
            '<circle cx="130" cy="648" r="220" fill="#000000" opacity="0.10"/>',
            '<text x="64" y="404" font-family="Arial,sans-serif" font-size="116" font-weight="800" fill="', seed.fg, '">', seed.label, '</text>',
            '<text x="68" y="490" font-family="Arial,sans-serif" font-size="42" font-weight="600" fill="', seed.fg, '" opacity="0.88">', seed.caption, '</text>',
            '</svg>'
        ) AS svg_content
    FROM demo_image_seed seed
    JOIN image_asset ia
      ON (seed.user_id IS NOT NULL AND ia.user_id = seed.user_id)
      OR (seed.pet_id IS NOT NULL AND ia.pet_id = seed.pet_id)
    JOIN (
        SELECT 'AVATAR' AS variant, 256 AS size
        UNION ALL
        SELECT 'DISPLAY' AS variant, 768 AS size
    ) sizes
) image_rows;

DROP TEMPORARY TABLE IF EXISTS demo_image_seed;

SET @offer_clara_trip_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @clara_id AND title = 'Urlaubsbetreuung fuer Bruno und Lotta' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000001'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_clara_trip_id, DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 14 DAY),
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 24 DAY), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY),
    @clara_id, @clara_id, @pet_clara_bruno_id,
    'Urlaubsbetreuung fuer Bruno und Lotta', 'ONE_TIME', 'PET_AND_HOUSE_SITTING', 'DOG', 'FULL_DAY',
    'OWNER_OFFER', 85.00, 'Drei Tage Betreuung fuer zwei Hunde inklusive Hausrunde.', 'BOOKED'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @clara_id AND title = 'Urlaubsbetreuung fuer Bruno und Lotta');

SET @offer_mia_cat_visit_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @mia_id AND title = 'Flexible Katzenbesuche in Leipzig' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000002'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_mia_cat_visit_id, DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY),
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 9 DAY), CURRENT_TIMESTAMP(6),
    @mia_id, @mia_id, NULL,
    'Flexible Katzenbesuche in Leipzig', 'ONE_TIME', 'PET_SITTING', 'CAT', 'MORNING',
    'SITTER_OFFER', 28.00, 'Futter, Wasser, Spielzeit und kurze Foto-Updates fuer Katzen.', 'BOOKED'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @mia_id AND title = 'Flexible Katzenbesuche in Leipzig');

SET @offer_tim_gassi_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @tim_id AND title = 'Gassi-Service Dresden regelmaessig' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000003'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_tim_gassi_id, NULL, NULL,
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 12 DAY), CURRENT_TIMESTAMP(6),
    @tim_id, @tim_id, NULL,
    'Gassi-Service Dresden regelmaessig', 'REGULAR', 'PET_SITTING', 'DOG', 'AFTERNOON',
    'SITTER_OFFER', 22.00, 'Regelmaessige Nachmittagsrunden fuer Hunde in Dresden.', 'BOOKED'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @tim_id AND title = 'Gassi-Service Dresden regelmaessig');

SET @offer_jonas_rango_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @jonas_id AND title = 'Terrarium-Check fuer Rango' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000004'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_jonas_rango_id, NULL, NULL,
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 40 DAY), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 8 DAY),
    @jonas_id, @jonas_id, @pet_jonas_rango_id,
    'Terrarium-Check fuer Rango', 'REGULAR', 'PET_SITTING', 'REPTILE', 'MORNING',
    'OWNER_OFFER', 18.00, 'Regelmaessige Kontrolle von Licht, Temperatur und Futter.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @jonas_id AND title = 'Terrarium-Check fuer Rango');

SET @offer_sofia_momo_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @sofia_id AND title = 'Kaninchenbetreuung fuer Momo' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000005'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_sofia_momo_id, DATE_ADD(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 24 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @sofia_id, @sofia_id, @pet_sofia_momo_id,
    'Kaninchenbetreuung fuer Momo', 'ONE_TIME', 'PET_SITTING', 'SMALL_ANIMAL', 'MORNING',
    'OWNER_OFFER', 35.00, 'Futter, Wasser und Gehegecheck fuer Momo waehrend einer Reise.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @sofia_id AND title = 'Kaninchenbetreuung fuer Momo');

SET @offer_anna_mila_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @anna_id AND title = 'Mila braucht Abendbesuche' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000006'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_anna_mila_id, DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @anna_id, @anna_id, @pet_anna_mila_id,
    'Mila braucht Abendbesuche', 'ONE_TIME', 'PET_SITTING', 'CAT', 'AFTERNOON',
    'OWNER_OFFER', 25.00, 'Abends fuettern, Wasser wechseln und kurz mit Mila spielen.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @anna_id AND title = 'Mila braucht Abendbesuche');

SET @offer_ben_nino_draft_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @ben_id AND title = 'Entwurf: Nino morgens fuettern' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000007'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_ben_nino_draft_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @ben_id, @ben_id, @pet_ben_nino_id,
    'Entwurf: Nino morgens fuettern', 'REGULAR', 'PET_SITTING', 'SMALL_ANIMAL', 'MORNING',
    'OWNER_OFFER', 20.00, 'Noch nicht fertig: regelmaessige Morgenpflege fuer Nino.', 'DRAFT'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @ben_id AND title = 'Entwurf: Nino morgens fuettern');

SET @offer_tim_house_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @tim_id AND title = 'Haussitting mit Tierpflege in Dresden' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000008'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_tim_house_id, DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 21 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @tim_id, @tim_id, NULL,
    'Haussitting mit Tierpflege in Dresden', 'ONE_TIME', 'PET_AND_HOUSE_SITTING', 'OTHER', 'FULL_DAY',
    'SITTER_OFFER', 95.00, 'Komplettpaket fuer Haustiere, Pflanzen, Post und kurze Hauschecks.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @tim_id AND title = 'Haussitting mit Tierpflege in Dresden');

SET @offer_mia_regular_cat_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @mia_id AND title = 'Katzenbesuche Leipzig Mitte' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000009'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_mia_regular_cat_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @mia_id, @mia_id, NULL,
    'Katzenbesuche Leipzig Mitte', 'REGULAR', 'PET_SITTING', 'CAT', 'MORNING',
    'SITTER_OFFER', 30.00, 'Regelmaessige Katzenbesuche mit Futter, Streu und Spielzeit.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @mia_id AND title = 'Katzenbesuche Leipzig Mitte');

SET @offer_roland_senior_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @roland_id AND title = 'Seniorenhund Betreuung Duesseldorf' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000010'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_roland_senior_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @roland_id, @roland_id, NULL,
    'Seniorenhund Betreuung Duesseldorf', 'REGULAR', 'PET_SITTING', 'DOG', 'FULL_DAY',
    'SITTER_OFFER', 40.00, 'Ruhige Betreuung fuer aeltere Hunde mit kurzen Runden und Pausen.', 'OPEN'
WHERE @roland_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @roland_id AND title = 'Seniorenhund Betreuung Duesseldorf');

SET @offer_jonas_aquarium_draft_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @jonas_id AND title = 'Entwurf: Aquarium Pflege Frankfurt' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000011'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_jonas_aquarium_draft_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @jonas_id, @jonas_id, @pet_jonas_nemo_id,
    'Entwurf: Aquarium Pflege Frankfurt', 'REGULAR', 'PET_SITTING', 'FISH', 'MORNING',
    'OWNER_OFFER', 20.00, 'Entwurf fuer woechentliche Aquariumkontrolle.', 'DRAFT'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @jonas_id AND title = 'Entwurf: Aquarium Pflege Frankfurt');

SET @offer_lara_coco_luna_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @lara_id AND title = 'Coco und Luna nachmittags betreuen' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000012'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_lara_coco_luna_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @lara_id, @lara_id, @pet_lara_luna_id,
    'Coco und Luna nachmittags betreuen', 'REGULAR', 'PET_SITTING', 'OTHER', 'AFTERNOON',
    'OWNER_OFFER', 45.00, 'Regelmaessige Betreuung fuer Hund und Vogel am Nachmittag.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @lara_id AND title = 'Coco und Luna nachmittags betreuen');

SET @offer_tim_midday_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @tim_id AND title = 'Kurzfristige Mittagsrunde Dresden' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000013'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_tim_midday_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @tim_id, @tim_id, NULL,
    'Kurzfristige Mittagsrunde Dresden', 'ONE_TIME', 'PET_SITTING', 'DOG', 'AFTERNOON',
    'SITTER_OFFER', 32.00, 'Einmalige Runde fuer kurzfristige Termine rund um die Innenstadt.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @tim_id AND title = 'Kurzfristige Mittagsrunde Dresden');

SET @offer_mia_small_animals_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @mia_id AND title = 'Kleintier-Checks am Wochenende' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000014'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_mia_small_animals_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @mia_id, @mia_id, NULL,
    'Kleintier-Checks am Wochenende', 'REGULAR', 'PET_SITTING', 'SMALL_ANIMAL', 'MORNING',
    'SITTER_OFFER', 24.00, 'Wochenend-Checks fuer Kaninchen, Fische und andere Kleintiere.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @mia_id AND title = 'Kleintier-Checks am Wochenende');

SET @offer_roland_cat_dropin_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @roland_id AND title = 'Katzenfuetterung Duesseldorf Stadtmitte' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000015'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_roland_cat_dropin_id, DATE_ADD(CURDATE(), INTERVAL 8 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @roland_id, @roland_id, NULL,
    'Katzenfuetterung Duesseldorf Stadtmitte', 'ONE_TIME', 'PET_SITTING', 'CAT', 'MORNING',
    'SITTER_OFFER', 30.00, 'Zwei Morgenbesuche mit Futter, Wasser und Katzenklo-Check.', 'OPEN'
WHERE @roland_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @roland_id AND title = 'Katzenfuetterung Duesseldorf Stadtmitte');

SET @offer_sofia_pepper_regular_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @sofia_id AND title = 'Pepper morgens in Stuttgart' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000016'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_sofia_pepper_regular_id, NULL, NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @sofia_id, @sofia_id, @pet_sofia_pepper_id,
    'Pepper morgens in Stuttgart', 'REGULAR', 'PET_SITTING', 'CAT', 'MORNING',
    'OWNER_OFFER', 26.00, 'Regelmaessige Morgenbesuche fuer Futter, Wasser und Spielzeit.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @sofia_id AND title = 'Pepper morgens in Stuttgart');

SET @offer_clara_weekend_bruno_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @clara_id AND title = 'Wochenendrunde fuer Bruno' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000017'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_clara_weekend_bruno_id, DATE_ADD(CURDATE(), INTERVAL 6 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @clara_id, @clara_id, @pet_clara_bruno_id,
    'Wochenendrunde fuer Bruno', 'ONE_TIME', 'PET_SITTING', 'DOG', 'AFTERNOON',
    'OWNER_OFFER', 40.00, 'Lange Nachmittagsrunde fuer Bruno am Wochenende.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @clara_id AND title = 'Wochenendrunde fuer Bruno');

SET @offer_jonas_aquarium_visit_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @jonas_id AND title = 'Aquarium Kurzcheck Frankfurt' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000018'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_jonas_aquarium_visit_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @jonas_id, @jonas_id, @pet_jonas_nemo_id,
    'Aquarium Kurzcheck Frankfurt', 'ONE_TIME', 'PET_SITTING', 'FISH', 'MORNING',
    'OWNER_OFFER', 15.00, 'Kurzer Check von Futter, Licht und Wasserstand.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @jonas_id AND title = 'Aquarium Kurzcheck Frankfurt');

SET @offer_lara_luna_walk_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @lara_id AND title = 'Luna braucht Mittagsspaziergang' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000019'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_lara_luna_walk_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @lara_id, @lara_id, @pet_lara_luna_id,
    'Luna braucht Mittagsspaziergang', 'ONE_TIME', 'PET_SITTING', 'DOG', 'AFTERNOON',
    'OWNER_OFFER', 32.00, 'Ein ruhiger Spaziergang fuer Luna in Hamburg-Mitte.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @lara_id AND title = 'Luna braucht Mittagsspaziergang');

SET @offer_ben_nino_weekend_id = COALESCE((SELECT offer_id FROM offer WHERE create_user = @ben_id AND title = 'Nino Wochenende Pflege' LIMIT 1), UUID_TO_BIN('33000000-0000-0000-0000-000000000020'));
INSERT INTO offer (
    offer_id, start_date, end_date, create_date, update_date, create_user, update_user, pet_id,
    title, frequency, care_type, animal_type, time_slot, offer_type, price, description, status
) SELECT
    @offer_ben_nino_weekend_id, DATE_ADD(CURDATE(), INTERVAL 11 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), @ben_id, @ben_id, @pet_ben_nino_id,
    'Nino Wochenende Pflege', 'ONE_TIME', 'PET_SITTING', 'SMALL_ANIMAL', 'MORNING',
    'OWNER_OFFER', 22.00, 'Wochenendpflege fuer Nino mit Heu, Wasser und Gehegecheck.', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM offer WHERE create_user = @ben_id AND title = 'Nino Wochenende Pflege');

INSERT IGNORE INTO offer_pet (offer_id, pet_id) VALUES
    (@offer_clara_trip_id, @pet_clara_bruno_id),
    (@offer_clara_trip_id, @pet_clara_lotta_id),
    (@offer_jonas_rango_id, @pet_jonas_rango_id),
    (@offer_sofia_momo_id, @pet_sofia_momo_id),
    (@offer_anna_mila_id, @pet_anna_mila_id),
    (@offer_ben_nino_draft_id, @pet_ben_nino_id),
    (@offer_jonas_aquarium_draft_id, @pet_jonas_nemo_id),
    (@offer_lara_coco_luna_id, @pet_lara_luna_id),
    (@offer_lara_coco_luna_id, @pet_lara_coco_id),
    (@offer_sofia_pepper_regular_id, @pet_sofia_pepper_id),
    (@offer_clara_weekend_bruno_id, @pet_clara_bruno_id),
    (@offer_jonas_aquarium_visit_id, @pet_jonas_nemo_id),
    (@offer_lara_luna_walk_id, @pet_lara_luna_id),
    (@offer_ben_nino_weekend_id, @pet_ben_nino_id);

INSERT IGNORE INTO offer_weekday (offer_id, weekday) VALUES
    (@offer_tim_gassi_id, 'MONDAY'),
    (@offer_tim_gassi_id, 'WEDNESDAY'),
    (@offer_tim_gassi_id, 'FRIDAY'),
    (@offer_jonas_rango_id, 'TUESDAY'),
    (@offer_jonas_rango_id, 'FRIDAY'),
    (@offer_ben_nino_draft_id, 'TUESDAY'),
    (@offer_ben_nino_draft_id, 'THURSDAY'),
    (@offer_mia_regular_cat_id, 'MONDAY'),
    (@offer_mia_regular_cat_id, 'WEDNESDAY'),
    (@offer_mia_regular_cat_id, 'FRIDAY'),
    (@offer_roland_senior_id, 'TUESDAY'),
    (@offer_roland_senior_id, 'THURSDAY'),
    (@offer_jonas_aquarium_draft_id, 'SATURDAY'),
    (@offer_lara_coco_luna_id, 'MONDAY'),
    (@offer_lara_coco_luna_id, 'THURSDAY'),
    (@offer_mia_small_animals_id, 'SATURDAY'),
    (@offer_mia_small_animals_id, 'SUNDAY'),
    (@offer_sofia_pepper_regular_id, 'TUESDAY'),
    (@offer_sofia_pepper_regular_id, 'THURSDAY');

SET @request_clara_tim_id = COALESCE((SELECT id FROM `request` WHERE offer_id = @offer_clara_trip_id AND requester_id = @tim_id LIMIT 1), UUID_TO_BIN('34000000-0000-0000-0000-000000000001'));
INSERT INTO `request` (id, offer_id, requester_id, status, message, created_at, updated_at)
SELECT @request_clara_tim_id, @offer_clara_trip_id, @tim_id, 'ACCEPTED',
       'Ich kann beide Hunde fuer die drei Tage uebernehmen und bleibe auch ueber Nacht.',
       DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 23 DAY), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 22 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `request` WHERE offer_id = @offer_clara_trip_id AND requester_id = @tim_id);

SET @request_mia_sofia_id = COALESCE((SELECT id FROM `request` WHERE offer_id = @offer_mia_cat_visit_id AND requester_id = @sofia_id LIMIT 1), UUID_TO_BIN('34000000-0000-0000-0000-000000000002'));
INSERT INTO `request` (id, offer_id, requester_id, status, message, created_at, updated_at)
SELECT @request_mia_sofia_id, @offer_mia_cat_visit_id, @sofia_id, 'ACCEPTED',
       'Pepper braucht morgens Futter und ein kurzes Spielupdate.',
       DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `request` WHERE offer_id = @offer_mia_cat_visit_id AND requester_id = @sofia_id);

SET @request_tim_clara_id = COALESCE((SELECT id FROM `request` WHERE offer_id = @offer_tim_gassi_id AND requester_id = @clara_id LIMIT 1), UUID_TO_BIN('34000000-0000-0000-0000-000000000003'));
INSERT INTO `request` (id, offer_id, requester_id, status, message, created_at, updated_at)
SELECT @request_tim_clara_id, @offer_tim_gassi_id, @clara_id, 'ACCEPTED',
       'Bruno und Lotta brauchen regelmaessig Bewegung am Nachmittag.',
       DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 11 DAY), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 10 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `request` WHERE offer_id = @offer_tim_gassi_id AND requester_id = @clara_id);

SET @request_jonas_mia_id = COALESCE((SELECT id FROM `request` WHERE offer_id = @offer_jonas_rango_id AND requester_id = @mia_id LIMIT 1), UUID_TO_BIN('34000000-0000-0000-0000-000000000004'));
INSERT INTO `request` (id, offer_id, requester_id, status, message, created_at, updated_at)
SELECT @request_jonas_mia_id, @offer_jonas_rango_id, @mia_id, 'ACCEPTED',
       'Ich habe Erfahrung mit Terrarien und kann die Kontrollen uebernehmen.',
       DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 39 DAY), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 38 DAY)
WHERE NOT EXISTS (SELECT 1 FROM `request` WHERE offer_id = @offer_jonas_rango_id AND requester_id = @mia_id);

SET @booking_clara_trip_id = COALESCE((SELECT id FROM booking WHERE accepted_request_id = @request_clara_tim_id LIMIT 1), UUID_TO_BIN('35000000-0000-0000-0000-000000000001'));
INSERT INTO booking (
    id, offer_id, accepted_request_id, owner_id, sitter_id, pet_id,
    start_date, end_date, price_per_day, total_price, frequency, time_slot, recurring_ended_on, status, created_at
) SELECT
    @booking_clara_trip_id, @offer_clara_trip_id, @request_clara_tim_id,
    @clara_id, @tim_id, @pet_clara_bruno_id,
    DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 14 DAY),
    85.00, 255.00, 'ONE_TIME', 'FULL_DAY', NULL, 'COMPLETED',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 22 DAY)
WHERE NOT EXISTS (SELECT 1 FROM booking WHERE id = @booking_clara_trip_id);

SET @booking_mia_sofia_id = COALESCE((SELECT id FROM booking WHERE accepted_request_id = @request_mia_sofia_id LIMIT 1), UUID_TO_BIN('35000000-0000-0000-0000-000000000002'));
INSERT INTO booking (
    id, offer_id, accepted_request_id, owner_id, sitter_id, pet_id,
    start_date, end_date, price_per_day, total_price, frequency, time_slot, recurring_ended_on, status, created_at
) SELECT
    @booking_mia_sofia_id, @offer_mia_cat_visit_id, @request_mia_sofia_id,
    @sofia_id, @mia_id, NULL,
    DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY),
    28.00, 84.00, 'ONE_TIME', 'MORNING', NULL, 'CREATED',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM booking WHERE id = @booking_mia_sofia_id);

SET @booking_tim_clara_id = COALESCE((SELECT id FROM booking WHERE accepted_request_id = @request_tim_clara_id LIMIT 1), UUID_TO_BIN('35000000-0000-0000-0000-000000000003'));
INSERT INTO booking (
    id, offer_id, accepted_request_id, owner_id, sitter_id, pet_id,
    start_date, end_date, price_per_day, total_price, frequency, time_slot, recurring_ended_on, status, created_at
) SELECT
    @booking_tim_clara_id, @offer_tim_gassi_id, @request_tim_clara_id,
    @clara_id, @tim_id, NULL,
    NULL, NULL, 22.00, 66.00, 'REGULAR', 'AFTERNOON', NULL, 'CREATED',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 10 DAY)
WHERE NOT EXISTS (SELECT 1 FROM booking WHERE id = @booking_tim_clara_id);

SET @booking_jonas_mia_id = COALESCE((SELECT id FROM booking WHERE accepted_request_id = @request_jonas_mia_id LIMIT 1), UUID_TO_BIN('35000000-0000-0000-0000-000000000004'));
INSERT INTO booking (
    id, offer_id, accepted_request_id, owner_id, sitter_id, pet_id,
    start_date, end_date, price_per_day, total_price, frequency, time_slot, recurring_ended_on, status, created_at
) SELECT
    @booking_jonas_mia_id, @offer_jonas_rango_id, @request_jonas_mia_id,
    @jonas_id, @mia_id, @pet_jonas_rango_id,
    NULL, NULL, 18.00, 36.00, 'REGULAR', 'MORNING', DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'ENDED',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 38 DAY)
WHERE NOT EXISTS (SELECT 1 FROM booking WHERE id = @booking_jonas_mia_id);

INSERT IGNORE INTO booking_weekday (booking_id, weekday) VALUES
    (@booking_tim_clara_id, 'MONDAY'),
    (@booking_tim_clara_id, 'WEDNESDAY'),
    (@booking_tim_clara_id, 'FRIDAY'),
    (@booking_jonas_mia_id, 'TUESDAY'),
    (@booking_jonas_mia_id, 'FRIDAY');

SET @payment_clara_trip_id = COALESCE((SELECT id FROM booking_payment WHERE booking_id = @booking_clara_trip_id LIMIT 1), UUID_TO_BIN('36000000-0000-0000-0000-000000000001'));
INSERT INTO booking_payment (
    id, booking_id, owner_id, sitter_id, amount, status,
    held_at, release_requested_at, released_at, refunded_at, created_at, updated_at
) SELECT
    @payment_clara_trip_id, @booking_clara_trip_id, @clara_id, @tim_id, 255.00, 'RELEASED',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 22 DAY),
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 12 DAY),
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY),
    NULL,
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 22 DAY),
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM booking_payment WHERE booking_id = @booking_clara_trip_id);

SET @payment_mia_sofia_id = COALESCE((SELECT id FROM booking_payment WHERE booking_id = @booking_mia_sofia_id LIMIT 1), UUID_TO_BIN('36000000-0000-0000-0000-000000000002'));
INSERT INTO booking_payment (
    id, booking_id, owner_id, sitter_id, amount, status,
    held_at, release_requested_at, released_at, refunded_at, created_at, updated_at
) SELECT
    @payment_mia_sofia_id, @booking_mia_sofia_id, @sofia_id, @mia_id, 84.00, 'HELD',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY),
    NULL, NULL, NULL,
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY),
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM booking_payment WHERE booking_id = @booking_mia_sofia_id);

SET @last_week_monday = DATE_SUB(CURDATE(), INTERVAL (WEEKDAY(CURDATE()) + 7) DAY);
SET @last_week_sunday = DATE_ADD(@last_week_monday, INTERVAL 6 DAY);
SET @ended_period_monday = DATE_SUB(CURDATE(), INTERVAL (WEEKDAY(CURDATE()) + 21) DAY);
SET @ended_period_sunday = DATE_ADD(@ended_period_monday, INTERVAL 6 DAY);

SET @recurring_tim_clara_payment_id = COALESCE((SELECT id FROM recurring_booking_payment WHERE booking_id = @booking_tim_clara_id AND period_start = @last_week_monday LIMIT 1), UUID_TO_BIN('37000000-0000-0000-0000-000000000001'));
INSERT INTO recurring_booking_payment (
    id, booking_id, owner_id, sitter_id, period_start, period_end,
    occurrence_count, price_per_occurrence, amount, status,
    held_at, release_requested_at, released_at, refunded_at, created_at, updated_at
) SELECT
    @recurring_tim_clara_payment_id, @booking_tim_clara_id, @clara_id, @tim_id,
    @last_week_monday, @last_week_sunday, 3, 22.00, 66.00, 'HELD',
    DATE_ADD(@last_week_monday, INTERVAL 1 DAY), NULL, NULL, NULL,
    DATE_ADD(@last_week_monday, INTERVAL 1 DAY), DATE_ADD(@last_week_monday, INTERVAL 1 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM recurring_booking_payment
    WHERE booking_id = @booking_tim_clara_id AND period_start = @last_week_monday
);

SET @recurring_jonas_mia_payment_id = COALESCE((SELECT id FROM recurring_booking_payment WHERE booking_id = @booking_jonas_mia_id AND period_start = @ended_period_monday LIMIT 1), UUID_TO_BIN('37000000-0000-0000-0000-000000000002'));
INSERT INTO recurring_booking_payment (
    id, booking_id, owner_id, sitter_id, period_start, period_end,
    occurrence_count, price_per_occurrence, amount, status,
    held_at, release_requested_at, released_at, refunded_at, created_at, updated_at
) SELECT
    @recurring_jonas_mia_payment_id, @booking_jonas_mia_id, @jonas_id, @mia_id,
    @ended_period_monday, @ended_period_sunday, 2, 18.00, 36.00, 'RELEASED',
    DATE_ADD(@ended_period_monday, INTERVAL 1 DAY),
    DATE_ADD(@ended_period_sunday, INTERVAL 1 DAY),
    DATE_ADD(@ended_period_sunday, INTERVAL 3 DAY),
    NULL,
    DATE_ADD(@ended_period_monday, INTERVAL 1 DAY),
    DATE_ADD(@ended_period_sunday, INTERVAL 3 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM recurring_booking_payment
    WHERE booking_id = @booking_jonas_mia_id AND period_start = @ended_period_monday
);

UPDATE wallet_account
SET available_balance = CASE user_id
    WHEN @clara_id THEN 279.00
    WHEN @sofia_id THEN 116.00
    WHEN @jonas_id THEN 114.00
    WHEN @tim_id THEN 255.00
    WHEN @mia_id THEN 36.00
    ELSE available_balance
END
WHERE user_id IN (@clara_id, @sofia_id, @jonas_id, @tim_id, @mia_id);

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000001'), @wallet_clara_id, NULL, NULL,
         'DEV_TOP_UP', 600.00, 600.00, 'Demo-Guthaben aufgeladen',
         DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 23 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000001'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000002'), @wallet_clara_id, @payment_clara_trip_id, NULL,
         'ESCROW_HOLD', -255.00, 345.00, 'Treuhandzahlung fuer Urlaubsbetreuung fuer Bruno und Lotta',
         DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 22 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000002'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000003'), @wallet_tim_id, @payment_clara_trip_id, NULL,
         'PAYOUT', 255.00, 255.00, 'Auszahlung fuer Urlaubsbetreuung fuer Bruno und Lotta',
         DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000003'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000004'), @wallet_sofia_id, NULL, NULL,
         'DEV_TOP_UP', 200.00, 200.00, 'Demo-Guthaben aufgeladen',
         DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000004'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000005'), @wallet_sofia_id, @payment_mia_sofia_id, NULL,
         'ESCROW_HOLD', -84.00, 116.00, 'Treuhandzahlung fuer Flexible Katzenbesuche in Leipzig',
         DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000005'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000006'), @wallet_clara_id, NULL, @recurring_tim_clara_payment_id,
         'ESCROW_HOLD', -66.00, 279.00, 'Woechentliche Treuhandzahlung fuer Gassi-Service Dresden regelmaessig',
         DATE_ADD(@last_week_monday, INTERVAL 1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000006'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000007'), @wallet_jonas_id, NULL, NULL,
         'DEV_TOP_UP', 150.00, 150.00, 'Demo-Guthaben aufgeladen',
         DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 38 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000007'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000008'), @wallet_jonas_id, NULL, @recurring_jonas_mia_payment_id,
         'ESCROW_HOLD', -36.00, 114.00, 'Woechentliche Treuhandzahlung fuer Terrarium-Check fuer Rango',
         DATE_ADD(@ended_period_monday, INTERVAL 1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000008'));

INSERT INTO wallet_transaction (
    id, wallet_account_id, booking_payment_id, recurring_booking_payment_id,
    type, amount, balance_after, description, created_at
) SELECT UUID_TO_BIN('38000000-0000-0000-0000-000000000009'), @wallet_mia_id, NULL, @recurring_jonas_mia_payment_id,
         'PAYOUT', 36.00, 36.00, 'Auszahlung regelmaessiger Termine fuer Terrarium-Check fuer Rango',
         DATE_ADD(@ended_period_sunday, INTERVAL 3 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wallet_transaction WHERE id = UUID_TO_BIN('38000000-0000-0000-0000-000000000009'));

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000001'), @anna_id, @offer_tim_house_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @anna_id AND offer_id = @offer_tim_house_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000002'), @ben_id, @offer_mia_regular_cat_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @ben_id AND offer_id = @offer_mia_regular_cat_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000003'), @sofia_id, @offer_roland_senior_id, CURRENT_TIMESTAMP(6)
WHERE @offer_roland_senior_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @sofia_id AND offer_id = @offer_roland_senior_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000004'), @tim_id, @offer_sofia_momo_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @tim_id AND offer_id = @offer_sofia_momo_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000005'), @clara_id, @offer_anna_mila_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @clara_id AND offer_id = @offer_anna_mila_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000006'), @anna_id, @offer_tim_midday_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @anna_id AND offer_id = @offer_tim_midday_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000007'), @sofia_id, @offer_mia_small_animals_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @sofia_id AND offer_id = @offer_mia_small_animals_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000008'), @clara_id, @offer_roland_cat_dropin_id, CURRENT_TIMESTAMP(6)
WHERE @offer_roland_cat_dropin_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @clara_id AND offer_id = @offer_roland_cat_dropin_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000009'), @tim_id, @offer_lara_luna_walk_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @tim_id AND offer_id = @offer_lara_luna_walk_id);

INSERT INTO favorite (favorite_id, user_id, offer_id, created_at)
SELECT UUID_TO_BIN('39000000-0000-0000-0000-000000000010'), @mia_id, @offer_ben_nino_weekend_id, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM favorite WHERE user_id = @mia_id AND offer_id = @offer_ben_nino_weekend_id);

INSERT INTO user_review (id, booking_id, reviewer_id, reviewee_id, rating, comment, created_at)
SELECT UUID_TO_BIN('3a000000-0000-0000-0000-000000000001'), @booking_clara_trip_id, @clara_id, @tim_id,
       5, 'Sehr zuverlaessig, Bruno und Lotta waren entspannt.',
       DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM user_review WHERE booking_id = @booking_clara_trip_id AND reviewer_id = @clara_id
);

INSERT INTO user_review (id, booking_id, reviewer_id, reviewee_id, rating, comment, created_at)
SELECT UUID_TO_BIN('3a000000-0000-0000-0000-000000000002'), @booking_clara_trip_id, @tim_id, @clara_id,
       5, 'Klare Absprachen und liebe Hunde.',
       DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM user_review WHERE booking_id = @booking_clara_trip_id AND reviewer_id = @tim_id
);
