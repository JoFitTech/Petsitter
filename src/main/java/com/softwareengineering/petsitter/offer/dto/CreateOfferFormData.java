package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.time.LocalDate;
import java.util.List;

public record CreateOfferFormData(
        List<OfferType> offerTypes,
        List<OfferPetOptionDto> pets,
        LocalDate minimumStartDate,
        CreateOfferDateSelection dateSelection,
        int descriptionMaxLength
) {
    public CreateOfferFormData {
        offerTypes = List.copyOf(offerTypes);
        pets = List.copyOf(pets);
    }
}
