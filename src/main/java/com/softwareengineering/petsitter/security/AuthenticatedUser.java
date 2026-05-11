package com.softwareengineering.petsitter.security;

import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
        Authentication authentication = getAuthentication();
        if (!isUsableAuthentication(authentication)) {
            return Optional.empty();
        }
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .filter(user -> user.getAccountStatus() == AccountStatus.VERIFIED);
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isUsableAuthentication(authentication)) {
            return authentication;
        }

        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return authentication;
        }

        Object storedContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (storedContext instanceof SecurityContext securityContext) {
            return securityContext.getAuthentication();
        }
        return authentication;
    }

    private boolean isUsableAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
