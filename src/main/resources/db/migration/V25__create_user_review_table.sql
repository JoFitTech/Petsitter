CREATE TABLE user_review (
    id BINARY(16) NOT NULL PRIMARY KEY,
    booking_id BINARY(16) NOT NULL,
    reviewer_id BINARY(16) NOT NULL,
    reviewee_id BINARY(16) NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT uk_user_review_booking_reviewer UNIQUE (booking_id, reviewer_id),
    CONSTRAINT chk_user_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_user_review_not_self CHECK (reviewer_id <> reviewee_id),

    CONSTRAINT fk_user_review_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_user_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES `user`(id),
    CONSTRAINT fk_user_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES `user`(id),

    KEY idx_user_review_reviewee (reviewee_id),
    KEY idx_user_review_booking (booking_id)
);


