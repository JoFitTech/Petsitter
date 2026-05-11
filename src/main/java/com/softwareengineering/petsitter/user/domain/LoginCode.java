package com.softwareengineering.petsitter.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LoginCode Entity - repräsentiert einen temporären Code für die Registrierungsbestätigung.
 *
 * <p>Verantwortlichkeiten:
 * - Speichert gehashte Codes (nie Plaintext!)
 * - Verfolgt Ablaufzeiten
 * - Zählt Versuche (für Rate-Limiting)
 * - Blockiert wiederholte Nutzung (used_at-Timestamp)
 * - Speichert IP für Sicherheitsaudit
 *
 * <p>Sicherheit:
 * - Code wird als BCrypt-Hash gespeichert
 * - Alte Codes werden nach 1 Stunde automatisch gelöscht (via Service/Schedule)
 * - Rate-Limit: max 3 Versuche pro Code
 */
@Entity
@Table(name = "login_code")
public class LoginCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Email-Adresse – wird für Lookup verwendet.
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * BCrypt-Hash des 6-stelligen Codes.
     */
    @Column(nullable = false, length = 255)
    private String codeHash;

    /**
     * Zeitpunkt, wann der Code abläuft.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Zeitpunkt, wann der Code verwendet wurde (null = noch nicht benutzt).
     */
    @Column
    private LocalDateTime usedAt;

    /**
     * Zähler für fehlgeschlagene Versuche (zum Rate-Limiting).
     */
    @Column(nullable = false)
    private int attempts = 0;

    /**
     * IP-Adresse des Anfragenden (für Audit/Sicherheit).
     */
    @Column(length = 45)
    private String requestIp;

    /**
     * Erstellungszeitstempel.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public LoginCode() {
    }

    public LoginCode(String email, String codeHash, LocalDateTime expiresAt, String requestIp) {
        this.email = email;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.requestIp = requestIp;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getter & Setter
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

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Prüft, ob der Code noch gültig ist (nicht abgelaufen, noch nicht verwendet, unter 3 Versuchen).
     */
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt)
                && usedAt == null
                && attempts < 3;
    }

    /**
     * Prüft, ob der Code abgelaufen ist.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Prüft, ob der Code bereits verwendet wurde.
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Prüft, ob zu viele Versuche unternommen wurden.
     */
    public boolean isRateLimited() {
        return attempts >= 3;
    }
}
