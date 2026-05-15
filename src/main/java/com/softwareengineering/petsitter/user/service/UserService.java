package com.softwareengineering.petsitter.user.service;

import com.softwareengineering.petsitter.location.dto.PostalCodeValidationResult;
import com.softwareengineering.petsitter.location.service.PostalCodeService;
import com.softwareengineering.petsitter.pet.service.PetService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserLoginRequest;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.dto.UserProfileUpdateRequest;
import com.softwareengineering.petsitter.user.dto.UserRegistrationConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.UserRegistrationRequest;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;
    private final PasswordEncoder passwordEncoder;
    private final LoginCodeService loginCodeService;
    private final PetService petService;
    private final PostalCodeService postalCodeService;

    public UserService(
            UserRepository userRepository,
            AuthenticatedUser authenticatedUser,
            PasswordEncoder passwordEncoder,
            LoginCodeService loginCodeService,
            PetService petService,
            PostalCodeService postalCodeService
    ) {
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
        this.passwordEncoder = passwordEncoder;
        this.loginCodeService = loginCodeService;
        this.petService = petService;
        this.postalCodeService = postalCodeService;
    }

    public Optional<User> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public UserAuthResult login(UserLoginRequest request) {
        if (request == null) {
            return UserAuthResult.failure("Bitte E-Mail und Passwort eingeben.");
        }
        String email = normalizeEmail(request.email());
        if (email.isBlank() || isBlank(request.password())) {
            return UserAuthResult.failure("Bitte E-Mail und Passwort eingeben.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return UserAuthResult.failure("E-Mail oder Passwort ist falsch.");
        }

        User user = userOpt.get();
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            return UserAuthResult.failure("Bitte bestätige zuerst deine Registrierung mit dem Code aus der E-Mail.");
        }
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            return UserAuthResult.failure("Dieser Account ist gesperrt.");
        }
        if (user.getAccountStatus() != AccountStatus.VERIFIED) {
            return UserAuthResult.failure("Dieser Account kann aktuell nicht angemeldet werden.");
        }
        if (isBlank(user.getPasswordHash()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return UserAuthResult.failure("E-Mail oder Passwort ist falsch.");
        }

        return UserAuthResult.success("Anmeldung erfolgreich.", toProfileDto(user));
    }

    @Transactional
    public UserAuthResult startRegistration(UserRegistrationRequest request, String clientIp) {
        Optional<String> validationError = validateRegistrationRequest(request);
        if (validationError.isPresent()) {
            return UserAuthResult.failure(validationError.get());
        }

        String email = normalizeEmail(request.email());
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User existing = existingUser.get();
            if (existing.getAccountStatus() == AccountStatus.VERIFIED) {
                return UserAuthResult.failure("Für diese E-Mail existiert bereits ein Account.");
            }
            if (existing.getAccountStatus() == AccountStatus.BLOCKED) {
                return UserAuthResult.failure("Diese E-Mail kann nicht registriert werden.");
            }
            updatePendingUser(existing, request, email);
            userRepository.save(existing);
            loginCodeService.requestLoginCode(email, clientIp);
            return UserAuthResult.success("Registrierungs-Code wurde versendet.", toProfileDto(existing));
        }

        User user = new User();
        updatePendingUser(user, request, email);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            return UserAuthResult.failure("Für diese E-Mail existiert bereits ein Account.");
        }

        loginCodeService.requestLoginCode(email, clientIp);
        return UserAuthResult.success("Registrierungs-Code wurde versendet.", toProfileDto(user));
    }

    @Transactional
    public UserAuthResult completeRegistration(UserRegistrationConfirmationRequest request) {
        String email = normalizeEmail(request.email());
        if (email.isBlank() || isBlank(request.code())) {
            return UserAuthResult.failure("Bitte E-Mail und Code eingeben.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getAccountStatus() != AccountStatus.PENDING) {
            return UserAuthResult.failure("Keine offene Registrierung für diese E-Mail gefunden.");
        }

        if (!loginCodeService.validateLoginCode(email, request.code().trim())) {
            return UserAuthResult.failure("Der Code ist ungültig oder abgelaufen.");
        }

        User user = userOpt.get();
        user.setAccountStatus(AccountStatus.VERIFIED);
        user.setDeleteAfter(null);
        User savedUser = userRepository.save(user);
        return UserAuthResult.success("Registrierung erfolgreich abgeschlossen.", toProfileDto(savedUser));
    }

    @Transactional
    @Scheduled(
            initialDelayString = "${petsitter.user.pending-cleanup-initial-delay-ms:3600000}",
            fixedDelayString = "${petsitter.user.pending-cleanup-interval-ms:3600000}"
    )
    public int cleanupExpiredPendingUsers() {
        List<User> expiredUsers = userRepository.findByAccountStatusAndDeleteAfterLessThanEqual(
                AccountStatus.PENDING,
                LocalDateTime.now()
        );
        expiredUsers.forEach(user -> loginCodeService.invalidateCodesForEmail(user.getEmail()));
        userRepository.deleteAll(expiredUsers);
        return expiredUsers.size();
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getCurrentUserProfile() {
        return authenticatedUser.get().map(this::toProfileDto);
    }

    @Transactional
    public UserAuthResult updateCurrentUserProfile(UserProfileUpdateRequest request) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isEmpty()) {
            return UserAuthResult.failure("Bitte melde dich erneut an.");
        }

        Optional<String> validationError = validateProfileUpdateRequest(request);
        if (validationError.isPresent()) {
            return UserAuthResult.failure(validationError.get());
        }

        User user = userOpt.get();
        applyProfileUpdate(user, request);
        User savedUser = userRepository.save(user);
        return UserAuthResult.success("Profil gespeichert.", toProfileDto(savedUser));
    }

    @Transactional
    public UserAuthResult requestCurrentUserEmailChange(String newEmail, String clientIp) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isEmpty()) {
            return UserAuthResult.failure("Bitte melde dich erneut an.");
        }

        User user = userOpt.get();
        String normalizedEmail = normalizeEmail(newEmail);
        if (!isValidEmail(normalizedEmail)) {
            return UserAuthResult.failure("Bitte eine gültige E-Mail eingeben.");
        }
        if (normalizedEmail.equals(user.getEmail())) {
            user.setPendingEmail(null);
            user.setPendingEmailRequestedAt(null);
            User savedUser = userRepository.save(user);
            return UserAuthResult.success("E-Mail-Adresse ist unverändert.", toProfileDto(savedUser));
        }
        if (emailBelongsToAnotherUser(normalizedEmail, user.getId())) {
            return UserAuthResult.failure("Für diese E-Mail existiert bereits ein Account.");
        }

        user.setPendingEmail(normalizedEmail);
        user.setPendingEmailRequestedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        loginCodeService.requestLoginCode(normalizedEmail, clientIp);
        return UserAuthResult.success("Bestätigungscode wurde an die neue E-Mail gesendet.", toProfileDto(savedUser));
    }

    @Transactional
    public UserAuthResult confirmCurrentUserEmailChange(String code) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isEmpty()) {
            return UserAuthResult.failure("Bitte melde dich erneut an.");
        }

        User user = userOpt.get();
        if (isBlank(user.getPendingEmail())) {
            return UserAuthResult.failure("Es gibt keine offene E-Mail-Änderung.");
        }
        if (isBlank(code)) {
            return UserAuthResult.failure("Bitte den Bestätigungscode eingeben.");
        }
        if (emailBelongsToAnotherUser(user.getPendingEmail(), user.getId())) {
            return UserAuthResult.failure("Für diese E-Mail existiert bereits ein Account.");
        }
        if (!loginCodeService.validateLoginCode(user.getPendingEmail(), code.trim())) {
            return UserAuthResult.failure("Der Code ist ungültig oder abgelaufen.");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setPendingEmailRequestedAt(null);
        User savedUser = userRepository.save(user);
        return UserAuthResult.success("E-Mail-Adresse wurde aktualisiert.", toProfileDto(savedUser));
    }

    public String getCurrentUser() {
        return authenticatedUser.get()
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Gast");
    }

    private UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                defaultIfBlank(user.getDisplayName(), user.getFirstName()),
                user.getPhone(),
                user.getBirthDate(),
                user.getNationality(),
                defaultIfBlank(user.getLanguage(), "deutsch"),
                user.getBio(),
                user.getStreet(),
                user.getHouseNumber(),
                user.getPostalCode(),
                user.getCity(),
                user.getAddressAddition(),
                defaultIfBlank(user.getCountry(), "Deutschland"),
                user.getPendingEmail(),
                user.getPendingEmailRequestedAt(),
                petService.getPetSummaryForOwner(user.getId()),
                user.getAccountRole(),
                user.getAccountStatus()
        );
    }

    private Optional<String> validateProfileUpdateRequest(UserProfileUpdateRequest request) {
        if (request == null) {
            return Optional.of("Profildaten fehlen.");
        }
        if (isBlank(request.firstName())
                || isBlank(request.lastName())
                || isBlank(request.street())
                || isBlank(request.houseNumber())
                || isBlank(request.postalCode())
                || isBlank(request.city())
                || isBlank(request.country())) {
            return Optional.of("Bitte alle Pflichtfelder ausfüllen.");
        }
        if (!isGermany(request.country())) {
            return Optional.of("Aktuell werden nur deutsche Adressen unterstützt.");
        }
        Optional<String> postalCodeError = validateGermanPostalCode(request.postalCode(), request.city());
        if (postalCodeError.isPresent()) {
            return postalCodeError;
        }
        return Optional.empty();
    }

    private void applyProfileUpdate(User user, UserProfileUpdateRequest request) {
        String firstName = clean(request.firstName());
        user.setFirstName(firstName);
        user.setLastName(clean(request.lastName()));
        user.setDisplayName(defaultIfBlank(cleanNullable(request.displayName()), firstName));
        user.setPhone(cleanNullable(request.phone()));
        user.setBirthDate(request.birthDate());
        user.setNationality(cleanNullable(request.nationality()));
        user.setLanguage(defaultIfBlank(cleanNullable(request.language()), "deutsch"));
        user.setBio(cleanNullable(request.bio()));
        user.setStreet(clean(request.street()));
        user.setHouseNumber(clean(request.houseNumber()));
        user.setPostalCode(clean(request.postalCode()));
        user.setCity(clean(request.city()));
        user.setAddressAddition(cleanNullable(request.addressAddition()));
        user.setCountry(clean(request.country()));
    }

    private Optional<String> validateRegistrationRequest(UserRegistrationRequest request) {
        if (request == null) {
            return Optional.of("Registrierungsdaten fehlen.");
        }
        if (!isValidEmail(normalizeEmail(request.email()))) {
            return Optional.of("Bitte eine gültige E-Mail eingeben.");
        }
        if (isBlank(request.password()) || request.password().length() < MIN_PASSWORD_LENGTH) {
            return Optional.of("Das Passwort muss mindestens 8 Zeichen lang sein.");
        }
        if (!request.password().equals(request.confirmPassword())) {
            return Optional.of("Die Passwörter stimmen nicht überein.");
        }
        if (isBlank(request.firstName())
                || isBlank(request.lastName())
                || isBlank(request.street())
                || isBlank(request.houseNumber())
                || isBlank(request.postalCode())
                || isBlank(request.city())) {
            return Optional.of("Bitte alle Pflichtfelder ausfüllen.");
        }
        Optional<String> postalCodeError = validateGermanPostalCode(request.postalCode(), request.city());
        if (postalCodeError.isPresent()) {
            return postalCodeError;
        }
        if (request.birthDate() == null || request.birthDate().isAfter(LocalDate.now().minusYears(18))) {
            return Optional.of("Du musst mindestens 18 Jahre alt sein.");
        }
        return Optional.empty();
    }

    private Optional<String> validateGermanPostalCode(String postalCode, String city) {
        if (postalCodeService == null) {
            return Optional.empty();
        }
        PostalCodeValidationResult validation = postalCodeService.validateGermanPostalCode(postalCode, city);
        return validation.valid() ? Optional.empty() : Optional.of(validation.message());
    }

    private void updatePendingUser(User user, UserRegistrationRequest request, String normalizedEmail) {
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(clean(request.firstName()));
        user.setLastName(clean(request.lastName()));
        user.setDisplayName(clean(request.firstName()));
        user.setPhone(cleanNullable(request.phone()));
        user.setLanguage("deutsch");
        user.setStreet(clean(request.street()));
        user.setHouseNumber(clean(request.houseNumber()));
        user.setPostalCode(clean(request.postalCode()));
        user.setCity(clean(request.city()));
        user.setAddressAddition(cleanNullable(request.addressAddition()));
        user.setCountry(request.country() != null && !request.country().isBlank()
                ? request.country() : "Deutschland");
        user.setBirthDate(request.birthDate());
        user.setNationality(cleanNullable(request.nationality()));
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        user.setAccountStatus(AccountStatus.PENDING);
        user.setDeleteAfter(LocalDateTime.now().plusHours(24));
    }

    private boolean emailBelongsToAnotherUser(String email, UUID currentUserId) {
        return userRepository.findByEmail(email)
                .filter(existingUser -> !Objects.equals(existingUser.getId(), currentUserId))
                .isPresent();
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

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanNullable(String value) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? null : cleaned;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isGermany(String country) {
        if (country == null) {
            return false;
        }
        String normalized = country.trim().toLowerCase(Locale.GERMANY);
        return normalized.equals("deutschland")
                || normalized.equals("germany")
                || normalized.equals("de")
                || normalized.equals("deu");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
