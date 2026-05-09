package com.softwareengineering.petsitter.offerrequest.domain;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * OfferRequest Entity – repräsentiert eine Anfrage/Request auf ein bestehendes Offer.
 *
 * <p>Szenarios:
 * <ul>
 *   <li>Sitter sieht ein OWNER_OFFER (z.B. "Anna sucht Hundebetreuer") → erstellt Request</li>
 *   <li>Owner sieht ein SITTER_OFFER (z.B. "Ben bietet Betreuung an") → erstellt Request</li>
 * </ul>
 *
 * <p>Zustand-Übergänge:
 * <ul>
 *   <li>PENDING (Initial) → ACCEPTED oder DENIED</li>
 *   <li>ACCEPTED → (wird zum Booking)</li>
 *   <li>DENIED → (von anderen Requests bei Acceptance oder Offer-Edit)</li>
 *   <li>CANCELLED (von Requester selbst geändert)</li>
 * </ul>
 *
 * <p>Wichtige Businessregel (Unique Constraint):
 * Pro (offer_id, requester_id) Kombination darf es nur EINEN Request geben.
 * Das verhindert Duplikate und Verwirrung.
 *
 * <p>Lifecycle:
 * 1. User erstellt Request via {@link com.softwareengineering.petsitter.offerrequest.service.RequestService#createRequest}
 * 2. Request ist PENDING
 * 3. Offer-Creator sieht alle PENDING Requests
 * 4. Creator akzeptiert einen Request via {@link com.softwareengineering.petsitter.booking.service.BookingService#acceptRequest}
 * 5. → Request wird ACCEPTED, Booking entsteht, andere werden DENIED
 *
 * @see com.softwareengineering.petsitter.offer.domain.Offer
 * @see com.softwareengineering.petsitter.booking.domain.Booking
 */
@Entity
@Table(
        name = "requests",
        uniqueConstraints = @UniqueConstraint(name = "uk_offer_requester", columnNames = {"offer_id", "requester_id"})
)
public class OfferRequest {

    /**
     * Eindeutige ID. Auto-Increment Primary Key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Das Offer, auf das dieser Request bezieht.
     * LAZY loading für Performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    /**
     * Der User, der diesen Request erstellt hat (will das Offer nutzen).
     * LAZY loading.
     * Constraint: requester.id != offer.creator.id (wird im Service geprüft)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /**
     * Status des Requests.
     * - PENDING: Gerade erstellt, Offer-Creator hat noch nicht reagiert
     * - ACCEPTED: Von Creator akzeptiert → Booking entsteht
     * - DENIED: Abgelehnt (entweder explicit oder bei Acceptance anderer Requests)
     * - CANCELLED: Von Requester selbst gelöscht
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    /**
     * Optional: Nachricht des Requesters an den Creator.
     * Beispiel: "Ich bin sehr zuverlässig und hatte 5+ Jahre Erfahrung mit Hunden!"
     * Max 1000 Zeichen.
     */
    @Column(length = 1000)
    private String message;

    /**
     * Zeitstempel der Erstellung – wird automatisch beim Speichern gesetzt.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Zeitstempel der letzten Änderung – wird automatisch beim Update gesetzt.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public OfferRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

