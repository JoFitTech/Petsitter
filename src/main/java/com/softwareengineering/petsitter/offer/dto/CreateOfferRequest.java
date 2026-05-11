package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateOfferRequest(
        OfferType offerType,
        LocalDate startDate,
        LocalDate endDate,
        UUID petId,
        BigDecimal price,
        String description
) {
}
