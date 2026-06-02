package com.softwareengineering.petsitter.pet.dto;

import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.domain.PetTag;
import com.softwareengineering.petsitter.pet.domain.PetVaccinationStatus;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record PetDto(
        UUID id,
        String name,
        PetSpecies species,
        String customSpecies,
        String breed,
        LocalDate birthDate,
        String notes,
        PetVaccinationStatus vaccinationStatus,
        Set<PetTag> tags) {

    public static PetDto from(Pet pet) {
        return new PetDto(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getCustomSpecies(),
                pet.getBreed(),
                pet.getBirthDate(),
                pet.getNotes(),
                pet.getVaccinationStatus() == null ? PetVaccinationStatus.UNBEKANNT : pet.getVaccinationStatus(),
                new LinkedHashSet<>(pet.getTags())
        );
    }
}
