package com.softwareengineering.petsitter.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.PasswordResetCode;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.dto.PasswordResetConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.PasswordResetRequest;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.repository.PasswordResetCodeRepository;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordResetServiceTest {

    private static final String STRONG_PASSWORD = "Axiom-River8!Q";
    private static final String NEW_STRONG_PASSWORD = "Axiom-Forest9!Q";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void requestPasswordResetCreatesCodeForVerifiedUserAndSendsMail() {
        User user = user("anna.mueller@petsitter.local", AccountStatus.VERIFIED);
        PasswordResetCodeRepositoryFake codeRepository = new PasswordResetCodeRepositoryFake();
        PasswordResetMailServiceFake mailService = new PasswordResetMailServiceFake();

        UserAuthResult result = service(
                new UserRepositoryFake(user).repository(),
                codeRepository.repository(),
                mailService
        ).requestPasswordReset(new PasswordResetRequest(" Anna.Mueller@Petsitter.Local "), "127.0.0.1");

        assertThat(result.success()).isTrue();
        assertThat(codeRepository.savedCodes).hasSize(1);
        PasswordResetCode savedCode = codeRepository.savedCodes.get(0);
        assertThat(savedCode.getEmail()).isEqualTo("anna.mueller@petsitter.local");
        assertThat(savedCode.getRequestIp()).isEqualTo("127.0.0.1");
        assertThat(savedCode.getCodeHash()).isNotEqualTo(mailService.sentCode);
        assertThat(passwordEncoder.matches(mailService.sentCode, savedCode.getCodeHash())).isTrue();
        assertThat(mailService.sentEmail).isEqualTo("anna.mueller@petsitter.local");
    }

    @Test
    void requestPasswordResetDoesNotCreateCodeForUnknownOrPendingUser() {
        User pending = user("pending@petsitter.local", AccountStatus.PENDING);
        PasswordResetCodeRepositoryFake codeRepository = new PasswordResetCodeRepositoryFake();
        PasswordResetMailServiceFake mailService = new PasswordResetMailServiceFake();
        PasswordResetService service = service(
                new UserRepositoryFake(pending).repository(),
                codeRepository.repository(),
                mailService
        );

        UserAuthResult unknown = service.requestPasswordReset(
                new PasswordResetRequest("missing@petsitter.local"),
                "127.0.0.1");
        UserAuthResult unverified = service.requestPasswordReset(
                new PasswordResetRequest("pending@petsitter.local"),
                "127.0.0.1");

        assertThat(unknown.success()).isTrue();
        assertThat(unverified.success()).isTrue();
        assertThat(codeRepository.savedCodes).isEmpty();
        assertThat(mailService.sentEmail).isNull();
    }

    @Test
    void completePasswordResetRejectsWrongCodeAndIncrementsAttempts() {
        User user = user("anna.mueller@petsitter.local", AccountStatus.VERIFIED);
        PasswordResetCode code = codeFor("anna.mueller@petsitter.local", "123456");
        PasswordResetCodeRepositoryFake codeRepository = new PasswordResetCodeRepositoryFake(code);

        UserAuthResult result = service(
                new UserRepositoryFake(user).repository(),
                codeRepository.repository(),
                new PasswordResetMailServiceFake()
        ).completePasswordReset(new PasswordResetConfirmationRequest(
                "anna.mueller@petsitter.local",
                "000000",
                NEW_STRONG_PASSWORD,
                NEW_STRONG_PASSWORD
        ));

        assertThat(result.success()).isFalse();
        assertThat(code.getAttempts()).isEqualTo(1);
        assertThat(passwordEncoder.matches(STRONG_PASSWORD, user.getPasswordHash())).isTrue();
    }

    @Test
    void completePasswordResetRejectsWeakPasswordBeforeChangingHash() {
        User user = user("anna.mueller@petsitter.local", AccountStatus.VERIFIED);
        PasswordResetCode code = codeFor("anna.mueller@petsitter.local", "123456");
        PasswordResetCodeRepositoryFake codeRepository = new PasswordResetCodeRepositoryFake(code);

        UserAuthResult result = service(
                new UserRepositoryFake(user).repository(),
                codeRepository.repository(),
                new PasswordResetMailServiceFake()
        ).completePasswordReset(new PasswordResetConfirmationRequest(
                "anna.mueller@petsitter.local",
                "123456",
                "short",
                "short"
        ));

        assertThat(result.success()).isFalse();
        assertThat(code.getUsedAt()).isNull();
        assertThat(passwordEncoder.matches(STRONG_PASSWORD, user.getPasswordHash())).isTrue();
    }

    @Test
    void completePasswordResetUpdatesPasswordAndMarksCodeUsed() {
        User user = user("anna.mueller@petsitter.local", AccountStatus.VERIFIED);
        PasswordResetCode code = codeFor("anna.mueller@petsitter.local", "123456");
        PasswordResetCodeRepositoryFake codeRepository = new PasswordResetCodeRepositoryFake(code);
        UserRepositoryFake userRepository = new UserRepositoryFake(user);

        UserAuthResult result = service(
                userRepository.repository(),
                codeRepository.repository(),
                new PasswordResetMailServiceFake()
        ).completePasswordReset(new PasswordResetConfirmationRequest(
                " Anna.Mueller@Petsitter.Local ",
                "123456",
                NEW_STRONG_PASSWORD,
                NEW_STRONG_PASSWORD
        ));

        assertThat(result.success()).isTrue();
        assertThat(passwordEncoder.matches(NEW_STRONG_PASSWORD, user.getPasswordHash())).isTrue();
        assertThat(code.getUsedAt()).isNotNull();
        assertThat(userRepository.savedUser).isSameAs(user);
    }

    private PasswordResetService service(
            UserRepository userRepository,
            PasswordResetCodeRepository codeRepository,
            PasswordResetMailService mailService
    ) {
        return new PasswordResetService(
                userRepository,
                codeRepository,
                passwordEncoder,
                mailService,
                passwordPolicyService()
        );
    }

    private PasswordPolicyService passwordPolicyService() {
        return new PasswordPolicyService(new ByteArrayResource(
                "password\npetsitter".getBytes(StandardCharsets.UTF_8)));
    }

    private PasswordResetCode codeFor(String email, String plainCode) {
        PasswordResetCode code = new PasswordResetCode(
                email,
                passwordEncoder.encode(plainCode),
                LocalDateTime.now().plusMinutes(20),
                "127.0.0.1"
        );
        code.setCreatedAt(LocalDateTime.now());
        return code;
    }

    private User user(String email, AccountStatus status) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(STRONG_PASSWORD));
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setDisplayName("Anna");
        user.setPhone("+49 221 111222");
        user.setBirthDate(LocalDate.of(1995, 5, 2));
        user.setNationality("deutsch");
        user.setLanguage("deutsch");
        user.setStreet("Rosenweg");
        user.setHouseNumber("14");
        user.setPostalCode("50667");
        user.setCity("Koeln");
        user.setCountry("Deutschland");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        user.setAccountStatus(status);
        return user;
    }

    private static class PasswordResetMailServiceFake extends PasswordResetMailService {
        private String sentEmail;
        private String sentCode;

        @Override
        public void sendPasswordResetCode(String email, String plainCode) {
            this.sentEmail = email;
            this.sentCode = plainCode;
        }
    }

    private static class PasswordResetCodeRepositoryFake {
        private final List<PasswordResetCode> savedCodes = new ArrayList<>();

        PasswordResetCodeRepositoryFake(PasswordResetCode... initialCodes) {
            savedCodes.addAll(List.of(initialCodes));
        }

        PasswordResetCodeRepository repository() {
            return (PasswordResetCodeRepository) Proxy.newProxyInstance(
                    PasswordResetCodeRepository.class.getClassLoader(),
                    new Class<?>[] {PasswordResetCodeRepository.class},
                    (proxy, method, args) -> {
                        if ("save".equals(method.getName())) {
                            PasswordResetCode code = (PasswordResetCode) args[0];
                            if (code.getCreatedAt() == null) {
                                code.setCreatedAt(LocalDateTime.now());
                            }
                            if (!savedCodes.contains(code)) {
                                savedCodes.add(code);
                            }
                            return code;
                        }
                        if ("saveAll".equals(method.getName())) {
                            for (Object code : (Iterable<?>) args[0]) {
                                if (!savedCodes.contains(code)) {
                                    savedCodes.add((PasswordResetCode) code);
                                }
                            }
                            return args[0];
                        }
                        if ("findByEmailAndUsedAtIsNull".equals(method.getName())) {
                            String email = (String) args[0];
                            return savedCodes.stream()
                                    .filter(code -> email.equals(code.getEmail()))
                                    .filter(code -> code.getUsedAt() == null)
                                    .toList();
                        }
                        if ("findFirstByEmailAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc".equals(method.getName())) {
                            String email = (String) args[0];
                            LocalDateTime now = (LocalDateTime) args[1];
                            return savedCodes.stream()
                                    .filter(code -> email.equals(code.getEmail()))
                                    .filter(code -> code.getUsedAt() == null)
                                    .filter(code -> code.getExpiresAt().isAfter(now))
                                    .max(Comparator.comparing(PasswordResetCode::getCreatedAt));
                        }
                        if ("toString".equals(method.getName())) {
                            return "PasswordResetCodeRepositoryFake";
                        }
                        throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                    }
            );
        }
    }

    private static class UserRepositoryFake {
        private final Map<String, User> usersByEmail = new HashMap<>();
        private User savedUser;

        UserRepositoryFake(User... users) {
            for (User user : users) {
                usersByEmail.put(user.getEmail(), user);
            }
        }

        UserRepository repository() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[] {UserRepository.class},
                    (proxy, method, args) -> {
                        if ("findByEmail".equals(method.getName())) {
                            return Optional.ofNullable(usersByEmail.get((String) args[0]));
                        }
                        if ("save".equals(method.getName())) {
                            savedUser = (User) args[0];
                            usersByEmail.put(savedUser.getEmail(), savedUser);
                            return savedUser;
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
