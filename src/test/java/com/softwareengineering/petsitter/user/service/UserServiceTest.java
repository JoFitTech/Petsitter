package com.softwareengineering.petsitter.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    @Test
    void findUserByIdLoadsUserFromRepository() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        AtomicReference<UUID> requestedId = new AtomicReference<>();
        UserRepository userRepository = repositoryReturning(user, requestedId);
        UserService userService = new UserService(userRepository, authenticatedUser(Optional.empty()));

        Optional<User> result = userService.findUserById(userId);

        assertThat(result).contains(user);
        assertThat(requestedId).hasValue(userId);
    }

    @Test
    void getCurrentUserProfileMapsAuthenticatedUserToDto() {
        UUID userId = UUID.randomUUID();
        User domainUser = user(userId);
        UserService userService = new UserService(repositoryReturning(domainUser), authenticatedUser(Optional.of(domainUser)));

        Optional<UserProfileDto> result = userService.getCurrentUserProfile();

        assertThat(result).hasValueSatisfying(profile -> {
            assertThat(profile.id()).isEqualTo(userId);
            assertThat(profile.email()).isEqualTo("anna.mueller@petsitter.local");
            assertThat(profile.firstName()).isEqualTo("Anna");
            assertThat(profile.lastName()).isEqualTo("Mueller");
            assertThat(profile.street()).isEqualTo("Rosenweg");
            assertThat(profile.houseNumber()).isEqualTo("14");
            assertThat(profile.postalCode()).isEqualTo("50667");
            assertThat(profile.city()).isEqualTo("Koeln");
            assertThat(profile.addressAddition()).isEqualTo("2. OG links");
            assertThat(profile.accountRole()).isEqualTo(AccountRole.SIGNED_IN_USER);
        });
    }

    @Test
    void getCurrentUserUsesAuthenticatedUsersFullName() {
        User domainUser = user(UUID.randomUUID());
        UserService userService = new UserService(repositoryReturning(domainUser), authenticatedUser(Optional.of(domainUser)));

        assertThat(userService.getCurrentUser()).isEqualTo("Anna Mueller");
    }

    @Test
    void getCurrentUserFallsBackToGuestWhenNoUserIsAuthenticated() {
        UserService userService = new UserService(repositoryReturning(null), authenticatedUser(Optional.empty()));

        assertThat(userService.getCurrentUser()).isEqualTo("Gast");
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("anna.mueller@petsitter.local");
        user.setPasswordHash("$2y$10$uZHE15gXghc9i7PVWGhDOOJUt3vZgKg3oiknQQwv9D4lHzsIiBqP2");
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setStreet("Rosenweg");
        user.setHouseNumber("14");
        user.setPostalCode("50667");
        user.setCity("Koeln");
        user.setAddressAddition("2. OG links");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        return user;
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
        return repositoryReturning(user, new AtomicReference<>());
    }

    private UserRepository repositoryReturning(User user, AtomicReference<UUID> requestedId) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] {UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        requestedId.set((UUID) args[0]);
                        return Optional.ofNullable(user);
                    }
                    if ("findByEmail".equals(method.getName())) {
                        return Optional.ofNullable(user);
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                }
        );
    }
}
