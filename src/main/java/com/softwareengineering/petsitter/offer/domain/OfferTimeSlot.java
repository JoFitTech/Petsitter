package com.softwareengineering.petsitter.offer.domain;

import java.util.Locale;

public enum OfferTimeSlot {
    MORNING("vormittags"),
    AFTERNOON("nachmittags"),
    FULL_DAY("ganztags");

    private final String label;

    OfferTimeSlot(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static OfferTimeSlot fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (OfferTimeSlot slot : values()) {
            if (slot.name().equals(normalized)) {
                return slot;
            }
        }
        return null;
    }

    public String queryValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
