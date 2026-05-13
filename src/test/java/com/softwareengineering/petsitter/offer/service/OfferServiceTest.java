package com.softwareengineering.petsitter.offer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.CreateOfferFormData;
import com.softwareengineering.petsitter.offer.dto.CreateOfferRequest;
import com.softwareengineering.petsitter.offer.dto.CreateOfferResult;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferPetOptionDto;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OfferServiceTest {

    @Test
    void findCurrentUserPetOptionsLoadsCurrentUsersPetsFromRepository() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, "Balu", PetSpecies.DOG);
        AtomicReference<UUID> requestedOwnerId = new AtomicReference<>();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(pet), requestedOwnerId, new AtomicReference<>()),
                Optional.of(owner)
        );

        List<OfferPetOptionDto> result = offerService.findCurrentUserPetOptions();

        assertThat(result).containsExactly(new OfferPetOptionDto(pet.getId(), "Balu", PetSpecies.DOG));
        assertThat(result.getFirst().label()).isEqualTo("Balu (DOG)");
        assertThat(requestedOwnerId).hasValue(owner.getId());
    }

    @Test
    void getCreateOfferFormDataLoadsPetsAndBackendFormRules() {
        User owner = user(UUID.randomUUID());
        Pet pet = pet(UUID.randomUUID(), owner, "Balu", PetSpecies.DOG);
        AtomicReference<UUID> requestedOwnerId = new AtomicReference<>();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(pet), requestedOwnerId, new AtomicReference<>()),
                Optional.of(owner),
                fixedCreateOfferFormRules()
        );

        CreateOfferFormData result = offerService.getCreateOfferFormData();

        assertThat(result.offerTypes()).containsExactly(OfferType.values());
        assertThat(result.frequencies()).containsExactly(OfferFrequency.values());
        assertThat(result.careTypes()).containsExactly(OfferCareType.values());
        assertThat(result.animalTypes()).containsExactly(OfferAnimalType.values());
        assertThat(result.pets()).containsExactly(new OfferPetOptionDto(pet.getId(), "Balu", PetSpecies.DOG));
        assertThat(result.minimumStartDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(result.dateSelection().minimumEndDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(result.dateSelection().summary()).isEqualTo("Bitte Start- und Enddatum auswaehlen.");
        assertThat(result.titleMaxLength()).isEqualTo(120);
        assertThat(result.descriptionMaxLength()).isEqualTo(255);
        assertThat(requestedOwnerId).hasValue(owner.getId());
    }

    @Test
    void summarizeCreateOfferTotalPriceUsesBackendFormRules() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                fixedCreateOfferFormRules()
        );

        String result = offerService.summarizeCreateOfferTotalPrice(
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                new BigDecimal("25.50"));

        assertThat(result).isEqualTo("Gesamtpreis: 76.50 EUR");
    }

    @Test
    void createOfferSavesOpenOfferWithCurrentUserAndSelectedPet() {
        UUID savedOfferId = UUID.randomUUID();
        User owner = user(UUID.randomUUID());
        Pet selectedPet = pet(UUID.randomUUID(), owner, "Mila", PetSpecies.CAT);
        AtomicReference<Offer> savedOfferReference = new AtomicReference<>();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(savedOfferReference, saveCount, savedOfferId),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>(selectedPet)),
                Optional.of(owner)
        );
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        CreateOfferRequest request = new CreateOfferRequest(
                OfferType.OWNER_OFFER,
                startDate,
                endDate,
                selectedPet.getId(),
                BigDecimal.valueOf(25.50),
                "Katzenbetreuung gesucht",
                OfferFrequency.ONE_TIME,
                OfferCareType.PET_SITTING,
                null,
                "Fuettern und Gassi gehen"
        );

        CreateOfferResult result = offerService.createOffer(request);

        Offer savedOffer = savedOfferReference.get();
        assertThat(result.offerId()).isEqualTo(savedOfferId);
        assertThat(saveCount).hasValue(1);
        assertThat(savedOffer.getStartDate()).isEqualTo(startDate);
        assertThat(savedOffer.getEndDate()).isEqualTo(endDate);
        assertThat(savedOffer.getCreateUser()).isSameAs(owner);
        assertThat(savedOffer.getUpdateUser()).isSameAs(owner);
        assertThat(savedOffer.getPet()).isSameAs(selectedPet);
        assertThat(savedOffer.getTitle()).isEqualTo("Katzenbetreuung gesucht");
        assertThat(savedOffer.getFrequency()).isEqualTo(OfferFrequency.ONE_TIME);
        assertThat(savedOffer.getCareType()).isEqualTo(OfferCareType.PET_SITTING);
        assertThat(savedOffer.getAnimalType()).isNull();
        assertThat(savedOffer.getOfferType()).isEqualTo(OfferType.OWNER_OFFER);
        assertThat(savedOffer.getPrice()).isEqualByComparingTo("25.50");
        assertThat(savedOffer.getDescription()).isEqualTo("Fuettern und Gassi gehen");
        assertThat(savedOffer.getStatus()).isEqualTo(OfferStatus.OPEN);
    }

    @Test
    void createOfferAcceptsSelectedPetOptionWithoutFrontendPetIdMapping() {
        User owner = user(UUID.randomUUID());
        Pet selectedPet = pet(UUID.randomUUID(), owner, "Mila", PetSpecies.CAT);
        AtomicReference<Offer> savedOfferReference = new AtomicReference<>();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(savedOfferReference, saveCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>(selectedPet)),
                Optional.of(owner)
        );
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);

        offerService.createOffer(
                OfferType.OWNER_OFFER,
                startDate,
                endDate,
                new OfferPetOptionDto(selectedPet.getId(), selectedPet.getName(), selectedPet.getSpecies()),
                BigDecimal.valueOf(25.50),
                "Fuettern und Gassi gehen");

        assertThat(saveCount).hasValue(1);
        assertThat(savedOfferReference.get().getPet()).isSameAs(selectedPet);
    }

    @Test
    void createOfferSavesSitterAnimalTypeWithoutPet() {
        UUID savedOfferId = UUID.randomUUID();
        User sitter = user(UUID.randomUUID());
        AtomicReference<Offer> savedOfferReference = new AtomicReference<>();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(savedOfferReference, saveCount, savedOfferId),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(sitter)
        );
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        CreateOfferRequest request = new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                startDate,
                endDate,
                null,
                BigDecimal.valueOf(30),
                "Ich betreue Hunde",
                OfferFrequency.REGULAR,
                OfferCareType.PET_AND_HOUSE_SITTING,
                OfferAnimalType.DOG,
                "Erfahrung mit großen Hunden"
        );

        CreateOfferResult result = offerService.createOffer(request);

        Offer savedOffer = savedOfferReference.get();
        assertThat(result.offerId()).isEqualTo(savedOfferId);
        assertThat(saveCount).hasValue(1);
        assertThat(savedOffer.getOfferType()).isEqualTo(OfferType.SITTER_OFFER);
        assertThat(savedOffer.getPet()).isNull();
        assertThat(savedOffer.getTitle()).isEqualTo("Ich betreue Hunde");
        assertThat(savedOffer.getFrequency()).isEqualTo(OfferFrequency.REGULAR);
        assertThat(savedOffer.getCareType()).isEqualTo(OfferCareType.PET_AND_HOUSE_SITTING);
        assertThat(savedOffer.getAnimalType()).isEqualTo(OfferAnimalType.DOG);
        assertThat(savedOffer.getDescription()).isEqualTo("Erfahrung mit großen Hunden");
        assertThat(savedOffer.getStatus()).isEqualTo(OfferStatus.OPEN);
    }

    @Test
    void createOfferRejectsMissingAuthenticatedUser() {
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), saveCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        assertThatThrownBy(() -> offerService.createOffer(validRequest(null)))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(saveCount).hasValue(0);
    }

    @Test
    void createOfferRejectsMissingRequiredFieldsAndInvalidDates() {
        User owner = user(UUID.randomUUID());
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), saveCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(owner)
        );
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        assertInvalid(offerService, new CreateOfferRequest(null, today, tomorrow, null, null, null));
        assertInvalid(offerService, new CreateOfferRequest(OfferType.SITTER_OFFER, null, tomorrow, null, null, null));
        assertInvalid(offerService, new CreateOfferRequest(OfferType.SITTER_OFFER, today, null, null, null, null));
        assertInvalid(offerService, new CreateOfferRequest(OfferType.SITTER_OFFER, today.minusDays(1), tomorrow, null, null, null));
        assertInvalid(offerService, new CreateOfferRequest(OfferType.SITTER_OFFER, today, today, null, null, null));
        assertInvalid(offerService, new CreateOfferRequest(OfferType.SITTER_OFFER, today.plusDays(2), tomorrow, null, null, null));
        assertInvalid(offerService, new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                today,
                tomorrow,
                null,
                null,
                "x".repeat(256)
        ));
        assertInvalid(offerService, new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                today,
                tomorrow,
                null,
                null,
                "x".repeat(121),
                null,
                null,
                null,
                null
        ));
        assertInvalid(offerService, new CreateOfferRequest(OfferType.OWNER_OFFER, today, tomorrow, null, null, null));

        assertThat(saveCount).hasValue(0);
    }

    @Test
    void createOfferRejectsPetOnSitterOfferAndAnimalTypeOnOwnerOffer() {
        User owner = user(UUID.randomUUID());
        Pet selectedPet = pet(UUID.randomUUID(), owner, "Mila", PetSpecies.CAT);
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), saveCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>(selectedPet)),
                Optional.of(owner)
        );
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        assertInvalid(offerService, new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                today,
                tomorrow,
                selectedPet.getId(),
                BigDecimal.TEN,
                null
        ));
        assertInvalid(offerService, new CreateOfferRequest(
                OfferType.OWNER_OFFER,
                today,
                tomorrow,
                selectedPet.getId(),
                BigDecimal.TEN,
                null,
                null,
                null,
                OfferAnimalType.CAT,
                null
        ));

        assertThat(saveCount).hasValue(0);
    }

    @Test
    void createOfferRejectsUnknownPet() {
        User owner = user(UUID.randomUUID());
        UUID missingPetId = UUID.randomUUID();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), saveCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(owner)
        );

        assertThatThrownBy(() -> offerService.createOffer(validOwnerRequest(missingPetId)))
                .isInstanceOf(NotFoundException.class);

        assertThat(saveCount).hasValue(0);
    }

    @Test
    void createOfferRejectsPetOwnedByAnotherUser() {
        User owner = user(UUID.randomUUID());
        User otherOwner = user(UUID.randomUUID());
        Pet otherUsersPet = pet(UUID.randomUUID(), otherOwner, "Nino", PetSpecies.RABBIT);
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), saveCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>(otherUsersPet)),
                Optional.of(owner)
        );

        assertThatThrownBy(() -> offerService.createOffer(validOwnerRequest(otherUsersPet.getId())))
                .isInstanceOf(ForbiddenOperationException.class);

        assertThat(saveCount).hasValue(0);
    }

    @Test
    void searchOpenOffersForTierhalterUsesMinimumEarningsAndOverlappingDates() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID matchingOfferId = UUID.randomUUID();
        AtomicReference<OfferType> requestedType = new AtomicReference<>();
        AtomicReference<OfferStatus> requestedStatus = new AtomicReference<>();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(matchingOfferId, OfferType.OWNER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 19), BigDecimal.valueOf(120)),
                        offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 18), BigDecimal.valueOf(90)),
                        offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), BigDecimal.valueOf(200)),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 18), BigDecimal.valueOf(150)),
                        offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.BOOKED,
                                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 18), BigDecimal.valueOf(150))
                ), requestedType, requestedStatus),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                new OfferSearchCriteria(OfferSearchMode.TIERHALTER, from, to, BigDecimal.valueOf(100), 5));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
        assertThat(requestedType).hasValue(OfferType.OWNER_OFFER);
        assertThat(requestedStatus).hasValue(OfferStatus.OPEN);
    }

    @Test
    void searchOpenOffersForTiersitterUsesMaximumEarnings() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID matchingOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(matchingOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80)),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(130))
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                new OfferSearchCriteria(OfferSearchMode.TIERSITTER, from, to, BigDecimal.valueOf(100), 5));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
    }

    @Test
    void searchOpenOffersWithoutEarningsDoesNotFilterByPrice() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID pricedOfferId = UUID.randomUUID();
        UUID unpricedOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(pricedOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(130)),
                        offer(unpricedOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, null)
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                new OfferSearchCriteria(OfferSearchMode.TIERSITTER, from, to, null, 5));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(pricedOfferId, unpricedOfferId);
    }

    private void assertInvalid(OfferService offerService, CreateOfferRequest request) {
        assertThatThrownBy(() -> offerService.createOffer(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private CreateOfferRequest validRequest(UUID petId) {
        LocalDate startDate = LocalDate.now();
        return new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                startDate,
                startDate.plusDays(1),
                petId,
                BigDecimal.TEN,
                "Betreuung"
        );
    }

    private CreateOfferRequest validOwnerRequest(UUID petId) {
        LocalDate startDate = LocalDate.now();
        return new CreateOfferRequest(
                OfferType.OWNER_OFFER,
                startDate,
                startDate.plusDays(1),
                petId,
                BigDecimal.TEN,
                "Betreuung"
        );
    }

    private OfferService serviceWithAuthenticatedUser(OfferRepository offerRepository, PetRepository petRepository,
            Optional<User> user) {
        return serviceWithAuthenticatedUser(offerRepository, petRepository, user, new CreateOfferFormRules());
    }

    private OfferService serviceWithAuthenticatedUser(OfferRepository offerRepository, PetRepository petRepository,
            Optional<User> user, CreateOfferFormRules createOfferFormRules) {
        return new OfferService(offerRepository, petRepository, authenticatedUser(user), createOfferFormRules);
    }

    private CreateOfferFormRules fixedCreateOfferFormRules() {
        return new CreateOfferFormRules(Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneOffset.UTC));
    }

    private AuthenticatedUser authenticatedUser(Optional<User> user) {
        return new AuthenticatedUser(userRepositoryReturning(user)) {
            @Override
            public Optional<User> get() {
                return user;
            }
        };
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(userId + "@petsitter.local");
        user.setPasswordHash("$2y$10$uZHE15gXghc9i7PVWGhDOOJUt3vZgKg3oiknQQwv9D4lHzsIiBqP2");
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setStreet("Rosenweg");
        user.setHouseNumber("14");
        user.setPostalCode("50667");
        user.setCity("Koeln");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        return user;
    }

    private Pet pet(UUID petId, User owner, String name, PetSpecies species) {
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setOwner(owner);
        pet.setName(name);
        pet.setSpecies(species);
        return pet;
    }

    private Offer offer(UUID offerId, OfferType offerType, OfferStatus status,
            LocalDate startDate, LocalDate endDate, BigDecimal price) {
        Offer offer = new Offer();
        offer.setOfferId(offerId);
        offer.setOfferType(offerType);
        offer.setStatus(status);
        offer.setStartDate(startDate);
        offer.setEndDate(endDate);
        offer.setPrice(price);
        offer.setTitle(offerType.name());
        return offer;
    }

    private OfferRepository offerRepository(AtomicReference<Offer> savedOffer, AtomicInteger saveCount) {
        return offerRepository(savedOffer, saveCount, UUID.randomUUID());
    }

    private OfferRepository offerRepository(AtomicReference<Offer> savedOffer, AtomicInteger saveCount, UUID offerId) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        Offer offer = (Offer) args[0];
                        offer.setOfferId(offerId);
                        savedOffer.set(offer);
                        saveCount.incrementAndGet();
                        return offer;
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private OfferRepository offerRepositoryReturningOffers(List<Offer> offers,
            AtomicReference<OfferType> requestedType, AtomicReference<OfferStatus> requestedStatus) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> {
                    if ("findAllByOfferTypeAndStatus".equals(method.getName())) {
                        OfferType offerType = (OfferType) args[0];
                        OfferStatus status = (OfferStatus) args[1];
                        requestedType.set(offerType);
                        requestedStatus.set(status);
                        return offers.stream()
                                .filter(offer -> offer.getOfferType() == offerType)
                                .filter(offer -> offer.getStatus() == status)
                                .toList();
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRepositorySearchTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private PetRepository petRepository(List<Pet> pets, AtomicReference<UUID> requestedOwnerId,
            AtomicReference<Pet> petById) {
        return (PetRepository) Proxy.newProxyInstance(
                PetRepository.class.getClassLoader(),
                new Class<?>[] {PetRepository.class},
                (proxy, method, args) -> {
                    if ("findAllByOwnerId".equals(method.getName())) {
                        requestedOwnerId.set((UUID) args[0]);
                        return pets;
                    }
                    if ("findById".equals(method.getName())) {
                        Pet pet = petById.get();
                        if (pet != null && pet.getId().equals(args[0])) {
                            return Optional.of(pet);
                        }
                        return Optional.empty();
                    }
                    if ("toString".equals(method.getName())) {
                        return "PetRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
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
}
