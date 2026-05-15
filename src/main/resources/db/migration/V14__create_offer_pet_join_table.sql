CREATE TABLE IF NOT EXISTS offer_pet (
    offer_id BINARY(16) NOT NULL,
    pet_id BINARY(16) NOT NULL,
    PRIMARY KEY (offer_id, pet_id),
    KEY idx_offer_pet_pet_id (pet_id),
    CONSTRAINT fk_offer_pet_join_offer
        FOREIGN KEY (offer_id) REFERENCES offer(offer_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_offer_pet_join_pet
        FOREIGN KEY (pet_id) REFERENCES pet(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

INSERT IGNORE INTO offer_pet (offer_id, pet_id)
SELECT offer_id, pet_id
FROM offer
WHERE pet_id IS NOT NULL;
