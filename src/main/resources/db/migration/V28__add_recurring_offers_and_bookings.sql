ALTER TABLE offer
    MODIFY COLUMN start_date DATE NULL,
    MODIFY COLUMN end_date DATE NULL,
    ADD COLUMN time_slot VARCHAR(32) NULL AFTER animal_type;

UPDATE offer
SET frequency = 'ONE_TIME'
WHERE frequency IS NULL;

ALTER TABLE offer
    ADD CONSTRAINT chk_offer_time_slot
        CHECK (time_slot IS NULL OR time_slot IN ('MORNING', 'AFTERNOON', 'FULL_DAY'));

CREATE TABLE offer_weekday (
    offer_id BINARY(16) NOT NULL,
    weekday VARCHAR(16) NOT NULL,
    PRIMARY KEY (offer_id, weekday),
    CONSTRAINT fk_offer_weekday_offer
        FOREIGN KEY (offer_id) REFERENCES offer(offer_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

ALTER TABLE booking
    MODIFY COLUMN start_date DATE NULL,
    MODIFY COLUMN end_date DATE NULL,
    ADD COLUMN frequency VARCHAR(32) NULL AFTER total_price,
    ADD COLUMN time_slot VARCHAR(32) NULL AFTER frequency,
    ADD COLUMN recurring_ended_on DATE NULL AFTER time_slot;

UPDATE booking
SET frequency = 'ONE_TIME'
WHERE frequency IS NULL;

ALTER TABLE booking
    MODIFY COLUMN frequency VARCHAR(32) NOT NULL;

CREATE TABLE booking_weekday (
    booking_id BINARY(16) NOT NULL,
    weekday VARCHAR(16) NOT NULL,
    PRIMARY KEY (booking_id, weekday),
    CONSTRAINT fk_booking_weekday_booking
        FOREIGN KEY (booking_id) REFERENCES booking(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE booking_pause (
    id BINARY(16) NOT NULL PRIMARY KEY,
    booking_id BINARY(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_by BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    KEY idx_booking_pause_booking_dates (booking_id, start_date, end_date),
    CONSTRAINT fk_booking_pause_booking
        FOREIGN KEY (booking_id) REFERENCES booking(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_pause_created_by
        FOREIGN KEY (created_by) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_booking_pause_dates
        CHECK (start_date <= end_date)
);

CREATE TABLE recurring_booking_payment (
    id BINARY(16) NOT NULL PRIMARY KEY,
    booking_id BINARY(16) NOT NULL,
    owner_id BINARY(16) NOT NULL,
    sitter_id BINARY(16) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    occurrence_count INT NOT NULL,
    price_per_occurrence DECIMAL(12, 2) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    held_at DATETIME(6) NULL,
    release_requested_at DATETIME(6) NULL,
    released_at DATETIME(6) NULL,
    refunded_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_recurring_booking_payment_period (booking_id, period_start),
    KEY idx_recurring_payment_status_requested (status, release_requested_at),
    CONSTRAINT fk_recurring_payment_booking
        FOREIGN KEY (booking_id) REFERENCES booking(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_recurring_payment_owner
        FOREIGN KEY (owner_id) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_recurring_payment_sitter
        FOREIGN KEY (sitter_id) REFERENCES `user`(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_recurring_payment_period
        CHECK (period_start <= period_end),
    CONSTRAINT chk_recurring_payment_count
        CHECK (occurrence_count >= 0),
    CONSTRAINT chk_recurring_payment_amount
        CHECK (amount >= 0),
    CONSTRAINT chk_recurring_payment_status
        CHECK (status IN ('AWAITING_FUNDS', 'HELD', 'RELEASE_REQUESTED', 'RELEASED', 'REFUNDED', 'SKIPPED'))
);

ALTER TABLE wallet_transaction
    ADD COLUMN recurring_booking_payment_id BINARY(16) NULL AFTER booking_payment_id,
    ADD CONSTRAINT fk_wallet_transaction_recurring_payment
        FOREIGN KEY (recurring_booking_payment_id) REFERENCES recurring_booking_payment(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL;
