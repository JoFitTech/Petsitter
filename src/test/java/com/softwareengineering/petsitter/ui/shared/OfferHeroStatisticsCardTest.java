package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.offer.dto.OfferHeroStatisticsDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OfferHeroStatisticsCardTest {

    @Test
    void rendersCityScopedSingularWithoutMissingRating() {
        OfferHeroStatisticsCard card = new OfferHeroStatisticsCard(
                new OfferHeroStatisticsDto(1, Optional.empty(), true),
                "Sitter-Angebot",
                "Sitter-Angebote");

        assertThat(containsText(card, "Aktuell verfügbar")).isTrue();
        assertThat(containsText(card, "1 offenes Sitter-Angebot in deiner Umgebung")).isTrue();
        assertThat(containsText(card, "Sterne")).isFalse();
    }

    @Test
    void rendersGlobalPlural() {
        OfferHeroStatisticsCard card = new OfferHeroStatisticsCard(
                new OfferHeroStatisticsDto(3, Optional.empty(), false),
                "Halter-Angebot",
                "Halter-Angebote");

        assertThat(containsText(card, "3 offene Halter-Angebote insgesamt")).isTrue();
    }

    @Test
    void rendersGermanFormattedAverageRatingWhenAvailable() {
        OfferHeroStatisticsCard card = new OfferHeroStatisticsCard(
                new OfferHeroStatisticsDto(3, Optional.of(new BigDecimal("4.9")), false),
                "Halter-Angebot",
                "Halter-Angebote");

        assertThat(containsText(card, "Ø 4,9 Sterne")).isTrue();
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && hasText.getText() != null && hasText.getText().contains(text)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }
}
