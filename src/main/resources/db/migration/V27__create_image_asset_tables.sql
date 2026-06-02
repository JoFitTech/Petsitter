CREATE TABLE image_asset (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NULL,
    pet_id BINARY(16) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_image_asset_exactly_one_owner
        CHECK ((user_id IS NOT NULL AND pet_id IS NULL)
            OR (user_id IS NULL AND pet_id IS NOT NULL)),
    CONSTRAINT uk_image_asset_user UNIQUE (user_id),
    CONSTRAINT uk_image_asset_pet UNIQUE (pet_id),
    CONSTRAINT fk_image_asset_user FOREIGN KEY (user_id) REFERENCES `user`(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_image_asset_pet FOREIGN KEY (pet_id) REFERENCES pet(id)
        ON DELETE CASCADE
);

CREATE TABLE image_asset_variant (
    asset_id BINARY(16) NOT NULL,
    variant VARCHAR(32) NOT NULL,
    mime_type VARCHAR(64) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    file_size BIGINT NOT NULL,
    content MEDIUMBLOB NOT NULL,
    PRIMARY KEY (asset_id, variant),
    CONSTRAINT fk_image_asset_variant_asset FOREIGN KEY (asset_id) REFERENCES image_asset(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_image_asset_variant
        CHECK (variant IN ('AVATAR', 'DISPLAY')),
    CONSTRAINT chk_image_asset_variant_dimensions
        CHECK (width > 0 AND height > 0 AND file_size > 0)
);
