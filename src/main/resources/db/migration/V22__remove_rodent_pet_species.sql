UPDATE pet SET species = 'OTHER', custom_species = 'Nagetier' WHERE species = 'RODENT';

ALTER TABLE pet
    DROP CONSTRAINT chk_pets_species;

ALTER TABLE pet
    ADD CONSTRAINT chk_pets_species
        CHECK (species IN ('DOG', 'CAT', 'BIRD', 'RABBIT', 'FISH', 'REPTILE', 'OTHER'));
