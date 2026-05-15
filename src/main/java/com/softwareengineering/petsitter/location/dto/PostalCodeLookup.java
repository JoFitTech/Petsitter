package com.softwareengineering.petsitter.location.dto;

import java.math.BigDecimal;
import java.util.List;

public record PostalCodeLookup(
        String countryCode,
        String postalCode,
        List<Place> places
) {
    public PostalCodeLookup {
        places = places == null ? List.of() : List.copyOf(places);
    }

    public record Place(
            String name,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }
}
