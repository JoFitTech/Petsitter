package com.softwareengineering.petsitter.user.dto;

public record UserRegistrationConfirmationRequest(
        String email,
        String code
) {
}
