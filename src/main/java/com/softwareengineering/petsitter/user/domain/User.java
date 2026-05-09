package com.softwareengineering.petsitter.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * User Entity – repräsentiert einen Benutzer im Petsitter-System.
 *
 * <p>Ein User kann gleichzeitig in den Rollen "Tierhalter" (Owner) und "Tiersitter" (Sitter) tätig sein.
 * Die Rollen werden nicht durch separate Entities definiert, sondern ergeben sich aus der Nutzung:
 * - User mit Haustieren und Owner Offers → Owner
 * - User mit Sitter Offers → Sitter
 *
 * <p>Verantwortlichkeiten:
 * - Speichert Authentifizierungsdaten (Email, Password-Hash)
 * - Speichert Kontaktdaten (Name, Phone, City)
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
     * Eindeutige ID. Auto-Increment Primary Key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email-Adresse – Unique, Login-ID, wird nicht geändert.
     * Validierung: Muss unique sein und gültige E-Mail-Syntax haben (später im Service).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt-Hash des Passworts. NIEMALS Plaintext!
     * Format: $2a$10$... (BCrypt mit 10 Rounds)
     */
    @Column(nullable = false)
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
     * Telefonnummer – optional, für Kontakt mit Bushmännern.
     */
    private String phone;

    /**
     * Stadt – wird für Matching-Logik verwendet (z.B. "Vienna", "Berlin").
     * Optional: Sitter kann in mehreren Städten tätig sein.
     */
    private String city;

    /**
     * Zeitstempel der Erstellung – wird automatisch beim Speichern gesetzt.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

