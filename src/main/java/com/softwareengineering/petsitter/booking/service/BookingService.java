package com.softwareengineering.petsitter.booking.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.dto.BookingDto;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.util.List;
import java.util.UUID;
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

    public BookingService(
            BookingRepository bookingRepository,
            OfferRequestRepository offerRequestRepository,
            OfferRepository offerRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.bookingRepository = bookingRepository;
        this.offerRequestRepository = offerRequestRepository;
        this.offerRepository = offerRepository;
        this.eventPublisher = eventPublisher;
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

        Offer offer = request.getOffer();

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

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Offer offer = booking.getOffer();
        offer.setStatus(OfferStatus.OPEN);
        offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public boolean isBookingCancelledForRequest(UUID requestId) {
        return bookingRepository.findByAcceptedRequest_Id(requestId)
                .map(b -> b.getStatus() == BookingStatus.CANCELLED)
                .orElse(false);
    }

    /**
     * Gibt alle Bookings eines Users zurück (als Owner oder Sitter), gemappt auf DTOs.
     *
     * @param userId ID des Users
     * @return Liste der BookingDtos
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<BookingDto> getBookings(UUID userId) {
        return bookingRepository.findAllByOwnerIdOrSitterId(userId, userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ── Private Hilfsmethoden ────────────────────────────────────────────

    private Booking buildBooking(Offer offer, OfferRequest acceptedRequest) {
        User creator  = offer.getCreator();
        User requester = acceptedRequest.getRequester();

        User owner;
        User sitter;

        if (offer.getType() == OfferType.OWNER_OFFER) {
            // Owner hat das Offer erstellt, Sitter hat den Request gestellt
            owner  = creator;
            sitter = requester;
        } else {
            // Sitter hat das Offer erstellt, Owner hat den Request gestellt
            sitter = creator;
            owner  = requester;
        }

        Booking booking = new Booking();
        booking.setOffer(offer);
        booking.setAcceptedRequest(acceptedRequest);
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setPet(offer.getPet());
        booking.setStartDate(offer.getStartDate());
        booking.setEndDate(offer.getEndDate());
        booking.setPricePerWeek(offer.getPricePerWeek());
        booking.setStatus(BookingStatus.CREATED);
        return booking;
    }

    private BookingDto toDto(Booking booking) {
        String offerTitle = (booking.getOffer().getTitle() != null && !booking.getOffer().getTitle().isBlank())
                ? booking.getOffer().getTitle()
                : "Betreuungsvereinbarung";

        String petName = (booking.getPet() != null) ? booking.getPet().getName() : null;

        String ownerName  = fullName(booking.getOwner());
        String sitterName = fullName(booking.getSitter());

        return new BookingDto(
                booking.getId(),
                booking.getOwner().getId(),
                offerTitle,
                ownerName,
                sitterName,
                petName,
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getPricePerWeek(),
                booking.getStatus()
        );
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
