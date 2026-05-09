package com.softwareengineering.petsitter.booking.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

/**
 * BookingService – verwaltet Buchungen (Bookings) und die zentrale Akzeptanz-Logik.
 *
 * <p>Kernverantwortung:
 * <ul>
 *   <li><b>acceptRequest:</b> Dies ist DIE ZENTRALE Methode der gesamten Anwendung!
 *       Sie implementiert eine komplexe Transaktion:
 *       <ol>
 *         <li>1. Request wird auf Status ACCEPTED gesetzt</li>
 *         <li>2. Neues Booking wird erstellt (mit Owner, Sitter, Pet, Zeitraum, Preis)</li>
 *         <li>3. Offer wird auf Status BOOKED gesetzt (keine neuen Requests mehr möglich)</li>
 *         <li>4. ALLE anderen PENDING Requests auf dasselbe Offer werden auf DENIED gesetzt</li>
 *         <li>5. Optional: Notification an alle non-akzeptierten Requester</li>
 *       </ol>
 *       Wenn irgendein Schritt fehlschlägt → ROLLBACK, nichts bleibt!
 *   </li>
 *   <li><b>cancelBooking:</b> User (Owner oder Sitter) storniert ein bestätigtes Booking.
 *   </li>
 *   <li><b>getBookings:</b> Findet alle Bookings für einen User (als Owner oder Sitter).
 *   </li>
 * </ul>
 *
 * <p>Wichtige Regeln (in acceptRequest durchgesetzt):
 * <ul>
 *   <li>Nur der Offer-Creator (Owner oder Sitter) darf einen Request akzeptieren!</li>
 *   <li>Request.status muss PENDING sein</li>
 *   <li>Offer.status muss OPEN sein</li>
 *   <li>startDate <= endDate (impliziert vom Offer)</li>
 *   <li>Wenn akzeptiert: Offer wird BOOKED → keine Edits mehr möglich!</li>
 * </ul>
 *
 * <p>Warum @Transactional? Damit alle 4-5 Schritte atomar ablaufen.
 * Ein DB-Fehler nach Schritt 2 würde Inkonsistenz verursachen (Booking ohne Offer-Update).
 * Deshalb: "Alles erfolgreich oder Rollback!"
 *
 * @see com.softwareengineering.petsitter.booking.domain.Booking
 * @see com.softwareengineering.petsitter.offerrequest.domain.OfferRequest
 * @see com.softwareengineering.petsitter.offer.domain.Offer
 */
@Service
public class BookingService {

    /**
     * Akzeptiert einen Request und erzeugt ein Booking.
     *
     * <p><b>Dies ist eine atomare Transaktion mit 4 Schritten:</b>
     * <ol>
     *   <li>Request wird auf ACCEPTED gesetzt</li>
     *   <li>Neues Booking wird erstellt und persistiert</li>
     *   <li>Offer wird auf BOOKED gesetzt</li>
     *   <li>Alle anderen PENDING Requests auf diesem Offer werden DENIED</li>
     * </ol>
     *
     * <p>Validierungen (vor Step 1):
     * - {@code offerCreatorId} muss == Request.offer.creator.id (Zugriffskontrolle)
     * - Request.status == PENDING
     * - Offer.status == OPEN
     *
     * <p>Diese Methode ist SEHR wichtig und wird von der UI aufgerufen, wenn der
     * Offer-Ersteller einen Request annimmt.
     *
     * @param requestId Die ID des zu akzeptierenden Requests
     * @param offerCreatorId Die User-ID des Offer-Creators (zur Zugriffskontrolle)
     * @return Das neu erzeugte Booking
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn Request, Offer oder User nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn {@code offerCreatorId} != Offer.creator.id
     *         (nur Offer-Creator darf akzeptieren!)
     * @throws com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException
     *         wenn Request.status != PENDING oder Offer.status != OPEN
     */
    @Transactional
    public Booking acceptRequest(Long requestId, Long offerCreatorId) {
        throw new UnsupportedOperationException("acceptRequest noch nicht implementiert");
    }

    /**
     * Storniert ein Booking.
     *
     * <p>Nur Owner oder Sitter (aus dem Booking) darf stornieren.
     * Status wird zu CANCELLED.
     *
     * Hinweis: Je nach Anforderung könnte man dadurch auch das Offer zurück auf OPEN setzen,
     * damit andere Requests erneut erfolgreich sein können. Für Phase 1 einfach CANCELLED halten.
     *
     * @param bookingId Die Booking-ID
     * @param userId Die User-ID des Requesters (Owner oder Sitter, zur Zugriffskontrolle)
     * @throws com.softwareengineering.petsitter.shared.exception.NotFoundException
     *         wenn Booking nicht gefunden
     * @throws com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException
     *         wenn {@code userId} weder Owner noch Sitter des Bookings ist
     */
    public void cancelBooking(Long bookingId, Long userId) {
        throw new UnsupportedOperationException("cancelBooking noch nicht implementiert");
    }

    /**
     * Findet alle Bookings eines Users (entweder als Owner oder als Sitter).
     *
     * @param userId Die User-ID
     * @return Liste aller Bookings, in denen dieser User involviert ist
     */
    public List<String> getBookings() {
        return Collections.emptyList();
    }
}
