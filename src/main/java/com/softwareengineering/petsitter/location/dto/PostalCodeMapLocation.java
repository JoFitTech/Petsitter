package com.softwareengineering.petsitter.location.dto;

import java.math.BigDecimal;

public record PostalCodeMapLocation(
        String postalCode,
        String placeName,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
