package com.softwareengineering.petsitter.offer.domain;

public enum OfferAnimalType {
    DOG("Hund"),
    CAT("Katze"),
    SMALL_ANIMAL("Kleintier"),
    BIRD("Vogel"),
    REPTILE("Reptil"),
    FISH("Fisch"),
    OTHER("Sonstiges");

    private final String label;

    OfferAnimalType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
