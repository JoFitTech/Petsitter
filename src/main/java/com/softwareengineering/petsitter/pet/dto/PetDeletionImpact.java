package com.softwareengineering.petsitter.pet.dto;

import java.util.List;

public record PetDeletionImpact(
        PetDto pet,
        List<PetDeletionOfferImpact> offers
) {
    public PetDeletionImpact {
        offers = offers == null ? List.of() : List.copyOf(offers);
    }

    public boolean hasAffectedOffers() {
        return !offers.isEmpty();
    }

    public boolean hasBlockingOffers() {
        return offers.stream().anyMatch(PetDeletionOfferImpact::blocksDeletion);
    }
}
