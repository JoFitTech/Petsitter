package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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
        Set<OfferAnimalType> animalTypes,
        Set<DayOfWeek> recurringWeekdays,
        OfferTimeSlot timeSlot
) {
    public static final int ANY_DISTANCE_KM = -1;

    public OfferSearchCriteria(OfferSearchMode mode, LocalDate from, LocalDate to,
            OfferDateFilterMode dateFilterMode, int dateFlexDays, BigDecimal earnings, int distanceKm,
            OfferCareType careType, OfferFrequency frequency, Set<OfferAnimalType> animalTypes) {
        this(mode, from, to, dateFilterMode, dateFlexDays, earnings, distanceKm, null,
                careType, frequency, animalTypes, Set.of(), null);
    }

    public OfferSearchCriteria(OfferSearchMode mode, LocalDate from, LocalDate to,
            OfferDateFilterMode dateFilterMode, int dateFlexDays, BigDecimal earnings, int distanceKm,
            String originPostalCode, OfferCareType careType, OfferFrequency frequency, Set<OfferAnimalType> animalTypes) {
        this(mode, from, to, dateFilterMode, dateFlexDays, earnings, distanceKm, originPostalCode,
                careType, frequency, animalTypes, Set.of(), null);
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
        recurringWeekdays = recurringWeekdays == null ? Set.of() : Set.copyOf(recurringWeekdays);
    }

    public boolean hasUnlimitedDistance() {
        return distanceKm == ANY_DISTANCE_KM;
    }
}
