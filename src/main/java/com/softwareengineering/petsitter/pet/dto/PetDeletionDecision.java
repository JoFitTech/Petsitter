package com.softwareengineering.petsitter.pet.dto;

import java.util.UUID;

public record PetDeletionDecision(
        UUID offerId,
        PetDeletionAction action
) {
}
