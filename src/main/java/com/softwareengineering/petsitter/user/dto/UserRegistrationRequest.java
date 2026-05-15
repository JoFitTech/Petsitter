package com.softwareengineering.petsitter.user.dto;

import java.time.LocalDate;

public record UserRegistrationRequest(
        String email,
        String password,
        String confirmPassword,
        String firstName,
        String lastName,
        String phone,
        String street,
        String houseNumber,
        String postalCode,
        String city,
        String addressAddition,
        LocalDate birthDate,
        String nationality,
        String country
) {
}
