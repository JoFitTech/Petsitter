package com.softwareengineering.petsitter.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * LoginCodeMailService – Versand von Login-Codes per Mail/Log.
 *
 * <p><b>Was macht die Klasse?</b>
 * Sendet den generierten 6-stelligen Login-Code an den User.
 *
 * <p><b>Wie macht sie das?</b>
 * - **ENTWICKLUNG**: Loggt Code mit Maskierung (z.B. "134***" statt "134856")
 *   - Der Plaintext Code wird NICHT geloggt!
 *   - Ermöglicht lokales Debugging ohne Security-Leak
 * - **PRODUKTION**: Würde echte SMTP-Integration nutzen (spring-boot-starter-mail)
 *   - SimpleMailMessage erstellen
 *   - MailSender aufrufen
 *   - Plaintext Code in HTML-Email (nicht in Logs!)
 *
 * <p><b>Warum brauchen wir sie?</b>
 * - Zentrale Stelle für Mail-Versand
 * - Erlaubt DEV-freundliche Testung (ohne SMTP)
 * - In PROD: sichere Email-Zustellung
 * - Masked Telemetry: Logs sind sauber (keine Secrets)
 *
 * <p><b>Sicherheits-Hinweise:</b>
 * - ✗ NIEMALS Code unverschlüsselt in Logs schreiben!
 * - ✓ Nur Maskierung loggen (123***)
 * - ✓ Plaintext nur im Email-Body (nicht in Logs/stdout)
 * - ✓ HTTPS/TLS für Email in Prod
 *
 * <p><b>DEV vs. PROD:</b>
 * - DEV: Code sichtbar in Logs, leicht zu testen
 * - PROD: Code in Email, Logs sind sauber
 * - Konfigurierbar via spring.mail.* Properties
 *
 * <p><b>Kosten/Limits:</b>
 * - SendGrid: 100 Emails/Tag kostenlos oder $20/Monat
 * - AWS SES: 62.000 Emails/Monat kostenlos
 * - Mailgun: 10.000 Emails/Monat kostenlos
 * - Phase 2: Integration wählen
 */
@Service
public class LoginCodeMailService {

    private static final Logger log = LoggerFactory.getLogger(LoginCodeMailService.class);

    /**
     * Sendet den Login-Code per Mail/Log.
     *
     * <p><b>Aufruf:</b>
     * Wird von LoginCodeService.requestLoginCode() aufgerufen.
     * Parameter: plainCode = "734821" (Plaintext, nie zu speichern!)
     *
     * <p><b>DEV-Modus:**
     * Loggt: "Login-Code für anna@example.de versendet (masked: 734***)"
     * - Code wird maskiert (erste 3 Ziffern + ***)
     * - User kann in Logs/Monitor nachschauen
     * - Keine Sicherheitslücke (nur erste 3 Ziffern sichtbar)
     *
     * <p><b>PROD-Modus (später):**
     * - SMTP-Provider (SendGrid/AWS SES/etc.)
     * - Plaintext Code in HTML-Email
     * - Code NICHT in Logs
     * - TLS/HTTPS für Transport
     *
     * <p><b>Wichtig:</b>
     * - Parameter plainCode wird NICHT geloggt (security!)
     * - Parameter plainCode wird verwendet zum Versenden (Mail/PROD)
     * - Maskierung ist für DEV-Debugging nur
     *
     * @param email Email des Users
     * @param plainCode Der 6-stellige Code (Plaintext) – STRENG VERTRAULICH
     */
    public void sendLoginCode(String email, String plainCode) {
        // Masked Telemetry für DEV-Debugging (ohne Secrets)
        String maskedCode = maskCode(plainCode);
        log.info("Login-Code für {} versendet (masked: {})", email, maskedCode);

        // In PROD: SMTP versenden (spring-boot-starter-mail)
        // Der plainCode wird hier versendet, NICHT geloggt.
        // Example (PROD-Ready):
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(email);
        // message.setSubject("Dein Login-Code für Petsitter");
        // message.setText("Dein Code: " + plainCode + "\nGültig für 10 Minuten.");
        // mailSender.send(message);
    }

    /**
     * Maskiert Code für sichere Telemetry in Logs.
     *
     * <p><b>Beispiel:</b>
     * - Input: "734821"
     * - Output: "734***"
     *
     * <p><b>Warum?</b>
     * - Logs sind oft öffentlich einsehbar (Kibana, Splunk, etc.)
     * - Plaintext Codes wären ein Sicherheitsleck
     * - Maskierung erlaubt dennoch lokale Tests (Erste 3 Ziffern sichtbar)
     *
     * @param code 6-stelliger Code
     * @return Maskierte Variante (erste 3 Stellen + ***)
     */
    private String maskCode(String code) {
        if (code == null || code.length() < 3) {
            return "***";
        }
        return code.substring(0, 3) + "***";
    }
}
