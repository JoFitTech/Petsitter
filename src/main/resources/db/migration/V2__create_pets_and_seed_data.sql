CREATE TABLE IF NOT EXISTS pets (
    id BINARY(16) NOT NULL PRIMARY KEY,
    owner_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(32) NOT NULL,
    breed VARCHAR(120),
    age INT,
    notes VARCHAR(1000),
    KEY idx_pets_owner_id (owner_id),
    CONSTRAINT fk_pets_owner
        FOREIGN KEY (owner_id) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_pets_species
        CHECK (species IN ('DOG', 'CAT', 'BIRD', 'RABBIT', 'OTHER')),
    CONSTRAINT chk_pets_age
        CHECK (age IS NULL OR age >= 0)
);

INSERT INTO pets (
    id, owner_id, name, species, breed, age, notes
)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    'Balu',
    'DOG',
    'Golden Retriever',
    4,
    'Braucht lange Spaziergaenge und liebt Apportieren.'
FROM users u
WHERE u.email = 'anna.mueller@petsitter.local'
  AND NOT EXISTS (
      SELECT 1
      FROM pets p
      WHERE p.owner_id = u.id
        AND p.name = 'Balu'
  );

INSERT INTO pets (
    id, owner_id, name, species, breed, age, notes
)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    'Mila',
    'CAT',
    'Europaeisch Kurzhaar',
    2,
    'Eher ruhig, braucht morgens und abends Futter.'
FROM users u
WHERE u.email = 'anna.mueller@petsitter.local'
  AND NOT EXISTS (
      SELECT 1
      FROM pets p
      WHERE p.owner_id = u.id
        AND p.name = 'Mila'
  );

INSERT INTO pets (
    id, owner_id, name, species, breed, age, notes
)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    'Nino',
    'RABBIT',
    'Zwergkaninchen',
    1,
    'Braucht taeglich frisches Heu und ruhige Umgebung.'
FROM users u
WHERE u.email = 'ben.schmidt@petsitter.local'
  AND NOT EXISTS (
      SELECT 1
      FROM pets p
      WHERE p.owner_id = u.id
        AND p.name = 'Nino'
  );

INSERT INTO pets (
    id, owner_id, name, species, breed, age, notes
)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    'Coco',
    'BIRD',
    'Wellensittich',
    3,
    'Kaefig morgens oeffnen und abends wieder sichern.'
FROM users u
WHERE u.email = 'lara.weber@petsitter.local'
  AND NOT EXISTS (
      SELECT 1
      FROM pets p
      WHERE p.owner_id = u.id
        AND p.name = 'Coco'
  );

INSERT INTO pets (
    id, owner_id, name, species, breed, age, notes
)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    'Luna',
    'DOG',
    'Dackel',
    6,
    'Mag kurze Spaziergaenge und vertraegt kein Huehnchen.'
FROM users u
WHERE u.email = 'lara.weber@petsitter.local'
  AND NOT EXISTS (
      SELECT 1
      FROM pets p
      WHERE p.owner_id = u.id
        AND p.name = 'Luna'
  );
