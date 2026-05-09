package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.user.domain.AccountRole;
import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String street,
        String houseNumber,
        String postalCode,
        String city,
        String addressAddition,
        AccountRole accountRole
) {
}
