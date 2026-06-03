package com.softwareengineering.petsitter.booking.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.dto.BookingDto;
import com.softwareengineering.petsitter.booking.dto.BookingAcceptancePreview;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.offer.service.OfferImagePresentationMapper;
import com.softwareengineering.petsitter.offer.dto.OfferCoverTileDto;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.shared.exception.InsufficientBalanceException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.wallet.dto.BookingPaymentDto;
import com.softwareengineering.petsitter.wallet.service.WalletService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BookingService – verwaltet Buchungen (Bookings) und die zentrale Akzeptanz-Logik.
 *
 * <p>Kernverantwortung:
 * <ul>
 *   <li><b>acceptRequest:</b> Atomare Transaktion: Request ACCEPTED → Booking erstellen
 *       → Offer BOOKED → andere Requests DENIED</li>
 *   <li><b>cancelBooking:</b> Storniert ein Booking (nur Owner oder Sitter)</li>
 *   <li><b>getBookings:</b> Alle Bookings eines Users als DTOs</li>
 * </ul>
 *
 * @see Booking
 * @see com.softwareengineering.petsitter.offerrequest.domain.OfferRequest
 * @see com.softwareengineering.petsitter.offer.domain.Offer
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OfferRequestRepository offerRequestRepository;
    private final OfferRepository offerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;
    private final OfferImagePresentationMapper imagePresentationMapper;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            OfferRequestRepository offerRequestRepository,
            OfferRepository offerRepository,
            ApplicationEventPublisher eventPublisher,
            WalletService walletService,
            OfferImagePresentationMapper imagePresentationMapper
    ) {
        this.bookingRepository = bookingRepository;
        this.offerRequestRepository = offerRequestRepository;
        this.offerRepository = offerRepository;
        this.eventPublisher = eventPublisher;
        this.walletService = walletService;
        this.imagePresentationMapper = imagePresentationMapper;
    }

    public BookingService(
            BookingRepository bookingRepository,
            OfferRequestRepository offerRequestRepository,
            OfferRepository offerRepository,
            ApplicationEventPublisher eventPublisher,
            WalletService walletService
    ) {
        this(bookingRepository, offerRequestRepository, offerRepository, eventPublisher, walletService, null);
    }

    BookingService(
            BookingRepository bookingRepository,
            OfferRequestRepository offerRequestRepository,
            OfferRepository offerRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this(bookingRepository, offerRequestRepository, offerRepository, eventPublisher, null, null);
    }

    /**
     * Akzeptiert einen Request und erzeugt ein Booking (atomare Transaktion).
     *
     * <p>Schritte:
     * <ol>
     *   <li>Request validieren (gefunden, PENDING, Offer OPEN)</li>
     *   <li>Zugriffskontrolle: offerCreatorId == offer.creator.id</li>
     *   <li>Request → ACCEPTED</li>
     *   <li>Booking erstellen und speichern</li>
     *   <li>Offer → BOOKED</li>
     *   <li>Alle anderen PENDING Requests → DENIED</li>
     * </ol>
     *
     * @param requestId      ID des zu akzeptierenden Requests
     * @param offerCreatorId ID des Offer-Creators (Zugriffskontrolle)
     * @return Das neu erstellte Booking
     */
    @Transactional
    public Booking acceptRequest(UUID requestId, UUID offerCreatorId) {
        OfferRequest request = offerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request nicht gefunden: " + requestId));

        UUID offerId = request.getOffer().getOfferId();
        Offer offer = offerRepository.findByOfferIdForUpdate(offerId)
                .orElseThrow(() -> new NotFoundException("Offer nicht gefunden: " + offerId));

        // Zugriffskontrolle: nur Offer-Creator darf akzeptieren
        if (!offer.getCreator().getId().equals(offerCreatorId)) {
            throw new ForbiddenOperationException(
                    "Nur der Offer-Creator darf einen Request akzeptieren.");
        }

        // Businessregel: Request muss PENDING sein
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Request ist nicht mehr PENDING (aktuell: " + request.getStatus() + ").");
        }

        // Businessregel: Offer muss OPEN sein
        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new BusinessRuleViolationException(
                    "Das Offer ist nicht mehr OPEN (aktuell: " + offer.getStatus() + ").");
        }

        // Schritt 1: Request auf ACCEPTED setzen
        request.setStatus(RequestStatus.ACCEPTED);
        offerRequestRepository.save(request);

        // Schritt 2: Booking erstellen
        Booking booking = buildBooking(offer, request);
        booking = bookingRepository.save(booking);
        if (walletService != null) {
            try {
                walletService.holdForBooking(booking);
            } catch (InsufficientBalanceException exception) {
                if (!exception.getOwnerId().equals(offerCreatorId)) {
                    walletService.notifyTopUpRequired(exception.getOwnerId());
                }
                throw exception;
            }
        }

        // Schritt 3: Offer auf BOOKED setzen
        offer.setStatus(OfferStatus.BOOKED);
        offerRepository.save(offer);

        // Schritt 4: Alle anderen PENDING Requests auf DENIED setzen
        List<OfferRequest> otherPending = offerRequestRepository
                .findAllByOfferOfferIdAndStatus(offer.getOfferId(), RequestStatus.PENDING);
        for (OfferRequest other : otherPending) {
            other.setStatus(RequestStatus.DENIED);
        }
        offerRequestRepository.saveAll(otherPending);

        // Schritt 5: Event publizieren für Chat-Konversation (entkopplt via Listener)
        eventPublisher.publishEvent(new BookingCreatedEvent(this, booking.getId()));

        return booking;
    }

    /**
     * Storniert ein Booking. Nur Owner oder Sitter dürfen stornieren.
     *
     * @param bookingId ID des Bookings
     * @param userId    ID des anfragenden Users
     */
    @Transactional
    public void cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));

        boolean isOwner  = booking.getOwner().getId().equals(userId);
        boolean isSitter = booking.getSitter().getId().equals(userId);

        if (!isOwner && !isSitter) {
            throw new ForbiddenOperationException(
                    "Nur Owner oder Sitter dürfen ein Booking stornieren.");
        }

        if (booking.getStartDate() != null
                && !booking.getStartDate().isAfter(java.time.LocalDate.now())) {
            throw new BusinessRuleViolationException(
                    "Stornierung nicht moeglich: Die Betreuung hat bereits begonnen.");
        }

        if (walletService != null) {
            walletService.refundForCancelledBooking(booking);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Offer offer = booking.getOffer();
        offer.setStatus(OfferStatus.OPEN);
        offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<UUID> findActiveBookingIdForOffer(UUID offerId) {
        return bookingRepository.findByOffer_OfferIdAndStatus(offerId, BookingStatus.CREATED)
                .map(Booking::getId);
    }

    @Transactional(readOnly = true)
    public boolean isBookingCancelledForRequest(UUID requestId) {
        List<Booking> bookings = bookingRepository.findAllByAcceptedRequest_Id(requestId);
        if (bookings.isEmpty()) return false;
        return bookings.stream().allMatch(b -> b.getStatus() == BookingStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public BookingAcceptancePreview previewForOffer(UUID offerId, UUID requesterId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer nicht gefunden: " + offerId));

        BigDecimal totalPrice = calculateTotalPrice(offer);

        if (walletService == null) {
            return new BookingAcceptancePreview(requesterId, true, offer.getPrice(), totalPrice, totalPrice, true);
        }

        if (offer.getType() != com.softwareengineering.petsitter.offer.domain.OfferType.SITTER_OFFER) {
            return new BookingAcceptancePreview(requesterId, false, offer.getPrice(), totalPrice, totalPrice, true);
        }

        BigDecimal availableBalance = walletService.getAvailableBalance(requesterId);
        return new BookingAcceptancePreview(
                requesterId,
                true,
                offer.getPrice(),
                totalPrice,
                availableBalance,
                availableBalance.compareTo(totalPrice) >= 0);
    }

    @Transactional(readOnly = true)
    public BookingAcceptancePreview previewAcceptance(UUID requestId, UUID offerCreatorId) {
        OfferRequest request = offerRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request nicht gefunden: " + requestId));
        Offer offer = request.getOffer();
        if (!offer.getCreator().getId().equals(offerCreatorId)) {
            throw new ForbiddenOperationException("Nur der Offer-Creator darf einen Request akzeptieren.");
        }
        if (request.getStatus() != RequestStatus.PENDING || offer.getStatus() != OfferStatus.OPEN) {
            throw new BusinessRuleViolationException("Diese Anfrage kann nicht mehr angenommen werden.");
        }

        BookingParties parties = determineParties(offer, request);
        BigDecimal totalPrice = calculateTotalPrice(offer);
        BigDecimal availableBalance = walletService == null
                ? BigDecimal.ZERO
                : walletService.getAvailableBalance(parties.owner().getId());
        return new BookingAcceptancePreview(
                parties.owner().getId(),
                parties.owner().getId().equals(offerCreatorId),
                offer.getPrice(),
                totalPrice,
                availableBalance,
                availableBalance.compareTo(totalPrice) >= 0);
    }

    public void releasePayment(UUID bookingId, UUID userId) {
        walletService.releasePayment(bookingId, userId);
    }

    public void requestPaymentRelease(UUID bookingId, UUID userId) {
        walletService.requestRelease(bookingId, userId);
    }

    /**
     * Markiert ein Booking als COMPLETED (Termin ist vorbei).
     *
     * Nur der Owner oder Sitter können diese Aktion durchführen.
     * Das Booking muss den Status CREATED haben.
     *
     * @param bookingId ID des zu markierenden Bookings
     * @param userId ID des Users (Owner oder Sitter)
     * @throws NotFoundException wenn Booking nicht gefunden
     * @throws ForbiddenOperationException wenn User kein Teilnehmer ist
     * @throws BusinessRuleViolationException wenn Status ungültig ist
     */
    @Transactional
    public Booking markBookingCompleted(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));

        // Zugriffskontrolle: Nur Owner oder Sitter
        if (!booking.getOwner().getId().equals(userId) && !booking.getSitter().getId().equals(userId)) {
            throw new ForbiddenOperationException("Nur Teilnehmer dieser Buchung können den Status ändern.");
        }

        // Status-Validierung: Nur aus CREATED heraus möglich
        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new BusinessRuleViolationException(
                    "Eine Buchung kann nur im Status CREATED als abgeschlossen markiert werden.");
        }

        LocalDate today = LocalDate.now();
        if (booking.getEndDate() == null || !today.isAfter(booking.getEndDate())) {
            throw new BusinessRuleViolationException(
                    "Eine Buchung kann erst nach dem Enddatum als abgeschlossen markiert werden.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        Booking savedBooking = bookingRepository.save(booking);

        // Publish event for listeners (e.g., Chat review reminder)
        eventPublisher.publishEvent(new BookingCompletedEvent(this, savedBooking.getId()));

        return savedBooking;
    }

    /**
     * Gibt alle Bookings eines Users zurück (als Owner oder Sitter), gemappt auf DTOs.
     *
     * @param userId ID des Users
     * @return Liste der BookingDtos
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<BookingDto> getBookings(UUID userId) {
        List<Booking> bookings = bookingRepository.findAllByOwnerIdOrSitterId(userId, userId);
        Map<UUID, List<OfferCoverTileDto>> tiles = imagePresentationMapper == null
                ? Map.of()
                : imagePresentationMapper.coverTilesByOffer(bookings.stream().map(Booking::getOffer).toList());
        return bookings.stream()
                .map(booking -> toDto(booking, tiles.getOrDefault(booking.getOffer().getOfferId(), List.of())))
                .toList();
    }

    // ── Private Hilfsmethoden ────────────────────────────────────────────

    private Booking buildBooking(Offer offer, OfferRequest acceptedRequest) {
        BookingParties parties = determineParties(offer, acceptedRequest);

        Booking booking = new Booking();
        booking.setOffer(offer);
        booking.setAcceptedRequest(acceptedRequest);
        booking.setOwner(parties.owner());
        booking.setSitter(parties.sitter());
        booking.setPet(offer.getPet());
        booking.setStartDate(offer.getStartDate());
        booking.setEndDate(offer.getEndDate());
        booking.setPricePerDay(offer.getPrice());
        booking.setTotalPrice(calculateTotalPrice(offer));
        booking.setStatus(BookingStatus.CREATED);
        return booking;
    }

    private BookingDto toDto(Booking booking) {
        return toDto(booking, imagePresentationMapper == null
                ? List.of()
                : imagePresentationMapper.coverTiles(booking.getOffer()));
    }

    private BookingDto toDto(Booking booking, List<OfferCoverTileDto> coverTiles) {
        String offerTitle = (booking.getOffer().getTitle() != null && !booking.getOffer().getTitle().isBlank())
                ? booking.getOffer().getTitle()
                : "Betreuungsvereinbarung";

        String petName = (booking.getPet() != null) ? booking.getPet().getName() : null;

        String ownerName  = fullName(booking.getOwner());
        String sitterName = fullName(booking.getSitter());
        BookingPaymentDto payment = walletService == null
                ? null
                : walletService.getPaymentForBooking(booking.getId());

        return new BookingDto(
                booking.getId(),
                booking.getOwner().getId(),
                offerTitle,
                ownerName,
                sitterName,
                petName,
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getPricePerDay(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt(),
                payment == null ? null : payment.status(),
                payment == null ? null : payment.releaseRequestedAt(),
                payment == null ? null : payment.automaticReleaseAt(),
                coverTiles
        );
    }

    private BookingParties determineParties(Offer offer, OfferRequest acceptedRequest) {
        User creator = offer.getCreator();
        User requester = acceptedRequest.getRequester();
        if (offer.getType() == OfferType.OWNER_OFFER) {
            return new BookingParties(creator, requester);
        }
        return new BookingParties(requester, creator);
    }

    private BigDecimal calculateTotalPrice(Offer offer) {
        if (offer.getPrice() == null || offer.getStartDate() == null || offer.getEndDate() == null) {
            throw new BusinessRuleViolationException("Preis und Betreuungszeitraum muessen vollstaendig sein.");
        }
        long days = ChronoUnit.DAYS.between(offer.getStartDate(), offer.getEndDate()) + 1;
        if (days <= 0) {
            throw new BusinessRuleViolationException("Das Enddatum muss am oder nach dem Startdatum liegen.");
        }
        return offer.getPrice()
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private record BookingParties(User owner, User sitter) {
    }

    private String fullName(User user) {
        if (user == null) {
            throw new BusinessRuleViolationException("Benutzerdaten fehlen fuer die Namensanzeige.");
        }

        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new BusinessRuleViolationException("Vorname darf nicht leer sein.");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new BusinessRuleViolationException("Nachname darf nicht leer sein.");
        }

        return user.getFirstName().trim() + " " + user.getLastName().trim();
    }
}
