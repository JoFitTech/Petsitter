package com.softwareengineering.petsitter.auth.service;

import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

/**
 * AuthService – zentrale Authentication-Logik für Email+Code Login.
 *
 * <p><b>Was macht die Klasse?</b>
 * Orchestriert den kompletten Login-Fluss:
 * 1. User anhand Email laden oder neu erstellen
 * 2. Code anfordern (Mail-Versand)
 * 3. Code validieren
 * 4. Session erstellen
 *
 * <p><b>Wie macht sie das?</b>
 * - `getOrCreateUser(email)`:
 *   - Sucht User in DB
 *   - Falls nicht gefunden: erstellt Placeholder-User mit Placeholder-Daten
 *   - Returns gespeicherten/neu erstellten User
 * - `requestCodeForEmail(email, ip)`:
 *   - Ruft `LoginCodeService.requestLoginCode()` auf
 *   - Code wird generiert, gehashed, versendet
 * - `verifyCodeAndGetUser(email, code)`:
 *   - Ruft `LoginCodeService.validateLoginCode()` auf
 *   - Falls gültig: User aus DB laden und returnieren
 *   - Falls ungültig: Optional.empty()
 * - `toUserDetails(user)`:
 *   - Konvertiert Domain-User zu Spring Security UserDetails
 *   - Wird für Session-Erstellung genutzt
 *
 * <p><b>Warum brauchen wir sie?</b>
 * - Zentrale Orchestrierung (Single Responsibility)
 * - Trennung von Security-Details (LoginCodeService) und UI-Logic (LoginView)
 * - Wiederverwendbar in REST-API + Vaadin
 * - Transaktionale Konsistenz
 *
 * <p><b>Design-Pattern:</b>
 * - Service Orchestrator: Koordiniert mehrere Lower-Level Services
 * - Repository Pattern: Datenzugriff abstrahiert
 * - Dependency Injection: Services werden injiziert
 *
 * <p><b>Phase 1 (Jetzt):</b>
 * - Passwortloses Email+Code Login
 * - User-Erstellung ohne CAPTCHA (TODO Phase 2!)
 *
 * <p><b>Phase 2 (Später):</b>
 * - CAPTCHA integrieren
 * - Rate-Limiting pro IP/Email
 * - Audit-Logging
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final LoginCodeService loginCodeService;

    public AuthService(UserRepository userRepository, LoginCodeService loginCodeService) {
        this.userRepository = userRepository;
        this.loginCodeService = loginCodeService;
    }

    /**
     * Lädt existierenden User oder erstellt neuen Placeholder.
     *
     * <p><b>Ablauf:</b>
     * 1. Suche User in DB per Email
     * 2. Falls gefunden: return User
     * 3. Falls nicht gefunden:
     *    - Erstelle neuen User mit Placeholder-Daten
     *    - Email: echte Email
     *    - FirstName/LastName: "" (leer)
     *    - Address: "" (leer)
     *    - PasswordHash: "" (passwortlos)
     *    - AccountRole: SIGNED_IN_USER (Standard)
     *    - Speichere in DB und return
     *
     * <p><b>Warum?</b>
     * - Ermöglicht registrierungsloses Signup
     * - User kann gleich nach Email-Validierung eingeloggt sein
     * - Profil kann später via ProfileView vervollständigt werden
     *
     * <p><b>Wichtig (Phase 2):</b>
     * - Ohne CAPTCHA/Throttle: DB-Flooding möglich!
     * - TODO: Rate-Limit pro IP (max 5 Signups/Stunde)
     * - TODO: CAPTCHA beim Signup
     *
     * @param email Email-Adresse des Users
     * @return existierender oder neu erstellter User
     */
    public User getOrCreateUser(String email) {
        String normalizedEmail = normalizeEmail(email);
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            log.info("User existiert: {}", normalizedEmail);
            return existingUser.get();
        }

        log.info("Erstelle neuen User: {}", normalizedEmail);
        User newUser = new User();
        newUser.setEmail(normalizedEmail);
        newUser.setFirstName(""); // Placeholder
        newUser.setLastName("");
        newUser.setStreet("");
        newUser.setHouseNumber("");
        newUser.setPostalCode("");
        newUser.setCity("");
        newUser.setAccountRole(com.softwareengineering.petsitter.user.domain.AccountRole.SIGNED_IN_USER);
        newUser.setPasswordHash(""); // Leerer Hash für passwortlose Auth

        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException ex) {
            // Parallel-Request hat denselben User ggf. gerade angelegt -> erneut lesen.
            return userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> ex);
        }
    }

    /**
     * Konvertiert einen Domain-User zu Spring Security UserDetails.
     *
     * <p><b>Aufruf:</b>
     * In LoginView.setSecurityContext() nach erfolgreichem Code-Validieren.
     *
     * <p><b>Konvertierung:</b>
     * Domain-User (JPA Entity):
     *   - email: String
     *   - passwordHash: String oder null/""
     *   - accountRole: enum (ADMIN/SIGNED_IN_USER)
     *
     * → Spring Security UserDetails:
     *   - username: email
     *   - password: passwordHash (oder "")
     *   - authorities: [ROLE_ADMIN] oder [ROLE_SIGNED_IN_USER]
     *
     * <p><b>Warum?</b>
     * - Spring Security kennt nur UserDetails Interface
     * - Domain-User ist DB-Entity, zyklisch, nicht serialisierbar
     * - UserDetails ist lightweight, serializable, für SessionSpeicherung
     *
     * <p><b>Sicherheit:</b>
     * - Falls passwordHash null/leer: "" wird gesetzt (sicher, passwortlos)
     * - Falls accountRole null: DEFAULT "SIGNED_IN_USER" (kein ADMIN!)
     *
     * @param user Domain User Entity
     * @return Spring SecurityUserDetails
     */
    public UserDetails toUserDetails(User user) {
        UserBuilder builder = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                .roles(user.getAccountRole() != null ? user.getAccountRole().name() : "SIGNED_IN_USER");

        return builder.build();
    }

    /**
     * Vollständiger Login-Fluss: Code anfordern.
     *
     * <p><b>Aufruf:</b>
     * In LoginView.onRequestCodeClicked() wenn User Email eingegeben hat.
     *
     * <p><b>Ablauf:</b>
     * 1. getOrCreateUser(email) – stelle sicher User existiert
     * 2. loginCodeService.requestLoginCode(email, ip) – Code generieren + versenden
     *
     * <p><b>Post-Condition:</b>
     * - User existiert in DB (oder wurde neu erstellt)
     * - Code wurde generiert, gehashed, in DB gespeichert
     * - Mail/Log wurde versendet
     * - User auf Frontend sieht: "Code versendet, prüfe deine Email"
     *
     * <p><b>TODO Phase 2:</b>
     * - CAPTCHA-Check hinzufügen
     * - Rate-Limit pro IP/Email implementieren
     *
     * @param email Email-Adresse des Users
     * @param clientIp IP-Adresse für Audit
     */
    public void requestCodeForEmail(String email, String clientIp) {
        String normalizedEmail = normalizeEmail(email);
        // Throttle (TODO Phase 2): Rate-Limit pro IP/Email
        getOrCreateUser(normalizedEmail); // Stelle sicher, dass User existiert
        loginCodeService.requestLoginCode(normalizedEmail, clientIp);
    }

    /**
     * Code validieren und User laden.
     *
     * <p><b>Aufruf:</b>
     * In LoginView.onVerifyCodeClicked() wenn User Code eingegeben hat.
     *
     * <p><b>Ablauf:</b>
     * 1. loginCodeService.validateLoginCode(email, code)
     *    - BCrypt-Vergleich
     *    - Rate-Limit-Check
     *    - Expiry-Check
     * 2. Falls ungültig: return Optional.empty()
     * 3. Falls gültig: User aus DB laden → return Optional.of(user)
     *
     * <p><b>Post-Condition:</b>
     * - Falls erfolgreich: User wird zurückgegeben
     * - Code ist als "used" markiert (kann nicht reused werden)
     *
     * <p><b>Integration mit LoginView:</b>
     * ```
     * Optional<User> userOpt = authService.verifyCodeAndGetUser(email, code);
     * if (userOpt.isPresent()) {
     *     User user = userOpt.get();
     *     setSecurityContext(user);  // Session erstellen
     *     ui.navigate("");            // Zur StartView
     * } else {
     *     showError("Code ungültig");
     * }
     * ```
     *
     * @param email Email-Adresse
     * @param code 6-stelliger Code (Plaintext, vom User eingegeben)
     * @return Optional<User> – User falls gültig, empty() falls ungültig
     */
    public Optional<User> verifyCodeAndGetUser(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        if (loginCodeService.validateLoginCode(normalizedEmail, code)) {
            return userRepository.findByEmail(normalizedEmail);
        }
        return Optional.empty();
    }

    /**
     * Vereinheitlicht Email-Adressen für konsistente Lookups und eindeutige User-Erstellung.
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
