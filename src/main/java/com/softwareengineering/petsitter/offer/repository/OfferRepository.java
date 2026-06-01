package com.softwareengineering.petsitter.offer.repository;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findAllByOfferTypeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            OfferType type,
            OfferStatus status,
            LocalDate endDate,
            LocalDate startDate
    );

    List<Offer> findAllByOfferTypeAndStatus(OfferType offerType, OfferStatus status);

    List<Offer> findAllByCreateUserIdOrderByCreateDateDesc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offer o where o.offerId = :offerId")
    Optional<Offer> findByOfferIdForUpdate(@Param("offerId") UUID offerId);

    @Query("""
            select distinct o
            from Offer o
            left join o.pets p
            where o.createUser.id = :userId
              and (p.id = :petId or o.pet.id = :petId)
            """)
    List<Offer> findAllByCreateUserIdAndPetId(@Param("userId") UUID userId, @Param("petId") UUID petId);
}
