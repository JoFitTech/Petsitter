package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class ProfileMenuTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void treatsAnonymousAuthenticationAsSignedOut() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        ));

        assertThat(ProfileMenu.hasAuthenticatedSession()).isFalse();
    }

    @Test
    void detectsAuthenticatedSession() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "localuser",
                "localpass",
                AuthorityUtils.createAuthorityList("ROLE_USER")
        ));

        assertThat(ProfileMenu.hasAuthenticatedSession()).isTrue();
    }

    @Test
    void guestMenuOffersLogin() {
        ProfileMenu menu = new ProfileMenu(false);

        assertThat(menu.getActionLabel()).isEqualTo("Login");
        assertThat(menu.getActionTarget()).isEqualTo("login");
    }

    @Test
    void authenticatedMenuOffersLogout() {
        ProfileMenu menu = new ProfileMenu(true);

        assertThat(menu.getActionLabel()).isEqualTo("Logout");
        assertThat(menu.getActionTarget()).isEqualTo("/logout");
    }
}
