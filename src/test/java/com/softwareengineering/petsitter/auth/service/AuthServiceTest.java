package com.softwareengineering.petsitter.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class AuthServiceTest {

    @Test
    void getOrCreateUserReturnsExistingUserWithNormalizedEmail() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        LoginCodeService loginCodeService = org.mockito.Mockito.mock(LoginCodeService.class);
        AuthService authService = new AuthService(userRepository, loginCodeService);

        User existing = user("anna.mueller@petsitter.local");
        when(userRepository.findByEmail("anna.mueller@petsitter.local")).thenReturn(Optional.of(existing));

        User result = authService.getOrCreateUser("  Anna.Mueller@Petsitter.Local  ");

        assertThat(result).isSameAs(existing);
        verify(userRepository).findByEmail("anna.mueller@petsitter.local");
    }

    @Test
    void getOrCreateUserCreatesNewUserWhenMissing() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        LoginCodeService loginCodeService = org.mockito.Mockito.mock(LoginCodeService.class);
        AuthService authService = new AuthService(userRepository, loginCodeService);

        when(userRepository.findByEmail("new.user@petsitter.local")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = authService.getOrCreateUser("new.user@petsitter.local");

        assertThat(created.getEmail()).isEqualTo("new.user@petsitter.local");
        assertThat(created.getAccountRole()).isEqualTo(AccountRole.SIGNED_IN_USER);
        assertThat(created.getPasswordHash()).isEqualTo("");
    }

    @Test
    void getOrCreateUserHandlesParallelInsertAndLoadsExistingUser() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        LoginCodeService loginCodeService = org.mockito.Mockito.mock(LoginCodeService.class);
        AuthService authService = new AuthService(userRepository, loginCodeService);

        User existingAfterRace = user("race@petsitter.local");

        when(userRepository.findByEmail("race@petsitter.local"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingAfterRace));
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        User result = authService.getOrCreateUser("race@petsitter.local");

        assertThat(result).isSameAs(existingAfterRace);
    }

    @Test
    void requestCodeForEmailUsesNormalizedEmailAndEnsuresUserExists() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        LoginCodeService loginCodeService = org.mockito.Mockito.mock(LoginCodeService.class);
        AuthService authService = new AuthService(userRepository, loginCodeService);

        User existing = user("anna.mueller@petsitter.local");
        when(userRepository.findByEmail("anna.mueller@petsitter.local")).thenReturn(Optional.of(existing));

        authService.requestCodeForEmail("  Anna.Mueller@Petsitter.Local  ", "127.0.0.1");

        verify(loginCodeService).requestLoginCode(eq("anna.mueller@petsitter.local"), eq("127.0.0.1"));
    }

    @Test
    void verifyCodeAndGetUserUsesNormalizedEmail() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        LoginCodeService loginCodeService = org.mockito.Mockito.mock(LoginCodeService.class);
        AuthService authService = new AuthService(userRepository, loginCodeService);

        User existing = user("anna.mueller@petsitter.local");
        when(loginCodeService.validateLoginCode("anna.mueller@petsitter.local", "123456")).thenReturn(true);
        when(userRepository.findByEmail("anna.mueller@petsitter.local")).thenReturn(Optional.of(existing));

        Optional<User> result = authService.verifyCodeAndGetUser("  ANNA.MUELLER@PETSITTER.LOCAL  ", "123456");

        assertThat(result).contains(existing);
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        user.setPasswordHash("");
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setStreet("Rosenweg");
        user.setHouseNumber("14");
        user.setPostalCode("50667");
        user.setCity("Koeln");
        return user;
    }
}

