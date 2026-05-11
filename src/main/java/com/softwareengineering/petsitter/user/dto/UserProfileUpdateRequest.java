package com.softwareengineering.petsitter.user.dto;

import java.time.LocalDate;

public record UserProfileUpdateRequest(
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
        String country
) {
}
