package com.softwareengineering.petsitter.offer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.location.domain.PostalCodeLocation;
import com.softwareengineering.petsitter.location.dto.PostalCodeMapLocation;
import com.softwareengineering.petsitter.location.service.PostalCodeLookupException;
import com.softwareengineering.petsitter.location.service.PostalCodeService;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.CreateOfferFormData;
import com.softwareengineering.petsitter.offer.dto.CreateOfferRequest;
import com.softwareengineering.petsitter.offer.dto.CreateOfferResult;
import com.softwareengineering.petsitter.offer.dto.MyOfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferMapLocation;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        assertThat(savedOffer.getPets()).containsExactly(selectedPet);
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
    void createOfferSavesAllSelectedOwnerPets() {
        UUID savedOfferId = UUID.randomUUID();
        User owner = user(UUID.randomUUID());
        Pet dog = pet(UUID.randomUUID(), owner, "Balu", PetSpecies.DOG);
        Pet cat = pet(UUID.randomUUID(), owner, "Mila", PetSpecies.CAT);
        AtomicReference<Offer> savedOfferReference = new AtomicReference<>();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(savedOfferReference, saveCount, savedOfferId),
                petRepository(List.of(dog, cat), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(owner)
        );
        LocalDate startDate = LocalDate.now();
        CreateOfferRequest request = new CreateOfferRequest(
                OfferType.OWNER_OFFER,
                startDate,
                startDate.plusDays(2),
                null,
                List.of(dog.getId(), cat.getId()),
                BigDecimal.valueOf(25.50),
                "Betreuung gesucht",
                OfferFrequency.ONE_TIME,
                OfferCareType.PET_SITTING,
                null,
                "Fuettern und spielen"
        );

        CreateOfferResult result = offerService.createOffer(request);

        Offer savedOffer = savedOfferReference.get();
        assertThat(result.offerId()).isEqualTo(savedOfferId);
        assertThat(saveCount).hasValue(1);
        assertThat(savedOffer.getPet()).isSameAs(dog);
        assertThat(savedOffer.getPets()).containsExactly(dog, cat);
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
        assertThat(savedOffer.getPets()).isEmpty();
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
    void createOfferUsesSpecificDateValidationMessages() {
        User owner = user(UUID.randomUUID());
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(owner),
                fixedCreateOfferFormRules()
        );

        assertThatThrownBy(() -> offerService.createOffer(new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                LocalDate.of(2026, 5, 9),
                LocalDate.of(2026, 5, 11),
                null,
                null,
                "Betreuung")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Das Startdatum darf nicht in der Vergangenheit liegen.");
        assertThatThrownBy(() -> offerService.createOffer(new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 10),
                null,
                null,
                "Betreuung")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Das Enddatum ist ungueltig.");
        assertThatThrownBy(() -> offerService.createOffer(new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 12),
                null,
                null,
                "Betreuung")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Das Enddatum muss am oder nach dem Startdatum liegen.");
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
    void getCurrentUserOffersLoadsAllOffersCreatedByAuthenticatedUser() {
        User currentUser = user(UUID.randomUUID());
        AtomicReference<UUID> requestedUserId = new AtomicReference<>();
        Offer ownerOpen = offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 22), BigDecimal.valueOf(145));
        ownerOpen.setTitle("Katzenbetreuung");
        ownerOpen.setDescription("Füttern und spielen");
        ownerOpen.setFrequency(OfferFrequency.ONE_TIME);
        ownerOpen.setCareType(OfferCareType.PET_SITTING);
        ownerOpen.setPets(List.of(
                pet(UUID.randomUUID(), currentUser, "Mila", PetSpecies.CAT),
                pet(UUID.randomUUID(), currentUser, "Balu", PetSpecies.DOG)));
        Offer sitterBooked = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.BOOKED,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), BigDecimal.valueOf(250));
        sitterBooked.setAnimalType(OfferAnimalType.DOG);
        Offer ownerCancelled = offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.CANCELLED,
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 11), null);
        ownerCancelled.setTitle(null);
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningCurrentUserOffers(List.of(ownerOpen, sitterBooked, ownerCancelled), requestedUserId),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(currentUser)
        );

        List<MyOfferCardDto> result = offerService.getCurrentUserOffers();

        assertThat(requestedUserId).hasValue(currentUser.getId());
        assertThat(result).extracting(MyOfferCardDto::id)
                .containsExactly(ownerOpen.getOfferId(), sitterBooked.getOfferId(), ownerCancelled.getOfferId());
        assertThat(result).extracting(MyOfferCardDto::offerType)
                .containsExactly(OfferType.OWNER_OFFER, OfferType.SITTER_OFFER, OfferType.OWNER_OFFER);
        assertThat(result).extracting(MyOfferCardDto::status)
                .containsExactly(OfferStatus.OPEN, OfferStatus.BOOKED, OfferStatus.CANCELLED);
        assertThat(result.get(0).title()).isEqualTo("Katzenbetreuung");
        assertThat(result.get(0).description()).isEqualTo("Füttern und spielen");
        assertThat(result.get(0).frequency()).isEqualTo(OfferFrequency.ONE_TIME);
        assertThat(result.get(0).careType()).isEqualTo(OfferCareType.PET_SITTING);
        assertThat(result.get(0).petName()).isEqualTo("Mila, Balu");
        assertThat(result.get(0).petSpecies()).isEqualTo("Katze, Hund");
        assertThat(result.get(1).animalType()).isEqualTo(OfferAnimalType.DOG);
        assertThat(result.get(2).title()).isEqualTo("Auftrag");
    }

    @Test
    void getCurrentUserOffersReturnsEmptyListWithoutAuthenticatedUser() {
        AtomicReference<UUID> requestedUserId = new AtomicReference<>();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningCurrentUserOffers(List.of(), requestedUserId),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<MyOfferCardDto> result = offerService.getCurrentUserOffers();

        assertThat(result).isEmpty();
        assertThat(requestedUserId).hasValue(null);
    }

    @Test
    void canCurrentUserEditOfferOnlyAllowsOwnOpenOffers() {
        User currentUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        Offer ownOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownOpen.setCreateUser(currentUser);
        Offer ownBooked = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.BOOKED,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownBooked.setCreateUser(currentUser);
        Offer ownCancelled = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.CANCELLED,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownCancelled.setCreateUser(currentUser);
        Offer foreignOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        foreignOpen.setCreateUser(otherUser);

        assertThat(serviceWithEditableOffer(ownOpen, Optional.of(currentUser))
                .canCurrentUserEditOffer(ownOpen.getOfferId())).isTrue();
        assertThat(serviceWithEditableOffer(ownBooked, Optional.of(currentUser))
                .canCurrentUserEditOffer(ownBooked.getOfferId())).isFalse();
        assertThat(serviceWithEditableOffer(ownCancelled, Optional.of(currentUser))
                .canCurrentUserEditOffer(ownCancelled.getOfferId())).isFalse();
        assertThat(serviceWithEditableOffer(foreignOpen, Optional.of(currentUser))
                .canCurrentUserEditOffer(foreignOpen.getOfferId())).isFalse();
        assertThat(serviceWithEditableOffer(null, Optional.of(currentUser))
                .canCurrentUserEditOffer(UUID.randomUUID())).isFalse();
        assertThat(serviceWithEditableOffer(ownOpen, Optional.empty())
                .canCurrentUserEditOffer(ownOpen.getOfferId())).isFalse();
    }

    @Test
    void getCurrentUserOfferForEditMapsEditableOfferIntoCreateRequest() {
        User currentUser = user(UUID.randomUUID());
        Pet selectedPet = pet(UUID.randomUUID(), currentUser, "Mila", PetSpecies.CAT);
        Pet secondPet = pet(UUID.randomUUID(), currentUser, "Balu", PetSpecies.DOG);
        Offer offer = offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(2), BigDecimal.valueOf(42));
        offer.setCreateUser(currentUser);
        offer.setPets(List.of(selectedPet, secondPet));
        offer.setTitle("Katzenbetreuung");
        offer.setFrequency(OfferFrequency.REGULAR);
        offer.setCareType(OfferCareType.PET_AND_HOUSE_SITTING);
        offer.setDescription("Bitte viel spielen");
        OfferService offerService = serviceWithEditableOffer(offer, Optional.of(currentUser));

        CreateOfferRequest result = offerService.getCurrentUserOfferForEdit(offer.getOfferId());

        assertThat(result.offerType()).isEqualTo(OfferType.OWNER_OFFER);
        assertThat(result.startDate()).isEqualTo(offer.getStartDate());
        assertThat(result.endDate()).isEqualTo(offer.getEndDate());
        assertThat(result.petId()).isEqualTo(selectedPet.getId());
        assertThat(result.petIds()).containsExactly(selectedPet.getId(), secondPet.getId());
        assertThat(result.price()).isEqualByComparingTo("42");
        assertThat(result.title()).isEqualTo("Katzenbetreuung");
        assertThat(result.frequency()).isEqualTo(OfferFrequency.REGULAR);
        assertThat(result.careType()).isEqualTo(OfferCareType.PET_AND_HOUSE_SITTING);
        assertThat(result.animalType()).isNull();
        assertThat(result.description()).isEqualTo("Bitte viel spielen");
    }

    @Test
    void updateCurrentUserOfferUpdatesOwnOpenOffer() {
        User currentUser = user(UUID.randomUUID());
        Pet selectedPet = pet(UUID.randomUUID(), currentUser, "Mila", PetSpecies.CAT);
        Pet secondPet = pet(UUID.randomUUID(), currentUser, "Balu", PetSpecies.DOG);
        Offer offer = offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(2), BigDecimal.valueOf(20));
        offer.setCreateUser(currentUser);
        offer.setUpdateUser(currentUser);
        AtomicReference<Offer> savedOffer = new AtomicReference<>();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryForEdit(offer, savedOffer, saveCount),
                petRepository(List.of(selectedPet, secondPet), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(currentUser)
        );
        LocalDate newStart = LocalDate.now().plusDays(1);
        LocalDate newEnd = newStart.plusDays(3);

        CreateOfferResult result = offerService.updateCurrentUserOffer(offer.getOfferId(), new CreateOfferRequest(
                OfferType.OWNER_OFFER,
                newStart,
                newEnd,
                null,
                List.of(selectedPet.getId(), secondPet.getId()),
                BigDecimal.valueOf(55),
                "Neuer Titel",
                OfferFrequency.ONE_TIME,
                OfferCareType.PET_SITTING,
                null,
                "Neue Beschreibung"));

        assertThat(result.offerId()).isEqualTo(offer.getOfferId());
        assertThat(saveCount).hasValue(1);
        assertThat(savedOffer.get()).isSameAs(offer);
        assertThat(offer.getStartDate()).isEqualTo(newStart);
        assertThat(offer.getEndDate()).isEqualTo(newEnd);
        assertThat(offer.getPet()).isSameAs(selectedPet);
        assertThat(offer.getPets()).containsExactly(selectedPet, secondPet);
        assertThat(offer.getTitle()).isEqualTo("Neuer Titel");
        assertThat(offer.getFrequency()).isEqualTo(OfferFrequency.ONE_TIME);
        assertThat(offer.getCareType()).isEqualTo(OfferCareType.PET_SITTING);
        assertThat(offer.getPrice()).isEqualByComparingTo("55");
        assertThat(offer.getDescription()).isEqualTo("Neue Beschreibung");
        assertThat(offer.getStatus()).isEqualTo(OfferStatus.OPEN);
    }

    @Test
    void updateCurrentUserOfferRejectsForbiddenNonOpenAndInvalidUpdates() {
        User currentUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        LocalDate start = LocalDate.now();
        CreateOfferRequest validUpdate = new CreateOfferRequest(
                OfferType.SITTER_OFFER,
                start,
                start.plusDays(1),
                null,
                BigDecimal.TEN,
                "Titel",
                OfferFrequency.ONE_TIME,
                OfferCareType.PET_SITTING,
                OfferAnimalType.DOG,
                "Beschreibung");
        Offer foreignOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                start, start.plusDays(1), BigDecimal.TEN);
        foreignOpen.setCreateUser(otherUser);
        Offer ownBooked = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.BOOKED,
                start, start.plusDays(1), BigDecimal.TEN);
        ownBooked.setCreateUser(currentUser);
        Offer ownOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                start, start.plusDays(1), BigDecimal.TEN);
        ownOpen.setCreateUser(currentUser);

        assertThatThrownBy(() -> serviceWithEditableOffer(foreignOpen, Optional.of(currentUser))
                .updateCurrentUserOffer(foreignOpen.getOfferId(), validUpdate))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> serviceWithEditableOffer(ownBooked, Optional.of(currentUser))
                .updateCurrentUserOffer(ownBooked.getOfferId(), validUpdate))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> serviceWithEditableOffer(ownOpen, Optional.of(currentUser))
                .updateCurrentUserOffer(ownOpen.getOfferId(), new CreateOfferRequest(
                        OfferType.OWNER_OFFER,
                        start,
                        start.plusDays(1),
                        null,
                        BigDecimal.TEN,
                        "Falscher Typ")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void deleteCurrentUserOfferDeletesOwnOpenOffer() {
        User currentUser = user(UUID.randomUUID());
        Offer ownOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownOpen.setCreateUser(currentUser);
        AtomicReference<Offer> deletedOffer = new AtomicReference<>();
        AtomicInteger deleteCount = new AtomicInteger();
        AtomicInteger saveCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryForEditAndDelete(ownOpen, new AtomicReference<>(), saveCount,
                        deletedOffer, deleteCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(currentUser)
        );

        offerService.deleteCurrentUserOffer(ownOpen.getOfferId());

        assertThat(deleteCount).hasValue(1);
        assertThat(deletedOffer).hasValue(ownOpen);
        assertThat(saveCount).hasValue(0);
    }

    @Test
    void deleteCurrentUserOfferRejectsForeignNonOpenAndAnonymousUser() {
        User currentUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        Offer foreignOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        foreignOpen.setCreateUser(otherUser);
        Offer ownBooked = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.BOOKED,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownBooked.setCreateUser(currentUser);
        Offer ownCancelled = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.CANCELLED,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownCancelled.setCreateUser(currentUser);
        Offer ownOpen = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.TEN);
        ownOpen.setCreateUser(currentUser);

        assertDeleteRejected(foreignOpen, Optional.of(currentUser), ForbiddenOperationException.class);
        assertDeleteRejected(ownBooked, Optional.of(currentUser), BusinessRuleViolationException.class);
        assertDeleteRejected(ownCancelled, Optional.of(currentUser), BusinessRuleViolationException.class);
        assertDeleteRejected(ownOpen, Optional.empty(), BusinessRuleViolationException.class);
    }

    @Test
    void searchOpenOffersForTierhalterUsesMinimumEarningsAndFlexibleDateWindow() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID matchingOfferId = UUID.randomUUID();
        AtomicReference<OfferType> requestedType = new AtomicReference<>();
        AtomicReference<OfferStatus> requestedStatus = new AtomicReference<>();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(UUID.randomUUID(), OfferType.OWNER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 19), BigDecimal.valueOf(120)),
                        offer(matchingOfferId, OfferType.OWNER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 18), BigDecimal.valueOf(120)),
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
                searchCriteria(OfferSearchMode.TIERHALTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100)));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
        assertThat(requestedType).hasValue(OfferType.OWNER_OFFER);
        assertThat(requestedStatus).hasValue(OfferStatus.OPEN);
    }

    @Test
    void getOpenOffersByTypeExcludesCurrentUsersOffersAndExpiredOpenOffers() {
        User currentUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        otherUser.setDisplayName("Ben Betreuung");
        UUID visibleOfferId = UUID.randomUUID();
        Offer ownOffer = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        ownOffer.setCreateUser(currentUser);
        Offer visibleOffer = offer(visibleOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        visibleOffer.setCreateUser(otherUser);
        Offer expiredOpenOffer = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 9), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        expiredOpenOffer.setCreateUser(otherUser);
        AtomicReference<OfferType> requestedType = new AtomicReference<>();
        AtomicReference<OfferStatus> requestedStatus = new AtomicReference<>();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(ownOffer, visibleOffer, expiredOpenOffer),
                        requestedType, requestedStatus),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(currentUser),
                fixedCreateOfferFormRules()
        );

        List<OfferCardDto> result = offerService.getOpenOffersByType(OfferType.SITTER_OFFER);

        assertThat(result).extracting(OfferCardDto::id).containsExactly(visibleOfferId);
        assertThat(result.get(0).creatorUserId()).isEqualTo(otherUser.getId());
        assertThat(result.get(0).creatorDisplayName()).isEqualTo("Ben Betreuung");
        assertThat(requestedType).hasValue(OfferType.SITTER_OFFER);
        assertThat(requestedStatus).hasValue(OfferStatus.OPEN);
    }

    @Test
    void searchOpenOffersExcludesCurrentUsersOffersAndExpiredOpenOffers() {
        User currentUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        UUID visibleOfferId = UUID.randomUUID();
        Offer ownOffer = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        ownOffer.setCreateUser(currentUser);
        Offer visibleOffer = offer(visibleOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        visibleOffer.setCreateUser(otherUser);
        Offer expiredOpenOffer = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 9), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        expiredOpenOffer.setCreateUser(otherUser);
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(ownOffer, visibleOffer, expiredOpenOffer),
                        new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.of(currentUser),
                fixedCreateOfferFormRules()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, null, null, OfferDateFilterMode.ANY, 0, null));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(visibleOfferId);
    }

    @Test
    void searchOpenOffersRejectsPastDateCriteria() {
        UUID visibleOfferId = UUID.randomUUID();
        Offer visibleOffer = offer(visibleOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(visibleOffer), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                fixedCreateOfferFormRules()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER,
                        LocalDate.of(2026, 5, 9), LocalDate.of(2026, 5, 13),
                        OfferDateFilterMode.OVERLAP, 0, null));

        assertThat(result).isEmpty();
    }

    @Test
    void getOpenOffersByTypeKeepsCurrentOffersForAnonymousUsers() {
        UUID visibleOfferId = UUID.randomUUID();
        Offer visibleOffer = offer(visibleOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), BigDecimal.valueOf(90));
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(visibleOffer), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                fixedCreateOfferFormRules()
        );

        List<OfferCardDto> result = offerService.getOpenOffersByType(OfferType.SITTER_OFFER);

        assertThat(result).extracting(OfferCardDto::id).containsExactly(visibleOfferId);
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
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100)));

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
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, null));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(pricedOfferId, unpricedOfferId);
    }

    @Test
    void searchOpenOffersDateModeAnyIgnoresDates() {
        UUID matchingOfferId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(10);
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(matchingOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                startDate, startDate.plusDays(4), BigDecimal.valueOf(80))
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER,
                        LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 20),
                        OfferDateFilterMode.ANY, 0, null));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
    }

    @Test
    void searchOpenOffersDateModeContainedRequiresOfferInsideSearchRange() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 20);
        UUID exactOfferId = UUID.randomUUID();
        UUID containedOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(exactOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN, from, to, BigDecimal.valueOf(80)),
                        offer(containedOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 19), BigDecimal.valueOf(80)),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 19), BigDecimal.valueOf(80)),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 21), BigDecimal.valueOf(80))
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to, OfferDateFilterMode.CONTAINED, 0, null));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(exactOfferId, containedOfferId);
    }

    @Test
    void searchOpenOffersDateModeOverlapRequiresOfferInsideFlexibleWindow() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 20);
        UUID leftFlexOfferId = UUID.randomUUID();
        UUID rightFlexOfferId = UUID.randomUUID();
        UUID insideOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 12), BigDecimal.valueOf(80)),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 14), BigDecimal.valueOf(80)),
                        offer(leftFlexOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 13), LocalDate.of(2026, 6, 14), BigDecimal.valueOf(80)),
                        offer(insideOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 18), LocalDate.of(2026, 6, 19), BigDecimal.valueOf(80)),
                        offer(rightFlexOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 21), LocalDate.of(2026, 6, 22), BigDecimal.valueOf(80)),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 23), LocalDate.of(2026, 6, 24), BigDecimal.valueOf(80))
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to, OfferDateFilterMode.OVERLAP, 2, null));

        assertThat(result).extracting(OfferCardDto::id)
                .containsExactly(leftFlexOfferId, insideOfferId, rightFlexOfferId);
    }

    @Test
    void searchOpenOffersDateModeOverlapWithZeroFlexExcludesPartialOverlap() {
        LocalDate from = LocalDate.of(2026, 6, 4);
        LocalDate to = LocalDate.of(2026, 6, 7);
        UUID matchingOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 6), BigDecimal.valueOf(80)),
                        offer(matchingOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 7), BigDecimal.valueOf(80))
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to, OfferDateFilterMode.OVERLAP, 0, null));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
    }

    @Test
    void searchOpenOffersFiltersByCareType() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID matchingOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(matchingOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null,
                                OfferCareType.PET_AND_HOUSE_SITTING, null),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null,
                                OfferCareType.PET_SITTING, null),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null, null, null)
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, null,
                        OfferCareType.PET_AND_HOUSE_SITTING, null, Set.of()));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
    }

    @Test
    void searchOpenOffersFiltersByFrequency() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID matchingOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), OfferFrequency.ONE_TIME, null, null),
                        offer(matchingOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), OfferFrequency.REGULAR, null, null),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null, null, null)
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, null,
                        null, OfferFrequency.REGULAR, Set.of()));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
    }

    @Test
    void searchOpenOffersAnimalTypesFilterSitterOffersAndRejectNullPreference() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID dogOfferId = UUID.randomUUID();
        UUID catOfferId = UUID.randomUUID();
        UUID noPreferenceOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(dogOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null, null, OfferAnimalType.DOG),
                        offer(catOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null, null, OfferAnimalType.CAT),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null, null, OfferAnimalType.BIRD),
                        offer(noPreferenceOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), null, null, null)
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, null,
                        null, null, Set.of(OfferAnimalType.DOG, OfferAnimalType.CAT)));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(dogOfferId, catOfferId);
    }

    @Test
    void searchOpenOffersAnimalTypeFiltersOwnerOffersToAllowedPetTypeSet() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID dogOfferId = UUID.randomUUID();
        UUID catOfferId = UUID.randomUUID();
        UUID dogCatOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        ownerOffer(dogOfferId, from, to, BigDecimal.valueOf(120), PetSpecies.DOG),
                        ownerOffer(catOfferId, from, to, BigDecimal.valueOf(120), PetSpecies.CAT),
                        ownerOffer(dogCatOfferId, from, to, BigDecimal.valueOf(120), PetSpecies.DOG, PetSpecies.CAT),
                        ownerOffer(UUID.randomUUID(), from, to, BigDecimal.valueOf(120),
                                PetSpecies.DOG, PetSpecies.CAT, PetSpecies.BIRD),
                        ownerOffer(UUID.randomUUID(), from, to, BigDecimal.valueOf(120), PetSpecies.RABBIT),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(120), null, null, OfferAnimalType.SMALL_ANIMAL)
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> dogOnlyResult = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERHALTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100),
                        null, null, Set.of(OfferAnimalType.DOG)));

        List<OfferCardDto> dogAndCatResult = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERHALTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100),
                        null, null, Set.of(OfferAnimalType.DOG, OfferAnimalType.CAT)));

        assertThat(dogOnlyResult).extracting(OfferCardDto::id).containsExactly(dogOfferId);
        assertThat(dogAndCatResult).extracting(OfferCardDto::id)
                .containsExactly(dogOfferId, catOfferId, dogCatOfferId);
    }

    @Test
    void searchOpenOffersAdditionalFiltersAreAdditiveWithDateAndEarnings() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID matchingOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(matchingOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), OfferFrequency.REGULAR,
                                OfferCareType.PET_AND_HOUSE_SITTING, OfferAnimalType.DOG),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), OfferFrequency.REGULAR,
                                OfferCareType.PET_SITTING, OfferAnimalType.DOG),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), OfferFrequency.ONE_TIME,
                                OfferCareType.PET_AND_HOUSE_SITTING, OfferAnimalType.DOG),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(80), OfferFrequency.REGULAR,
                                OfferCareType.PET_AND_HOUSE_SITTING, OfferAnimalType.CAT),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                from, to, BigDecimal.valueOf(130), OfferFrequency.REGULAR,
                                OfferCareType.PET_AND_HOUSE_SITTING, OfferAnimalType.DOG),
                        offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN,
                                LocalDate.of(2026, 6, 19), LocalDate.of(2026, 6, 20), BigDecimal.valueOf(80),
                                OfferFrequency.REGULAR, OfferCareType.PET_AND_HOUSE_SITTING, OfferAnimalType.DOG)
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100),
                        OfferCareType.PET_AND_HOUSE_SITTING,
                        OfferFrequency.REGULAR,
                        Set.of(OfferAnimalType.DOG)));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(matchingOfferId);
    }

    @Test
    void searchOpenOffersFiltersByPostalCodeDistanceAndSortsNearestFirst() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        User nearUser = user(UUID.randomUUID());
        nearUser.setPostalCode("20000");
        nearUser.setCity("Nahstadt");
        User farUser = user(UUID.randomUUID());
        farUser.setPostalCode("30000");
        farUser.setCity("Fernstadt");
        User outsideUser = user(UUID.randomUUID());
        outsideUser.setPostalCode("40000");
        outsideUser.setCity("Zuweitstadt");
        UUID nearOfferId = UUID.randomUUID();
        UUID farOfferId = UUID.randomUUID();
        Offer farOffer = offer(farOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN, from, to, BigDecimal.valueOf(80));
        farOffer.setCreateUser(farUser);
        Offer nearOffer = offer(nearOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN, from, to, BigDecimal.valueOf(80));
        nearOffer.setCreateUser(nearUser);
        Offer outsideOffer = offer(UUID.randomUUID(), OfferType.SITTER_OFFER, OfferStatus.OPEN, from, to, BigDecimal.valueOf(80));
        outsideOffer.setCreateUser(outsideUser);
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(farOffer, outsideOffer, nearOffer),
                        new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeService(Map.of(
                        "10000", location("10000", 0, 0),
                        "20000", location("20000", 0, 0.05),
                        "30000", location("30000", 0, 0.15),
                        "40000", location("40000", 0, 1.0)
                ))
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                new OfferSearchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100), 20,
                        "10000", null, null, Set.of()));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(nearOfferId, farOfferId);
        assertThat(result).extracting(OfferCardDto::distanceKm).containsExactly(6, 17);
        assertThat(result.getFirst().postalCode()).isEqualTo("20000");
        assertThat(result.getFirst().city()).isEqualTo("Nahstadt");
    }

    @Test
    void searchOpenOffersIgnoresDistanceWhenOriginPostalCodeIsMissing() {
        LocalDate from = LocalDate.of(2026, 6, 15);
        LocalDate to = LocalDate.of(2026, 6, 18);
        UUID firstOfferId = UUID.randomUUID();
        UUID secondOfferId = UUID.randomUUID();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryReturningOffers(List.of(
                        offer(firstOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN, from, to, BigDecimal.valueOf(80)),
                        offer(secondOfferId, OfferType.SITTER_OFFER, OfferStatus.OPEN, from, to, BigDecimal.valueOf(80))
                ), new AtomicReference<>(), new AtomicReference<>()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty()
        );

        List<OfferCardDto> result = offerService.searchOpenOffers(
                searchCriteria(OfferSearchMode.TIERSITTER, from, to,
                        OfferDateFilterMode.OVERLAP, 0, BigDecimal.valueOf(100)));

        assertThat(result).extracting(OfferCardDto::id).containsExactly(firstOfferId, secondOfferId);
        assertThat(result).extracting(OfferCardDto::distanceKm).containsExactly(null, null);
    }

    @Test
    void validateOriginPostalCodeRejectsBlankPostalCode() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeServiceWithLookup(Map.of())
        );

        assertThat(offerService.validateOriginPostalCode(null)).contains("Bitte eine Ausgangs-PLZ eingeben.");
        assertThat(offerService.validateOriginPostalCode("")).contains("Bitte eine Ausgangs-PLZ eingeben.");
        assertThat(offerService.validateOriginPostalCode("   ")).contains("Bitte eine Ausgangs-PLZ eingeben.");
    }

    @Test
    void validateOriginPostalCodeAcceptsKnownPostalCode() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeServiceWithLookup(Map.of("49080", location("49080", 52.2588709, 8.0327320)))
        );

        assertThat(offerService.validateOriginPostalCode("49080")).isEmpty();
    }

    @Test
    void validateOriginPostalCodeRejectsUnknownPostalCode() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeServiceWithLookup(Map.of())
        );

        assertThat(offerService.validateOriginPostalCode("49205"))
                .contains("Bitte eine gültige deutsche Postleitzahl eingeben.");
    }

    @Test
    void validateOriginPostalCodeFetchesFromApiWhenNotCached() {
        PostalCodeLocation fetchedLocation = location("49205", 52.2, 8.1);
        PostalCodeService serviceWithApiFallback = new PostalCodeService(null, null) {
            @Override
            public Optional<PostalCodeLocation> findGermanLocation(String postalCode) {
                return "49205".equals(postalCode) ? Optional.of(fetchedLocation) : Optional.empty();
            }

            @Override
            public Optional<PostalCodeLocation> findCachedGermanLocation(String postalCode) {
                return Optional.empty();
            }

            @Override
            public Map<String, PostalCodeLocation> findCachedGermanLocations(Set<String> postalCodes) {
                return Map.of();
            }
        };
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                serviceWithApiFallback
        );

        assertThat(offerService.validateOriginPostalCode("49205")).isEmpty();
    }

    @Test
    void validateOriginPostalCodeReturnsErrorWhenApiFails() {
        PostalCodeService serviceWithApiError = new PostalCodeService(null, null) {
            @Override
            public Optional<PostalCodeLocation> findGermanLocation(String postalCode) {
                throw new PostalCodeLookupException("Nominatim nicht erreichbar.");
            }

            @Override
            public Optional<PostalCodeLocation> findCachedGermanLocation(String postalCode) {
                return Optional.empty();
            }

            @Override
            public Map<String, PostalCodeLocation> findCachedGermanLocations(Set<String> postalCodes) {
                return Map.of();
            }
        };
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                serviceWithApiError
        );

        assertThat(offerService.validateOriginPostalCode("49205"))
                .contains("Die Postleitzahl konnte gerade nicht überprüft werden. Bitte später erneut versuchen.");
    }

    @Test
    void resolveSearchOriginLocationReturnsMapLocationForKnownPostalCode() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeServiceWithLookup(Map.of("49080", location("49080", "Osnabrueck", 52.2588709, 8.0327320)))
        );

        Optional<PostalCodeMapLocation> result = offerService.resolveSearchOriginLocation(" 49080 ");

        assertThat(result).isPresent();
        assertThat(result.get().postalCode()).isEqualTo("49080");
        assertThat(result.get().placeName()).isEqualTo("Osnabrueck");
        assertThat(result.get().latitude()).isEqualByComparingTo("52.2588709");
        assertThat(result.get().longitude()).isEqualByComparingTo("8.032732");
    }

    @Test
    void resolveSearchOriginLocationReturnsEmptyForMissingOrInvalidPostalCode() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeServiceWithLookup(Map.of())
        );

        assertThat(offerService.resolveSearchOriginLocation(null)).isEmpty();
        assertThat(offerService.resolveSearchOriginLocation("   ")).isEmpty();
        assertThat(offerService.resolveSearchOriginLocation("1234")).isEmpty();
        assertThat(offerService.resolveSearchOriginLocation("49205")).isEmpty();
    }

    @Test
    void resolveOfferMapLocationsReturnsCoordinatesForFilteredOffers() {
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepository(new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                Optional.empty(),
                new CreateOfferFormRules(),
                postalCodeServiceWithLookup(Map.of(
                        "10115", location("10115", "Berlin", 52.5321, 13.3849),
                        "49080", location("49080", "Osnabrueck", 52.2588709, 8.0327320)))
        );
        UUID firstOfferId = UUID.randomUUID();
        UUID secondOfferId = UUID.randomUUID();

        List<OfferMapLocation> result = offerService.resolveOfferMapLocations(List.of(
                offerCard(firstOfferId, "Sitter in Berlin", "10115", "Berlin"),
                offerCard(secondOfferId, "Betreuung in Osnabrueck", "49080", "Osnabrueck"),
                offerCard(UUID.randomUUID(), "Ohne Koordinaten", "99999", "Unbekannt"),
                offerCard(UUID.randomUUID(), "Ungueltige PLZ", "1234", "Unbekannt")));

        assertThat(result).extracting(OfferMapLocation::offerId).containsExactly(firstOfferId, secondOfferId);
        assertThat(result).extracting(OfferMapLocation::title)
                .containsExactly("Sitter in Berlin", "Betreuung in Osnabrueck");
        assertThat(result).extracting(OfferMapLocation::postalCode).containsExactly("10115", "49080");
        assertThat(result).extracting(OfferMapLocation::placeName).containsExactly("Berlin", "Osnabrueck");
        assertThat(result.getFirst().latitude()).isEqualByComparingTo("52.5321");
        assertThat(result.getFirst().longitude()).isEqualByComparingTo("13.3849");
    }

    private OfferSearchCriteria searchCriteria(OfferSearchMode mode, LocalDate from, LocalDate to,
            OfferDateFilterMode dateFilterMode, int dateFlexDays, BigDecimal earnings) {
        return searchCriteria(mode, from, to, dateFilterMode, dateFlexDays, earnings, null, null, Set.of());
    }

    private OfferSearchCriteria searchCriteria(OfferSearchMode mode, LocalDate from, LocalDate to,
            OfferDateFilterMode dateFilterMode, int dateFlexDays, BigDecimal earnings,
            OfferCareType careType, OfferFrequency frequency, Set<OfferAnimalType> animalTypes) {
        return new OfferSearchCriteria(mode, from, to, dateFilterMode, dateFlexDays, earnings, 5,
                careType, frequency, animalTypes);
    }

    private void assertInvalid(OfferService offerService, CreateOfferRequest request) {
        assertThatThrownBy(() -> offerService.createOffer(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private void assertDeleteRejected(Offer offer, Optional<User> user,
            Class<? extends RuntimeException> expectedException) {
        AtomicInteger deleteCount = new AtomicInteger();
        OfferService offerService = serviceWithAuthenticatedUser(
                offerRepositoryForEditAndDelete(offer, new AtomicReference<>(), new AtomicInteger(),
                        new AtomicReference<>(), deleteCount),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                user
        );

        assertThatThrownBy(() -> offerService.deleteCurrentUserOffer(offer.getOfferId()))
                .isInstanceOf(expectedException);
        assertThat(deleteCount).hasValue(0);
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
        return serviceWithAuthenticatedUser(offerRepository, petRepository, user, createOfferFormRules,
                postalCodeService(Map.of()));
    }

    private OfferService serviceWithAuthenticatedUser(OfferRepository offerRepository, PetRepository petRepository,
            Optional<User> user, CreateOfferFormRules createOfferFormRules, PostalCodeService postalCodeService) {
        return new OfferService(offerRepository, petRepository, authenticatedUser(user),
                createOfferFormRules, postalCodeService);
    }

    private OfferService serviceWithEditableOffer(Offer offer, Optional<User> user) {
        return serviceWithAuthenticatedUser(
                offerRepositoryForEdit(offer, new AtomicReference<>(), new AtomicInteger()),
                petRepository(List.of(), new AtomicReference<>(), new AtomicReference<>()),
                user
        );
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

    private OfferCardDto offerCard(UUID offerId, String title, String postalCode, String city) {
        return new OfferCardDto(
                offerId,
                title,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 18),
                BigDecimal.valueOf(80),
                OfferAnimalType.DOG,
                true,
                "Beschreibung",
                OfferFrequency.ONE_TIME,
                OfferCareType.PET_SITTING,
                null,
                null,
                null,
                postalCode,
                city,
                null,
                false,
                OfferType.SITTER_OFFER);
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

    private Offer offer(UUID offerId, OfferType offerType, OfferStatus status,
            LocalDate startDate, LocalDate endDate, BigDecimal price,
            OfferFrequency frequency, OfferCareType careType, OfferAnimalType animalType) {
        Offer offer = offer(offerId, offerType, status, startDate, endDate, price);
        offer.setFrequency(frequency);
        offer.setCareType(careType);
        offer.setAnimalType(animalType);
        return offer;
    }

    private Offer ownerOffer(UUID offerId, LocalDate startDate, LocalDate endDate,
            BigDecimal price, PetSpecies... petSpecies) {
        Offer offer = offer(offerId, OfferType.OWNER_OFFER, OfferStatus.OPEN, startDate, endDate, price);
        User owner = user(UUID.randomUUID());
        offer.setPets(java.util.Arrays.stream(petSpecies)
                .map(species -> pet(UUID.randomUUID(), owner, "Testtier", species))
                .toList());
        return offer;
    }

    private PostalCodeLocation location(String postalCode, double latitude, double longitude) {
        return location(postalCode, postalCode, latitude, longitude);
    }

    private PostalCodeLocation location(String postalCode, String primaryPlaceName, double latitude, double longitude) {
        return new PostalCodeLocation(
                "DE",
                postalCode,
                primaryPlaceName,
                primaryPlaceName,
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude),
                java.time.LocalDateTime.now());
    }

    private PostalCodeService postalCodeService(Map<String, PostalCodeLocation> locations) {
        return new PostalCodeService(null, null) {
            @Override
            public Optional<PostalCodeLocation> findGermanLocation(String postalCode) {
                throw new AssertionError("Offer search must not call the postal code API lookup path.");
            }

            @Override
            public Optional<PostalCodeLocation> findCachedGermanLocation(String postalCode) {
                return Optional.ofNullable(locations.get(postalCode));
            }

            @Override
            public Map<String, PostalCodeLocation> findCachedGermanLocations(Set<String> postalCodes) {
                return postalCodes.stream()
                        .filter(locations::containsKey)
                        .collect(java.util.stream.Collectors.toMap(
                                postalCode -> postalCode,
                                locations::get));
            }
        };
    }

    private PostalCodeService postalCodeServiceWithLookup(Map<String, PostalCodeLocation> locations) {
        return new PostalCodeService(null, null) {
            @Override
            public Optional<PostalCodeLocation> findGermanLocation(String postalCode) {
                return Optional.ofNullable(locations.get(postalCode));
            }

            @Override
            public Optional<PostalCodeLocation> findCachedGermanLocation(String postalCode) {
                return Optional.ofNullable(locations.get(postalCode));
            }

            @Override
            public Map<String, PostalCodeLocation> findCachedGermanLocations(Set<String> postalCodes) {
                return postalCodes.stream()
                        .filter(locations::containsKey)
                        .collect(java.util.stream.Collectors.toMap(
                                postalCode -> postalCode,
                                locations::get));
            }
        };
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

    private OfferRepository offerRepositoryReturningCurrentUserOffers(List<Offer> offers,
            AtomicReference<UUID> requestedUserId) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> {
                    if ("findAllByCreateUserIdOrderByCreateDateDesc".equals(method.getName())) {
                        requestedUserId.set((UUID) args[0]);
                        return offers;
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRepositoryCurrentUserOffersTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private OfferRepository offerRepositoryForEdit(Offer offer, AtomicReference<Offer> savedOffer,
            AtomicInteger saveCount) {
        return offerRepositoryForEditAndDelete(offer, savedOffer, saveCount,
                new AtomicReference<>(), new AtomicInteger());
    }

    private OfferRepository offerRepositoryForEditAndDelete(Offer offer, AtomicReference<Offer> savedOffer,
            AtomicInteger saveCount, AtomicReference<Offer> deletedOffer, AtomicInteger deleteCount) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        UUID requestedOfferId = (UUID) args[0];
                        if (offer != null && offer.getOfferId().equals(requestedOfferId)) {
                            return Optional.of(offer);
                        }
                        return Optional.empty();
                    }
                    if ("save".equals(method.getName())) {
                        Offer updatedOffer = (Offer) args[0];
                        savedOffer.set(updatedOffer);
                        saveCount.incrementAndGet();
                        return updatedOffer;
                    }
                    if ("delete".equals(method.getName())) {
                        Offer removedOffer = (Offer) args[0];
                        deletedOffer.set(removedOffer);
                        deleteCount.incrementAndGet();
                        return null;
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRepositoryEditTestDouble";
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
                        return pets.stream()
                                .filter(candidate -> candidate.getId().equals(args[0]))
                                .findFirst();
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
