package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String displayName,
        String phone,
        LocalDate birthDate,
        String nationality,
        String language,
        String bio,
        String street,
        String houseNumber,
        String postalCode,
        String city,
        String addressAddition,
        String country,
        String pendingEmail,
        LocalDateTime pendingEmailRequestedAt,
        String petSummary,
        AccountRole accountRole,
        AccountStatus accountStatus
) {
}
