package com.softwareengineering.petsitter.offerrequest.repository;

import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRequestRepository extends JpaRepository<OfferRequest, Long> {
    Optional<OfferRequest> findByOfferIdAndRequesterId(Long offerId, Long requesterId);

    List<OfferRequest> findAllByOfferIdAndStatus(Long offerId, RequestStatus status);

    List<OfferRequest> findAllByRequesterId(Long requesterId);
}

