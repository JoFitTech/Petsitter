package com.softwareengineering.petsitter.offer.domain;

import java.util.Locale;

public enum OfferCareType {
    PET_SITTING("Tiersitting"),
    PET_AND_HOUSE_SITTING("Tiersitting + Haussitting");

    private final String label;

    OfferCareType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static OfferCareType fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (OfferCareType careType : values()) {
            if (careType.name().equals(normalized)) {
                return careType;
            }
        }
        return null;
    }

    public String queryValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
