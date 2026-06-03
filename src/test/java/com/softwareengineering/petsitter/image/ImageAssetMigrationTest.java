package com.softwareengineering.petsitter.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ImageAssetMigrationTest {

    @Test
    void migrationDefinesOwnershipUniquenessAndCascadeRules() throws Exception {
        try (var input = getClass().getResourceAsStream("/db/migration/V27__create_image_asset_tables.sql")) {
            String migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(migration).contains("CREATE TABLE image_asset");
            assertThat(migration).contains("CHECK ((user_id IS NOT NULL AND pet_id IS NULL)");
            assertThat(migration).contains("CONSTRAINT uk_image_asset_user UNIQUE (user_id)");
            assertThat(migration).contains("CONSTRAINT uk_image_asset_pet UNIQUE (pet_id)");
            assertThat(migration).contains("ON DELETE CASCADE");
            assertThat(migration).contains("CREATE TABLE image_asset_variant");
            assertThat(migration).contains("MEDIUMBLOB");
        }
    }
}
