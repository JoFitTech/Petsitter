package com.softwareengineering.petsitter.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.domain.PetTag;
import com.softwareengineering.petsitter.pet.domain.PetVaccinationStatus;
import com.softwareengineering.petsitter.pet.dto.PetDeletionAction;
import com.softwareengineering.petsitter.pet.dto.PetDeletionDecision;
import com.softwareengineering.petsitter.pet.dto.PetDeletionImpact;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PetServiceTest {

    @Test
    void getPetsForOwnerLoadsPetsFromRepository() {
        UUID ownerId = UUID.randomUUID();
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet(PetSpecies.DOG)));

        List<Pet> result = new PetService(petRepository.repository(), null).getPetsForOwner(ownerId);

        assertThat(result).hasSize(1);
        assertThat(petRepository.requestedOwnerId).hasValue(ownerId);
    }

    @Test
    void getPetSummaryForOwnerFormatsSpeciesCounts() {
        UUID ownerId = UUID.randomUUID();
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(
                pet(PetSpecies.DOG),
                pet(PetSpecies.DOG),
                pet(PetSpecies.CAT),
                pet(PetSpecies.RABBIT)
        ));

        String summary = new PetService(petRepository.repository(), null).getPetSummaryForOwner(ownerId);

        assertThat(summary).isEqualTo("2 Hunde, 1 Katze, 1 Kaninchen");
    }

    @Test
    void getPetSummaryForOwnerHandlesEmptyPets() {
        String summary = new PetService(new PetRepositoryFake(List.of()).repository(), null)
                .getPetSummaryForOwner(UUID.randomUUID());

        assertThat(summary).isEqualTo("Keine Haustiere");
    }

    @Test
    void createPetForCurrentUserDefaultsUnknownVaccinationStatusAndEmptyTags() {
        User owner = user(UUID.randomUUID());
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of());
        PetService petService = service(owner, petRepository.repository(), null);

        petService.createPetForCurrentUser(new PetDto(
                null,
                "Balu",
                PetSpecies.DOG,
                null,
                null,
                null,
                null,
                null,
                null));

        assertThat(petRepository.savedPets).hasSize(1);
        Pet savedPet = petRepository.savedPets.getFirst();
        assertThat(savedPet.getOwner()).isSameAs(owner);
        assertThat(savedPet.getName()).isEqualTo("Balu");
        assertThat(savedPet.getVaccinationStatus()).isEqualTo(PetVaccinationStatus.UNBEKANNT);
        assertThat(savedPet.getTags()).isEmpty();
    }

    @Test
    void updatePetStoresVaccinationStatusAndTags() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        PetService petService = service(owner, petRepository.repository(), null);

        petService.updatePet(pet.getId(), new PetDto(
                pet.getId(),
                "Balu",
                PetSpecies.DOG,
                null,
                "Golden Retriever",
                null,
                "Braucht viel Auslauf",
                PetVaccinationStatus.GEIMPFT,
                Set.of(PetTag.STUBENREIN, PetTag.VERSPIELT)));

        assertThat(petRepository.savedPets).containsExactly(pet);
        assertThat(pet.getVaccinationStatus()).isEqualTo(PetVaccinationStatus.GEIMPFT);
        assertThat(pet.getTags()).containsExactlyInAnyOrder(PetTag.STUBENREIN, PetTag.VERSPIELT);
    }

    @Test
    void analyzeCurrentUserPetDeletionDescribesAffectedOffers() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet secondPet = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Offer singlePetOffer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Nur Balu", pet);
        Offer multiPetOffer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Balu und Mila", pet, secondPet);
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(singlePetOffer, multiPetOffer));
        PetService petService = service(owner, new PetRepositoryFake(List.of(pet)).repository(),
                offerRepository.repository());

        PetDeletionImpact result = petService.analyzeCurrentUserPetDeletion(pet.getId());

        assertThat(result.pet().id()).isEqualTo(pet.getId());
        assertThat(result.offers()).hasSize(2);
        assertThat(result.offers().get(0).title()).isEqualTo("Nur Balu");
        assertThat(result.offers().get(0).requiresDecision()).isFalse();
        assertThat(result.offers().get(1).title()).isEqualTo("Balu und Mila");
        assertThat(result.offers().get(1).requiresDecision()).isTrue();
        assertThat(result.offers().get(1).availableActions())
                .containsExactly(PetDeletionAction.REMOVE_PET_FROM_OFFER, PetDeletionAction.DELETE_OFFER);
        assertThat(offerRepository.requestedUserId).hasValue(owner.getId());
        assertThat(offerRepository.requestedPetId).hasValue(pet.getId());
    }

    @Test
    void analyzeCurrentUserPetDeletionTreatsDraftOffersLikeOpenOffers() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet secondPet = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Offer draftOffer = offer(UUID.randomUUID(), owner, OfferStatus.DRAFT, "Entwurf", pet, secondPet);
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(draftOffer));
        PetService petService = service(owner, new PetRepositoryFake(List.of(pet)).repository(),
                offerRepository.repository());

        PetDeletionImpact result = petService.analyzeCurrentUserPetDeletion(pet.getId());

        assertThat(result.offers()).hasSize(1);
        assertThat(result.offers().getFirst().blocksDeletion()).isFalse();
        assertThat(result.offers().getFirst().requiresDecision()).isTrue();
        assertThat(result.offers().getFirst().availableActions())
                .containsExactly(PetDeletionAction.REMOVE_PET_FROM_OFFER, PetDeletionAction.DELETE_OFFER);
    }

    @Test
    void deleteCurrentUserPetDeletesPetWithoutOffers() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of());
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        petService.deleteCurrentUserPet(pet.getId(), List.of());

        assertThat(petRepository.deletedPets).containsExactly(pet);
        assertThat(offerRepository.deletedOffers).isEmpty();
        assertThat(offerRepository.savedOffers).isEmpty();
    }

    @Test
    void deleteCurrentUserPetDeletesSinglePetOfferAndPet() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Offer offer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Hundebetreuung", pet);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(offer));
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        petService.deleteCurrentUserPet(pet.getId(), List.of(
                new PetDeletionDecision(offer.getOfferId(), PetDeletionAction.DELETE_OFFER)));

        assertThat(offerRepository.deletedOffers).containsExactly(offer);
        assertThat(offerRepository.savedOffers).isEmpty();
        assertThat(petRepository.deletedPets).containsExactly(pet);
    }

    @Test
    void deleteCurrentUserPetCanRemovePetFromMultiPetOffer() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet secondPet = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Offer offer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Zwei Tiere", pet, secondPet);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(offer));
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        petService.deleteCurrentUserPet(pet.getId(), List.of(
                new PetDeletionDecision(offer.getOfferId(), PetDeletionAction.REMOVE_PET_FROM_OFFER)));

        assertThat(offerRepository.deletedOffers).isEmpty();
        assertThat(offerRepository.savedOffers).containsExactly(offer);
        assertThat(offer.getPets()).containsExactly(secondPet);
        assertThat(offer.getUpdateUser()).isSameAs(owner);
        assertThat(petRepository.deletedPets).containsExactly(pet);
    }

    @Test
    void deleteCurrentUserPetCanDeleteMultiPetOffer() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet secondPet = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Offer offer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Zwei Tiere", pet, secondPet);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(offer));
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        petService.deleteCurrentUserPet(pet.getId(), List.of(
                new PetDeletionDecision(offer.getOfferId(), PetDeletionAction.DELETE_OFFER)));

        assertThat(offerRepository.deletedOffers).containsExactly(offer);
        assertThat(offerRepository.savedOffers).isEmpty();
        assertThat(petRepository.deletedPets).containsExactly(pet);
    }

    @Test
    void deleteCurrentUserPetCanRemovePetFromMultiPetDraftOffer() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet secondPet = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Offer draftOffer = offer(UUID.randomUUID(), owner, OfferStatus.DRAFT, "Entwurf", pet, secondPet);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(draftOffer));
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        petService.deleteCurrentUserPet(pet.getId(), List.of(
                new PetDeletionDecision(draftOffer.getOfferId(), PetDeletionAction.REMOVE_PET_FROM_OFFER)));

        assertThat(offerRepository.deletedOffers).isEmpty();
        assertThat(offerRepository.savedOffers).containsExactly(draftOffer);
        assertThat(draftOffer.getPets()).containsExactly(secondPet);
        assertThat(draftOffer.getUpdateUser()).isSameAs(owner);
        assertThat(petRepository.deletedPets).containsExactly(pet);
    }

    @Test
    void deleteCurrentUserPetAppliesMixedOfferActionsAtomically() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet cat = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Pet rabbit = pet(UUID.randomUUID(), owner, PetSpecies.RABBIT);
        Offer singlePetOffer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Nur Hund", pet);
        Offer removeFromOffer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Hund und Katze", pet, cat);
        Offer deleteOffer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Hund und Kaninchen", pet, rabbit);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(singlePetOffer, removeFromOffer, deleteOffer));
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        petService.deleteCurrentUserPet(pet.getId(), List.of(
                new PetDeletionDecision(singlePetOffer.getOfferId(), PetDeletionAction.DELETE_OFFER),
                new PetDeletionDecision(removeFromOffer.getOfferId(), PetDeletionAction.REMOVE_PET_FROM_OFFER),
                new PetDeletionDecision(deleteOffer.getOfferId(), PetDeletionAction.DELETE_OFFER)));

        assertThat(offerRepository.deletedOffers).containsExactly(singlePetOffer, deleteOffer);
        assertThat(offerRepository.savedOffers).containsExactly(removeFromOffer);
        assertThat(removeFromOffer.getPets()).containsExactly(cat);
        assertThat(petRepository.deletedPets).containsExactly(pet);
    }

    @Test
    void deleteCurrentUserPetBlocksNonOpenOffers() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Offer bookedOffer = offer(UUID.randomUUID(), owner, OfferStatus.BOOKED, "Gebucht", pet);
        Offer cancelledOffer = offer(UUID.randomUUID(), owner, OfferStatus.CANCELLED, "Storniert", pet);
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet));
        OfferRepositoryFake offerRepository = new OfferRepositoryFake(List.of(bookedOffer, cancelledOffer));
        PetService petService = service(owner, petRepository.repository(), offerRepository.repository());

        assertThatThrownBy(() -> petService.deleteCurrentUserPet(pet.getId(), List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(offerRepository.deletedOffers).isEmpty();
        assertThat(offerRepository.savedOffers).isEmpty();
        assertThat(petRepository.deletedPets).isEmpty();
    }

    @Test
    void deleteCurrentUserPetRejectsMissingOrStaleDecision() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, PetSpecies.DOG);
        Pet secondPet = pet(UUID.randomUUID(), owner, PetSpecies.CAT);
        Offer offer = offer(UUID.randomUUID(), owner, OfferStatus.OPEN, "Zwei Tiere", pet, secondPet);

        assertThatThrownBy(() -> service(owner, new PetRepositoryFake(List.of(pet)).repository(),
                new OfferRepositoryFake(List.of(offer)).repository())
                .deleteCurrentUserPet(pet.getId(), List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThatThrownBy(() -> service(owner, new PetRepositoryFake(List.of(pet)).repository(),
                new OfferRepositoryFake(List.of(offer)).repository())
                .deleteCurrentUserPet(pet.getId(), List.of(
                        new PetDeletionDecision(UUID.randomUUID(), PetDeletionAction.DELETE_OFFER))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private PetService service(User user, PetRepository petRepository, OfferRepository offerRepository) {
        return new PetService(petRepository, authenticatedUser(Optional.of(user)), offerRepository);
    }

    private AuthenticatedUser authenticatedUser(Optional<User> user) {
        return new AuthenticatedUser(userRepositoryReturning(user)) {
            @Override
            public Optional<User> get() {
                return user;
            }
        };
    }

    private UserRepository userRepositoryReturning(Optional<User> user) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] {UserRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmail".equals(method.getName())) {
                        return user;
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(userId + "@petsitter.local");
        user.setPasswordHash("$2y$10$uZHE15gXghc9i7PVWGhDOOJUt3vZgKg3oiknQQwv9D4lHzsIiBqP2");
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        return user;
    }

    private Pet pet(PetSpecies species) {
        Pet pet = new Pet();
        pet.setSpecies(species);
        return pet;
    }

    private Pet pet(UUID petId, User owner, PetSpecies species) {
        Pet pet = pet(species);
        pet.setId(petId);
        pet.setOwner(owner);
        pet.setName(species.name());
        return pet;
    }

    private Offer offer(UUID offerId, User owner, OfferStatus status, String title, Pet... pets) {
        Offer offer = new Offer();
        offer.setOfferId(offerId);
        offer.setCreateUser(owner);
        offer.setUpdateUser(owner);
        offer.setOfferType(OfferType.OWNER_OFFER);
        offer.setStatus(status);
        offer.setTitle(title);
        offer.setStartDate(LocalDate.now());
        offer.setEndDate(LocalDate.now().plusDays(1));
        offer.setPrice(BigDecimal.TEN);
        offer.setPets(List.of(pets));
        return offer;
    }

    private static class PetRepositoryFake {
        private final List<Pet> pets;
        private final AtomicReference<UUID> requestedOwnerId = new AtomicReference<>();
        private final List<Pet> deletedPets = new ArrayList<>();
        private final List<Pet> savedPets = new ArrayList<>();

        PetRepositoryFake(List<Pet> pets) {
            this.pets = pets;
        }

        PetRepository repository() {
            return (PetRepository) Proxy.newProxyInstance(
                    PetRepository.class.getClassLoader(),
                    new Class<?>[] {PetRepository.class},
                    (proxy, method, args) -> {
                        if ("findAllByOwnerId".equals(method.getName())) {
                            requestedOwnerId.set((UUID) args[0]);
                            return pets;
                        }
                        if ("findById".equals(method.getName())) {
                            return pets.stream()
                                    .filter(pet -> pet.getId() != null && pet.getId().equals(args[0]))
                                    .findFirst();
                        }
                        if ("delete".equals(method.getName())) {
                            deletedPets.add((Pet) args[0]);
                            return null;
                        }
                        if ("save".equals(method.getName())) {
                            Pet pet = (Pet) args[0];
                            savedPets.add(pet);
                            return pet;
                        }
                        if ("toString".equals(method.getName())) {
                            return "PetRepositoryFake";
                        }
                        throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                    }
            );
        }
    }

    private static class OfferRepositoryFake {
        private final List<Offer> offers;
        private final AtomicReference<UUID> requestedUserId = new AtomicReference<>();
        private final AtomicReference<UUID> requestedPetId = new AtomicReference<>();
        private final List<Offer> deletedOffers = new ArrayList<>();
        private final List<Offer> savedOffers = new ArrayList<>();

        OfferRepositoryFake(List<Offer> offers) {
            this.offers = offers;
        }

        OfferRepository repository() {
            return (OfferRepository) Proxy.newProxyInstance(
                    OfferRepository.class.getClassLoader(),
                    new Class<?>[] {OfferRepository.class},
                    (proxy, method, args) -> {
                        if ("findAllByCreateUserIdAndPetId".equals(method.getName())) {
                            requestedUserId.set((UUID) args[0]);
                            requestedPetId.set((UUID) args[1]);
                            return offers;
                        }
                        if ("save".equals(method.getName())) {
                            Offer offer = (Offer) args[0];
                            savedOffers.add(offer);
                            return offer;
                        }
                        if ("delete".equals(method.getName())) {
                            deletedOffers.add((Offer) args[0]);
                            return null;
                        }
                        if ("toString".equals(method.getName())) {
                            return "OfferRepositoryFake";
                        }
                        throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                    }
            );
        }
    }
}
