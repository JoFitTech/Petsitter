package com.softwareengineering.petsitter.offer.dto;

import java.math.BigDecimal;
import java.util.Optional;

public record OfferHeroStatisticsDto(
        long openOfferCount,
        Optional<BigDecimal> averageRating,
        boolean cityScoped
) {
    public OfferHeroStatisticsDto {
        averageRating = averageRating != null ? averageRating : Optional.empty();
    }
}
