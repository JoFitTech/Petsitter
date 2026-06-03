package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
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
        AccountStatus accountStatus,
        ImageRefDto profileImage
) {
    public PublicUserProfileDto(
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
        this(id, displayName, birthDate, language, bio, postalCode, city, petSummary, accountStatus, null);
    }
}
