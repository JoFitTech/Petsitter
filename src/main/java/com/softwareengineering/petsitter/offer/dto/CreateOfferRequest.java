package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateOfferRequest(
        OfferType offerType,
        LocalDate startDate,
        LocalDate endDate,
        UUID petId,
        BigDecimal price,
        String title,
        OfferFrequency frequency,
        OfferCareType careType,
        OfferAnimalType animalType,
        String description
) {
    public CreateOfferRequest(OfferType offerType, LocalDate startDate, LocalDate endDate,
            UUID petId, BigDecimal price, String description) {
        this(offerType, startDate, endDate, petId, price, null, null, null, null, description);
    }
}
