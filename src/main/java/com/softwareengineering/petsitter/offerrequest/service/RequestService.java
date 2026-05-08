package com.softwareengineering.petsitter.offerrequest.service;

import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    public OfferRequest createRequest(Long offerId, Long requesterId, String message) {
        throw new UnsupportedOperationException("createRequest noch nicht implementiert");
    }

    public void cancelRequest(Long requestId, Long requesterId) {
        throw new UnsupportedOperationException("cancelRequest noch nicht implementiert");
    }

    public List<OfferRequest> findRequestsForOffer(Long offerId, Long creatorId) {
        return Collections.emptyList();
    }

    public List<OfferRequest> findMyRequests(Long userId) {
        return Collections.emptyList();
    }
}

