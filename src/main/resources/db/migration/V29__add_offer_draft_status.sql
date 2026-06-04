ALTER TABLE offer
    DROP CONSTRAINT chk_offer_status;

ALTER TABLE offer
    ADD CONSTRAINT chk_offer_status
        CHECK (status IN ('DRAFT', 'OPEN', 'BOOKED', 'CANCELLED'));
