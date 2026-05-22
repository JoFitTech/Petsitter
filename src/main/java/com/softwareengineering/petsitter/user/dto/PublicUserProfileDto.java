package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.user.domain.AccountStatus;
import java.time.LocalDate;
import java.util.UUID;

public record PublicUserProfileDto(
        UUID id,
        String displayName,
        LocalDate birthDate,
        String language,
        String bio,
        String postalCode,
        String city,
        String petSummary,
        AccountStatus accountStatus
) {
}
