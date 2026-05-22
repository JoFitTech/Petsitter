ALTER TABLE pet
    DROP CONSTRAINT chk_pets_species;

ALTER TABLE pet
    ADD CONSTRAINT chk_pets_species
        CHECK (species IN ('DOG', 'CAT', 'BIRD', 'RABBIT', 'FISH', 'REPTILE', 'RODENT', 'OTHER'));

UPDATE pet SET species = 'FISH',    custom_species = NULL WHERE species = 'OTHER' AND custom_species = 'Fisch';
UPDATE pet SET species = 'REPTILE', custom_species = NULL WHERE species = 'OTHER' AND custom_species = 'Reptil';
UPDATE pet SET species = 'RODENT',  custom_species = NULL WHERE species = 'OTHER' AND custom_species = 'Nagetier';
