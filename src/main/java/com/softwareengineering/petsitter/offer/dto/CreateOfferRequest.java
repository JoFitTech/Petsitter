package com.softwareengineering.petsitter.offer.dto;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreateOfferRequest(
        OfferType offerType,
        LocalDate startDate,
        LocalDate endDate,
        UUID petId,
        List<UUID> petIds,
        BigDecimal price,
        String title,
        OfferFrequency frequency,
        OfferCareType careType,
        OfferAnimalType animalType,
        String description,
        Set<DayOfWeek> recurringWeekdays,
        OfferTimeSlot timeSlot
) {
    public CreateOfferRequest {
        petIds = normalizedPetIds(petId, petIds);
        petId = petIds.isEmpty() ? null : petIds.getFirst();
        recurringWeekdays = normalizedWeekdays(recurringWeekdays);
    }

    public CreateOfferRequest(
            OfferType offerType,
            LocalDate startDate,
            LocalDate endDate,
            UUID petId,
            List<UUID> petIds,
            BigDecimal price,
            String title,
            OfferFrequency frequency,
            OfferCareType careType,
            OfferAnimalType animalType,
            String description) {
        this(offerType, startDate, endDate, petId, petIds, price, title, frequency, careType, animalType,
                description, Set.of(), null);
    }

    public CreateOfferRequest(OfferType offerType, LocalDate startDate, LocalDate endDate,
            UUID petId, BigDecimal price, String title, OfferFrequency frequency,
            OfferCareType careType, OfferAnimalType animalType, String description) {
        this(offerType, startDate, endDate, petId, null, price, title, frequency, careType, animalType,
                description, Set.of(), null);
    }

    public CreateOfferRequest(OfferType offerType, LocalDate startDate, LocalDate endDate,
            UUID petId, BigDecimal price, String description) {
        this(offerType, startDate, endDate, petId, price, null, null, null, null, description);
    }

    public CreateOfferRequest(OfferType offerType, LocalDate startDate, LocalDate endDate,
            UUID petId, BigDecimal price, String title, OfferFrequency frequency,
            OfferCareType careType, OfferAnimalType animalType, String description,
            Set<DayOfWeek> recurringWeekdays, OfferTimeSlot timeSlot) {
        this(offerType, startDate, endDate, petId, null, price, title, frequency, careType, animalType,
                description, recurringWeekdays, timeSlot);
    }

    private static List<UUID> normalizedPetIds(UUID petId, List<UUID> petIds) {
        LinkedHashSet<UUID> ids = new LinkedHashSet<>();
        if (petIds != null) {
            petIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .forEach(ids::add);
        }
        if (ids.isEmpty() && petId != null) {
            ids.add(petId);
        }
        return List.copyOf(ids);
    }

    private static Set<DayOfWeek> normalizedWeekdays(Set<DayOfWeek> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<DayOfWeek> normalized = new LinkedHashSet<>();
        weekdays.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt(DayOfWeek::getValue))
                .forEach(normalized::add);
        return Set.copyOf(normalized);
    }
}
