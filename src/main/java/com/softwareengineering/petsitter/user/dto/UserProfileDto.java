package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.review.dto.UserReviewDto;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        AccountStatus accountStatus,
        ImageRefDto profileImage,
        UserRatingSummary ratingSummary,
        List<UserReviewDto> recentReviews
) {
    public UserProfileDto(
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
        this(id, email, firstName, lastName, displayName, phone, birthDate, nationality, language, bio,
                street, houseNumber, postalCode, city, addressAddition, country, pendingEmail,
                pendingEmailRequestedAt, petSummary, accountRole, accountStatus, null, null, List.of());
    }

    public UserProfileDto(
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
            AccountStatus accountStatus,
            ImageRefDto profileImage
    ) {
        this(id, email, firstName, lastName, displayName, phone, birthDate, nationality, language, bio,
                street, houseNumber, postalCode, city, addressAddition, country, pendingEmail,
                pendingEmailRequestedAt, petSummary, accountRole, accountStatus, profileImage, null, List.of());
    }

    public UserProfileDto {
        recentReviews = recentReviews == null ? List.of() : List.copyOf(recentReviews);
    }
}
