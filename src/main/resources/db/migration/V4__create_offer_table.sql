CREATE TABLE IF NOT EXISTS offer (
    offer_id BINARY(16) NOT NULL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    create_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    update_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    create_user BINARY(16) NOT NULL,
    update_user BINARY(16) NOT NULL,
    pet_id BINARY(16),
    offer_type VARCHAR(32) NOT NULL,
    price DECIMAL(12, 2),
    description VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    KEY idx_offer_create_user (create_user),
    KEY idx_offer_update_user (update_user),
    KEY idx_offer_pet_id (pet_id),
    KEY idx_offer_status (status),
    CONSTRAINT fk_offer_create_user
        FOREIGN KEY (create_user) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_offer_update_user
        FOREIGN KEY (update_user) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_offer_pet
        FOREIGN KEY (pet_id) REFERENCES pets(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_offer_type
        CHECK (offer_type IN ('OWNER_OFFER', 'SITTER_OFFER')),
    CONSTRAINT chk_offer_status
        CHECK (status IN ('OPEN', 'BOOKED', 'CANCELLED')),
    CONSTRAINT chk_offer_dates
        CHECK (start_date <= end_date),
    CONSTRAINT chk_offer_price
        CHECK (price IS NULL OR price >= 0)
);
