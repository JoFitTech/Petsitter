package com.softwareengineering.petsitter.location.dto;

public record PostalCodeValidationResult(
        boolean valid,
        String message
) {
    public static PostalCodeValidationResult success() {
        return new PostalCodeValidationResult(true, null);
    }

    public static PostalCodeValidationResult invalid(String message) {
        return new PostalCodeValidationResult(false, message);
    }
}
