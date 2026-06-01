package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.dto.OfferHeroStatisticsDto;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class OfferHeroStatisticsCard extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";

    public OfferHeroStatisticsCard(OfferHeroStatisticsDto statistics, String singularLabel, String pluralLabel) {
        OfferHeroStatisticsDto safeStatistics = statistics != null
                ? statistics
                : new OfferHeroStatisticsDto(0, Optional.empty(), false);

        getStyle()
                .set("background", "white")
                .set("border-radius", "28px")
                .set("padding", "26px 34px")
                .set("width", "210px")
                .set("min-width", "210px")
                .set("margin-top", "10px")
                .set("box-shadow", CARD_SHADOW);

        Span available = new Span("Aktuell verfügbar");
        available.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("color", "#71946e")
                .set("margin-bottom", "8px");

        H2 headline = new H2(formatHeadline(safeStatistics, singularLabel, pluralLabel));
        headline.getStyle()
                .set("font-size", "26px")
                .set("line-height", "1.12")
                .set("margin", "0 0 14px 0")
                .set("color", DARK);

        add(available, headline);
        safeStatistics.averageRating()
                .ifPresent(averageRating -> add(createRating(averageRating)));
    }

    static String formatHeadline(OfferHeroStatisticsDto statistics, String singularLabel, String pluralLabel) {
        long count = statistics.openOfferCount();
        String offerLabel = count == 1 ? singularLabel : pluralLabel;
        String scopeLabel = statistics.cityScoped() ? "in deiner Umgebung" : "insgesamt";
        return count + (count == 1 ? " offenes " : " offene ") + offerLabel + " " + scopeLabel;
    }

    private Span createRating(BigDecimal averageRating) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.GERMANY);
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(1);

        Span rating = new Span("Ø " + formatter.format(averageRating) + " Sterne");
        rating.getStyle()
                .set("font-size", "15px")
                .set("color", "#6f6862");
        return rating;
    }
}
