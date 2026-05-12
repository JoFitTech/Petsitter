package com.softwareengineering.petsitter.offer.domain;

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
}
