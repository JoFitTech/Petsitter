package com.softwareengineering.petsitter.offer.service;

import com.softwareengineering.petsitter.offer.domain.Offer;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class OfferService {

    public Offer createOwnerOffer(Long userId, Long petId) {
        throw new UnsupportedOperationException("createOwnerOffer noch nicht implementiert");
    }

    public Offer createSitterOffer(Long userId) {
        throw new UnsupportedOperationException("createSitterOffer noch nicht implementiert");
    }

    public Offer updateOffer(Long offerId, Long userId) {
        throw new UnsupportedOperationException("updateOffer noch nicht implementiert");
    }

    public void cancelOffer(Long offerId, Long userId) {
        throw new UnsupportedOperationException("cancelOffer noch nicht implementiert");
    }

    public List<Offer> findMatchingOffersForUser(Long userId) {
        return Collections.emptyList();
    }

    public List<String> getOffers() {
        return Collections.emptyList();
    }
}
