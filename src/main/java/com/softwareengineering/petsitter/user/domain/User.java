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
import java.time.LocalDate;
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
@Table(name = "`user`")
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
     * Wird für Passwort-Login als BCrypt-Hash gespeichert.
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
     * Öffentlicher Anzeigename im Profil.
     */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /**
     * Telefonnummer – optional, für Kontaktaufnahme.
     */
    private String phone;

    /**
     * Geburtsdatum – optionaler Profilbestandteil.
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Nationalität – optionaler Profilbestandteil.
     */
    @Column(length = 100)
    private String nationality;

    /**
     * Gesprochene Sprache für das öffentliche Profil.
     */
    @Column(nullable = false, length = 80)
    private String language = "deutsch";

    /**
     * Freitext für die Profilbeschreibung.
     */
    @Column(length = 1000)
    private String bio;

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
     * Land der Hauptadresse.
     */
    @Column(nullable = false, length = 100)
    private String country = "Deutschland";

    /**
     * Noch nicht bestätigte neue E-Mail-Adresse.
     */
    @Column(name = "pending_email")
    private String pendingEmail;

    /**
     * Zeitpunkt der letzten E-Mail-Änderungsanforderung.
     */
    @Column(name = "pending_email_requested_at")
    private LocalDateTime pendingEmailRequestedAt;

    /**
     * Technische Account-Rolle für Login und Berechtigungen.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_role", nullable = false)
    private AccountRole accountRole;

    /**
     * Verifikationsstatus des Accounts.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.VERIFIED;

    /**
     * Zeitstempel der Erstellung - wird automatisch beim Speichern gesetzt.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Zeitpunkt, ab dem ein unverifizierter Account gelöscht werden darf.
     */
    @Column(name = "delete_after")
    private LocalDateTime deleteAfter;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPendingEmail() {
        return pendingEmail;
    }

    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }

    public LocalDateTime getPendingEmailRequestedAt() {
        return pendingEmailRequestedAt;
    }

    public void setPendingEmailRequestedAt(LocalDateTime pendingEmailRequestedAt) {
        this.pendingEmailRequestedAt = pendingEmailRequestedAt;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeleteAfter() {
        return deleteAfter;
    }

    public void setDeleteAfter(LocalDateTime deleteAfter) {
        this.deleteAfter = deleteAfter;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (accountStatus == null) {
            accountStatus = AccountStatus.VERIFIED;
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = firstName;
        }
        if (language == null || language.isBlank()) {
            language = "deutsch";
        }
        if (country == null || country.isBlank()) {
            country = "Deutschland";
        }
    }
}
