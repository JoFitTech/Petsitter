package com.softwareengineering.petsitter.auth.service;

import com.softwareengineering.petsitter.user.domain.LoginCode;
import com.softwareengineering.petsitter.user.repository.LoginCodeRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LoginCodeService – Core-Logic für passwortlose Email+Code-Authentifizierung.
 *
 * <p><b>Was macht die Klasse?</b>
 * Verwaltet den kompletten Lifecycle von Einmalcodes:
 * 1. Code-Generierung (6 zufällige Ziffern)
 * 2. Sicheres Hashing (BCrypt)
 * 3. DB-Speicherung mit Expiry-Zeit (10 Min)
 * 4. Validierung bei Login
 * 5. Rate-Limiting (max 3 Versuche)
 * 6. Cleanup alter Codes
 *
 * <p><b>Wie macht sie das?</b>
 * - `requestLoginCode(email, ip)`:
 *   - Alte Codes für Email invalidieren
 *   - Neuen Code generieren (zufällig 6 Stellen)
 *   - BCrypt hashen (nie Plaintext speichern!)
 *   - In LoginCode Entity speichern mit Expiry = now+10Min
 *   - LoginCodeMailService aufrufen (Mail/Log versenden)
 * - `validateLoginCode(email, code)`:
 *   - Code aus DB laden
 *   - Prüfe: noch gültig? nicht abgelaufen? nicht bereits verwendet?
 *   - Prüfe: zu viele Versuche? (Rate-Limit bei 3)
 *   - BCrypt-Vergleich: eingegeben == gehashed?
 *   - Falls erfolgreich: Code markieren als "used"
 * - `cleanupExpiredCodes()`: alte/verwendete Codes löschen
 *
 * <p><b>Warum brauchen wir sie?</b>
 * - Zentrale Security-Logik (SingleResponsibility)
 * - Code wird NIEMALS als Plaintext gespeichert
 * - Jeder Code ist 10 Minuten gültig (Time-Limited-Token)
 * - Rate-Limiting verhindert Brute-Force (max 3 Versuche)
 * - Cleanup verhindert DB-Bloat
 * - Transaktionale Konsistenz (kein Race Condition)
 *
 * <p><b>Sicherheits-Features:</b>
 * - ✓ BCrypt-Hash (nicht revertierbar)
 * - ✓ SecureRandom für Code-Generierung
 * - ✓ Attempt-Counting (gegen Brute-Force)
 * - ✓ IP-Audit (requestIp gespeichert)
 * - ✓ Transactional (ACID)
 * - ✓ Masked Telemetry (Code nicht in Logs geloggt!)
 *
 * <p><b>Wichtig:</b>
 * - Code wird NICHT returniert (void requestLoginCode)!
 * - Code wird nur per Mail versendet
 * - Mail-Service in DEV: Logs mit Maskierung (123***)
 * - Mail-Service in PROD: SMTP mit Plaintext (hidden in Email, nicht in Logs)
 *
 * <p><b>Use-Case Beispiel:</b>
 * ┌─ User gibt Email "anna@example.de" ein
 * ├─ LoginCodeService.requestLoginCode("anna@example.de", "192.168.1.1")
 * │  ├─ Code "734821" generieren
 * │  ├─ Hash speichern: "$2a$10$..." (BCrypt)
 * │  └─ LoginCodeMailService.sendLoginCode() (logs: "734***")
 * ├─ User sieht Email: "Ihr Code: 734821"
 * ├─ User gibt Code "734821" ein
 * ├─ LoginCodeService.validateLoginCode("anna@example.de", "734821")
 * │  ├─ Code aus DB laden
 * │  ├─ BCrypt.matches("734821", "$2a$10...") → true
 * │  ├─ Code markieren: usedAt = now
 * │  └─ return true
 * └─ AuthService erstellt Session
 */
@Service
public class LoginCodeService {

    private static final Logger log = LoggerFactory.getLogger(LoginCodeService.class);

    private static final int CODE_LENGTH = 6;
    private static final int CODE_VALIDITY_MINUTES = 10;

    private final LoginCodeRepository loginCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginCodeMailService mailService;

    public LoginCodeService(
            LoginCodeRepository loginCodeRepository,
            PasswordEncoder passwordEncoder,
            LoginCodeMailService mailService
    ) {
        this.loginCodeRepository = loginCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    /**
     * Generiert einen neuen Code, speichert ihn und versendet per Mail.
     *
     * <p><b>Ablauf:</b>
     * 1. Alte, ungenutzte Codes für diese Email invalidieren (setzen usedAt=now)
     *    - Grund: nur 1 aktiver Code pro Email gleichzeitig
     * 2. Neuen 6-stelligen Plaintext-Code generieren (z.B. "734821")
     * 3. BCrypt hashen → speichern in DB
     * 4. ExpiresAt setzen: jetzt+10 Minuten
     * 5. IP-Adresse speichern (Audit)
     * 6. Mail versenden via LoginCodeMailService
     *    - Eine Kopie geht per Mail an den User
     * 7. Log (masked): "Code für anna@example.de versendet (123***)"
     *
     * <p><b>Post-Condition:</b>
     * - Genau 1 gültiger Code in DB für anna@example.de
     * - Code ist 10 Minuten gültig
     * - Attempt-Counter: 0 (noch keine Versuche)
     * - used_at: null (noch nicht verwendet)
     *
     * <p><b>Transactional:</b>
     * Alle DB-Operationen sind atomic. Sollte Mail-Versand fehlschlagen:
     * TX wird rolled back, Code ist nicht mehr gültig.
     *
     * @param email Target Email-Adresse
     * @param requestIp IP-Adresse des Anfragenden (Audit)
     */
    @Transactional
    public void requestLoginCode(String email, String requestIp) {
        log.info("Login-Code angefordert für: {}", email);

        // Alte Codes für diese Email invalidieren (nur 1 aktiver Code pro Email)
        List<LoginCode> oldCodes = loginCodeRepository.findByEmailAndUsedAtIsNull(email);
        oldCodes.forEach(code -> code.setUsedAt(LocalDateTime.now()));
        loginCodeRepository.saveAll(oldCodes);

        // Neuen Code generieren
        String plainCode = generateCode();
        String codeHash = passwordEncoder.encode(plainCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);

        LoginCode loginCode = new LoginCode(email, codeHash, expiresAt, requestIp);
        loginCodeRepository.save(loginCode);

        // Mail senden (Log-Fallback in Dev)
        // SICHERHEIT: Code wird NICHT returniert! Nur masked telemetry
        mailService.sendLoginCode(email, plainCode);

        // Log enthält KEIN Plaintext des Codes
        log.info("Login-Code für {} generiert und versendet (expires: {})", email, expiresAt);
    }

    /**
     * Validiert einen Plaintext-Code gegen den gehashten Code in der DB.
     *
     * <p><b>Ablauf:</b>
     * 1. Code aus DB laden: `findFirstBy...orderByCreatedAtDesc` (neuester, gültiger)
     * 2. Falls nicht gefunden → return false
     * 3. Prüfe: isRateLimited() (attempts < 3?) → return false
     * 4. Prüfe: isExpired() (expiresAt > now?) → return false
     * 5. BCrypt-Match: passwordEncoder.matches(plainCode, codeHash)
     *    - Falls mismatch: attempts++, save, return false
     * 6. Falls erfolgreich: usedAt=now, return true
     *
     * <p><b>Rate-Limiting:</b>
     * - Code speichert `attempts`-Feld
     * - Nach 3 falschen Versuchen: Code wird deniert
     * - User muss neu Code anfordern
     *
     * <p><b>Security-Gewinn:</b>
     * - BCrypt.matches ist timing-safe (gegen Timing Attacks)
     * - Attempt-Counting schützt vor Brute-Force
     * - Expiry verhindert, dass alter Code reused wird
     * - Used-Flag verhindert mehrfache Verwendung (Code einige 1x)
     *
     * @param email User Email
     * @param plainCode Der vom User eingegebene Code (z.B. "734821")
     * @return true wenn gültig, false sonst
     */
    @Transactional
    public boolean validateLoginCode(String email, String plainCode) {
        log.info("Login-Code Validierung für: {}", email);

        Optional<LoginCode> optionalCode = loginCodeRepository.findFirstByEmailAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                email,
                LocalDateTime.now()
        );

        if (optionalCode.isEmpty()) {
            log.warn("Kein gültiger Code gefunden für {}", email);
            return false;
        }

        LoginCode code = optionalCode.get();

        // Prüfe Rate-Limit
        if (code.isRateLimited()) {
            log.warn("Rate-Limit überschritten für {}", email);
            return false;
        }

        // Prüfe Ablauf
        if (code.isExpired()) {
            log.warn("Code abgelaufen für {}", email);
            return false;
        }

        // Prüfe Hash
        boolean matches = passwordEncoder.matches(plainCode, code.getCodeHash());
        if (!matches) {
            code.setAttempts(code.getAttempts() + 1);
            loginCodeRepository.save(code);
            log.warn("Falscher Code eingegeben für {} (Versuch: {})", email, code.getAttempts());
            return false;
        }

        // Code erfolgreich validiert -> markieren als benutzt
        code.setUsedAt(LocalDateTime.now());
        loginCodeRepository.save(code);

        log.info("Login-Code erfolgreich validiert für: {}", email);
        return true;
    }

    /**
     * Generiert einen zufälligen 6-stelligen numerischen Code.
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Hilfsmethode: Cleanup abgelaufener und verwendeter Codes.
     *
     * <p><b>Warum?</b>
     * - LoginCode-Tabelle kann ansonsten unwallständig wachsen
     * - Nach sauberem Logout: Code sollte gelöscht oder als used markiert werden
     * - Alte Codes (älter 1 Stunde): DB Platzverschwendung
     *
     * <p><b>Aufgerufen:</b>
     * - Manuell via Scheduled Task (würde @Scheduled(fixedRate=3600000) sein)
     * - Ab Phase 2: automatisch täglich
     *
     * <p><b>Löschkriterien:</b>
     * - expiresAt < now (abgelaufen)
     * - OR: createdAt < now-1h AND usedAt IS NOT NULL (alte, verwendete Codes)
     *
     * @see org.springframework.scheduling.annotation.Scheduled
     */
    @Transactional
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        loginCodeRepository.deleteExpiredCodes(now, oneHourAgo);
        log.debug("Cleanup abgelaufener Login-Codes durchgeführt");
    }
}

