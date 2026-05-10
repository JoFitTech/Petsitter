package com.softwareengineering.petsitter.offer.service;

import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

final class CreateOfferFormRules {

    static final int DESCRIPTION_MAX_LENGTH = 255;

    private static final String SELECT_DATES_MESSAGE = "Bitte Start- und Enddatum auswaehlen.";
    private static final String INVALID_DATE_RANGE_MESSAGE = "Das Enddatum muss am oder nach dem Startdatum liegen.";

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

    private String summarizeDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return SELECT_DATES_MESSAGE;
        }

        if (startDate.isAfter(endDate)) {
            return INVALID_DATE_RANGE_MESSAGE;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return "Gesamtdauer: " + totalDays + " Tag(e), inklusive Start- und Enddatum.";
    }
}
