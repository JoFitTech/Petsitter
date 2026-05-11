package com.softwareengineering.petsitter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.config.PetsitterSecurityProperties;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class DatabaseUserDetailsServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void loadsDatabaseUserByEmail() {
        String passwordHash = passwordEncoder.encode("secret");
        User user = user("anna.mueller@petsitter.local", passwordHash, AccountRole.SIGNED_IN_USER);
        DatabaseUserDetailsService service = service(repositoryReturning(Optional.of(user)), properties(true));

        UserDetails result = service.loadUserByUsername("anna.mueller@petsitter.local");

        assertThat(result.getUsername()).isEqualTo("anna.mueller@petsitter.local");
        assertThat(result.getPassword()).isEqualTo(passwordHash);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_SIGNED_IN_USER");
    }

    @Test
    void fallsBackToDemoUserWhenEnabled() {
        DatabaseUserDetailsService service = service(repositoryReturning(Optional.empty()), properties(true));

        UserDetails result = service.loadUserByUsername("localuser");

        assertThat(result.getUsername()).isEqualTo("localuser");
        assertThat(passwordEncoder.matches("localpass", result.getPassword())).isTrue();
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void doesNotUseDemoFallbackWhenDisabled() {
        DatabaseUserDetailsService service = service(repositoryReturning(Optional.empty()), properties(false));

        assertThatThrownBy(() -> service.loadUserByUsername("localuser"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void allowsDemoFallbackWhenDatabaseLookupFails() {
        DatabaseUserDetailsService service = service(repositoryThrowing(), properties(true));

        UserDetails result = service.loadUserByUsername("localuser");

        assertThat(result.getUsername()).isEqualTo("localuser");
    }

    @Test
    void rejectsUnknownUser() {
        DatabaseUserDetailsService service = service(repositoryReturning(Optional.empty()), properties(true));

        assertThatThrownBy(() -> service.loadUserByUsername("unknown@petsitter.local"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private DatabaseUserDetailsService service(
            UserRepository userRepository,
            PetsitterSecurityProperties properties
    ) {
        return new DatabaseUserDetailsService(userRepository, properties, passwordEncoder);
    }

    private PetsitterSecurityProperties properties(boolean demoEnabled) {
        PetsitterSecurityProperties properties = new PetsitterSecurityProperties();
        properties.getDemo().setEnabled(demoEnabled);
        properties.getDemo().setUsername("localuser");
        properties.getDemo().setPassword("localpass");
        properties.getDemo().setRole("USER");
        return properties;
    }

    private User user(String email, String passwordHash, AccountRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setAccountRole(role);
        user.setAccountStatus(AccountStatus.VERIFIED);
        return user;
    }

    private UserRepository repositoryReturning(Optional<User> user) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] {UserRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmail".equals(method.getName())) {
                        return user;
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }

    private UserRepository repositoryThrowing() {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] {UserRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmail".equals(method.getName())) {
                        throw new IllegalStateException("database unavailable");
                    }
                    if ("toString".equals(method.getName())) {
                        return "FailingUserRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }
}
