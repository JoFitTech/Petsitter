package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.time.LocalDate;
import java.util.List;

public record CreateOfferFormData(
        List<OfferType> offerTypes,
        List<OfferFrequency> frequencies,
        List<OfferCareType> careTypes,
        List<OfferAnimalType> animalTypes,
        List<OfferPetOptionDto> pets,
        LocalDate minimumStartDate,
        CreateOfferDateSelection dateSelection,
        int titleMaxLength,
        int descriptionMaxLength
) {
    public CreateOfferFormData {
        offerTypes = List.copyOf(offerTypes);
        frequencies = List.copyOf(frequencies);
        careTypes = List.copyOf(careTypes);
        animalTypes = List.copyOf(animalTypes);
        pets = List.copyOf(pets);
    }
}
