package com.softwareengineering.petsitter.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.location.dto.PostalCodeValidationResult;
import com.softwareengineering.petsitter.location.service.PostalCodeService;
import com.softwareengineering.petsitter.pet.service.PetService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.dto.PublicUserProfileDto;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserLoginRequest;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.dto.UserProfileUpdateRequest;
import com.softwareengineering.petsitter.user.dto.UserRegistrationConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.UserRegistrationRequest;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private static final String STRONG_PASSWORD = "Axiom-River8!Q";
    private static final String LENGTH_MESSAGE =
            "Das Passwort muss mindestens 14 Zeichen lang sein.";
    private static final String UPPERCASE_MESSAGE =
            "Das Passwort muss mindestens einen Großbuchstaben enthalten.";
    private static final String LOWERCASE_MESSAGE =
            "Das Passwort muss mindestens einen Kleinbuchstaben enthalten.";
    private static final String DIGIT_MESSAGE =
            "Das Passwort muss mindestens eine Zahl enthalten.";
    private static final String SPECIAL_CHARACTER_MESSAGE =
            "Das Passwort muss mindestens ein Sonderzeichen enthalten.";
    private static final String FORBIDDEN_TERM_MESSAGE =
            "Das Passwort darf keine schwachen Wörter enthalten.";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void findUserByIdLoadsUserFromRepository() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        UserRepositoryFake userRepository = new UserRepositoryFake(user);
        UserService userService = service(userRepository.repository(), authenticatedUser(Optional.empty()), new LoginCodeServiceFake());

        Optional<User> result = userService.findUserById(userId);

        assertThat(result).contains(user);
        assertThat(userRepository.requestedId).hasValue(userId);
    }

    @Test
    void getCurrentUserProfileMapsAuthenticatedUserToDto() {
        UUID userId = UUID.randomUUID();
        User domainUser = user(userId);
        UserService userService = service(
                repositoryReturning(domainUser),
                authenticatedUser(Optional.of(domainUser)),
                new LoginCodeServiceFake(),
                petService("1 Hund, 1 Katze")
        );

        Optional<UserProfileDto> result = userService.getCurrentUserProfile();

        assertThat(result).hasValueSatisfying(profile -> {
            assertThat(profile.id()).isEqualTo(userId);
            assertThat(profile.email()).isEqualTo("anna.mueller@petsitter.local");
            assertThat(profile.firstName()).isEqualTo("Anna");
            assertThat(profile.lastName()).isEqualTo("Mueller");
            assertThat(profile.displayName()).isEqualTo("Anna");
            assertThat(profile.phone()).isEqualTo("+49 221 111222");
            assertThat(profile.birthDate()).isEqualTo(LocalDate.of(1995, 5, 2));
            assertThat(profile.nationality()).isEqualTo("deutsch");
            assertThat(profile.language()).isEqualTo("deutsch");
            assertThat(profile.bio()).isEqualTo("Ich betreue gerne Hunde und Katzen.");
            assertThat(profile.street()).isEqualTo("Rosenweg");
            assertThat(profile.houseNumber()).isEqualTo("14");
            assertThat(profile.postalCode()).isEqualTo("50667");
            assertThat(profile.city()).isEqualTo("Koeln");
            assertThat(profile.addressAddition()).isEqualTo("2. OG links");
            assertThat(profile.country()).isEqualTo("Deutschland");
            assertThat(profile.pendingEmail()).isNull();
            assertThat(profile.pendingEmailRequestedAt()).isNull();
            assertThat(profile.petSummary()).isEqualTo("1 Hund, 1 Katze");
            assertThat(profile.accountRole()).isEqualTo(AccountRole.SIGNED_IN_USER);
            assertThat(profile.accountStatus()).isEqualTo(AccountStatus.VERIFIED);
        });
    }

    @Test
    void getPublicUserProfileMapsOnlyPublicFieldsForVerifiedUser() {
        UUID userId = UUID.randomUUID();
        User domainUser = user(userId);
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser);
        UserService userService = service(
                userRepository.repository(),
                authenticatedUser(Optional.empty()),
                new LoginCodeServiceFake(),
                petService("1 Hund, 1 Katze")
        );

        Optional<PublicUserProfileDto> result = userService.getPublicUserProfile(userId);

        assertThat(result).hasValueSatisfying(profile -> {
            assertThat(profile.id()).isEqualTo(userId);
            assertThat(profile.displayName()).isEqualTo("Anna");
            assertThat(profile.birthDate()).isEqualTo(LocalDate.of(1995, 5, 2));
            assertThat(profile.language()).isEqualTo("deutsch");
            assertThat(profile.bio()).isEqualTo("Ich betreue gerne Hunde und Katzen.");
            assertThat(profile.postalCode()).isEqualTo("50667");
            assertThat(profile.city()).isEqualTo("Koeln");
            assertThat(profile.petSummary()).isEqualTo("1 Hund, 1 Katze");
            assertThat(profile.accountStatus()).isEqualTo(AccountStatus.VERIFIED);
        });
        assertThat(userRepository.requestedId).hasValue(userId);
    }

    @Test
    void getPublicUserProfileReturnsEmptyForMissingOrUnverifiedUser() {
        UUID pendingUserId = UUID.randomUUID();
        User pending = user(pendingUserId);
        pending.setAccountStatus(AccountStatus.PENDING);
        UserService userService = service(
                new UserRepositoryFake(pending).repository(),
                authenticatedUser(Optional.empty()),
                new LoginCodeServiceFake()
        );

        assertThat(userService.getPublicUserProfile(pendingUserId)).isEmpty();
        assertThat(userService.getPublicUserProfile(UUID.randomUUID())).isEmpty();
        assertThat(userService.getPublicUserProfile(null)).isEmpty();
    }

    @Test
    void getCurrentUserUsesAuthenticatedUsersFullName() {
        User domainUser = user(UUID.randomUUID());
        UserService userService = service(repositoryReturning(domainUser), authenticatedUser(Optional.of(domainUser)), new LoginCodeServiceFake());

        assertThat(userService.getCurrentUser()).isEqualTo("Anna Mueller");
    }

    @Test
    void getCurrentUserFallsBackToGuestWhenNoUserIsAuthenticated() {
        UserService userService = service(repositoryReturning(null), authenticatedUser(Optional.empty()), new LoginCodeServiceFake());

        assertThat(userService.getCurrentUser()).isEqualTo("Gast");
    }

    @Test
    void updateCurrentUserProfileSavesTrimmedProfileDataForAuthenticatedUser() {
        User domainUser = user(UUID.randomUUID());
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser);
        UserService userService = service(userRepository.repository(), authenticatedUser(Optional.of(domainUser)), new LoginCodeServiceFake());

        UserAuthResult result = userService.updateCurrentUserProfile(new UserProfileUpdateRequest(
                " Lisa ",
                " Meier ",
                " ",
                " ",
                LocalDate.of(1998, 4, 12),
                " oesterreichisch ",
                " englisch ",
                " Ich betreue Kleintiere. ",
                " Neue Strasse ",
                " 9b ",
                " 10999 ",
                " Berlin ",
                " ",
                " Deutschland "
        ));

        assertThat(result.success()).isTrue();
        assertThat(userRepository.savedUser).isSameAs(domainUser);
        assertThat(domainUser.getFirstName()).isEqualTo("Lisa");
        assertThat(domainUser.getLastName()).isEqualTo("Meier");
        assertThat(domainUser.getDisplayName()).isEqualTo("Lisa");
        assertThat(domainUser.getPhone()).isNull();
        assertThat(domainUser.getBirthDate()).isEqualTo(LocalDate.of(1998, 4, 12));
        assertThat(domainUser.getNationality()).isEqualTo("oesterreichisch");
        assertThat(domainUser.getLanguage()).isEqualTo("englisch");
        assertThat(domainUser.getBio()).isEqualTo("Ich betreue Kleintiere.");
        assertThat(domainUser.getStreet()).isEqualTo("Neue Strasse");
        assertThat(domainUser.getHouseNumber()).isEqualTo("9b");
        assertThat(domainUser.getPostalCode()).isEqualTo("10999");
        assertThat(domainUser.getCity()).isEqualTo("Berlin");
        assertThat(domainUser.getAddressAddition()).isNull();
        assertThat(domainUser.getCountry()).isEqualTo("Deutschland");
    }

    @Test
    void updateCurrentUserProfileRejectsMissingRequiredFieldsAndMissingAuthentication() {
        User domainUser = user(UUID.randomUUID());
        UserService userService = service(new UserRepositoryFake(domainUser).repository(), authenticatedUser(Optional.of(domainUser)), new LoginCodeServiceFake());

        UserAuthResult missingField = userService.updateCurrentUserProfile(new UserProfileUpdateRequest(
                "",
                "Meier",
                "Lisa",
                null,
                null,
                null,
                "deutsch",
                null,
                "Neue Strasse",
                "9b",
                "10999",
                "Berlin",
                null,
                "Deutschland"
        ));
        UserAuthResult missingAuth = service(repositoryReturning(domainUser), authenticatedUser(Optional.empty()), new LoginCodeServiceFake())
                .updateCurrentUserProfile(profileUpdateRequest());

        assertThat(missingField.success()).isFalse();
        assertThat(missingAuth.success()).isFalse();
    }

    @Test
    void requestCurrentUserEmailChangeStoresPendingEmailAndSendsCode() {
        User domainUser = user(UUID.randomUUID());
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();
        UserService userService = service(userRepository.repository(), authenticatedUser(Optional.of(domainUser)), loginCodeService);

        UserAuthResult result = userService.requestCurrentUserEmailChange(" New.Email@Petsitter.Local ", "127.0.0.1");

        assertThat(result.success()).isTrue();
        assertThat(domainUser.getEmail()).isEqualTo("anna.mueller@petsitter.local");
        assertThat(domainUser.getPendingEmail()).isEqualTo("new.email@petsitter.local");
        assertThat(domainUser.getPendingEmailRequestedAt()).isNotNull();
        assertThat(loginCodeService.requestedEmail).isEqualTo("new.email@petsitter.local");
        assertThat(loginCodeService.requestedIp).isEqualTo("127.0.0.1");
        assertThat(userRepository.savedUser).isSameAs(domainUser);
    }

    @Test
    void requestCurrentUserEmailChangeRejectsDuplicateEmail() {
        User domainUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        otherUser.setEmail("taken@petsitter.local");
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser, otherUser);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.of(domainUser)), loginCodeService)
                .requestCurrentUserEmailChange("taken@petsitter.local", "127.0.0.1");

        assertThat(result.success()).isFalse();
        assertThat(domainUser.getPendingEmail()).isNull();
        assertThat(loginCodeService.requestedEmail).isNull();
    }

    @Test
    void confirmCurrentUserEmailChangeValidatesCodeAndUpdatesLoginEmail() {
        User domainUser = user(UUID.randomUUID());
        domainUser.setPendingEmail("new.email@petsitter.local");
        domainUser.setPendingEmailRequestedAt(LocalDateTime.now());
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();
        loginCodeService.validCode = true;

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.of(domainUser)), loginCodeService)
                .confirmCurrentUserEmailChange("123456");

        assertThat(result.success()).isTrue();
        assertThat(domainUser.getEmail()).isEqualTo("new.email@petsitter.local");
        assertThat(domainUser.getPendingEmail()).isNull();
        assertThat(domainUser.getPendingEmailRequestedAt()).isNull();
        assertThat(loginCodeService.validatedEmail).isEqualTo("new.email@petsitter.local");
        assertThat(loginCodeService.validatedCode).isEqualTo("123456");
    }

    @Test
    void confirmCurrentUserEmailChangeRejectsInvalidCode() {
        User domainUser = user(UUID.randomUUID());
        domainUser.setPendingEmail("new.email@petsitter.local");
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();
        loginCodeService.validCode = false;

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.of(domainUser)), loginCodeService)
                .confirmCurrentUserEmailChange("000000");

        assertThat(result.success()).isFalse();
        assertThat(domainUser.getEmail()).isEqualTo("anna.mueller@petsitter.local");
        assertThat(domainUser.getPendingEmail()).isEqualTo("new.email@petsitter.local");
        assertThat(userRepository.saveCount).isZero();
    }

    @Test
    void loginSucceedsForVerifiedUserWithPassword() {
        User user = user(UUID.randomUUID());
        user.setPasswordHash(passwordEncoder.encode("secret123"));
        UserRepositoryFake userRepository = new UserRepositoryFake(user);

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), new LoginCodeServiceFake())
                .login(new UserLoginRequest("  Anna.Mueller@Petsitter.Local  ", "secret123"));

        assertThat(result.success()).isTrue();
        assertThat(result.userProfile().email()).isEqualTo("anna.mueller@petsitter.local");
    }

    @Test
    void loginRejectsWrongPasswordOrUnknownEmail() {
        User user = user(UUID.randomUUID());
        user.setPasswordHash(passwordEncoder.encode("secret123"));
        UserService userService = service(new UserRepositoryFake(user).repository(), authenticatedUser(Optional.empty()), new LoginCodeServiceFake());

        assertThat(userService.login(new UserLoginRequest("anna.mueller@petsitter.local", "wrong")).success()).isFalse();
        assertThat(userService.login(new UserLoginRequest("missing@petsitter.local", "secret123")).success()).isFalse();
    }

    @Test
    void loginRejectsPendingAndBlockedUsers() {
        User pending = user(UUID.randomUUID());
        pending.setEmail("pending@petsitter.local");
        pending.setAccountStatus(AccountStatus.PENDING);
        User blocked = user(UUID.randomUUID());
        blocked.setEmail("blocked@petsitter.local");
        blocked.setAccountStatus(AccountStatus.BLOCKED);
        UserRepositoryFake userRepository = new UserRepositoryFake(pending, blocked);
        UserService userService = service(userRepository.repository(), authenticatedUser(Optional.empty()), new LoginCodeServiceFake());

        assertThat(userService.login(new UserLoginRequest("pending@petsitter.local", "secret123")).success()).isFalse();
        assertThat(userService.login(new UserLoginRequest("blocked@petsitter.local", "secret123")).success()).isFalse();
    }

    @Test
    void startRegistrationCreatesPendingUserAndSendsCode() {
        UserRepositoryFake userRepository = new UserRepositoryFake();
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .startRegistration(registrationRequest("new.user@petsitter.local"), "127.0.0.1");

        assertThat(result.success()).isTrue();
        assertThat(userRepository.savedUser).isNotNull();
        assertThat(userRepository.savedUser.getAccountStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(userRepository.savedUser.getDeleteAfter()).isAfter(LocalDateTime.now().plusHours(23));
        assertThat(loginCodeService.requestedEmail).isEqualTo("new.user@petsitter.local");
        assertThat(loginCodeService.requestedIp).isEqualTo("127.0.0.1");
    }

    @Test
    void startRegistrationRejectsVerifiedUser() {
        User verified = user(UUID.randomUUID());
        UserRepositoryFake userRepository = new UserRepositoryFake(verified);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .startRegistration(registrationRequest("anna.mueller@petsitter.local"), "127.0.0.1");

        assertThat(result.success()).isFalse();
        assertThat(loginCodeService.requestedEmail).isNull();
    }

    @Test
    void startRegistrationUpdatesPendingUserAndSendsNewCode() {
        User pending = user(UUID.randomUUID());
        pending.setAccountStatus(AccountStatus.PENDING);
        pending.setDeleteAfter(LocalDateTime.now().minusHours(1));
        UserRepositoryFake userRepository = new UserRepositoryFake(pending);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .startRegistration(registrationRequest("anna.mueller@petsitter.local"), "127.0.0.1");

        assertThat(result.success()).isTrue();
        assertThat(pending.getDeleteAfter()).isAfter(LocalDateTime.now().plusHours(23));
        assertThat(loginCodeService.requestedEmail).isEqualTo("anna.mueller@petsitter.local");
    }

    @Test
    void startRegistrationRejectsPasswordShorterThan14Characters() {
        assertWeakRegistrationPasswordRejected("Aa1!short", LENGTH_MESSAGE);
    }

    @Test
    void startRegistrationRejectsPasswordWithoutUppercaseLetter() {
        assertWeakRegistrationPasswordRejected("strongpassword1!", UPPERCASE_MESSAGE);
    }

    @Test
    void startRegistrationRejectsPasswordWithoutLowercaseLetter() {
        assertWeakRegistrationPasswordRejected("STRONGPASSWORD1!", LOWERCASE_MESSAGE);
    }

    @Test
    void startRegistrationRejectsPasswordWithoutDigit() {
        assertWeakRegistrationPasswordRejected("StrongPassword!!", DIGIT_MESSAGE);
    }

    @Test
    void startRegistrationRejectsPasswordWithoutSpecialCharacter() {
        assertWeakRegistrationPasswordRejected("StrongPassword12", SPECIAL_CHARACTER_MESSAGE);
    }

    @Test
    void startRegistrationRejectsPasswordPatternFromPolicy() {
        UserRepositoryFake userRepository = new UserRepositoryFake();
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        UserAuthResult result = service(
                userRepository.repository(),
                authenticatedUser(Optional.empty()),
                loginCodeService,
                petService("Keine Haustiere"),
                postalCodeServiceReturning(PostalCodeValidationResult.success()),
                passwordPolicyService("petsitter")
        ).startRegistration(registrationRequest("blocked.pattern@petsitter.local", "Axiom-Petsitter8!Q"), "127.0.0.1");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo(FORBIDDEN_TERM_MESSAGE);
        assertThat(userRepository.savedUser).isNull();
        assertThat(loginCodeService.requestedEmail).isNull();
    }

    @Test
    void startRegistrationRejectsInvalidPostalCode() {
        UserRepositoryFake userRepository = new UserRepositoryFake();
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();
        PostalCodeService postalCodeService = postalCodeServiceReturning(
                PostalCodeValidationResult.invalid("Bitte eine gültige deutsche Postleitzahl eingeben."));

        UserAuthResult result = service(
                userRepository.repository(),
                authenticatedUser(Optional.empty()),
                loginCodeService,
                petService("Keine Haustiere"),
                postalCodeService
        ).startRegistration(registrationRequest("new.user@petsitter.local"), "127.0.0.1");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("Bitte eine gültige deutsche Postleitzahl eingeben.");
        assertThat(userRepository.savedUser).isNull();
        assertThat(loginCodeService.requestedEmail).isNull();
    }

    @Test
    void updateCurrentUserProfileRejectsInvalidPostalCode() {
        User domainUser = user(UUID.randomUUID());
        UserRepositoryFake userRepository = new UserRepositoryFake(domainUser);
        PostalCodeService postalCodeService = postalCodeServiceReturning(
                PostalCodeValidationResult.invalid("Die Postleitzahl passt nicht zum angegebenen Ort."));

        UserAuthResult result = service(
                userRepository.repository(),
                authenticatedUser(Optional.of(domainUser)),
                new LoginCodeServiceFake(),
                petService("Keine Haustiere"),
                postalCodeService
        ).updateCurrentUserProfile(profileUpdateRequest());

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("Die Postleitzahl passt nicht zum angegebenen Ort.");
        assertThat(userRepository.saveCount).isZero();
    }

    @Test
    void completeRegistrationVerifiesPendingUser() {
        User pending = user(UUID.randomUUID());
        pending.setAccountStatus(AccountStatus.PENDING);
        pending.setDeleteAfter(LocalDateTime.now().plusHours(24));
        UserRepositoryFake userRepository = new UserRepositoryFake(pending);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();
        loginCodeService.validCode = true;

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .completeRegistration(new UserRegistrationConfirmationRequest("anna.mueller@petsitter.local", "123456"));

        assertThat(result.success()).isTrue();
        assertThat(pending.getAccountStatus()).isEqualTo(AccountStatus.VERIFIED);
        assertThat(pending.getDeleteAfter()).isNull();
        assertThat(loginCodeService.validatedEmail).isEqualTo("anna.mueller@petsitter.local");
        assertThat(loginCodeService.validatedCode).isEqualTo("123456");
    }

    @Test
    void completeRegistrationRejectsInvalidCode() {
        User pending = user(UUID.randomUUID());
        pending.setAccountStatus(AccountStatus.PENDING);
        UserRepositoryFake userRepository = new UserRepositoryFake(pending);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();
        loginCodeService.validCode = false;

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .completeRegistration(new UserRegistrationConfirmationRequest("anna.mueller@petsitter.local", "000000"));

        assertThat(result.success()).isFalse();
        assertThat(pending.getAccountStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(userRepository.saveCount).isZero();
    }

    @Test
    void cleanupExpiredPendingUsersDeletesExpiredPendingUsersAndInvalidatesCodes() {
        User expired = user(UUID.randomUUID());
        expired.setAccountStatus(AccountStatus.PENDING);
        expired.setDeleteAfter(LocalDateTime.now().minusMinutes(1));
        UserRepositoryFake userRepository = new UserRepositoryFake();
        userRepository.expiredUsers = List.of(expired);
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        int deleted = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .cleanupExpiredPendingUsers();

        assertThat(deleted).isEqualTo(1);
        assertThat(loginCodeService.invalidatedEmails).containsExactly(expired.getEmail());
        assertThat(userRepository.deletedUsers).containsExactly(expired);
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("anna.mueller@petsitter.local");
        user.setPasswordHash("$2y$10$uZHE15gXghc9i7PVWGhDOOJUt3vZgKg3oiknQQwv9D4lHzsIiBqP2");
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setDisplayName("Anna");
        user.setPhone("+49 221 111222");
        user.setBirthDate(LocalDate.of(1995, 5, 2));
        user.setNationality("deutsch");
        user.setLanguage("deutsch");
        user.setBio("Ich betreue gerne Hunde und Katzen.");
        user.setStreet("Rosenweg");
        user.setHouseNumber("14");
        user.setPostalCode("50667");
        user.setCity("Koeln");
        user.setAddressAddition("2. OG links");
        user.setCountry("Deutschland");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        user.setAccountStatus(AccountStatus.VERIFIED);
        return user;
    }

    private UserRegistrationRequest registrationRequest(String email) {
        return registrationRequest(email, STRONG_PASSWORD);
    }

    private UserRegistrationRequest registrationRequest(String email, String password) {
        return new UserRegistrationRequest(
                email,
                password,
                password,
                "Anna",
                "Mueller",
                "+49 221 111222",
                "Rosenweg",
                "14",
                "50667",
                "Koeln",
                "2. OG links",
                java.time.LocalDate.of(1990, 1, 1),
                "Deutsch",
                "Deutschland"
        );
    }

    private void assertWeakRegistrationPasswordRejected(String password, String expectedMessage) {
        UserRepositoryFake userRepository = new UserRepositoryFake();
        LoginCodeServiceFake loginCodeService = new LoginCodeServiceFake();

        UserAuthResult result = service(userRepository.repository(), authenticatedUser(Optional.empty()), loginCodeService)
                .startRegistration(registrationRequest("weak.password@petsitter.local", password), "127.0.0.1");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo(expectedMessage);
        assertThat(userRepository.savedUser).isNull();
        assertThat(loginCodeService.requestedEmail).isNull();
    }

    private UserProfileUpdateRequest profileUpdateRequest() {
        return new UserProfileUpdateRequest(
                "Anna",
                "Mueller",
                "Anna",
                "+49 221 111222",
                LocalDate.of(1995, 5, 2),
                "deutsch",
                "deutsch",
                "Ich betreue gerne Hunde und Katzen.",
                "Rosenweg",
                "14",
                "50667",
                "Koeln",
                "2. OG links",
                "Deutschland"
        );
    }

    private UserService service(
            UserRepository userRepository,
            AuthenticatedUser authenticatedUser,
            LoginCodeService loginCodeService
    ) {
        return service(userRepository, authenticatedUser, loginCodeService, petService("Keine Haustiere"));
    }

    private UserService service(
            UserRepository userRepository,
            AuthenticatedUser authenticatedUser,
            LoginCodeService loginCodeService,
            PetService petService
    ) {
        return service(userRepository, authenticatedUser, loginCodeService, petService,
                postalCodeServiceReturning(PostalCodeValidationResult.success()));
    }

    private UserService service(
            UserRepository userRepository,
            AuthenticatedUser authenticatedUser,
            LoginCodeService loginCodeService,
            PetService petService,
            PostalCodeService postalCodeService
    ) {
        return service(userRepository, authenticatedUser, loginCodeService, petService, postalCodeService,
                passwordPolicyService());
    }

    private UserService service(
            UserRepository userRepository,
            AuthenticatedUser authenticatedUser,
            LoginCodeService loginCodeService,
            PetService petService,
            PostalCodeService postalCodeService,
            PasswordPolicyService passwordPolicyService
    ) {
        return new UserService(userRepository, authenticatedUser, passwordEncoder, loginCodeService,
                petService, postalCodeService, passwordPolicyService);
    }

    private PasswordPolicyService passwordPolicyService(String... forbiddenTerms) {
        return new PasswordPolicyService(new ByteArrayResource(
                String.join("\n", forbiddenTerms).getBytes(StandardCharsets.UTF_8)));
    }

    private PostalCodeService postalCodeServiceReturning(PostalCodeValidationResult validationResult) {
        return new PostalCodeService(null, null) {
            @Override
            public PostalCodeValidationResult validateGermanPostalCode(String postalCode, String city) {
                return validationResult;
            }
        };
    }

    private PetService petService(String summary) {
        return new PetService(null, null) {
            @Override
            public String getPetSummaryForOwner(UUID ownerId) {
                return summary;
            }
        };
    }

    private AuthenticatedUser authenticatedUser(Optional<User> user) {
        return new AuthenticatedUser(repositoryReturning(user.orElse(null))) {
            @Override
            public Optional<User> get() {
                return user;
            }
        };
    }

    private UserRepository repositoryReturning(User user) {
        return new UserRepositoryFake(user).repository();
    }

    private static class LoginCodeServiceFake extends LoginCodeService {
        private String requestedEmail;
        private String requestedIp;
        private String validatedEmail;
        private String validatedCode;
        private boolean validCode = true;
        private final List<String> invalidatedEmails = new ArrayList<>();

        LoginCodeServiceFake() {
            super(null, null, null);
        }

        @Override
        public void requestLoginCode(String email, String requestIp) {
            this.requestedEmail = email;
            this.requestedIp = requestIp;
        }

        @Override
        public boolean validateLoginCode(String email, String plainCode) {
            this.validatedEmail = email;
            this.validatedCode = plainCode;
            return validCode;
        }

        @Override
        public void invalidateCodesForEmail(String email) {
            invalidatedEmails.add(email);
        }
    }

    private static class UserRepositoryFake {
        private final Map<String, User> usersByEmail = new HashMap<>();
        private final AtomicReference<UUID> requestedId = new AtomicReference<>();
        private User savedUser;
        private int saveCount;
        private List<User> expiredUsers = List.of();
        private final List<User> deletedUsers = new ArrayList<>();

        UserRepositoryFake(User... users) {
            for (User user : users) {
                if (user != null) {
                    usersByEmail.put(user.getEmail(), user);
                }
            }
        }

        UserRepository repository() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[] {UserRepository.class},
                    (proxy, method, args) -> {
                        if ("findById".equals(method.getName())) {
                            requestedId.set((UUID) args[0]);
                            return usersByEmail.values().stream()
                                    .filter(user -> args[0].equals(user.getId()))
                                    .findFirst();
                        }
                        if ("findByEmail".equals(method.getName())) {
                            return Optional.ofNullable(usersByEmail.get((String) args[0]));
                        }
                        if ("save".equals(method.getName())) {
                            savedUser = (User) args[0];
                            saveCount++;
                            usersByEmail.entrySet().removeIf(entry -> entry.getValue() == savedUser
                                    && !entry.getKey().equals(savedUser.getEmail()));
                            usersByEmail.put(savedUser.getEmail(), savedUser);
                            return savedUser;
                        }
                        if ("findByAccountStatusAndDeleteAfterLessThanEqual".equals(method.getName())) {
                            return expiredUsers;
                        }
                        if ("deleteAll".equals(method.getName())) {
                            for (Object user : (Iterable<?>) args[0]) {
                                deletedUsers.add((User) user);
                            }
                            return null;
                        }
                        if ("toString".equals(method.getName())) {
                            return "UserRepositoryFake";
                        }
                        throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                    }
            );
        }
    }
}
