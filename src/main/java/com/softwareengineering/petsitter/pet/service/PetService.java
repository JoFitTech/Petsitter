package com.softwareengineering.petsitter.pet.service;

import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
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
    private final AuthenticatedUser authenticatedUser;

    public PetService(PetRepository petRepository, AuthenticatedUser authenticatedUser) {
        this.petRepository = petRepository;
        this.authenticatedUser = authenticatedUser;
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

    @Transactional(readOnly = true)
    public List<PetDto> getPetDtosForCurrentUser() {
        return authenticatedUser.get()
                .map(user -> petRepository.findAllByOwnerId(user.getId()).stream()
                        .map(this::toDto).toList())
                .orElseGet(List::of);
    }

    @Transactional
    public void createPetForCurrentUser(PetDto dto) {
        User user = authenticatedUser.get()
                .orElseThrow(() -> new ForbiddenOperationException("Nicht angemeldet."));
        Pet pet = new Pet();
        pet.setOwner(user);
        applyDtoToPet(pet, dto);
        petRepository.save(pet);
    }

    @Transactional
    public void updatePet(UUID petId, PetDto dto) {
        User user = authenticatedUser.get()
                .orElseThrow(() -> new ForbiddenOperationException("Nicht angemeldet."));
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Haustier nicht gefunden."));
        if (!pet.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Dieses Haustier gehört dir nicht.");
        }
        applyDtoToPet(pet, dto);
        petRepository.save(pet);
    }

    @Transactional
    public void deletePet(UUID petId) {
        User user = authenticatedUser.get()
                .orElseThrow(() -> new ForbiddenOperationException("Nicht angemeldet."));
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Haustier nicht gefunden."));
        if (!pet.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Dieses Haustier gehört dir nicht.");
        }
        petRepository.delete(pet);
    }

    public static String speciesLabel(PetSpecies species) {
        return switch (species) {
            case DOG    -> "Hund";
            case CAT    -> "Katze";
            case BIRD   -> "Vogel";
            case RABBIT -> "Kaninchen";
            case OTHER  -> "Sonstiges";
        };
    }

    private void applyDtoToPet(Pet pet, PetDto dto) {
        pet.setName(dto.name() == null ? "" : dto.name().trim());
        pet.setSpecies(dto.species());
        pet.setBreed(dto.breed() == null || dto.breed().isBlank() ? null : dto.breed().trim());
        pet.setAge(dto.age());
        pet.setNotes(dto.notes() == null || dto.notes().isBlank() ? null : dto.notes().trim());
    }

    private PetDto toDto(Pet pet) {
        return new PetDto(pet.getId(), pet.getName(), pet.getSpecies(),
                pet.getBreed(), pet.getAge(), pet.getNotes());
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
