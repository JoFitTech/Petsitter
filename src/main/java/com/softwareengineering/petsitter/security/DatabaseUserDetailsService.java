package com.softwareengineering.petsitter.security;

import com.softwareengineering.petsitter.config.PetsitterSecurityProperties;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService, der zunaechst DB-User anhand der Email laedt
 * und optional auf einen Demo-User als Fallback zurueckfaellt.
 */
@Service
@Primary
public class DatabaseUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUserDetailsService.class);

    private final UserRepository userRepository;
    private final PetsitterSecurityProperties securityProperties;
    private final UserDetails demoUser;

    public DatabaseUserDetailsService(
            UserRepository userRepository,
            PetsitterSecurityProperties securityProperties,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.securityProperties = securityProperties;

        this.demoUser = org.springframework.security.core.userdetails.User
                .withUsername(securityProperties.getDemo().getUsername())
                .password(passwordEncoder.encode(securityProperties.getDemo().getPassword()))
                .roles(normalizeRole(securityProperties.getDemo().getRole()))
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        try {
            user = userRepository.findByEmail(username).orElse(null);
        } catch (RuntimeException ex) {
            // Bei fehlendem Schema/DB-Problemen weiterhin Demo-Login erlauben statt 500.
            log.warn("DB-User-Lookup fehlgeschlagen, pruefe Demo-Fallback fuer {}: {}", username, ex.getMessage());
        }

        if (user != null) {
            if (user.getAccountStatus() != AccountStatus.VERIFIED) {
                throw new UsernameNotFoundException("Benutzer ist nicht verifiziert: " + username);
            }

            // Spring Security braucht einen non-null Passwortwert.
            String password = user.getPasswordHash();
            if (password == null || password.isBlank()) {
                password = ""; // Spring Security braucht einen non-null Wert
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(password)
                    .roles(toSecurityRole(user.getAccountRole()))
                    .build();
        }

        if (securityProperties.getDemo().isEnabled()
                && securityProperties.getDemo().getUsername().equals(username)) {
            return demoUser;
        }

        throw new UsernameNotFoundException("Benutzer nicht gefunden: " + username);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.replace("ROLE_", "").toUpperCase(Locale.ROOT);
    }

    private String toSecurityRole(AccountRole accountRole) {
        if (accountRole == null) {
            return AccountRole.SIGNED_IN_USER.name();
        }
        return normalizeRole(accountRole.name());
    }
}
