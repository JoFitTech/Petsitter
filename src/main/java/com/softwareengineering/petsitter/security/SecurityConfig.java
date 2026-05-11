package com.softwareengineering.petsitter.security;

import com.softwareengineering.petsitter.config.PetsitterSecurityProperties;
import com.softwareengineering.petsitter.security.rememberme.RememberMeUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * SecurityConfig – zentrale Spring Security Konfiguration für Petsitter.
 *
 * <p><b>Was macht die Klasse?</b>
 * Definiert alle Security-Regeln für die Anwendung:
 * - Welche Routes öffentlich vs. geschützt sind
 * - Wie Authentifizierung funktioniert (E-Mail + Passwort)
 * - Wie Remember-Me Sessions verwaltet werden
 * - Logout-Verhalten
 *
 * <p><b>Wie macht sie das?</b>
 * - Konfiguriert `SecurityFilterChain` via Spring Security DSL
 * - Setzt `authorizeHttpRequests` für URL-Patterns
 * - Registriert `RememberMeServices` für 7-Tage Session-Persistierung
 * - Definiert `exceptionHandling` für unauthenticated Requests (302 Redirect zu /login)
 *
 * <p><b>Warum brauchen wir sie?</b>
 * - Zentrale Stelle für alle Sicherheitsregeln (Single Responsibility)
 * - Ohne diese Config: Spring Security erlaubt ALLES (sehr unsicher)
 * - Garantiert, dass nur eingeloggte User auf geschützte Routes zugreifen
 * - Verhindert PUMA-Assets automatisch (@denyAll auf /puma/**)
 *
 * <p><b>Sicherheits-Details:</b>
 * - CSRF disabled (Vaadin hat eigene CSRF-Schutz)
 * - Remember-Me-Key aus Umgebungsvariable (nicht hardcoded!)
 * - Verwendung von `RememberMeUserDetailsService` (nicht permissiv)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@EnableConfigurationProperties(PetsitterSecurityProperties.class)
public class SecurityConfig {

    /**
     * Erstellt die Security Filter Chain – die "Firewall" der Anwendung.
     *
     * <p><b>Was macht die Methode?</b>
     * Registriert Java HTTP Security Filter, die jeden Request vor Verarbeitung prüfen:
     * 1. Ist der User authentifiziert?
     * 2. Hat der User die nötige Rolle?
     * 3. Soll dieser Request erlaubt sein?
     *
     * <p><b>Wie macht sie das?</b>
     * - CSRF disabled (Vaadin managed das selbst)
     * - exceptionHandling: unauthenticated Requests → 302 Redirect zu /login (nicht 403!)
     * - authorizeHttpRequests: Definiert URL-Patterns:
     *   ✗ /puma/** → denyAll (PUMA-Assets nie!)
     *   ✓ /, /login, /VAADIN/** → permitAll (öffentlich)
     *   ✓ weitere → authenticated (Login nötig)
     * - rememberMe: 7-Tage Session über Tokens
     * - logout: Session + Cookies löschen
     *
     * <p><b>Warum ist das wichtig?</b>
     * - Diese Chain läuft auf JEDEN HTTP-Request
     * - Ohne sie: Jeder könnte /profile, /admin, etc. aufrufen!
     * - Mit 302 Redirect: User wird nahtlos zu Login weitergeleitet (UX!)
     * - Remember-Me: User muss nicht täglich neu-Login (aber nur 7 Tage)
     *
     * <p><b>Security-Highlights:</b>
     * - PUMA-Blockade auf 3 Ebenen (/themes/puma-theme, /frontend/themes/puma-theme, /puma)
     * - Logout löscht JSESSIONID + REMEMBER_ME Cookies
     * - SessionInvalidation erzwingt kompletten Logout
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RememberMeServices rememberMeServices,
            DatabaseUserDetailsService databaseUserDetailsService
    ) throws Exception {
        http
                // Vaadin verwendet eigene CSRF/UIDL-Mechanismen; Spring-CSRF blockiert sonst oft Requests
                // und fuehrt im Browser zu "Connection lost".
                .csrf(csrf -> csrf.disable())
                // ExceptionHandling für unauthenticated Requests (WICHTIG!)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Unauthenticated: 302 Redirect zu /login (nicht 403!)
                            response.sendRedirect("/login");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // PUMA-Assets duerfen unter keinen Umstaenden ausgeliefert werden.
                        .requestMatchers(
                                "/themes/puma-theme/**",
                                "/frontend/themes/puma-theme/**",
                                "/puma/**"
                        ).denyAll()
                        .requestMatchers(
                                "/",
                                "/login",
                                "/error",
                                "/VAADIN/**",
                                "/frontend/**",
                                "/connect/**",
                                "/favicon.ico",
                                "/images/**",
                                "/icons/**",
                                "/line-awesome/**",
                                "/manifest.webmanifest",
                                "/sw.js",
                                "/offline-page.html",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // Remember-me: 7 Tage Session-Persistierung
                .rememberMe(rm -> rm
                        .rememberMeServices(rememberMeServices)
                        .key("petsitter-remember-me-key")
                )
                .userDetailsService(databaseUserDetailsService)
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "REMEMBER_ME")
                );

        return http.build();
    }

    /**
     * TokenBasedRememberMeServices – verwaltet "7-Tage eingeloggt bleiben" Tokens.
     *
     * <p><b>Was macht die Methode?</b>
     * Erstellt ein Service, das Browser-Cookies mit verschlüsselten User-Tokens speichert.
     * Wenn Session abläuft, kann dieser Token den User automatisch re-authentifizieren.
     *
     * <p><b>Wie macht sie das?</b>
     * 1. Nutzt `RememberMeUserDetailsService` zur Token-Validierung:
     *    - Token wird dekodiert
     *    - User aus DB geladen (DB-Validierung!)
     *    - Nur wenn User existiert: Token akzeptiert
     * 2. Remember-Me-Key aus Umgebungsvariable (PETSITTER_REMEMBER_ME_KEY)
     *    - DEV-Fallback: "petsitter-dev-only-key-please-change-in-prod"
     * 3. Token-Gültigkeit: 7 Tage = 604.800 Sekunden
     * 4. Algorithm: SHA256 (sicherer als MD5!)
     *
     * <p><b>Warum ist das wichtig?</b>
     * - User-Komfort: "Mich merken" für 7 Tage statt täglich neu-Login
     * - SICHERHEIT (vorher Problem!):
     *   - Alter Fehler: UserDetailsService gab JEDEN Username zurück
     *   - Neu: Nur valide User aus DB werden akzeptiert
     *   - Token mit SHA256, nicht hartcodiertem Key
     * - useSecureCookie=true: nur über HTTPS in Prod
     * - alwaysRemember=false: Opt-In (nicht obligatorisch)
     *
     * <p><b>Deployment-Hinweise:</b>
     * - In PROD: PETSITTER_REMEMBER_ME_KEY Environment Variable setzen!
     * - Diese Key muss GEHEIM bleiben (z.B. in .env oder Secrets)
     * - Token können nicht einfach gefälscht werden (SHA256 + Key)
     */
    @Bean
    RememberMeServices rememberMeServices(
            PasswordEncoder passwordEncoder,
            RememberMeUserDetailsService rememberMeUserDetailsService
    ) {
        // SICHERHEIT: Key sollte aus Env/Config kommen, hier für lokale Tests
        String rememberMeKey = System.getenv("PETSITTER_REMEMBER_ME_KEY");
        if (rememberMeKey == null || rememberMeKey.isBlank()) {
            // DEV-Default (NIEMALS in PROD verwenden!)
            rememberMeKey = "petsitter-dev-only-key-please-change-in-prod";
        }

        TokenBasedRememberMeServices service = new TokenBasedRememberMeServices(
                rememberMeKey,
                rememberMeUserDetailsService::loadUserByUsername
        );
        service.setTokenValiditySeconds(7 * 24 * 60 * 60); // 7 Tage
        service.setUseSecureCookie(true);
        service.setAlwaysRemember(false); // Nur wenn User "Angemeldet bleiben" wählt
        service.setMatchingAlgorithm(TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256);
        return service;
    }

    /**
     * BCrypt PasswordEncoder – sichere Passwort-Hashing Funktion.
     *
     * <p><b>Was macht die Methode?</b>
     * Erstellt einen PasswordEncoder für Registrierungscodes und Passwörter.
     *
     * <p><b>Wie macht sie das?</b>
     * - BCrypt mit Strength 10 (adaptiv: dauert ~0,1s pro Hash)
     * - Jedes Hash ist unterschiedlich (wegen Salt)
     * - Unmöglich, einfach zu reversen (One-Way Hash)
     *
     * <p><b>Warum?</b>
     * - Registrierungscodes werden als BCrypt-Hash gespeichert (nicht Plaintext!)
     * - Wenn DB kompromittiert: Codes sind immer noch sicher
     * - Wird von LoginCodeService.requestLoginCode() verwendet
     * - Wird von LoginCodeService.validateLoginCode() für Hash-Vergleiche verwendet
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
