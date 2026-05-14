package com.softwareengineering.petsitter.offer.service;

import com.softwareengineering.petsitter.location.domain.PostalCodeLocation;
import com.softwareengineering.petsitter.location.service.PostalCodeLookupException;
import com.softwareengineering.petsitter.location.service.PostalCodeService;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;
import com.softwareengineering.petsitter.offer.dto.CreateOfferFormData;
import com.softwareengineering.petsitter.offer.dto.CreateOfferRequest;
import com.softwareengineering.petsitter.offer.dto.CreateOfferResult;
import com.softwareengineering.petsitter.offer.dto.MyOfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferPetOptionDto;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OfferService – verwaltet die Erstellung, Änderung und Suche von Offers.
 *
 * <p>Ein Offer ist das Herzstück des Petsitter-Systems und hat zwei Formen:
 * <ul>
 *   <li><b>OWNER_OFFER:</b> Tierhalter sucht Sitter (mit spezifischem Haustier)</li>
 *   <li><b>SITTER_OFFER:</b> Sitter bietet Betreuung an (generisch, kein Pet)</li>
 * </ul>
 *
 * <p>Kernverantwortungen:
 * <ul>
 *   <li><b>createOwnerOffer:</b> Owner erstellt ein Offer für sein Haustier.
 *       Validierung: Pet muss existieren und gehört dem User.
 *   </li>
 *   <li><b>createSitterOffer:</b> Sitter erstellt ein Offer.
 *       Validierung: Keine Pet-Abhängigkeit.
 *   </li>
 *   <li><b>updateOffer:</b> Creator kann sein Offer bearbeiten.
 *       Validierung: Nur wenn Status = OPEN. Bei Edit werden PENDING Requests DENIED.
 *   </li>
 *   <li><b>cancelOffer:</b> Creator storniert sein Offer.
 *   </li>
 *   <li><b>findMatchingOffersForUser:</b> Matching-Logik.
 *       Für OWNER_OFFER: Alle offenen SITTER_OFFER mit überlappenden Daten in gleicher Stadt.
 *       Für SITTER_OFFER: Alle offenen OWNER_OFFER mit überlappenden Daten in gleicher Stadt.
 *   </li>
 * </ul>
 *
 * <p>Wichtige Regeln:
 * <ul>
 *   <li>Nur Creator darf sein Offer bearbeiten (updateOffer, cancelOffer)</li>
 *   <li>BOOKED Offers können nicht bearbeitet werden!</li>
 *   <li>Bei Edit eines OPEN Offers werden alle PENDING Requests DENIED (inkl. Notifications)</li>
 *   <li>Zeitraum: startDate <= endDate</li>
 *   <li>OWNER_OFFER braucht Pet, SITTER_OFFER darf keins haben</li>
 * </ul>
 *
 * @see com.softwareengineering.petsitter.offer.domain.Offer
 * @see com.softwareengineering.petsitter.pet.domain.Pet
 */
@Service
public class OfferService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferService.class);

    private final OfferRepository offerRepository;
    private final PetRepository petRepository;
    private final AuthenticatedUser authenticatedUser;
    private final CreateOfferFormRules createOfferFormRules;
    private final PostalCodeService postalCodeService;

    @Autowired
    public OfferService(OfferRepository offerRepository, PetRepository petRepository,
            AuthenticatedUser authenticatedUser, PostalCodeService postalCodeService) {
        this(offerRepository, petRepository, authenticatedUser, new CreateOfferFormRules(), postalCodeService);
    }

    OfferService(OfferRepository offerRepository, PetRepository petRepository,
            AuthenticatedUser authenticatedUser, CreateOfferFormRules createOfferFormRules) {
        this(offerRepository, petRepository, authenticatedUser, createOfferFormRules, null);
    }

    OfferService(OfferRepository offerRepository, PetRepository petRepository,
            AuthenticatedUser authenticatedUser, CreateOfferFormRules createOfferFormRules,
            PostalCodeService postalCodeService) {
        this.offerRepository = offerRepository;
        this.petRepository = petRepository;
        this.authenticatedUser = authenticatedUser;
        this.createOfferFormRules = createOfferFormRules;
        this.postalCodeService = postalCodeService;
    }

    public boolean hasAuthenticatedUser() {
        return authenticatedUser.get().isPresent();
    }

    @Transactional(readOnly = true)
    public List<OfferCardDto> getOpenOffersByType(OfferType offerType) {
        User currentUser = authenticatedUser.get().orElse(null);

        return offerRepository
                .findAllByOfferTypeAndStatus(offerType, OfferStatus.OPEN)
                .stream()
                .filter(offer -> isVisibleInPublicLists(offer, currentUser))
                .map(this::toCardDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferCardDto> searchOpenOffers(OfferSearchCriteria criteria) {
        if (criteria == null || hasInvalidSearchRange(criteria)) {
            LOGGER.info("Offer search skipped: criteria missing or invalid date range.");
            return List.of();
        }
        LOGGER.info("Offer search started: mode={}, originPostalCode={}, maxDistanceKm={}, earnings={}",
                criteria.mode(),
                criteria.originPostalCode(),
                criteria.distanceKm(),
                criteria.earnings());
        User currentUser = authenticatedUser.get().orElse(null);

        List<Offer> openOffers = offerRepository
                .findAllByOfferTypeAndStatus(criteria.mode().targetOfferType(), OfferStatus.OPEN);

        List<Offer> filteredOffers = openOffers
                .stream()
                .filter(offer -> matchesNonDistanceFilters(offer, criteria, currentUser))
                .toList();
        LOGGER.info("Offer search candidates: openCount={}, afterBaseFilters={}, candidateIds={}",
                openOffers.size(),
                filteredOffers.size(),
                describeOfferIds(filteredOffers));

        List<OfferCardDto> results = toDistanceAwareCardDtos(filteredOffers, criteria);
        LOGGER.info("Offer search finished: resultCount={}, resultIds={}",
                results.size(),
                describeOfferCardIds(results));
        return results;
    }

    private boolean matchesNonDistanceFilters(Offer offer, OfferSearchCriteria criteria, User currentUser) {
        boolean visible = isVisibleInPublicLists(offer, currentUser);
        boolean date = matchesDateRange(offer, criteria);
        boolean earnings = matchesEarnings(offer, criteria.earnings(), criteria.mode().minimumEarnings());
        boolean additional = matchesAdditionalFilters(offer, criteria);
        return visible && date && earnings && additional;
    }

    @Transactional(readOnly = true)
    public Optional<String> getCurrentUserPostalCode() {
        return authenticatedUser.get()
                .map(User::getPostalCode)
                .filter(postalCode -> postalCode != null && !postalCode.isBlank());
    }

    @Transactional
    public Optional<String> validateOriginPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return Optional.empty();
        }
        if (postalCodeService == null) {
            return Optional.of("Die Postleitzahl konnte gerade nicht überprüft werden. Bitte später erneut versuchen.");
        }

        String normalizedPostalCode = postalCodeService.normalizePostalCode(postalCode);
        if (!postalCodeService.isValidGermanPostalCodeFormat(normalizedPostalCode)) {
            return Optional.of("Bitte eine gültige deutsche Postleitzahl eingeben.");
        }
        try {
            if (postalCodeService.findGermanLocation(normalizedPostalCode).isEmpty()) {
                return Optional.of("Bitte eine gültige deutsche Postleitzahl eingeben.");
            }
        } catch (PostalCodeLookupException ex) {
            return Optional.of("Die Postleitzahl konnte gerade nicht überprüft werden. Bitte später erneut versuchen.");
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<MyOfferCardDto> getCurrentUserOffers() {
        return authenticatedUser.get()
                .map(user -> offerRepository.findAllByCreateUserIdOrderByCreateDateDesc(user.getId())
                        .stream()
                        .map(this::toMyOfferCardDto)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserOffer(UUID offerId) {
        return authenticatedUser.get()
                .flatMap(user -> findOfferById(offerId)
                        .map(offer -> isCreatedBy(offer, user)))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canCurrentUserEditOffer(UUID offerId) {
        return authenticatedUser.get()
                .flatMap(user -> findOfferById(offerId)
                        .map(offer -> isCreatedBy(offer, user) && offer.getStatus() == OfferStatus.OPEN))
                .orElse(false);
    }

    private boolean hasInvalidSearchRange(OfferSearchCriteria criteria) {
        return criteria.dateFilterMode() != OfferDateFilterMode.ANY
                && criteria.from() != null
                && criteria.to() != null
                && criteria.to().isBefore(criteria.from());
    }

    private boolean matchesDateRange(Offer offer, OfferSearchCriteria criteria) {
        return switch (criteria.dateFilterMode()) {
            case ANY -> true;
            case EXACT -> matchesExactDateRange(offer, criteria.from(), criteria.to());
            case CONTAINED -> matchesContainedDateRange(offer, criteria.from(), criteria.to());
            case OVERLAP -> matchesFlexibleDateRange(offer, criteria.from(), criteria.to(), criteria.dateFlexDays());
        };
    }

    private boolean matchesExactDateRange(Offer offer, LocalDate from, LocalDate to) {
        return from != null
                && to != null
                && offer.getStartDate().equals(from)
                && offer.getEndDate().equals(to);
    }

    private boolean matchesContainedDateRange(Offer offer, LocalDate from, LocalDate to) {
        return from != null
                && to != null
                && !offer.getStartDate().isBefore(from)
                && !offer.getEndDate().isAfter(to);
    }

    private boolean matchesFlexibleDateRange(Offer offer, LocalDate from, LocalDate to, int flexDays) {
        int normalizedFlexDays = Math.max(0, flexDays);
        if (from != null) {
            from = from.minusDays(normalizedFlexDays);
        }
        if (to != null) {
            to = to.plusDays(normalizedFlexDays);
        }

        if (from != null && offer.getStartDate().isBefore(from)) {
            return false;
        }
        if (to != null && offer.getEndDate().isAfter(to)) {
            return false;
        }
        return true;
    }

    private boolean matchesEarnings(Offer offer, BigDecimal earnings, boolean minimumEarnings) {
        if (earnings == null) {
            return true;
        }
        if (offer.getPrice() == null) {
            return false;
        }
        int comparison = offer.getPrice().compareTo(earnings);
        return minimumEarnings ? comparison >= 0 : comparison <= 0;
    }

    private boolean matchesAdditionalFilters(Offer offer, OfferSearchCriteria criteria) {
        return matchesCareType(offer, criteria.careType())
                && matchesFrequency(offer, criteria.frequency())
                && matchesAnimalTypes(offer, criteria.animalTypes());
    }

    private boolean matchesCareType(Offer offer, OfferCareType careType) {
        return careType == null || offer.getCareType() == careType;
    }

    private boolean matchesFrequency(Offer offer, OfferFrequency frequency) {
        return frequency == null || offer.getFrequency() == frequency;
    }

    private boolean matchesAnimalTypes(Offer offer, Set<OfferAnimalType> animalTypes) {
        if (animalTypes == null || animalTypes.isEmpty()) {
            return true;
        }
        if (offer.getOfferType() == OfferType.SITTER_OFFER) {
            return offer.getAnimalType() != null && animalTypes.contains(offer.getAnimalType());
        }
        if (offer.getOfferType() == OfferType.OWNER_OFFER) {
            return animalTypes.stream()
                    .anyMatch(animalType -> matchesOwnerPetSpecies(offer.getPet(), animalType));
        }
        return false;
    }

    private boolean matchesOwnerPetSpecies(Pet pet, OfferAnimalType animalType) {
        if (pet == null || pet.getSpecies() == null) {
            return false;
        }
        return switch (animalType) {
            case DOG -> pet.getSpecies() == PetSpecies.DOG;
            case CAT -> pet.getSpecies() == PetSpecies.CAT;
            case BIRD -> pet.getSpecies() == PetSpecies.BIRD;
            case SMALL_ANIMAL -> pet.getSpecies() == PetSpecies.RABBIT;
            case OTHER -> pet.getSpecies() == PetSpecies.OTHER;
            case REPTILE, FISH -> false;
        };
    }

    private List<OfferCardDto> toDistanceAwareCardDtos(List<Offer> offers, OfferSearchCriteria criteria) {
        if (criteria.originPostalCode() == null || criteria.originPostalCode().isBlank()) {
            List<OfferCardDto> results = offers.stream()
                    .map(this::toCardDto)
                    .toList();
            LOGGER.info("Distance filter inactive: resultCount={}, resultIds={}",
                    results.size(),
                    describeOfferCardIds(results));
            return results;
        }
        if (postalCodeService == null) {
            LOGGER.info("Distance filter active but PostalCodeService is unavailable. Returning no offers.");
            return List.of();
        }

        Optional<PostalCodeLocation> originLocation = postalCodeService.findCachedGermanLocation(
                criteria.originPostalCode());
        if (originLocation.isEmpty()) {
            LOGGER.info("Distance filter origin postal code is not cached: originPostalCode={}",
                    criteria.originPostalCode());
            return List.of();
        }

        int maxDistanceKm = Math.max(0, criteria.distanceKm());
        PostalCodeLocation resolvedOrigin = originLocation.get();
        Set<String> targetPostalCodes = offers.stream()
                .map(this::creatorPostalCode)
                .filter(postalCode -> postalCode != null && !postalCode.isBlank())
                .map(postalCodeService::normalizePostalCode)
                .filter(postalCodeService::isValidGermanPostalCodeFormat)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, PostalCodeLocation> targetLocations = postalCodeService.findCachedGermanLocations(targetPostalCodes);
        LOGGER.info("Distance filter active: originPostalCode={}, originPlace={}, originLat={}, originLon={}, "
                        + "maxDistanceKm={}, candidates={}, uniqueTargetPostalCodes={}, cachedTargetPostalCodes={}",
                criteria.originPostalCode(),
                resolvedOrigin.getPrimaryPlaceName(),
                resolvedOrigin.getLatitude(),
                resolvedOrigin.getLongitude(),
                maxDistanceKm,
                offers.size(),
                targetPostalCodes.size(),
                targetLocations.size());

        List<OfferCardDto> results = offers.stream()
                .map(offer -> offerDistance(offer, originLocation.get(), targetLocations))
                .flatMap(Optional::stream)
                .filter(offerDistance -> {
                    boolean included = offerDistance.distanceKm() <= maxDistanceKm;
                    LOGGER.info(
                            "Distance filter decision: offerId={}, targetPostalCode={}, targetCity={}, "
                                    + "targetLat={}, targetLon={}, distanceKm={}, maxDistanceKm={}, included={}",
                            offerDistance.offer().getOfferId(),
                            creatorPostalCode(offerDistance.offer()),
                            creatorCity(offerDistance.offer()),
                            offerDistance.targetLocation().getLatitude(),
                            offerDistance.targetLocation().getLongitude(),
                            postalCodeService.roundedDistanceKm(offerDistance.distanceKm()),
                            maxDistanceKm,
                            included);
                    return included;
                })
                .sorted(Comparator.comparingDouble(OfferDistance::distanceKm))
                .map(offerDistance -> toCardDto(
                        offerDistance.offer(),
                        postalCodeService.roundedDistanceKm(offerDistance.distanceKm())))
                .toList();
        LOGGER.info("Distance filter results: resultCount={}, resultIds={}",
                results.size(),
                describeOfferCardIds(results));
        return results;
    }

    private Optional<OfferDistance> offerDistance(
            Offer offer,
            PostalCodeLocation originLocation,
            Map<String, PostalCodeLocation> targetLocations) {
        User createUser = offer.getCreateUser();
        if (createUser == null || createUser.getPostalCode() == null || createUser.getPostalCode().isBlank()) {
            LOGGER.info("Distance filter excluded offer without creator postal code: offerId={}",
                    offer.getOfferId());
            return Optional.empty();
        }
        String targetPostalCode = postalCodeService.normalizePostalCode(createUser.getPostalCode());
        PostalCodeLocation targetLocation = targetLocations.get(targetPostalCode);
        if (targetLocation == null) {
            LOGGER.info("Distance filter excluded offer with uncached creator postal code: offerId={}, targetPostalCode={}",
                    offer.getOfferId(),
                    createUser.getPostalCode());
            return Optional.empty();
        }
        double distanceKm = postalCodeService.distanceKm(originLocation, targetLocation);
        return Optional.of(new OfferDistance(offer, targetLocation, distanceKm));
    }

    private List<UUID> describeOfferIds(List<Offer> offers) {
        return offers.stream()
                .map(Offer::getOfferId)
                .toList();
    }

    private List<UUID> describeOfferCardIds(List<OfferCardDto> offers) {
        return offers.stream()
                .map(OfferCardDto::id)
                .toList();
    }

    private String creatorPostalCode(Offer offer) {
        return offer.getCreateUser() != null ? offer.getCreateUser().getPostalCode() : null;
    }

    private String creatorCity(Offer offer) {
        return offer.getCreateUser() != null ? offer.getCreateUser().getCity() : null;
    }

    private OfferCardDto toCardDto(Offer offer) {
        return toCardDto(offer, null);
    }

    private OfferCardDto toCardDto(Offer offer, Integer distanceKm) {
        boolean verified = offer.getCreateUser() != null
                && offer.getCreateUser().getAccountStatus() == AccountStatus.VERIFIED;
        Pet pet = offer.getPet();
        User createUser = offer.getCreateUser();
        return new OfferCardDto(
                offer.getOfferId(),
                offer.getTitle() != null ? offer.getTitle() : "Angebot",
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getPrice(),
                offer.getAnimalType(),
                verified,
                offer.getDescription(),
                offer.getFrequency(),
                offer.getCareType(),
                pet != null ? pet.getName() : null,
                pet != null ? petSpeciesLabel(pet) : null,
                pet != null ? pet.getBreed() : null,
                createUser != null ? createUser.getPostalCode() : null,
                createUser != null ? createUser.getCity() : null,
                distanceKm
        );
    }

    private MyOfferCardDto toMyOfferCardDto(Offer offer) {
        Pet pet = offer.getPet();
        return new MyOfferCardDto(
                offer.getOfferId(),
                titleOrFallback(offer),
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getPrice(),
                offer.getOfferType(),
                offer.getStatus(),
                offer.getDescription(),
                offer.getFrequency(),
                offer.getCareType(),
                pet != null ? pet.getName() : null,
                pet != null ? petSpeciesLabel(pet) : null,
                pet != null ? pet.getBreed() : null,
                offer.getAnimalType()
        );
    }

    private String titleOrFallback(Offer offer) {
        if (offer.getTitle() != null && !offer.getTitle().isBlank()) {
            return offer.getTitle();
        }
        return offer.getOfferType() == OfferType.OWNER_OFFER ? "Auftrag" : "Angebot";
    }

    private String petSpeciesLabel(Pet pet) {
        if (pet.getSpecies() == PetSpecies.OTHER) {
            return pet.getCustomSpecies() != null ? pet.getCustomSpecies() : "Sonstiges";
        }
        return switch (pet.getSpecies()) {
            case DOG    -> "Hund";
            case CAT    -> "Katze";
            case BIRD   -> "Vogel";
            case RABBIT -> "Kaninchen";
            default     -> pet.getSpecies().name();
        };
    }

    @Transactional(readOnly = true)
    public List<OfferPetOptionDto> findCurrentUserPetOptions() {
        return authenticatedUser.get()
                .map(user -> petRepository.findAllByOwnerId(user.getId()).stream()
                        .map(this::toPetOption)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public CreateOfferFormData getCreateOfferFormData() {
        return new CreateOfferFormData(
                List.of(OfferType.values()),
                List.of(OfferFrequency.values()),
                List.of(OfferCareType.values()),
                List.of(OfferAnimalType.values()),
                findCurrentUserPetOptions(),
                createOfferFormRules.minimumStartDate(),
                createOfferFormRules.initialDateSelection(),
                createOfferFormRules.titleMaxLength(),
                createOfferFormRules.descriptionMaxLength());
    }

    public CreateOfferDateSelection updateCreateOfferDateSelection(LocalDate startDate, LocalDate endDate) {
        return createOfferFormRules.updateDateSelection(startDate, endDate);
    }

    public String summarizeCreateOfferTotalPrice(LocalDate startDate, LocalDate endDate, BigDecimal price) {
        return createOfferFormRules.totalPrice(startDate, endDate, price);
    }

    @Transactional
    public CreateOfferResult createOffer(OfferType offerType, LocalDate startDate, LocalDate endDate,
            OfferPetOptionDto selectedPet, BigDecimal price, String description) {
        return createOffer(new CreateOfferRequest(
                offerType,
                startDate,
                endDate,
                selectedPetId(selectedPet),
                price,
                description));
    }

    @Transactional
    public CreateOfferResult createOffer(OfferType offerType, LocalDate startDate, LocalDate endDate,
            OfferPetOptionDto selectedPet, BigDecimal price, String title, OfferFrequency frequency,
            OfferCareType careType, OfferAnimalType animalType, String description) {
        return createOffer(new CreateOfferRequest(
                offerType,
                startDate,
                endDate,
                selectedPetId(selectedPet),
                price,
                title,
                frequency,
                careType,
                animalType,
                description));
    }

    @Transactional
    public CreateOfferResult createOffer(CreateOfferRequest request) {
        User currentUser = authenticatedUser.get()
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Kein eingeloggter DB-User gefunden. Bitte mit einem gespeicherten User anmelden."));
        validateCreateOfferRequest(request);

        Offer offer = new Offer();
        offer.setStartDate(request.startDate());
        offer.setEndDate(request.endDate());
        offer.setCreateUser(currentUser);
        offer.setUpdateUser(currentUser);
        offer.setPet(resolvePet(request.petId(), currentUser));
        offer.setTitle(cleanText(request.title()));
        offer.setFrequency(request.frequency());
        offer.setCareType(request.careType());
        offer.setAnimalType(request.animalType());
        offer.setOfferType(request.offerType());
        offer.setPrice(request.price());
        offer.setDescription(cleanText(request.description()));
        offer.setStatus(OfferStatus.OPEN);

        Offer savedOffer = offerRepository.save(offer);
        return new CreateOfferResult(savedOffer.getOfferId());
    }

    @Transactional(readOnly = true)
    public CreateOfferRequest getCurrentUserOfferForEdit(UUID offerId) {
        Offer offer = loadEditableCurrentUserOffer(offerId, currentUserOrThrow());
        return toCreateOfferRequest(offer);
    }

    @Transactional
    public CreateOfferResult updateCurrentUserOffer(UUID offerId, CreateOfferRequest request) {
        User currentUser = currentUserOrThrow();
        Offer offer = loadEditableCurrentUserOffer(offerId, currentUser);
        validateCreateOfferRequest(request);
        if (request.offerType() != offer.getOfferType()) {
            throw new BusinessRuleViolationException("Der Angebotstyp kann nicht geaendert werden.");
        }

        offer.setStartDate(request.startDate());
        offer.setEndDate(request.endDate());
        offer.setUpdateUser(currentUser);
        offer.setPet(resolvePet(request.petId(), currentUser));
        offer.setTitle(cleanText(request.title()));
        offer.setFrequency(request.frequency());
        offer.setCareType(request.careType());
        offer.setAnimalType(request.animalType());
        offer.setPrice(request.price());
        offer.setDescription(cleanText(request.description()));

        Offer savedOffer = offerRepository.save(offer);
        return new CreateOfferResult(savedOffer.getOfferId());
    }

    /**
     * Erstellt ein OWNER_OFFER: Der Tierhalter sucht einen Sitter für sein Haustier.
     *
     * <p>Validierungen:
     * - Pet muss existieren
     * - Pet.owner.id muss == userId (User darf nur eigene Pets anbieten)
     * - startDate <= endDate
     * - city darf nicht leer sein
     *
     * @param userId Die User-ID des Owners
     * @param petId Die Pet-ID des zu betreuenden Haustiers
     * @param startDate Beginn des Betreuungszeitraums
     * @param endDate Ende des Betreuungszeitraums
     * @param city Die Stadt
     * @param description Beschreibung (z.B. "Füttern 2x täglich, viel Auslauf")
     * @return Das neu erstellte OWNER_OFFER (Status = OPEN)
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn User oder Pet nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn Pet nicht dem User gehört
     * @throws com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException
     *         wenn startDate > endDate oder city leer
     */
    public Offer createOwnerOffer(UUID userId, UUID petId, LocalDate startDate, LocalDate endDate,
                                   String city, String description) {
        throw new UnsupportedOperationException("createOwnerOffer noch nicht implementiert");
    }

    /**
     * Erstellt ein SITTER_OFFER: Der Sitter bietet seine Betreuungsdienste an.
     *
     * <p>Ein SITTER_OFFER hat kein Pet (generische Betreuungskapazität).
     * Das Pet wird erst bekannt, wenn ein Owner-Request akzeptiert wird.
     *
     * <p>Validierungen:
     * - startDate <= endDate
     * - city darf nicht leer sein
     * - pricePerWeek sollte > 0 sein (optional)
     *
     * @param userId Die User-ID des Sitters
     * @param startDate Beginn des Verfügbarkeitszeitraums
     * @param endDate Ende des Verfügbarkeitszeitraums
     * @param city Die Stadt, in der der Sitter tätig ist
     * @param pricePerWeek Preis pro Woche in EUR (optional)
     * @param description Beschreibung (z.B. "Erfahren mit Hunden und Katzen")
     * @return Das neu erstellte SITTER_OFFER (Status = OPEN, pet = null)
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn User nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException
     *         wenn startDate > endDate oder city leer
     */
    public Offer createSitterOffer(UUID userId, LocalDate startDate, LocalDate endDate,
                                    String city, BigDecimal pricePerWeek, String description) {
        throw new UnsupportedOperationException("createSitterOffer noch nicht implementiert");
    }

    /**
     * Bearbeitet ein existierendes Offer.
     *
     * <p>Wichtige Businessregel: Wenn ein OPEN Offer mit PENDING Requests geändert wird,
     * werden alle diese Requests auf DENIED gesetzt (mit Notification).
     *
     * <p>Validierungen:
     * - Nur der Creator darf bearbeiten (userId muss == Offer.creator.id)
     * - Offer.status muss OPEN sein (BOOKED Offers sind locked!)
     * - Neue Daten müssen gültig sein (startDate <= endDate, etc.)
     *
     * @param offerId Die ID des zu bearbeitenden Offers
     * @param userId Die User-ID des Requesters (zur Zugriffskontrolle)
     * @param startDate Neuer Beginn (optional, wenn null: nicht ändern)
     * @param endDate Neues Ende (optional, wenn null: nicht ändern)
     * @param description Neue Beschreibung (optional, wenn null: nicht ändern)
     * @param pricePerWeek Neuer Preis (optional, wenn null: nicht ändern)
     * @return Das aktualisierte Offer
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn Offer nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn userId != Offer.creator.id
     * @throws com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException
     *         wenn Offer.status != OPEN oder Validierung schlägt fehl
     */
    @Transactional
    public Offer updateOffer(UUID offerId, UUID userId, LocalDate startDate, LocalDate endDate,
                             String description, BigDecimal pricePerWeek) {
        throw new UnsupportedOperationException("updateOffer noch nicht implementiert");
    }

    /**
     * Storniert ein Offer.
     *
     * <p>Validierungen:
     * - Nur der Creator darf stornieren
     * - Nur OPEN Offers können storniert werden (BOOKED sind in Bearbeitung!)
     *
     * @param offerId Die ID des zu stornierenden Offers
     * @param userId Die User-ID des Requesters (zur Zugriffskontrolle)
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn Offer nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn userId != Offer.creator.id
     * @throws com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException
     *         wenn Offer.status != OPEN
     */
    public void cancelOffer(UUID offerId, UUID userId) {
        throw new UnsupportedOperationException("cancelOffer noch nicht implementiert");
    }

    /**
     * Findet passende Offers für einen User (Matching-Logik).
     *
     * <p>Matching-Kriterien:
     * - Gegenangebots-Typ (wenn User ein OWNER_OFFER hat, zeige SITTER_OFFERs)
     * - Status = OPEN (nur verfügbare anzeigen)
     * - City = User.city (optional: auch null/leer akzeptieren → überall tätig)
     * - Zeitraum überlappt oder ist komplementär
     * - NICHT: Offers vom User selbst
     *
     * <p>Beispiel:
     * - User Anna hat OWNER_OFFER [2026-07-01, 2026-07-14] in "Vienna"
     * - Sitter Ben hat SITTER_OFFER [2026-07-01, 2026-07-21] in "Vienna"
     * → Ben's Offer wird angezeigt (überlappender Zeitraum, gleiche Stadt)
     *
     * @param userId Die User-ID
     * @return List der passenden, offenen Offers (Typ + Stadt + Zeitraum stimmen)
     */
    public List<Offer> findMatchingOffersForUser(UUID userId) {
        return Collections.emptyList();
    }

    /**
     * Findet alle Offers eines Users (als Creator).
     *
     * @param userId Die User-ID
     * @return Liste aller Offers, die dieser User erstellt hat
     */
    public List<String> getOffers() {
        return Collections.emptyList();
    }

    private OfferPetOptionDto toPetOption(Pet pet) {
        return new OfferPetOptionDto(pet.getId(), pet.getName(), pet.getSpecies());
    }

    private UUID selectedPetId(OfferPetOptionDto selectedPet) {
        return selectedPet == null ? null : selectedPet.id();
    }

    private User currentUserOrThrow() {
        return authenticatedUser.get()
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Kein eingeloggter DB-User gefunden. Bitte mit einem gespeicherten User anmelden."));
    }

    private java.util.Optional<Offer> findOfferById(UUID offerId) {
        if (offerId == null) {
            return java.util.Optional.empty();
        }
        return offerRepository.findById(offerId);
    }

    private Offer loadEditableCurrentUserOffer(UUID offerId, User currentUser) {
        Offer offer = findOfferById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer nicht gefunden."));
        if (!isCreatedBy(offer, currentUser)) {
            throw new ForbiddenOperationException("Offer gehoert nicht dem aktuellen User.");
        }
        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new BusinessRuleViolationException("Nur offene Offers koennen bearbeitet werden.");
        }
        return offer;
    }

    private boolean isCreatedBy(Offer offer, User user) {
        return offer.getCreateUser() != null
                && user != null
                && user.getId() != null
                && user.getId().equals(offer.getCreateUser().getId());
    }

    private boolean isVisibleInPublicLists(Offer offer, User currentUser) {
        return !isExpiredOpenOffer(offer) && !isCreatedBy(offer, currentUser);
    }

    private boolean isExpiredOpenOffer(Offer offer) {
        return offer.getStatus() == OfferStatus.OPEN
                && offer.getStartDate() != null
                && offer.getStartDate().isBefore(createOfferFormRules.minimumStartDate());
    }

    private record OfferDistance(Offer offer, PostalCodeLocation targetLocation, double distanceKm) {
    }

    private CreateOfferRequest toCreateOfferRequest(Offer offer) {
        return new CreateOfferRequest(
                offer.getOfferType(),
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getPet() != null ? offer.getPet().getId() : null,
                offer.getPrice(),
                offer.getTitle(),
                offer.getFrequency(),
                offer.getCareType(),
                offer.getAnimalType(),
                offer.getDescription());
    }

    private void validateCreateOfferRequest(CreateOfferRequest request) {
        if (request == null || request.offerType() == null || request.startDate() == null || request.endDate() == null) {
            throw new BusinessRuleViolationException("Bitte alle Pflichtfelder korrekt ausfuellen.");
        }

        if (request.startDate().isBefore(createOfferFormRules.minimumStartDate())) {
            throw new BusinessRuleViolationException("Das Startdatum darf nicht in der Vergangenheit liegen.");
        }

        if (request.endDate().isBefore(createOfferFormRules.minimumEndDate(null))) {
            throw new BusinessRuleViolationException("Das Enddatum ist ungueltig.");
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new BusinessRuleViolationException("Das Enddatum muss am oder nach dem Startdatum liegen.");
        }

        if (request.offerType() == OfferType.OWNER_OFFER && request.petId() == null) {
            throw new BusinessRuleViolationException("Bitte ein Haustier fuer den Auftrag auswaehlen.");
        }

        if (request.offerType() == OfferType.OWNER_OFFER && request.animalType() != null) {
            throw new BusinessRuleViolationException("Tierarten werden nur bei Sitter-Angeboten gespeichert.");
        }

        if (request.offerType() == OfferType.SITTER_OFFER && request.petId() != null) {
            throw new BusinessRuleViolationException("Sitter-Angebote duerfen kein eigenes Haustier enthalten.");
        }

        if (request.title() != null && request.title().length() > createOfferFormRules.titleMaxLength()) {
            throw new BusinessRuleViolationException("Der Titel darf maximal 120 Zeichen enthalten.");
        }

        if (request.description() != null
                && request.description().length() > createOfferFormRules.descriptionMaxLength()) {
            throw new BusinessRuleViolationException("Die Beschreibung darf maximal 255 Zeichen enthalten.");
        }
    }

    private Pet resolvePet(UUID petId, User currentUser) {
        if (petId == null) {
            return null;
        }

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Pet nicht gefunden."));
        if (pet.getOwner() == null || !currentUser.getId().equals(pet.getOwner().getId())) {
            throw new ForbiddenOperationException("Pet gehoert nicht dem aktuellen User.");
        }
        return pet;
    }

    private String cleanText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
