package com.softwareengineering.petsitter.offerrequest.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record OfferRequestChatCardDto(
        UUID requestId,
        RequestStatus status,
        String offerTitle,
        OfferType offerType,
        LocalDate startDate,
        LocalDate endDate,
        OfferFrequency frequency,
        Set<DayOfWeek> recurringWeekdays,
        OfferTimeSlot timeSlot,
        String petSummary,
        OfferAnimalType animalType
) {
    public OfferRequestChatCardDto {
        recurringWeekdays = recurringWeekdays == null ? Set.of() : Set.copyOf(recurringWeekdays);
    }
}
