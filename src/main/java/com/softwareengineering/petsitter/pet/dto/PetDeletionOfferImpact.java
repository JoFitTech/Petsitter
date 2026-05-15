package com.softwareengineering.petsitter.pet.dto;

import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import java.util.List;
import java.util.UUID;

public record PetDeletionOfferImpact(
        UUID offerId,
        String title,
        OfferStatus status,
        int petCount,
        List<PetDeletionAction> availableActions
) {
    public PetDeletionOfferImpact {
        availableActions = availableActions == null ? List.of() : List.copyOf(availableActions);
    }

    public boolean blocksDeletion() {
        return status != OfferStatus.OPEN;
    }

    public boolean requiresDecision() {
        return status == OfferStatus.OPEN && petCount > 1;
    }
}
