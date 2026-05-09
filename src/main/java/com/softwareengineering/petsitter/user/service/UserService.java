package com.softwareengineering.petsitter.user.service;

import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;

    public UserService(UserRepository userRepository, AuthenticatedUser authenticatedUser) {
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
    }

    public Optional<User> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<UserProfileDto> getCurrentUserProfile() {
        return authenticatedUser.get().map(this::toProfileDto);
    }

    public String getCurrentUser() {
        return authenticatedUser.get()
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Gast");
    }

    private UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStreet(),
                user.getHouseNumber(),
                user.getPostalCode(),
                user.getCity(),
                user.getAddressAddition(),
                user.getAccountRole()
        );
    }
}
