package com.softwareengineering.petsitter.security.rememberme;

import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * RememberMeUserDetailsService – sichere User-Validierung für Remember-Me Tokens.
 *
 * <p><b>Was macht die Klasse?</b>
 * Lädt User-Details aus DB beim Validieren von Remember-Me Tokens.
 * Ist die Gegenseite zu `rememberMeServices()`: Token dekodiert → Username extrahiert → diese Klasse lädt User.
 *
 * <p><b>Wie macht sie das?</b>
 * 1. Token kommt vom Browser-Cookie: "REMEMBER_ME"
 * 2. Cookie wird dekodiert → Username extrahiert
 * 3. Diese Klasse: `loadUserByUsername(email)` wird aufgerufen
 * 4. User wird aus DB geladen via `userRepository.findByEmail(email)`
 * 5. Falls User existiert UND gültige Rolle hat → UserDetails zurückgeben
 * 6. Falls nicht → UsernameNotFoundException werfen (Token invalid)
 *
 * <p><b>Warum ist das wichtig?</b>
 * - SICHERHEIT (vorher kritisch!):
 *   - Alter Fehler: `UserDetailsService` im SecurityConfig gab JEDEN Username zurück!
 *   - Risiko: Mit bekanntem Key konnte man für beliebige Emails Token fälschen
 *   - Neu: Nur existierende User aus DB werden akzeptiert
 * - Verhindert Token-Hijacking:
 *   - Wenn User gelöscht wurde → Token funktioniert nicht mehr
 *   - Wenn Rolle entfernt wurde → Token funktioniert nicht mehr
 * - DB-Validierung auf JEDEM Remember-Me-Request
 *
 * <p><b>Unterschied zu DatabaseUserDetailsService?</b>
 * - `DatabaseUserDetailsService`: für normales Login (Email+Password-Form)
 * - `RememberMeUserDetailsService`: spezialisiert auf Token-Validierung
 * - Beide laden aus gleicher User-DB, aber für unterschiedliche Flows
 *
 * <p><b>Sicherheits-Checkliste:</b>
 * - ✓ Wirft UsernameNotFoundException, falls User nicht existiert
 * - ✓ Prüft, dass Rolle nicht null ist (keine bösen Defaults)
 * - ✓ Handhabt leere/null Passwörter defensiv
 * - ✓ Logged nicht sensitive Daten
 *
 * <p><b>Aufruf-Flow:</b>
 * Browser hat Remember-Me Cookie
 *   ↓ [SecurityFilter]
 * Token dekodieren → Username extrahieren
 *   ↓
 * rememberMeUserDetailsService.loadUserByUsername("anna@example.de")
 *   ↓ [diese Klasse]
 * User aus DB laden
 *   ↓
 * UserDetails zurückgeben → Session setzen
 *   ↓
 * User ist eingeloggt (ohne Passwort eingegeben zu haben)!
 */
@Service
public class RememberMeUserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(RememberMeUserDetailsService.class);

    private final UserRepository userRepository;

    public RememberMeUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lädt User-Details aus DB anhand seiner Email (Username für Remember-Me).
     *
     * <p><b>Aufruf-Kontext:</b>
     * Framework ruft diese Methode auf, wenn Remember-Me Token validiert wird:
     * 1. User-Browser hat "REMEMBER_ME" Cookie
     * 2. Spring Security dekodiert Token → extrahiert Email
     * 3. Diese Methode wird aufgerufen: loadUserByUsername(email)
     * 4. Returns: UserDetails-Objekt oder wirft UsernameNotFoundException
     *
     * <p><b>Implementierung:</b>
     * - Sucht User in DB per Email (als Unique-Key)
     * - Falls nicht gefunden → UsernameNotFoundException (Token invalid)
     * - Falls gefunden → prüft, dass Rolle gültig ist
     * - Falls Rolle null → Token invalid (Sicherheit!)
     * - Handhabt leere/null Passwörter defensiv
     *
     * <p><b>Warum diese Checks?</b>
     * - Verhindert, dass Admin-User with `null` Role akzeptiert werden
     * - Verhindert, dass gelöschte User weiter eingeloggt bleiben
     * - Leere Passwörter werden defensiv behandelt; Login selbst prüft Passwort und Accountstatus.
     *
     * @param username eigentlich Email-Adresse (Spring Security nutzt "username" als Key)
     * @return UserDetails für Spring Security
     * @throws UsernameNotFoundException falls User nicht existiert oder invalid
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("RememberMe-Token Validierung für: {}", username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User nicht gefunden: " + username));

        // Stelle sicher, dass Rolle gültig ist
        if (user.getAccountRole() == null) {
            log.warn("User {} hat keine Rolle! Token ungültig.", username);
            throw new UsernameNotFoundException("User-Rolle missing: " + username);
        }
        if (user.getAccountStatus() != AccountStatus.VERIFIED) {
            log.warn("User {} ist nicht verifiziert. Token ungültig.", username);
            throw new UsernameNotFoundException("User nicht verifiziert: " + username);
        }

        String password = user.getPasswordHash();
        if (password == null || password.isBlank()) {
            password = ""; // Passwortlose User OK
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(password)
                .roles(user.getAccountRole().name())
                .build();
    }
}
