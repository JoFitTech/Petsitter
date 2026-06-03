package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;

public record OfferCoverTileDto(
        ImageRefDto image,
        String fallbackLabel,
        OfferAnimalType fallbackType
) {
}
