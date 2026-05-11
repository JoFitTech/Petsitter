package com.softwareengineering.petsitter.user.dto;

public record UserLoginRequest(
        String email,
        String password
) {
}
