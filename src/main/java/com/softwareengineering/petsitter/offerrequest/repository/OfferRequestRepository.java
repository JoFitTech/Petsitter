package com.softwareengineering.petsitter.offerrequest.repository;

import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRequestRepository extends JpaRepository<OfferRequest, UUID> {
    Optional<OfferRequest> findByOfferOfferIdAndRequesterId(UUID offerId, UUID requesterId);

    List<OfferRequest> findAllByOfferOfferId(UUID offerId);

    List<OfferRequest> findAllByOfferOfferIdAndStatus(UUID offerId, RequestStatus status);

    List<OfferRequest> findAllByRequesterId(UUID requesterId);
}
