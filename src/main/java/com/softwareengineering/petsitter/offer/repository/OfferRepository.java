package com.softwareengineering.petsitter.offer.repository;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findAllByOfferTypeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            OfferType type,
            OfferStatus status,
            LocalDate endDate,
            LocalDate startDate
    );

    List<Offer> findAllByOfferTypeAndStatus(OfferType offerType, OfferStatus status);

    List<Offer> findAllByCreateUserIdOrderByCreateDateDesc(UUID userId);
}
