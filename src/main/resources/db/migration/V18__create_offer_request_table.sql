CREATE TABLE request (
    id BINARY(16) NOT NULL PRIMARY KEY,
    offer_id BINARY(16) NOT NULL,
    requester_id BINARY(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE KEY uk_offer_requester (offer_id, requester_id),
    CONSTRAINT fk_request_offer FOREIGN KEY (offer_id) REFERENCES offer(offer_id) ON DELETE CASCADE,
    CONSTRAINT fk_request_requester FOREIGN KEY (requester_id) REFERENCES `user`(id) ON DELETE CASCADE
);
