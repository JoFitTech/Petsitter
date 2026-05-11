package com.softwareengineering.petsitter.offer.service;

import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

final class CreateOfferFormRules {

    static final int DESCRIPTION_MAX_LENGTH = 255;

    private static final String SELECT_DATES_MESSAGE = "Bitte Start- und Enddatum auswaehlen.";
    private static final String INVALID_DATE_RANGE_MESSAGE = "Das Enddatum muss am oder nach dem Startdatum liegen.";
    private static final String MISSING_TOTAL_PRICE_MESSAGE = "Gesamtpreis: - EUR";

    private final Clock clock;

    CreateOfferFormRules() {
        this(Clock.systemDefaultZone());
    }

    CreateOfferFormRules(Clock clock) {
        this.clock = clock;
    }

    LocalDate minimumStartDate() {
        return LocalDate.now(clock);
    }

    LocalDate minimumEndDate(LocalDate startDate) {
        LocalDate tomorrow = minimumStartDate().plusDays(1);
        if (startDate != null && startDate.isAfter(tomorrow)) {
            return startDate;
        }
        return tomorrow;
    }

    CreateOfferDateSelection initialDateSelection() {
        return new CreateOfferDateSelection(minimumEndDate(null), false, summarizeDateRange(null, null));
    }

    CreateOfferDateSelection updateDateSelection(LocalDate startDate, LocalDate endDate) {
        LocalDate minimumEndDate = minimumEndDate(startDate);
        boolean clearEndDate = endDate != null && endDate.isBefore(minimumEndDate);
        LocalDate effectiveEndDate = clearEndDate ? null : endDate;

        return new CreateOfferDateSelection(
                minimumEndDate,
                clearEndDate,
                summarizeDateRange(startDate, effectiveEndDate));
    }

    int descriptionMaxLength() {
        return DESCRIPTION_MAX_LENGTH;
    }

    private long totalDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    String totalPrice(LocalDate startDate, LocalDate endDate, BigDecimal price) {
        if (startDate == null || endDate == null || price == null) {
            return MISSING_TOTAL_PRICE_MESSAGE;
        }

        if (startDate.isAfter(endDate)) {
            return INVALID_DATE_RANGE_MESSAGE;
        }

        BigDecimal calculatedTotalPrice = price
                .multiply(BigDecimal.valueOf(totalDays(startDate, endDate)))
                .setScale(2, RoundingMode.HALF_UP);
        return "Gesamtpreis: " + calculatedTotalPrice.toPlainString() + " EUR";
    }

    private String summarizeDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return SELECT_DATES_MESSAGE;
        }

        if (startDate.isAfter(endDate)) {
            return INVALID_DATE_RANGE_MESSAGE;
        }

        long totalDays = totalDays(startDate, endDate);
        return "Gesamtdauer: " + totalDays + " Tag(e), inklusive Start- und Enddatum.";
    }
}
