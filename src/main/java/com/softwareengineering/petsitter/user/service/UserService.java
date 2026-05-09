package com.softwareengineering.petsitter.user.service;

import com.softwareengineering.petsitter.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public Optional<User> findUserById(UUID userId) {
        return Optional.empty();
    }

    public String getCurrentUser() {
        return "localuser";
    }
}
