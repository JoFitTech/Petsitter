package com.softwareengineering.petsitter.user.dto;

public record UserAuthResult(
        boolean success,
        String message,
        UserProfileDto userProfile
) {
    public static UserAuthResult success(String message, UserProfileDto userProfile) {
        return new UserAuthResult(true, message, userProfile);
    }

    public static UserAuthResult failure(String message) {
        return new UserAuthResult(false, message, null);
    }
}
