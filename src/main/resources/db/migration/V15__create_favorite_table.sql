CREATE TABLE IF NOT EXISTS favorite (
    favorite_id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    offer_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_favorite_user_offer (user_id, offer_id),
    KEY idx_favorite_user_id (user_id),
    KEY idx_favorite_offer_id (offer_id),
    CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_favorite_offer
        FOREIGN KEY (offer_id) REFERENCES offer(offer_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
