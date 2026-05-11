package com.softwareengineering.petsitter.user.dto;

import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String street,
        String houseNumber,
        String postalCode,
        String city,
        String addressAddition,
        AccountRole accountRole,
        AccountStatus accountStatus
) {
}
