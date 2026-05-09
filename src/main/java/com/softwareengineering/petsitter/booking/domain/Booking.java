package com.softwareengineering.petsitter.booking.domain;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.pet.domain.Pet;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Booking Entity – die tatsächliche Buchung einer Betreuung nach Acceptance eines Requests.
 *
 * <p>Ein Booking entsteht AUTOMATISCH, wenn ein OfferRequest akzeptiert wird.
 * Es definiert die konkrete Vereinbarung zwischen Owner und Sitter:
 * - Wer (Owner, Sitter)
 * - Welches Pet wird betreut
 * - Wann (startDate, endDate)
 * - Wieviel (pricePerWeek)
 *
 * <p>Lifecycle:
 * 1. Owner erstellt OWNER_OFFER  → Status = OPEN
 * 2. Sitter erstellt Request → Status = PENDING
 * 3. Owner akzeptiert Request via {@link com.softwareengineering.petsitter.booking.service.BookingService#acceptRequest}
 * 4. → Booking wird automatisch erstellt (Status = CREATED)
 * 5. → Offer wird BOOKED (locked)
 * 6. → andere Requests werden DENIED
 * 7. User kann Booking später stornieren → Status = CANCELLED
 * 8. Nach Ablauf → Status = COMPLETED (manuell oder auto)
 *
 * <p>Wichtig: 1:1 Verhältnis zwischen Offer und Booking!
 * Unique Constraints auf offer_id und accepted_request_id garantieren das.
 *
 * @see com.softwareengineering.petsitter.booking.service.BookingService#acceptRequest
 * @see com.softwareengineering.petsitter.offer.domain.Offer
 * @see com.softwareengineering.petsitter.offerrequest.domain.OfferRequest
 */
@Entity
@Table(name = "bookings")
public class Booking {

    /**
     * Eindeutige ID als UUID. Auto-Generiert bei Erstellung.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Das übergeordnete Offer – 1:1 Verhältnis.
     * unique = true: Nur ein Booking pro Offer!
     * Wird gesetzt, wenn Request von Offer-Creator akzeptiert wird.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false, unique = true)
    private Offer offer;

    /**
     * Der akzeptierte Request – 1:1 Verhältnis.
     * unique = true: Jeder Request kann maximal einmal akzeptiert werden.
     * Dies ist der Request, der dieses Booking erzeugt hat.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accepted_request_id", nullable = false, unique = true)
    private OfferRequest acceptedRequest;

    /**
     * Der Owner (Tierhalter), der das Offer ursprünglich erstellt hat.
     * Bei einem Owner-Offer: ist dies der Pet-Besitzer.
     * Bei einem Sitter-Offer: ist dies der Requester (Owner, der anfragte).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Der Sitter, der den Request akzeptiert wurde / akzeptiert hat.
     * Bei einem Owner-Offer: ist dies der Requester (Sitter).
     * Bei einem Sitter-Offer: ist dies der Creator (Sitter).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sitter_id", nullable = false)
    private User sitter;

    /**
     * Das Haustier, das betreut wird – NULLABLE.
     * Bei Owner-Offer: wird vom Offer's pet übernommen.
     * Bei Sitter-Offer anfänglich null: wird beim Booking-Accept bekannt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    /**
     * Start-Datum der Betreuung (einschließlich).
     * Wird vom Offer übernommen.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * End-Datum der Betreuung (einschließlich).
     * Wird vom Offer übernommen.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Preis pro Woche für die Betreuung in EUR.
     * Wird vom Offer übernommen (z.B. 50.00).
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerWeek;

    /**
     * Status des Bookings.
     * - CREATED: Gerade erzeugt (bei Request-Acceptance)
     * - CANCELLED: Von Owner oder Sitter storniert
     * - COMPLETED: Nach Ablauf (manuell oder auto)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    /**
     * Zeitstempel der Erstellung – wird automatisch beim Speichern gesetzt.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Booking() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public OfferRequest getAcceptedRequest() {
        return acceptedRequest;
    }

    public void setAcceptedRequest(OfferRequest acceptedRequest) {
        this.acceptedRequest = acceptedRequest;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getSitter() {
        return sitter;
    }

    public void setSitter(User sitter) {
        this.sitter = sitter;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPricePerWeek() {
        return pricePerWeek;
    }

    public void setPricePerWeek(BigDecimal pricePerWeek) {
        this.pricePerWeek = pricePerWeek;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

