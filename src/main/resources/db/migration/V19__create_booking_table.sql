CREATE TABLE booking (
    id BINARY(16) NOT NULL PRIMARY KEY,
    offer_id BINARY(16) NOT NULL UNIQUE,
    accepted_request_id BINARY(16) NOT NULL UNIQUE,
    owner_id BINARY(16) NOT NULL,
    sitter_id BINARY(16) NOT NULL,
    pet_id BINARY(16) NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price_per_week DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_booking_offer FOREIGN KEY (offer_id) REFERENCES offer(offer_id),
    CONSTRAINT fk_booking_request FOREIGN KEY (accepted_request_id) REFERENCES request(id),
    CONSTRAINT fk_booking_owner FOREIGN KEY (owner_id) REFERENCES `user`(id),
    CONSTRAINT fk_booking_sitter FOREIGN KEY (sitter_id) REFERENCES `user`(id),
    CONSTRAINT fk_booking_pet FOREIGN KEY (pet_id) REFERENCES pet(id)
);
