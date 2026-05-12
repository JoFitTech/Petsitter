ALTER TABLE offer
    ADD COLUMN title VARCHAR(120) NULL AFTER pet_id,
    ADD COLUMN frequency VARCHAR(32) NULL AFTER title,
    ADD COLUMN care_type VARCHAR(64) NULL AFTER frequency,
    ADD COLUMN animal_type VARCHAR(32) NULL AFTER care_type;

ALTER TABLE offer
    ADD CONSTRAINT chk_offer_frequency
        CHECK (frequency IS NULL OR frequency IN ('ONE_TIME', 'REGULAR')),
    ADD CONSTRAINT chk_offer_care_type
        CHECK (care_type IS NULL OR care_type IN ('PET_SITTING', 'PET_AND_HOUSE_SITTING')),
    ADD CONSTRAINT chk_offer_animal_type
        CHECK (animal_type IS NULL OR animal_type IN ('DOG', 'CAT', 'SMALL_ANIMAL', 'BIRD', 'REPTILE', 'FISH', 'OTHER'));
