package com.softwareengineering.petsitter.offer.domain;

import java.util.Locale;

public enum OfferFrequency {
    ONE_TIME("einmalig"),
    REGULAR("regelmäßig");

    private final String label;

    OfferFrequency(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static OfferFrequency fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (OfferFrequency frequency : values()) {
            if (frequency.name().equals(normalized)) {
                return frequency;
            }
        }
        return null;
    }

    public String queryValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
