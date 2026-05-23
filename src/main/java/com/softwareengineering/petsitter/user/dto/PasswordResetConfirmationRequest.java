package com.softwareengineering.petsitter.user.dto;

public record PasswordResetConfirmationRequest(
        String email,
        String code,
        String password,
        String confirmPassword
) {
}
