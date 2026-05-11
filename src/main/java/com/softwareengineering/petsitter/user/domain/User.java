package com.softwareengineering.petsitter.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Entity – repräsentiert einen Benutzer im Petsitter-System.
 *
 * <p>Ein angemeldeter User hat eine technische Account-Rolle:
 * ADMIN oder SIGNED_IN_USER. Nicht angemeldete Besucher sind Guest-User,
 * werden aber nicht in dieser Tabelle gespeichert.
 *
 * <p>Verantwortlichkeiten:
 * - Speichert Authentifizierungsdaten (Email, Password-Hash)
 * - Speichert Profil- und Adressdaten
 * - Wird referenziert von Pet (als Owner), Offer (als Creator), OfferRequest (als Requester)
 *
 * <p>Sicherheit:
 * - Passwörter werden NIEMALS im Plaintext gespeichert, nur als BCrypt-Hash
 * - Email ist unique (Primary Identifier für Login)
 * - createdAt wird automatisch beim ersten Speichern gesetzt
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Eindeutige ID als UUID. Auto-Generiert bei Erstellung.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Email-Adresse – Unique, Login-ID, wird nicht geändert.
     * Validierung: Muss unique sein und gültige E-Mail-Syntax haben (später im Service).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt-Hash des Passworts. NIEMALS Plaintext!
     * Format: $2a$10$... (BCrypt mit 10 Rounds)
     *
     * Kann leer/null sein für passwortlose Authentifizierung (Email-Code-Flow).
     */
    @Column(nullable = true, columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String passwordHash;

    /**
     * Vorname des Users (z.B. "Anna", "Ben").
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * Nachname des Users.
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * Telefonnummer – optional, für Kontaktaufnahme.
     */
    private String phone;

    /**
     * Straße der Hauptadresse.
     */
    @Column(nullable = false)
    private String street;

    /**
     * Hausnummer der Hauptadresse.
     */
    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    /**
     * Postleitzahl der Hauptadresse.
     */
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    /**
     * Stadt – wird für Matching-Logik verwendet (z.B. "Vienna", "Berlin").
     */
    @Column(nullable = false)
    private String city;

    /**
     * Optionaler Adresszusatz wie Etage, Hinterhaus oder c/o.
     */
    @Column(name = "address_addition")
    private String addressAddition;

    /**
     * Technische Account-Rolle für Login und Berechtigungen.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_role", nullable = false)
    private AccountRole accountRole;

    /**
     * Zeitstempel der Erstellung – wird automatisch beim Speichern gesetzt.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddressAddition() {
        return addressAddition;
    }

    public void setAddressAddition(String addressAddition) {
        this.addressAddition = addressAddition;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
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
