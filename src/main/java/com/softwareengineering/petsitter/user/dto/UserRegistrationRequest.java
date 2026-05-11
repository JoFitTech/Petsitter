package com.softwareengineering.petsitter.user.dto;

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
        String addressAddition
) {
}
