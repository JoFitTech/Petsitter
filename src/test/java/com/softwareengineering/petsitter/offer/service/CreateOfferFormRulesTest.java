package com.softwareengineering.petsitter.offer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.offer.dto.CreateOfferDateSelection;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CreateOfferFormRulesTest {

    private final CreateOfferFormRules rules = new CreateOfferFormRules(
            Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void initialDateSelectionUsesTomorrowAsMinimumEndDate() {
        CreateOfferDateSelection result = rules.initialDateSelection();

        assertThat(rules.minimumStartDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(result.minimumEndDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(result.clearEndDate()).isFalse();
        assertThat(result.summary()).isEqualTo("Bitte Start- und Enddatum auswaehlen.");
    }

    @Test
    void updateDateSelectionMovesMinimumEndDateToLaterStartAndClearsInvalidEndDate() {
        CreateOfferDateSelection result = rules.updateDateSelection(
                LocalDate.of(2026, 5, 14),
                LocalDate.of(2026, 5, 12));

        assertThat(result.minimumEndDate()).isEqualTo(LocalDate.of(2026, 5, 14));
        assertThat(result.clearEndDate()).isTrue();
        assertThat(result.summary()).isEqualTo("Bitte Start- und Enddatum auswaehlen.");
    }

    @Test
    void updateDateSelectionSummarizesInclusiveDateRange() {
        CreateOfferDateSelection result = rules.updateDateSelection(
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12));

        assertThat(result.minimumEndDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(result.clearEndDate()).isFalse();
        assertThat(result.summary()).isEqualTo("Gesamtdauer: 3 Tag(e), inklusive Start- und Enddatum.");
    }
}
