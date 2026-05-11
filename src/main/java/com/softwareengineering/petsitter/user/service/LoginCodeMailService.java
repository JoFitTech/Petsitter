package com.softwareengineering.petsitter.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends registration verification codes. The current implementation logs codes in
 * development mode and can later be replaced by SMTP without changing UserService.
 */
@Service
public class LoginCodeMailService {

    private static final Logger log = LoggerFactory.getLogger(LoginCodeMailService.class);

    @Value("${petsitter.dev.show-login-code:false}")
    private boolean showLoginCodeInDev;

    public void sendLoginCode(String email, String plainCode) {
        log.info("Registrierungs-Code für {} versendet (masked: {})", email, maskCode(plainCode));

        if (showLoginCodeInDev) {
            log.warn("[DEV-ONLY] REGISTRIERUNGS-CODE FÜR {}: {}", email, plainCode);
            System.out.printf("%nREGISTRIERUNGS-CODE%nEmail: %s%nCode : %s%n", email, plainCode);
        }
    }

    private String maskCode(String code) {
        if (code == null || code.length() < 3) {
            return "***";
        }
        return code.substring(0, 3) + "***";
    }
}
