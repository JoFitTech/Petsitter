package com.softwareengineering.petsitter.pet.domain;

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
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Pet Entity – repräsentiert ein Haustier.
 *
 * <p>Ein Pet gehört genau einem Owner (User).
 * Es wird referenziert von OWNER_OFFERs (der Owner sucht Betreuung für sein Pet).
 * Sitter Offers referenzieren kein Pet (generische Betreuung).
 *
 * <p>Verwendung:
 * 1. User (Owner) erstellt seinen Pet via {@link com.softwareengineering.petsitter.pet.service.PetService}
 * 2. Owner erstellt OWNER_OFFER für sein Pet
 * 3. Sitter sieht das Offer mit Pet-Details (Name, Species, etc.)
 * 4. Sitter erstellt Request mit Pet-Info
 * 5. Bei Acceptance entsteht Booking mit Pet-Details
 *
 * @see com.softwareengineering.petsitter.user.domain.User
 * @see com.softwareengineering.petsitter.offer.domain.Offer
 */
@Entity
@Table(name = "pet")
public class Pet {

    /**
     * Eindeutige ID als UUID. Auto-Generiert bei Erstellung.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Der Owner (Besitzer) dieses Pets – NICHT NULL.
     * Ein Pet kann nur einem Owner gehören.
     * LAZY loading für Performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Name des Haustiers (z.B. "Balu", "Mila", "Nino").
     */
    @Column(nullable = false)
    private String name;

    /**
     * Art des Haustiers (DOG, CAT, BIRD, RABBIT, OTHER).
     * Wird für Matching und Filter verwendet.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSpecies species;

    /**
     * Rasse (optional, z.B. "Golden Retriever", "Perser", "Kaninchen").
     */
    private String breed;

    /**
     * Alter des Pets in Jahren (optional).
     */
    private Integer age;

    /**
     * Notizen des Owners (z.B. "Sehr aktiv!", "Allergie gegen Hühnchen").
     * Max 1000 Zeichen.
     */
    @Column(length = 1000)
    private String notes;

    public Pet() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PetSpecies getSpecies() {
        return species;
    }

    public void setSpecies(PetSpecies species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
