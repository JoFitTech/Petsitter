package com.softwareengineering.petsitter.image.repository;

import com.softwareengineering.petsitter.image.domain.ImageAssetVariant;
import com.softwareengineering.petsitter.image.domain.ImageVariant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetVariantRepository extends JpaRepository<ImageAssetVariant, ImageAssetVariant.Key> {
    Optional<ImageAssetVariant> findByAssetIdAndVariant(UUID assetId, ImageVariant variant);
}
