package com.softwareengineering.petsitter.offer.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Offer Entity – zentrale Entity für Betreuungsangebot oder Betreuungssuche.
 *
 * <p>Ein Offer kann nach zwei Szenarien unterteilt werden:
 * <ul>
 *   <li><b>OWNER_OFFER:</b> Ein Tierhalter sucht einen Sitter für sein Haustier im Zeitraum [startDate, endDate].
 *       MUSS ein Pet haben. Wird von Sittern angesehen, die einen Request erstellen.</li>
 *   <li><b>SITTER_OFFER:</b> Ein Sitter bietet seine Dienste an. Kein Pet erforderlich (Pet wird bei Booking bekannt).
 *       Wird von Tierhaltern angesehen.</li>
 * </ul>
 *
 * <p>Geschäftsregeln (werden in {@link com.softwareengineering.petsitter.offer.service.OfferService} durchgesetzt):
 * <ul>
 *   <li>Nur der Creator darf sein Offer bearbeiten.</li>
 *   <li>OWNER_OFFER braucht zwingend ein Pet.</li>
 *   <li>startDate muss <= endDate sein.</li>
 *   <li>Booked Offers können nicht bearbeitet werden.</li>
 *   <li>Bei einer Änderung werden alle PENDING Requests auf DENIED gesetzt.</li>
 * </ul>
 *
 * <p>Zustand-Übergänge:
 * <ul>
 *   <li>OPEN (Initial) → BOOKED (wenn Request akzeptiert) → COMPLETED (nach Ablauf)</li>
 *   <li>OPEN → CANCELLED (manuell durch Creator)</li>
 * </ul>
 *
 * <p>Optimistic Locking: Das Feld {@link #version} ermöglicht sichere konkurrierende Änderungen.
 * Wenn zwei Clients gleichzeitig ein Offer aktualisieren, wirft JPA {@code OptimisticLockException}.
 *
 * @see com.softwareengineering.petsitter.offer.service.OfferService
 * @see com.softwareengineering.petsitter.offerrequest.domain.OfferRequest
 */
@Entity
@Table(name = "offers")
public class Offer {

    /**
     * Eindeutige ID. Auto-Increment Primary Key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Der User, der dieses Offer erstellt hat (Owner oder Sitter).
     * LAZY loading: Wird nur geladen, wenn gezielt aufgerufen.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * Typ des Offers: OWNER_OFFER oder SITTER_OFFER.
     * Bestimmt ob PT erforderlich ist und die Matching-Logik.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType type;

    /**
     * Das zu betreuende Haustier – NUR bei OWNER_OFFER gefüllt.
     * Bei SITTER_OFFER ist dies null. Das Pet wird bei Booking bekannt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    /**
     * Start-Datum des Betreuungszeitraums (einschließlich).
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * End-Datum des Betreuungszeitraums (einschließlich).
     * Validierung: Muss >= startDate sein.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Stadt, für die dieses Offer gilt (z.B. "Vienna", "Berlin", "Zürich").
     * Wird für Matching-Logik verwendet.
     */
    @Column(nullable = false)
    private String city;

    /**
     * Preis pro Woche in EUR. Optional. Beispiel: 50.00 EUR.
     * Keine Validierung auf Nullable, da auch Sitter keine Preise angeben müssen.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerWeek;

    /**
     * Beschreibung des Angebots (z.B. "Ich kümmere mich um Hunde und Katzen").
     * Max 1000 Zeichen.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Zustand des Offers.
     * - OPEN: Angebot aktiv, Requests möglich
     * - BOOKED: Ein Request wurde akzeptiert → kein Offer-Edit mehr
     * - CANCELLED: Von Creator gelöscht
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    /**
     * Version für Optimistic Locking.
     * Wird automatisch inkrementiert beim Update.
     * Schützt vor gleichzeitigen Änderungen desselben Offers
     * (wirft OptimisticLockException wenn zwei User gleichzeitig ändern).
     */
    @Version
    private Long version;

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

    public Offer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public OfferType getType() {
        return type;
    }

    public void setType(OfferType type) {
        this.type = type;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getPricePerWeek() {
        return pricePerWeek;
    }

    public void setPricePerWeek(BigDecimal pricePerWeek) {
        this.pricePerWeek = pricePerWeek;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

