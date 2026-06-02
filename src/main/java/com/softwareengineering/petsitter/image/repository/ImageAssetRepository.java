package com.softwareengineering.petsitter.image.repository;

import com.softwareengineering.petsitter.image.domain.ImageAsset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, UUID> {
    Optional<ImageAsset> findByUserId(UUID userId);

    Optional<ImageAsset> findByPetId(UUID petId);

    List<ImageAsset> findAllByUserIdIn(Collection<UUID> userIds);

    List<ImageAsset> findAllByPetIdIn(Collection<UUID> petIds);
}
