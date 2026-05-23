ALTER TABLE pet
    ADD COLUMN vaccination_status VARCHAR(32) NOT NULL DEFAULT 'UNBEKANNT';

ALTER TABLE pet
    ADD CONSTRAINT chk_pet_vaccination_status
        CHECK (vaccination_status IN ('GEIMPFT', 'UNGEIMPFT', 'UNBEKANNT'));

CREATE TABLE IF NOT EXISTS pet_tag (
    pet_id BINARY(16) NOT NULL,
    tag VARCHAR(64) NOT NULL,
    PRIMARY KEY (pet_id, tag),
    CONSTRAINT fk_pet_tag_pet
        FOREIGN KEY (pet_id) REFERENCES pet(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_pet_tag
        CHECK (tag IN (
            'STUBENREIN',
            'AENGSTLICH',
            'VERSPIELT',
            'VERTRAEGLICH_MIT_KINDERN',
            'VERTRAEGLICH_MIT_ANDEREN_TIEREN',
            'BRAUCHT_MEDIKAMENTE',
            'KANN_ALLEIN_BLEIBEN',
            'LEINENFUEHRIG',
            'FUTTERSENSIBEL',
            'KASTRIERT'
        ))
);
