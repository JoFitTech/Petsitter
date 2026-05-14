package com.softwareengineering.petsitter.offer.domain;

import java.util.Locale;

public enum OfferDateFilterMode {
    ANY("any"),
    EXACT("exact"),
    CONTAINED("contained"),
    OVERLAP("overlap");

    private final String queryValue;

    OfferDateFilterMode(String queryValue) {
        this.queryValue = queryValue;
    }

    public static OfferDateFilterMode fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return OVERLAP;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (OfferDateFilterMode mode : values()) {
            if (mode.queryValue.equals(normalized)) {
                return mode;
            }
        }
        return OVERLAP;
    }

    public String queryValue() {
        return queryValue;
    }
}
