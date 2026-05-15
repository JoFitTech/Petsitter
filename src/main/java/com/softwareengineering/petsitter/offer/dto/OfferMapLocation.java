package com.softwareengineering.petsitter.offer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OfferMapLocation(
        UUID offerId,
        String title,
        String postalCode,
        String placeName,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
