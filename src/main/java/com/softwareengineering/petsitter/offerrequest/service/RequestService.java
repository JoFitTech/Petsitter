package com.softwareengineering.petsitter.offerrequest.service;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.offerrequest.dto.OfferRequestChatCardDto;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.DuplicateRequestException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RequestService – verwaltet Requests (Anfragen) von Usern auf existierende Offers.
 *
 * <p>Geschäftslogik:
 * <ul>
 *   <li><b>createRequest:</b> Ein User erstellt einen Request auf ein Offer (z.B. Sitter interessiert sich für Owner-Offer).
 *       Validierungen:
 *       - Requester darf NICHT der Creator des Offers sein
 *       - Pro User+Offer: nur ein Request erlaubt (Unique Constraint hinten)
 *       - Offer muss Status OPEN haben
 *       - Request startet im Status PENDING
 *   </li>
 *   <li><b>cancelRequest:</b> Requester kann seinen Request stornieren (z.B. Interesse verloren).
 *       Nur möglich wenn Status noch PENDING oder ACCEPTED ist.
 *   </li>
 *   <li><b>findRequestsForOffer:</b> Offer-Creator sieht alle Requests auf sein Offer.
 *       Zugriffskontrolle: Nur Creator darf das!
 *   </li>
 *   <li><b>findMyRequests:</b> User sieht alle seine erstellten Requests.
 *   </li>
 * </ul>
 *
 * <p>Wichtig:
 * Die zentrale Logik der Request-Akzeptanz (→ Booking erstellen, Offer buchen, andere denken) ist in
 * {@link com.softwareengineering.petsitter.booking.service.BookingService#acceptRequest}.
 * RequestService ist nur für Request-Verwaltung zuständig.
 *
 * @see com.softwareengineering.petsitter.offerrequest.domain.OfferRequest
 * @see com.softwareengineering.petsitter.booking.service.BookingService
 */
@Service
public class RequestService {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final OfferRequestRepository offerRequestRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

    public RequestService(
            OfferRequestRepository offerRequestRepository,
            OfferRepository offerRepository,
            UserRepository userRepository
    ) {
        this.offerRequestRepository = offerRequestRepository;
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Erstellt einen neuen Request von {@code requesterId} auf das Offer {@code offerId}.
     *
     * <p>Validierungen in Service durchgeführt:
     * <ul>
     *   <li>Requester != Offer Creator (Selbst-Request forbidden)</li>
     *   <li>Offer.status == OPEN (nur auf offene Angebote)</li>
     *   <li>Ein Request pro (offer, requester) Paar → Unique DB-Constraint fängt Duplikate</li>
     * </ul>
     *
     * @param offerId   Die Offer-ID, auf die der Request bezieht
     * @param requesterId Die User-ID des Requesters
     * @param message   Nachricht des Requesters (z.B. "Ich bin sehr zuverlässig!")
     * @return Der neue, persistierte Request (Status = PENDING)
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn Offer oder User nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn Requester == Offer Creator
     * @throws com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException
     *         wenn Offer nicht OPEN ist
     * @throws com.softwareengineering.petsitter.shared.exception.DuplicateRequestException
     *         wenn es bereits einen Request von dieser User auf dieses Offer gibt
     */
    @Transactional
    public OfferRequest createRequest(UUID offerId, UUID requesterId, String message) {
        requireId(offerId, "offerId darf nicht null sein");
        requireId(requesterId, "requesterId darf nicht null sein");

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer nicht gefunden: " + offerId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("User nicht gefunden: " + requesterId));

        if (offer.getCreator() == null || offer.getCreator().getId() == null) {
            throw new BusinessRuleViolationException("Offer hat keinen gueltigen Creator");
        }

        if (offer.getCreator().getId().equals(requesterId)) {
            throw new ForbiddenOperationException("Du kannst keinen Request auf dein eigenes Offer erstellen");
        }

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new BusinessRuleViolationException("Requests sind nur auf OPEN Offers erlaubt");
        }

        var existingOpt = offerRequestRepository.findByOfferOfferIdAndRequesterId(offerId, requesterId);
        if (existingOpt.isPresent()) {
            OfferRequest existing = existingOpt.get();
            if (existing.getStatus() == RequestStatus.PENDING) {
                throw new DuplicateRequestException("Es existiert bereits ein Request fuer dieses Offer und diesen User");
            }
            // DENIED or ACCEPTED (booking was cancelled, offer is OPEN again) → reuse row
            existing.setStatus(RequestStatus.PENDING);
            existing.setMessage(normalizeMessage(message));
            return offerRequestRepository.save(existing);
        }

        OfferRequest request = new OfferRequest();
        request.setOffer(offer);
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        request.setMessage(normalizeMessage(message));

        return offerRequestRepository.save(request);
    }

    /**
     * Storniert einen existierenden Request.
     *
     * <p>Nur der Ersteller des Requests (requester) darf ihn stornieren.
     * Status wird zu CANCELLED.
     *
     * @param requestId Die Request-ID
     * @param requesterId Die User-ID des Requesters (zur Zugriffskontrolle)
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn Request nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn {@code requesterId} != Request.requester.id
     */
    @Transactional
    public void cancelRequest(UUID requestId, UUID requesterId) {
        requireId(requestId, "requestId darf nicht null sein");
        requireId(requesterId, "requesterId darf nicht null sein");

        OfferRequest request = offerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request nicht gefunden: " + requestId));

        if (request.getRequester() == null || request.getRequester().getId() == null) {
            throw new BusinessRuleViolationException("Request hat keinen gueltigen Requester");
        }

        if (!requesterId.equals(request.getRequester().getId())) {
            throw new ForbiddenOperationException("Nur der Requester darf den Request stornieren");
        }

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.ACCEPTED) {
            throw new BusinessRuleViolationException("Nur PENDING oder ACCEPTED Requests duerfen storniert werden");
        }

        request.setStatus(RequestStatus.CANCELLED);
        offerRequestRepository.save(request);
    }

    /**
     * Findet alle Requests auf einem spezifischen Offer.
     *
     * <p>Zugriffskontrolle: Nur der Offer-Creator darf das!
     *
     * @param offerId Die Offer-ID
     * @param creatorId Die User-ID (muss == Offer.creator.id sein)
     * @return Liste aller Requests auf dieses Offer
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn {@code creatorId} != Offer.creator.id
     */
    public List<OfferRequest> findRequestsForOffer(UUID offerId, UUID creatorId) {
        requireId(offerId, "offerId darf nicht null sein");
        requireId(creatorId, "creatorId darf nicht null sein");

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer nicht gefunden: " + offerId));

        if (offer.getCreator() == null || offer.getCreator().getId() == null) {
            throw new BusinessRuleViolationException("Offer hat keinen gueltigen Creator");
        }

        if (!Objects.equals(offer.getCreator().getId(), creatorId)) {
            throw new ForbiddenOperationException("Nur der Offer-Creator darf Requests sehen");
        }

        return offerRequestRepository.findAllByOfferOfferId(offerId);
    }

    /**
     * Findet alle Requests, die ein User selbst erstellt hat.
     *
     * @param userId Die User-ID
     * @return Liste aller Requests von diesem User (als Requester)
     */
    public List<OfferRequest> findMyRequests(UUID userId) {
        requireId(userId, "userId darf nicht null sein");
        return offerRequestRepository.findAllByRequesterId(userId);
    }

    public OfferRequest findById(UUID requestId) {
        requireId(requestId, "requestId darf nicht null sein");
        return offerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request nicht gefunden: " + requestId));
    }

    @Transactional(readOnly = true)
    public OfferRequestChatCardDto findChatCardDetails(UUID requestId) {
        OfferRequest request = findById(requestId);
        Offer offer = request.getOffer();
        if (offer == null) {
            throw new BusinessRuleViolationException("Request hat kein gueltiges Offer");
        }
        return new OfferRequestChatCardDto(
                request.getId(),
                request.getStatus(),
                titleOrFallback(offer),
                offer.getOfferType(),
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getFrequency(),
                offer.getRecurringWeekdays(),
                offer.getTimeSlot(),
                petNames(offer),
                offer.getAnimalType()
        );
    }

    @Transactional(readOnly = true)
    public OfferRequest findByIdWithDetails(UUID requestId) {
        OfferRequest req = findById(requestId);
        // Initialize lazy associations within transaction so they remain accessible after it closes
        if (req.getOffer() != null) {
            req.getOffer().getOfferId();
            req.getOffer().getTitle();
            if (req.getOffer().getCreator() != null) {
                req.getOffer().getCreator().getId();
                req.getOffer().getCreator().getFirstName();
            }
        }
        if (req.getRequester() != null) {
            req.getRequester().getId();
            req.getRequester().getFirstName();
            req.getRequester().getLastName();
        }
        return req;
    }

    @Transactional
    public void denyRequest(UUID requestId, UUID offerCreatorId) {
        requireId(requestId, "requestId darf nicht null sein");
        requireId(offerCreatorId, "offerCreatorId darf nicht null sein");

        OfferRequest request = findById(requestId);

        if (!request.getOffer().getCreator().getId().equals(offerCreatorId)) {
            throw new ForbiddenOperationException("Nur der Offer-Creator darf eine Anfrage ablehnen");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleViolationException("Nur PENDING Requests koennen abgelehnt werden");
        }

        request.setStatus(RequestStatus.DENIED);
        offerRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public Optional<OfferRequest> findPendingRequestFromRequesterToCreator(UUID creatorId, UUID requesterId) {
        List<OfferRequest> pending = offerRequestRepository
            .findAllByOffer_CreateUser_IdAndRequester_IdAndStatus(creatorId, requesterId, RequestStatus.PENDING);
        if (pending.isEmpty()) {
            return Optional.empty();
        }
        OfferRequest req = pending.get(0);
        req.getOffer().getOfferId();
        req.getOffer().getTitle();
        req.getOffer().getCreator().getId();
        req.getOffer().getCreator().getFirstName();
        req.getRequester().getId();
        req.getRequester().getFirstName();
        req.getRequester().getLastName();
        return Optional.of(req);
    }

    @Transactional(readOnly = true)
    public Optional<OfferRequest> findAcceptedRequestFromRequesterToCreator(UUID creatorId, UUID requesterId) {
        List<OfferRequest> accepted = offerRequestRepository
            .findAllByOffer_CreateUser_IdAndRequester_IdAndStatus(creatorId, requesterId, RequestStatus.ACCEPTED);
        if (accepted.isEmpty()) {
            return Optional.empty();
        }
        OfferRequest req = accepted.get(0);
        req.getOffer().getOfferId();
        req.getOffer().getTitle();
        req.getRequester().getId();
        return Optional.of(req);
    }

    @Transactional(readOnly = true)
    public Optional<OfferRequest> findDeniedRequestFromRequesterToCreator(UUID creatorId, UUID requesterId) {
        List<OfferRequest> denied = offerRequestRepository
            .findAllByOffer_CreateUser_IdAndRequester_IdAndStatus(creatorId, requesterId, RequestStatus.DENIED);
        if (denied.isEmpty()) {
            return Optional.empty();
        }
        OfferRequest req = denied.get(0);
        req.getOffer().getOfferId();
        req.getOffer().getTitle();
        req.getRequester().getId();
        return Optional.of(req);
    }

    private void requireId(UUID value, String message) {
        if (value == null) {
            throw new BusinessRuleViolationException(message);
        }
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }
        String normalized = message.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessRuleViolationException("Nachricht darf maximal " + MAX_MESSAGE_LENGTH + " Zeichen haben");
        }
        return normalized;
    }

    private String titleOrFallback(Offer offer) {
        if (offer.getTitle() != null && !offer.getTitle().isBlank()) {
            return offer.getTitle();
        }
        return "Angebot";
    }

    private String petNames(Offer offer) {
        String names = offer.getPets().stream()
                .filter(Objects::nonNull)
                .map(Pet::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));
        return names.isBlank() ? null : names;
    }
}
