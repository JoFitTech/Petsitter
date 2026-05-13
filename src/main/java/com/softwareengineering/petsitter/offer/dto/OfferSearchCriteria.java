package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferSearchCriteria(
        OfferSearchMode mode,
        LocalDate from,
        LocalDate to,
        BigDecimal earnings,
        int distanceKm
) {
    public OfferSearchCriteria {
        if (mode == null) {
            mode = OfferSearchMode.TIERSITTER;
        }
    }
}
