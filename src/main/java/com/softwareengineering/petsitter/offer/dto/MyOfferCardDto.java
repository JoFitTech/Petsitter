package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MyOfferCardDto(
        UUID id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        OfferType offerType,
        OfferStatus status,
        String description,
        OfferFrequency frequency,
        OfferCareType careType,
        String petName,
        String petSpecies,
        String petBreed,
        String petTags,
        OfferAnimalType animalType
) {}
