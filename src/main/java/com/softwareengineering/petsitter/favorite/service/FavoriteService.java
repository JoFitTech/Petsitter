package com.softwareengineering.petsitter.favorite.service;

import com.softwareengineering.petsitter.favorite.domain.Favorite;
import com.softwareengineering.petsitter.favorite.repository.FavoriteRepository;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final OfferRepository offerRepository;
    private final AuthenticatedUser authenticatedUser;
    private final Clock clock;

    @Autowired
    public FavoriteService(
            FavoriteRepository favoriteRepository,
            OfferRepository offerRepository,
            AuthenticatedUser authenticatedUser) {
        this(favoriteRepository, offerRepository, authenticatedUser, Clock.systemDefaultZone());
    }

    FavoriteService(
            FavoriteRepository favoriteRepository,
            OfferRepository offerRepository,
            AuthenticatedUser authenticatedUser,
            Clock clock) {
        this.favoriteRepository = favoriteRepository;
        this.offerRepository = offerRepository;
        this.authenticatedUser = authenticatedUser;
        this.clock = clock;
    }

    @Transactional
    public boolean toggleCurrentUserFavorite(UUID offerId) {
        User currentUser = currentUserOrThrow();
        return favoriteRepository.findByUserIdAndOfferOfferId(currentUser.getId(), offerId)
                .map(favorite -> {
                    favoriteRepository.delete(favorite);
                    return false;
                })
                .orElseGet(() -> addFavorite(currentUser, offerId));
    }

    @Transactional
    public void removeCurrentUserFavorite(UUID offerId) {
        User currentUser = currentUserOrThrow();
        favoriteRepository.deleteByUserIdAndOfferOfferId(currentUser.getId(), offerId);
    }

    @Transactional(readOnly = true)
    public List<OfferCardDto> getCurrentUserFavoriteOffers() {
        return authenticatedUser.get()
                .map(user -> favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                        .stream()
                        .map(Favorite::getOffer)
                        .filter(offer -> isAvailableForFavorite(offer, user))
                        .map(this::toCardDto)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public Set<UUID> favoriteOfferIdsForCurrentUser(Collection<UUID> offerIds) {
        Set<UUID> normalizedOfferIds = normalizeOfferIds(offerIds);
        if (normalizedOfferIds.isEmpty()) {
            return Set.of();
        }
        var currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) {
            return Set.of();
        }
        return favoriteRepository.findAllByUserIdAndOfferOfferIdIn(currentUser.get().getId(), normalizedOfferIds)
                .stream()
                .map(favorite -> favorite.getOffer().getOfferId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean addFavorite(User currentUser, UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer nicht gefunden."));
        if (!isAvailableForFavorite(offer, currentUser)) {
            throw new BusinessRuleViolationException("Dieses Offer kann nicht favorisiert werden.");
        }
        favoriteRepository.save(new Favorite(currentUser, offer));
        return true;
    }

    private User currentUserOrThrow() {
        return authenticatedUser.get()
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Bitte melde dich an, um Favoriten zu speichern."));
    }

    private boolean isAvailableForFavorite(Offer offer, User currentUser) {
        return offer != null
                && offer.getStatus() == OfferStatus.OPEN
                && !isExpiredOpenOffer(offer)
                && !isCreatedBy(offer, currentUser);
    }

    private boolean isExpiredOpenOffer(Offer offer) {
        return offer.getStartDate() != null
                && offer.getStartDate().isBefore(LocalDate.now(clock));
    }

    private boolean isCreatedBy(Offer offer, User user) {
        return offer.getCreateUser() != null
                && user != null
                && user.getId() != null
                && user.getId().equals(offer.getCreateUser().getId());
    }

    private Set<UUID> normalizeOfferIds(Collection<UUID> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return Set.of();
        }
        return offerIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private OfferCardDto toCardDto(Offer offer) {
        boolean verified = offer.getCreateUser() != null
                && offer.getCreateUser().getAccountStatus() == AccountStatus.VERIFIED;
        List<Pet> pets = offerPets(offer);
        User createUser = offer.getCreateUser();
        return new OfferCardDto(
                offer.getOfferId(),
                titleOrFallback(offer),
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getPrice(),
                offer.getAnimalType(),
                verified,
                offer.getDescription(),
                offer.getFrequency(),
                offer.getCareType(),
                petNames(pets),
                petSpeciesLabels(pets),
                petBreeds(pets),
                createUser != null ? createUser.getPostalCode() : null,
                createUser != null ? createUser.getCity() : null,
                null,
                true,
                offer.getOfferType(),
                createUser != null ? createUser.getId() : null,
                creatorDisplayName(createUser)
        );
    }

    private String creatorDisplayName(User user) {
        if (user == null) {
            return null;
        }
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        return user.getFirstName();
    }

    private String titleOrFallback(Offer offer) {
        if (offer.getTitle() != null && !offer.getTitle().isBlank()) {
            return offer.getTitle();
        }
        return offer.getOfferType() == OfferType.OWNER_OFFER ? "Auftrag" : "Angebot";
    }

    private List<Pet> offerPets(Offer offer) {
        if (offer == null) {
            return List.of();
        }
        return offer.getPets().stream().toList();
    }

    private String petNames(List<Pet> pets) {
        return petSummary(pets, Pet::getName);
    }

    private String petSpeciesLabels(List<Pet> pets) {
        return petSummary(pets, this::petSpeciesLabel);
    }

    private String petBreeds(List<Pet> pets) {
        return petSummary(pets, Pet::getBreed);
    }

    private String petSpeciesLabel(Pet pet) {
        if (pet.getSpecies() == null) {
            return null;
        }
        if (pet.getSpecies() == PetSpecies.OTHER) {
            return pet.getCustomSpecies() != null ? pet.getCustomSpecies() : "Sonstiges";
        }
        return switch (pet.getSpecies()) {
            case DOG -> "Hund";
            case CAT -> "Katze";
            case BIRD -> "Vogel";
            case RABBIT -> "Kaninchen";
            default -> pet.getSpecies().name();
        };
    }

    private String petSummary(List<Pet> pets, java.util.function.Function<Pet, String> mapper) {
        String value = pets.stream()
                .map(mapper)
                .filter(text -> text != null && !text.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
        return value.isBlank() ? null : value;
    }
}
