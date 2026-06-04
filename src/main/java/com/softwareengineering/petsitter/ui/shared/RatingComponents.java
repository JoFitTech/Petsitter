package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Shared UI components for rendering user ratings consistently across views.
 */
public final class RatingComponents {

    public static final String EMPTY_RATING_TEXT = "Noch keine Bewertungen";
    private static final int MAX_STARS = 5;
    private static final String FILLED_STAR_COLOR = "#ffdf4a";
    private static final String EMPTY_STAR_COLOR = "#d0d0d0";
    private static final String TEXT_COLOR = "#7a6050";

    private RatingComponents() {
    }

    public static Component starsForSummary(UserRatingSummary summary, int sizePx) {
        HorizontalLayout starsLayout = new HorizontalLayout();
        starsLayout.setPadding(false);
        starsLayout.setSpacing(false);
        starsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        starsLayout.getStyle().set("gap", "2px");
        starsLayout.getElement().setAttribute("aria-label", labelFor(summary));

        int filledStars = roundedStarCount(summary);
        String size = Math.max(1, sizePx) + "px";

        for (int i = 0; i < MAX_STARS; i++) {
            boolean filled = i < filledStars;
            Icon star = new Icon(filled ? VaadinIcon.STAR : VaadinIcon.STAR_O);
            star.setSize(size);
            star.getStyle().set("color", filled ? FILLED_STAR_COLOR : EMPTY_STAR_COLOR);
            starsLayout.add(star);
        }

        return starsLayout;
    }

    public static Component compactRating(UserRatingSummary summary) {
        HorizontalLayout rating = new HorizontalLayout();
        rating.setPadding(false);
        rating.setSpacing(false);
        rating.setAlignItems(FlexComponent.Alignment.CENTER);
        rating.getStyle().set("gap", "6px");

        rating.add(starsForSummary(summary, 16));

        Span label = new Span(summaryText(summary));
        label.getStyle()
                .set("color", TEXT_COLOR)
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("white-space", "nowrap");
        rating.add(label);

        return rating;
    }

    public static int roundedStarCount(UserRatingSummary summary) {
        if (summary == null || summary.ratingCount() == 0 || summary.averageRating() <= 0.0d) {
            return 0;
        }
        int rounded = Math.round((float) summary.averageRating());
        return Math.max(0, Math.min(MAX_STARS, rounded));
    }

    private static String summaryText(UserRatingSummary summary) {
        if (summary == null || summary.ratingCount() == 0) {
            return EMPTY_RATING_TEXT;
        }
        return String.format(java.util.Locale.GERMAN, "Ø %.1f (%d)", summary.averageRating(), summary.ratingCount());
    }

    private static String labelFor(UserRatingSummary summary) {
        if (summary == null || summary.ratingCount() == 0) {
            return EMPTY_RATING_TEXT;
        }
        return String.format(java.util.Locale.GERMAN, "%.1f von 5 Sternen bei %d Bewertungen",
                summary.averageRating(), summary.ratingCount());
    }
}
