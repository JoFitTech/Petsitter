-- Drop FKs first (they back the unique indexes)
ALTER TABLE booking DROP FOREIGN KEY fk_booking_offer;
ALTER TABLE booking DROP FOREIGN KEY fk_booking_request;

-- Drop unique indexes
ALTER TABLE booking DROP INDEX offer_id;
ALTER TABLE booking DROP INDEX accepted_request_id;

-- Recreate as non-unique indexes + FKs
ALTER TABLE booking ADD INDEX idx_booking_offer (offer_id);
ALTER TABLE booking ADD CONSTRAINT fk_booking_offer FOREIGN KEY (offer_id) REFERENCES offer(offer_id);

ALTER TABLE booking ADD INDEX idx_booking_request (accepted_request_id);
ALTER TABLE booking ADD CONSTRAINT fk_booking_request FOREIGN KEY (accepted_request_id) REFERENCES request(id);
