package com.softwareengineering.petsitter.security;

import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Hilfskomponente, um den aktuell angemeldeten Domain-User zu laden.
 */
@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;

    public AuthenticatedUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .filter(user -> user.getAccountStatus() == AccountStatus.VERIFIED);
    }
}
