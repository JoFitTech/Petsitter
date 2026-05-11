package com.softwareengineering.petsitter.pet.service;

import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public List<Pet> getPetsForOwner(UUID ownerId) {
        if (ownerId == null) {
            return Collections.emptyList();
        }
        return petRepository.findAllByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public String getPetSummaryForOwner(UUID ownerId) {
        List<Pet> pets = getPetsForOwner(ownerId);
        if (pets.isEmpty()) {
            return "Keine Haustiere";
        }

        Map<PetSpecies, Long> counts = pets.stream()
                .collect(Collectors.groupingBy(
                        Pet::getSpecies,
                        () -> new EnumMap<>(PetSpecies.class),
                        Collectors.counting()
                ));

        return List.of(PetSpecies.DOG, PetSpecies.CAT, PetSpecies.BIRD, PetSpecies.RABBIT, PetSpecies.OTHER).stream()
                .filter(counts::containsKey)
                .map(species -> formatSpeciesCount(species, counts.get(species)))
                .collect(Collectors.joining(", "));
    }

    public List<String> getPets() {
        return Collections.emptyList();
    }

    private String formatSpeciesCount(PetSpecies species, long count) {
        return count + " " + (count == 1 ? singular(species) : plural(species));
    }

    private String singular(PetSpecies species) {
        return switch (species) {
            case DOG -> "Hund";
            case CAT -> "Katze";
            case BIRD -> "Vogel";
            case RABBIT -> "Kaninchen";
            case OTHER -> "Haustier";
        };
    }

    private String plural(PetSpecies species) {
        return switch (species) {
            case DOG -> "Hunde";
            case CAT -> "Katzen";
            case BIRD -> "Vögel";
            case RABBIT -> "Kaninchen";
            case OTHER -> "Haustiere";
        };
    }
}
