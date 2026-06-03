package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.review.dto.UserReviewDto;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import java.time.LocalDate;
import java.util.List;
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
        ImageRefDto profileImage,
        UserRatingSummary ratingSummary,
        List<UserReviewDto> recentReviews
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
        this(id, displayName, birthDate, language, bio, postalCode, city, petSummary, accountStatus, null, null, null);
    }

    public PublicUserProfileDto(
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
        this(id, displayName, birthDate, language, bio, postalCode, city, petSummary, accountStatus, profileImage, null, null);
    }
}
