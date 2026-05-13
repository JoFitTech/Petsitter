package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferSearchCriteria(
        OfferSearchMode mode,
        LocalDate from,
        LocalDate to,
        OfferDateFilterMode dateFilterMode,
        int dateFlexDays,
        BigDecimal earnings,
        int distanceKm
) {
    public OfferSearchCriteria {
        if (mode == null) {
            mode = OfferSearchMode.TIERSITTER;
        }
        if (dateFilterMode == null) {
            dateFilterMode = OfferDateFilterMode.OVERLAP;
        }
        if (dateFlexDays < 0) {
            dateFlexDays = 0;
        }
    }
}
