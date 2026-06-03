package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.time.LocalDate;
import java.util.List;

public record CreateOfferFormData(
        List<OfferType> offerTypes,
        List<OfferFrequency> frequencies,
        List<OfferTimeSlot> timeSlots,
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
        timeSlots = List.copyOf(timeSlots);
        careTypes = List.copyOf(careTypes);
        animalTypes = List.copyOf(animalTypes);
        pets = List.copyOf(pets);
    }
}
