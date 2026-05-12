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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Offer Entity - zentrale Entity fuer Betreuungsangebote.
 */
@Entity
@Table(name = "offer")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "offer_id", nullable = false)
    private UUID offerId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "create_user", nullable = false)
    private User createUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "update_user", nullable = false)
    private User updateUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(name = "title", length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 32)
    private OfferFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "care_type", length = 64)
    private OfferCareType careType;

    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", length = 32)
    private OfferAnimalType animalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false)
    private OfferType offerType;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OfferStatus status;

    public Offer() {
    }

    public UUID getOfferId() {
        return offerId;
    }

    public void setOfferId(UUID offerId) {
        this.offerId = offerId;
    }

    public UUID getId() {
        return offerId;
    }

    public void setId(UUID id) {
        this.offerId = id;
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

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getCreatedAt() {
        return createDate;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createDate = createdAt;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updateDate;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updateDate = updatedAt;
    }

    public User getCreateUser() {
        return createUser;
    }

    public void setCreateUser(User createUser) {
        this.createUser = createUser;
    }

    public User getCreator() {
        return createUser;
    }

    public void setCreator(User creator) {
        this.createUser = creator;
    }

    public User getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(User updateUser) {
        this.updateUser = updateUser;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OfferFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(OfferFrequency frequency) {
        this.frequency = frequency;
    }

    public OfferCareType getCareType() {
        return careType;
    }

    public void setCareType(OfferCareType careType) {
        this.careType = careType;
    }

    public OfferAnimalType getAnimalType() {
        return animalType;
    }

    public void setAnimalType(OfferAnimalType animalType) {
        this.animalType = animalType;
    }

    public OfferType getOfferType() {
        return offerType;
    }

    public void setOfferType(OfferType offerType) {
        this.offerType = offerType;
    }

    public OfferType getType() {
        return offerType;
    }

    public void setType(OfferType type) {
        this.offerType = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPricePerWeek() {
        return price;
    }

    public void setPricePerWeek(BigDecimal pricePerWeek) {
        this.price = pricePerWeek;
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

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createDate == null) {
            createDate = now;
        }
        if (updateDate == null) {
            updateDate = now;
        }
        if (updateUser == null) {
            updateUser = createUser;
        }
    }

    @PreUpdate
    void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
