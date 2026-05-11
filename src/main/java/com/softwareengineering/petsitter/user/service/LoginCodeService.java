package com.softwareengineering.petsitter.user.service;

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
 * Lifecycle service for one-time registration verification codes.
 */
@Service
public class LoginCodeService {

    private static final Logger log = LoggerFactory.getLogger(LoginCodeService.class);

    private static final int CODE_LENGTH = 6;
    private static final int CODE_VALIDITY_MINUTES = 20;

    private final LoginCodeRepository loginCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginCodeMailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public LoginCodeService(
            LoginCodeRepository loginCodeRepository,
            PasswordEncoder passwordEncoder,
            LoginCodeMailService mailService
    ) {
        this.loginCodeRepository = loginCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public void requestLoginCode(String email, String requestIp) {
        log.info("Registrierungs-Code angefordert für: {}", email);

        invalidateCodesForEmail(email);

        String plainCode = generateCode();
        String codeHash = passwordEncoder.encode(plainCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);

        loginCodeRepository.save(new LoginCode(email, codeHash, expiresAt, requestIp));
        mailService.sendLoginCode(email, plainCode);

        log.info("Registrierungs-Code für {} generiert und versendet (expires: {})", email, expiresAt);
    }

    @Transactional
    public boolean validateLoginCode(String email, String plainCode) {
        log.info("Registrierungs-Code Validierung für: {}", email);

        Optional<LoginCode> optionalCode = loginCodeRepository.findFirstByEmailAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                email,
                LocalDateTime.now()
        );

        if (optionalCode.isEmpty()) {
            log.warn("Kein gültiger Registrierungs-Code gefunden für {}", email);
            return false;
        }

        LoginCode code = optionalCode.get();
        if (code.isRateLimited() || code.isExpired()) {
            log.warn("Registrierungs-Code nicht mehr nutzbar für {}", email);
            return false;
        }

        if (!passwordEncoder.matches(plainCode, code.getCodeHash())) {
            code.setAttempts(code.getAttempts() + 1);
            loginCodeRepository.save(code);
            log.warn("Falscher Registrierungs-Code für {} (Versuch: {})", email, code.getAttempts());
            return false;
        }

        code.setUsedAt(LocalDateTime.now());
        loginCodeRepository.save(code);
        return true;
    }

    @Transactional
    public void invalidateCodesForEmail(String email) {
        List<LoginCode> oldCodes = loginCodeRepository.findByEmailAndUsedAtIsNull(email);
        oldCodes.forEach(code -> code.setUsedAt(LocalDateTime.now()));
        loginCodeRepository.saveAll(oldCodes);
    }

    @Transactional
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        loginCodeRepository.deleteExpiredCodes(now, now.minusHours(1));
        log.debug("Cleanup abgelaufener Registrierungs-Codes durchgeführt");
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
