package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.pet.dto.PetDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        String petTags,
        List<PetDto> pets,
        String postalCode,
        String city,
        Integer distanceKm,
        boolean favorited,
        OfferType offerType,
        UUID creatorUserId,
        String creatorDisplayName
) {
    public OfferCardDto {
        pets = pets == null ? List.of() : List.copyOf(pets);
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
            String petTags,
            String postalCode,
            String city,
            Integer distanceKm,
            boolean favorited,
            OfferType offerType
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, petTags, List.of(), postalCode, city, distanceKm,
                favorited, offerType, null, null);
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
            String petTags,
            String postalCode,
            String city,
            Integer distanceKm,
            boolean favorited
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, petTags, List.of(), postalCode, city, distanceKm,
                favorited, null, null, null);
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
            String petTags,
            String postalCode,
            String city,
            Integer distanceKm
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, petTags, List.of(), postalCode, city, distanceKm,
                false, null, null, null);
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
            String petTags
    ) {
        this(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, petTags, List.of(), null, null, null, false,
                null, null, null);
    }

    public OfferCardDto withFavorited(boolean favorited) {
        return new OfferCardDto(id, title, startDate, endDate, price, animalType, creatorVerified, description,
                frequency, careType, petName, petSpecies, petBreed, petTags, pets, postalCode, city, distanceKm,
                favorited, offerType, creatorUserId, creatorDisplayName);
    }
}
