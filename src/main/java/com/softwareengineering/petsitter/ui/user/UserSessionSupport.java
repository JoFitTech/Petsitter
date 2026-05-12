package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

final class UserSessionSupport {

    private UserSessionSupport() {
    }

    static void authenticate(UserProfileDto profile) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                profile.email(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + profile.accountRole().name())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        UI.getCurrent().getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);
        if (VaadinService.getCurrentRequest() != null) {
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context);
        }
    }

    static void logout() {
        SecurityContextHolder.clearContext();
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    null);
        }
        if (VaadinService.getCurrentRequest() != null) {
            VaadinService.getCurrentRequest().getWrappedSession().removeAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        }
    }
}
