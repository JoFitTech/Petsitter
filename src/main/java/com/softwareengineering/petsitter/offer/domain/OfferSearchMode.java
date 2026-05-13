package com.softwareengineering.petsitter.offer.domain;

import java.util.Locale;

public enum OfferSearchMode {
    TIERHALTER("tierhalter", OfferType.OWNER_OFFER, true),
    TIERSITTER("tiersitter", OfferType.SITTER_OFFER, false);

    private final String queryValue;
    private final OfferType targetOfferType;
    private final boolean minimumEarnings;

    OfferSearchMode(String queryValue, OfferType targetOfferType, boolean minimumEarnings) {
        this.queryValue = queryValue;
        this.targetOfferType = targetOfferType;
        this.minimumEarnings = minimumEarnings;
    }

    public static OfferSearchMode fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return TIERSITTER;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (OfferSearchMode mode : values()) {
            if (mode.queryValue.equals(normalized)) {
                return mode;
            }
        }
        return TIERSITTER;
    }

    public String queryValue() {
        return queryValue;
    }

    public OfferType targetOfferType() {
        return targetOfferType;
    }

    public boolean minimumEarnings() {
        return minimumEarnings;
    }
}
