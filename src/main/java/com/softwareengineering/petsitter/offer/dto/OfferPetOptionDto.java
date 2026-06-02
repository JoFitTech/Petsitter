package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import java.util.UUID;

public record OfferPetOptionDto(UUID id, String name, PetSpecies species, ImageRefDto profileImage) {

    public OfferPetOptionDto(UUID id, String name, PetSpecies species) {
        this(id, name, species, null);
    }

    public String label() {
        if (name == null || species == null) {
            return "";
        }
        return name + " (" + species + ")";
    }
}
