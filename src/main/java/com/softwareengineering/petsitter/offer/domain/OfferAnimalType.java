package com.softwareengineering.petsitter.offer.domain;

import java.util.Locale;

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

    public static OfferAnimalType fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (OfferAnimalType animalType : values()) {
            if (animalType.name().equals(normalized)) {
                return animalType;
            }
        }
        return null;
    }

    public String queryValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
