package com.softwareengineering.petsitter.offerrequest.service;

import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

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
    public OfferRequest createRequest(UUID offerId, UUID requesterId, String message) {
        throw new UnsupportedOperationException("createRequest noch nicht implementiert");
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
    public void cancelRequest(UUID requestId, UUID requesterId) {
        throw new UnsupportedOperationException("cancelRequest noch nicht implementiert");
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
        return Collections.emptyList();
    }

    /**
     * Findet alle Requests, die ein User selbst erstellt hat.
     *
     * @param userId Die User-ID
     * @return Liste aller Requests von diesem User (als Requester)
     */
    public List<OfferRequest> findMyRequests(UUID userId) {
        return Collections.emptyList();
    }
}

