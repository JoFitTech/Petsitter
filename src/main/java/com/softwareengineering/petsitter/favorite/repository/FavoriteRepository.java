package com.softwareengineering.petsitter.favorite.repository;

import com.softwareengineering.petsitter.favorite.domain.Favorite;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByUserIdAndOfferOfferId(UUID userId, UUID offerId);

    List<Favorite> findAllByUserIdAndOfferOfferIdIn(UUID userId, Collection<UUID> offerIds);

    List<Favorite> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteByUserIdAndOfferOfferId(UUID userId, UUID offerId);
}
