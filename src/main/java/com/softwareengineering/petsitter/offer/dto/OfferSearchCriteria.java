package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record OfferSearchCriteria(
        OfferSearchMode mode,
        LocalDate from,
        LocalDate to,
        OfferDateFilterMode dateFilterMode,
        int dateFlexDays,
        BigDecimal earnings,
        int distanceKm,
        String originPostalCode,
        OfferCareType careType,
        OfferFrequency frequency,
        Set<OfferAnimalType> animalTypes
) {
    public OfferSearchCriteria(OfferSearchMode mode, LocalDate from, LocalDate to,
            OfferDateFilterMode dateFilterMode, int dateFlexDays, BigDecimal earnings, int distanceKm,
            OfferCareType careType, OfferFrequency frequency, Set<OfferAnimalType> animalTypes) {
        this(mode, from, to, dateFilterMode, dateFlexDays, earnings, distanceKm, null,
                careType, frequency, animalTypes);
    }

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
        originPostalCode = originPostalCode == null || originPostalCode.isBlank()
                ? null
                : originPostalCode.trim();
        animalTypes = animalTypes == null ? Set.of() : Set.copyOf(animalTypes);
    }
}
