package com.softwareengineering.petsitter.pet.service;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.domain.PetTag;
import com.softwareengineering.petsitter.pet.domain.PetVaccinationStatus;
import com.softwareengineering.petsitter.pet.dto.PetDeletionAction;
import com.softwareengineering.petsitter.pet.dto.PetDeletionDecision;
import com.softwareengineering.petsitter.pet.dto.PetDeletionImpact;
import com.softwareengineering.petsitter.pet.dto.PetDeletionOfferImpact;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final AuthenticatedUser authenticatedUser;
    private final OfferRepository offerRepository;

    @Autowired
    public PetService(PetRepository petRepository, AuthenticatedUser authenticatedUser, OfferRepository offerRepository) {
        this.petRepository = petRepository;
        this.authenticatedUser = authenticatedUser;
        this.offerRepository = offerRepository;
    }

    public PetService(PetRepository petRepository, AuthenticatedUser authenticatedUser) {
        this(petRepository, authenticatedUser, null);
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
        deleteCurrentUserPet(petId, List.of());
    }

    @Transactional(readOnly = true)
    public PetDeletionImpact analyzeCurrentUserPetDeletion(UUID petId) {
        User user = currentUserOrThrow();
        Pet pet = loadCurrentUserPet(petId, user);
        return new PetDeletionImpact(toDto(pet), findCurrentUserOffersForPet(user, pet).stream()
                .map(this::toDeletionOfferImpact)
                .toList());
    }

    @Transactional
    public void deleteCurrentUserPet(UUID petId, List<PetDeletionDecision> decisions) {
        User user = currentUserOrThrow();
        Pet pet = loadCurrentUserPet(petId, user);
        List<Offer> offers = findCurrentUserOffersForPet(user, pet);
        if (offers.stream().anyMatch(offer -> offer.getStatus() != OfferStatus.OPEN)) {
            throw new BusinessRuleViolationException(
                    "Das Tier ist noch in gebuchten oder stornierten Angeboten hinterlegt.");
        }

        Map<UUID, PetDeletionAction> decisionsByOfferId = normalizeDecisions(decisions);
        Set<UUID> expectedDecisionOfferIds = offers.stream()
                .map(Offer::getOfferId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        validateDecisions(decisionsByOfferId, offers, expectedDecisionOfferIds);

        for (Offer offer : offers) {
            PetDeletionAction action = decisionsByOfferId.get(offer.getOfferId());
            if (action == PetDeletionAction.DELETE_OFFER) {
                offerRepository.delete(offer);
            } else if (action == PetDeletionAction.REMOVE_PET_FROM_OFFER) {
                removePetFromOffer(offer, pet, user);
            } else {
                throw new BusinessRuleViolationException("Ungueltige Auswahl fuer das Angebot.");
            }
        }
        petRepository.delete(pet);
    }

    public static String speciesLabel(PetSpecies species) {
        return switch (species) {
            case DOG     -> "Hund";
            case CAT     -> "Katze";
            case BIRD    -> "Vogel";
            case RABBIT  -> "Kaninchen";
            case FISH    -> "Fisch";
            case REPTILE -> "Reptil";
            case OTHER   -> "Sonstiges";
        };
    }

    private void applyDtoToPet(Pet pet, PetDto dto) {
        pet.setName(dto.name() == null ? "" : dto.name().trim());
        pet.setSpecies(dto.species());
        pet.setCustomSpecies(dto.species() == PetSpecies.OTHER && dto.customSpecies() != null && !dto.customSpecies().isBlank()
                ? dto.customSpecies().trim() : null);
        pet.setBreed(dto.breed() == null || dto.breed().isBlank() ? null : dto.breed().trim());
        pet.setBirthDate(dto.birthDate());
        pet.setNotes(dto.notes() == null || dto.notes().isBlank() ? null : dto.notes().trim());
        pet.setVaccinationStatus(dto.vaccinationStatus() == null
                ? PetVaccinationStatus.UNBEKANNT
                : dto.vaccinationStatus());
        pet.setTags(normalizeTags(dto.tags()));
    }

    private PetDto toDto(Pet pet) {
        return new PetDto(pet.getId(), pet.getName(), pet.getSpecies(),
                pet.getCustomSpecies(), pet.getBreed(), pet.getBirthDate(), pet.getNotes(),
                pet.getVaccinationStatus() == null ? PetVaccinationStatus.UNBEKANNT : pet.getVaccinationStatus(),
                new LinkedHashSet<>(pet.getTags()));
    }

    private Set<PetTag> normalizeTags(Set<PetTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        return tags.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private User currentUserOrThrow() {
        return authenticatedUser.get()
                .orElseThrow(() -> new ForbiddenOperationException("Nicht angemeldet."));
    }

    private Pet loadCurrentUserPet(UUID petId, User user) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Haustier nicht gefunden."));
        if (!pet.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Dieses Haustier gehört dir nicht.");
        }
        return pet;
    }

    private List<Offer> findCurrentUserOffersForPet(User user, Pet pet) {
        if (offerRepository == null) {
            return List.of();
        }
        return offerRepository.findAllByCreateUserIdAndPetId(user.getId(), pet.getId());
    }

    private PetDeletionOfferImpact toDeletionOfferImpact(Offer offer) {
        int petCount = petCount(offer);
        List<PetDeletionAction> actions = offer.getStatus() == OfferStatus.OPEN && petCount > 1
                ? List.of(PetDeletionAction.REMOVE_PET_FROM_OFFER, PetDeletionAction.DELETE_OFFER)
                : List.of();
        return new PetDeletionOfferImpact(
                offer.getOfferId(),
                titleOrFallback(offer),
                offer.getStatus(),
                petCount,
                actions);
    }

    private Map<UUID, PetDeletionAction> normalizeDecisions(List<PetDeletionDecision> decisions) {
        if (decisions == null || decisions.isEmpty()) {
            return Map.of();
        }
        Map<UUID, PetDeletionAction> normalized = new LinkedHashMap<>();
        for (PetDeletionDecision decision : decisions) {
            if (decision == null || decision.offerId() == null || decision.action() == null) {
                throw new BusinessRuleViolationException("Bitte fuer jedes betroffene Angebot eine Auswahl treffen.");
            }
            if (normalized.put(decision.offerId(), decision.action()) != null) {
                throw new BusinessRuleViolationException("Ein Angebot wurde mehrfach ausgewaehlt.");
            }
        }
        return normalized;
    }

    private void validateDecisions(Map<UUID, PetDeletionAction> decisionsByOfferId, List<Offer> offers,
            Set<UUID> expectedDecisionOfferIds) {
        if (!decisionsByOfferId.keySet().equals(expectedDecisionOfferIds)) {
            throw new BusinessRuleViolationException(
                    "Die Angebot-Auswahl ist nicht mehr aktuell. Bitte den Dialog erneut öffnen.");
        }
        for (Offer offer : offers) {
            PetDeletionAction action = decisionsByOfferId.get(offer.getOfferId());
            if (petCount(offer) <= 1 && action != PetDeletionAction.DELETE_OFFER) {
                throw new BusinessRuleViolationException("Ungueltige Auswahl fuer ein Angebot.");
            }
            if (petCount(offer) > 1
                    && action != PetDeletionAction.REMOVE_PET_FROM_OFFER
                    && action != PetDeletionAction.DELETE_OFFER) {
                throw new BusinessRuleViolationException("Ungueltige Auswahl fuer ein Angebot.");
            }
        }
    }

    private void removePetFromOffer(Offer offer, Pet pet, User user) {
        Set<Pet> remainingPets = offer.getPets().stream()
                .filter(candidate -> !pet.getId().equals(candidate.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (remainingPets.isEmpty()) {
            throw new BusinessRuleViolationException("Ein Angebot muss mindestens ein Tier enthalten.");
        }
        offer.setPets(remainingPets);
        offer.setUpdateUser(user);
        offerRepository.save(offer);
    }

    private int petCount(Offer offer) {
        return offer.getPets().size();
    }

    private String titleOrFallback(Offer offer) {
        if (offer.getTitle() != null && !offer.getTitle().isBlank()) {
            return offer.getTitle();
        }
        return "Angebot";
    }

    private String formatSpeciesCount(PetSpecies species, long count) {
        return count + " " + (count == 1 ? singular(species) : plural(species));
    }

    private String singular(PetSpecies species) {
        return switch (species) {
            case DOG     -> "Hund";
            case CAT     -> "Katze";
            case BIRD    -> "Vogel";
            case RABBIT  -> "Kaninchen";
            case FISH    -> "Fisch";
            case REPTILE -> "Reptil";
            case OTHER   -> "Haustier";
        };
    }

    private String plural(PetSpecies species) {
        return switch (species) {
            case DOG     -> "Hunde";
            case CAT     -> "Katzen";
            case BIRD    -> "Vögel";
            case RABBIT  -> "Kaninchen";
            case FISH    -> "Fische";
            case REPTILE -> "Reptilien";
            case OTHER   -> "Haustiere";
        };
    }
}
