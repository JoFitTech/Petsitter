package com.softwareengineering.petsitter.security;

import com.softwareengineering.petsitter.config.PetsitterSecurityProperties;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService, der zunaechst DB-User anhand der Email laedt
 * und optional auf einen Demo-User als Fallback zurueckfaellt.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

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
        User user = userRepository.findByEmail(username)
                .orElse(null);

        if (user != null) {
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles("USER")
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
}


