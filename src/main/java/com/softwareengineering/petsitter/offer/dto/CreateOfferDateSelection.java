package com.softwareengineering.petsitter.offer.dto;

import java.time.LocalDate;

public record CreateOfferDateSelection(
        LocalDate minimumEndDate,
        boolean clearEndDate,
        String summary
) {
}
