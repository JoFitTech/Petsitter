package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;

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
        boolean creatorVerified,
        String description,
        OfferFrequency frequency,
        OfferCareType careType,
        String petName,
        String petSpecies,
        String petBreed,
        String postalCode,
        String city,
        Integer distanceKm,
        boolean favorited,
        OfferType offerType
) {
    public OfferCardDto(
            UUID id,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal price,
            OfferAnimalType animalType,
            boolean creatorVerified,
            String description,
            OfferFrequency frequency,
            OfferCareType careType,
            String petName,
            String petSpecies,
            String petBreed,
            String postalCode,
            String city,
            Integer distanceKm,
            boolean favorited
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, postalCode, city, distanceKm, favorited, null);
    }

    public OfferCardDto(
            UUID id,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal price,
            OfferAnimalType animalType,
            boolean creatorVerified,
            String description,
            OfferFrequency frequency,
            OfferCareType careType,
            String petName,
            String petSpecies,
            String petBreed,
            String postalCode,
            String city,
            Integer distanceKm
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, postalCode, city, distanceKm, false, null);
    }

    public OfferCardDto(
            UUID id,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal price,
            OfferAnimalType animalType,
            boolean creatorVerified,
            String description,
            OfferFrequency frequency,
            OfferCareType careType,
            String petName,
            String petSpecies,
            String petBreed
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, null, null, null, false, null);
    }

    public OfferCardDto withFavorited(boolean favorited) {
        return new OfferCardDto(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, postalCode, city, distanceKm, favorited, offerType);
    }
}
