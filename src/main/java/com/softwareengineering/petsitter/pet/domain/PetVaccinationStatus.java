package com.softwareengineering.petsitter.pet.domain;

public enum PetVaccinationStatus {
    GEIMPFT("Geimpft"),
    UNGEIMPFT("Ungeimpft"),
    UNBEKANNT("Impfstatus unbekannt");

    private final String label;

    PetVaccinationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
