package com.softwareengineering.petsitter.user.service;

import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.PasswordResetCode;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.dto.PasswordResetConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.PasswordResetRequest;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.repository.PasswordResetCodeRepository;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int CODE_LENGTH = 6;
    private static final int CODE_VALIDITY_MINUTES = 20;
    private static final String GENERIC_REQUEST_MESSAGE =
            "Wenn ein verifizierter Account mit dieser E-Mail existiert, wurde ein Code versendet.";
    private static final String INVALID_CODE_MESSAGE = "Der Code ist ungültig oder abgelaufen.";

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailService mailService;
    private final PasswordPolicyService passwordPolicyService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetMailService mailService,
            PasswordPolicyService passwordPolicyService
    ) {
        this.userRepository = userRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.passwordPolicyService = passwordPolicyService;
    }

    @Transactional
    public UserAuthResult requestPasswordReset(PasswordResetRequest request, String requestIp) {
        if (request == null || !isValidEmail(normalizeEmail(request.email()))) {
            return UserAuthResult.failure("Bitte eine gültige E-Mail eingeben.");
        }

        String email = normalizeEmail(request.email());
        Optional<User> userOpt = userRepository.findByEmail(email)
                .filter(user -> user.getAccountStatus() == AccountStatus.VERIFIED);

        if (userOpt.isEmpty()) {
            log.info("Passwort-Reset für nicht verifizierte oder unbekannte E-Mail angefordert: {}", email);
            return UserAuthResult.failure("Es gibt keinen Account mit dieser E-Mail-Adresse.");
        }

        invalidateCodesForEmail(email);

        String plainCode = generateCode();
        String codeHash = passwordEncoder.encode(plainCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);

        passwordResetCodeRepository.save(new PasswordResetCode(email, codeHash, expiresAt, requestIp));
        mailService.sendPasswordResetCode(email, plainCode);

        log.info("Passwort-Reset-Code für {} generiert und versendet (expires: {})", email, expiresAt);
        return UserAuthResult.success(GENERIC_REQUEST_MESSAGE, null);
    }

    @Transactional
    public UserAuthResult completePasswordReset(PasswordResetConfirmationRequest request) {
        if (request == null) {
            return UserAuthResult.failure("Reset-Daten fehlen.");
        }

        String email = normalizeEmail(request.email());
        if (!isValidEmail(email)) {
            return UserAuthResult.failure("Bitte eine gültige E-Mail eingeben.");
        }
        if (isBlank(request.code())) {
            return UserAuthResult.failure("Bitte den Code eingeben.");
        }

        PasswordPolicyService.PasswordPolicyResult passwordPolicy = passwordPolicyService.evaluate(request.password());
        if (!passwordPolicy.valid()) {
            return UserAuthResult.failure(passwordPolicy.errorMessage());
        }
        if (!Objects.equals(request.password(), request.confirmPassword())) {
            return UserAuthResult.failure("Die Passwörter stimmen nicht überein.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email)
                .filter(user -> user.getAccountStatus() == AccountStatus.VERIFIED);
        if (userOpt.isEmpty()) {
            return UserAuthResult.failure(INVALID_CODE_MESSAGE);
        }

        Optional<PasswordResetCode> optionalCode = passwordResetCodeRepository
                .findFirstByEmailAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(email, LocalDateTime.now());
        if (optionalCode.isEmpty()) {
            return UserAuthResult.failure(INVALID_CODE_MESSAGE);
        }

        PasswordResetCode code = optionalCode.get();
        if (code.isRateLimited() || code.isExpired()) {
            return UserAuthResult.failure(INVALID_CODE_MESSAGE);
        }

        if (!passwordEncoder.matches(request.code().trim(), code.getCodeHash())) {
            code.setAttempts(code.getAttempts() + 1);
            passwordResetCodeRepository.save(code);
            log.warn("Falscher Passwort-Reset-Code für {} (Versuch: {})", email, code.getAttempts());
            return UserAuthResult.failure(INVALID_CODE_MESSAGE);
        }

        code.setUsedAt(LocalDateTime.now());
        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        passwordResetCodeRepository.save(code);
        userRepository.save(user);

        return UserAuthResult.success("Passwort wurde zurückgesetzt. Du kannst dich jetzt anmelden.", null);
    }

    @Transactional
    public void invalidateCodesForEmail(String email) {
        List<PasswordResetCode> oldCodes = passwordResetCodeRepository.findByEmailAndUsedAtIsNull(email);
        oldCodes.forEach(code -> code.setUsedAt(LocalDateTime.now()));
        passwordResetCodeRepository.saveAll(oldCodes);
    }

    @Transactional
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        passwordResetCodeRepository.deleteExpiredCodes(now, now.minusHours(1));
        log.debug("Cleanup abgelaufener Passwort-Reset-Codes durchgeführt");
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    private boolean isValidEmail(String email) {
        return !email.isBlank() && EMAIL_PATTERN.matcher(email).matches();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
