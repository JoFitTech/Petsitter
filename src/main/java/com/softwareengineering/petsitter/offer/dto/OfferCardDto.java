package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OfferCardDto(
        UUID id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        OfferAnimalType animalType,
        boolean creatorVerified
) {}
