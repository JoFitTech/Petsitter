package com.softwareengineering.petsitter.offer.domain;

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
}
