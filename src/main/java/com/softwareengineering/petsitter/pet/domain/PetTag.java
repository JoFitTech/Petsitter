package com.softwareengineering.petsitter.pet.domain;

public enum PetTag {
    STUBENREIN("Stubenrein"),
    AENGSTLICH("Ängstlich"),
    VERSPIELT("Verspielt"),
    VERTRAEGLICH_MIT_KINDERN("Verträglich mit Kindern"),
    VERTRAEGLICH_MIT_ANDEREN_TIEREN("Verträglich mit anderen Tieren"),
    BRAUCHT_MEDIKAMENTE("Braucht Medikamente"),
    KANN_ALLEIN_BLEIBEN("Kann allein bleiben"),
    LEINENFUEHRIG("Leinenführig"),
    FUTTERSENSIBEL("Futtersensibel"),
    KASTRIERT("Kastriert");

    private final String label;

    PetTag(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
