package com.softwareengineering.petsitter.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends password reset codes. In local development, codes can be printed to the
 * terminal; a real mail provider can later replace this implementation.
 */
@Service
public class PasswordResetMailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailService.class);

    @Value("${petsitter.dev.show-password-reset-code:false}")
    private boolean showPasswordResetCodeInDev;

    public void sendPasswordResetCode(String email, String plainCode) {
        log.info("Passwort-Reset-Code für {} versendet (masked: {})", email, maskCode(plainCode));

        if (showPasswordResetCodeInDev) {
            log.warn("[DEV-ONLY] PASSWORT-RESET-CODE FÜR {}: {}", email, plainCode);
            System.out.printf("%nPASSWORT-RESET-CODE%nEmail: %s%nCode : %s%n", email, plainCode);
        }
    }

    private String maskCode(String code) {
        if (code == null || code.length() < 3) {
            return "***";
        }
        return code.substring(0, 3) + "***";
    }
}
