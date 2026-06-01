ALTER TABLE booking
    CHANGE COLUMN price_per_week price_per_day DECIMAL(12, 2) NOT NULL,
    ADD COLUMN total_price DECIMAL(12, 2) NULL;

UPDATE booking
SET total_price = ROUND(price_per_day * (DATEDIFF(end_date, start_date) + 1), 2);

ALTER TABLE booking
    MODIFY COLUMN total_price DECIMAL(12, 2) NOT NULL;

CREATE TABLE wallet_account (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    available_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_wallet_account_user
        FOREIGN KEY (user_id) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_wallet_account_balance
        CHECK (available_balance >= 0)
);

INSERT INTO wallet_account (id, user_id, available_balance)
SELECT UUID_TO_BIN(UUID()), u.id, 0.00
FROM `user` u;

CREATE TABLE booking_payment (
    id BINARY(16) NOT NULL PRIMARY KEY,
    booking_id BINARY(16) NOT NULL UNIQUE,
    owner_id BINARY(16) NOT NULL,
    sitter_id BINARY(16) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    held_at DATETIME(6) NULL,
    release_requested_at DATETIME(6) NULL,
    released_at DATETIME(6) NULL,
    refunded_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_booking_payment_booking
        FOREIGN KEY (booking_id) REFERENCES booking(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_payment_owner
        FOREIGN KEY (owner_id) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_booking_payment_sitter
        FOREIGN KEY (sitter_id) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_booking_payment_amount
        CHECK (amount >= 0)
);

CREATE INDEX idx_booking_payment_status_requested
    ON booking_payment (status, release_requested_at);

INSERT INTO booking_payment (
    id, booking_id, owner_id, sitter_id, amount, status, created_at, updated_at
)
SELECT
    UUID_TO_BIN(UUID()), b.id, b.owner_id, b.sitter_id, b.total_price,
    'LEGACY_UNFUNDED', b.created_at, b.created_at
FROM booking b;

CREATE TABLE wallet_transaction (
    id BINARY(16) NOT NULL PRIMARY KEY,
    wallet_account_id BINARY(16) NOT NULL,
    booking_payment_id BINARY(16) NULL,
    type VARCHAR(32) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    balance_after DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_wallet_transaction_account
        FOREIGN KEY (wallet_account_id) REFERENCES wallet_account(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_wallet_transaction_payment
        FOREIGN KEY (booking_payment_id) REFERENCES booking_payment(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE INDEX idx_wallet_transaction_account_created
    ON wallet_transaction (wallet_account_id, created_at);
