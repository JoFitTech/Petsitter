package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import java.util.UUID;

public record OfferPetOptionDto(UUID id, String name, PetSpecies species) {

    public String label() {
        if (name == null || species == null) {
            return "";
        }
        return name + " (" + species + ")";
    }
}
