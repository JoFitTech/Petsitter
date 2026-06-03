package com.softwareengineering.petsitter.booking.dto;

import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferTimeSlot;
import com.softwareengineering.petsitter.offer.dto.OfferCoverTileDto;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import com.softwareengineering.petsitter.wallet.domain.RecurringPaymentStatus;

/**
 * DTO für die Darstellung einer Buchung in der UI.
 *
 * @param id            Booking-ID
 * @param offerTitle    Titel des zugrundeliegenden Offers (oder Fallback-Text)
 * @param ownerName     Vollname des Owners
 * @param sitterName    Vollname des Sitters
 * @param petName       Name des Haustieres (oder null bei SITTER_OFFER ohne Pet)
 * @param startDate     Startdatum der Betreuung
 * @param endDate       Enddatum der Betreuung
 * @param pricePerDay   Preis pro Tag in EUR
 * @param totalPrice    Fester Gesamtpreis in EUR
 * @param status        Aktueller Booking-Status
 */
public record BookingDto(
        UUID id,
        UUID ownerId,
        String offerTitle,
        String ownerName,
        String sitterName,
        String petName,
        LocalDate startDate,
        LocalDate endDate,
        OfferFrequency frequency,
        Set<DayOfWeek> recurringWeekdays,
        OfferTimeSlot timeSlot,
        LocalDate recurringEndedOn,
        BigDecimal pricePerDay,
        BigDecimal totalPrice,
        BookingStatus status,
        LocalDateTime bookedAt,
        PaymentStatus paymentStatus,
        LocalDateTime releaseRequestedAt,
        LocalDateTime automaticReleaseAt,
        RecurringPaymentStatus recurringPaymentStatus,
        int payableRecurringOccurrences,
        BigDecimal payableRecurringAmount,
        LocalDateTime recurringReleaseRequestedAt,
        LocalDateTime recurringAutomaticReleaseAt,
        List<OfferCoverTileDto> coverTiles
) {
    public BookingDto {
        coverTiles = coverTiles == null ? List.of() : List.copyOf(coverTiles);
        recurringWeekdays = recurringWeekdays == null ? Set.of() : Set.copyOf(recurringWeekdays);
        payableRecurringAmount = payableRecurringAmount == null ? BigDecimal.ZERO : payableRecurringAmount;
    }

    public BookingDto(
            UUID id,
            UUID ownerId,
            String offerTitle,
            String ownerName,
            String sitterName,
            String petName,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal pricePerDay,
            BigDecimal totalPrice,
            BookingStatus status,
            LocalDateTime bookedAt,
            PaymentStatus paymentStatus,
            LocalDateTime releaseRequestedAt,
            LocalDateTime automaticReleaseAt,
            List<OfferCoverTileDto> coverTiles
    ) {
        this(id, ownerId, offerTitle, ownerName, sitterName, petName, startDate, endDate,
                OfferFrequency.ONE_TIME, Set.of(), null, null, pricePerDay, totalPrice, status, bookedAt,
                paymentStatus, releaseRequestedAt, automaticReleaseAt, null, 0, BigDecimal.ZERO, null, null,
                coverTiles);
    }

    public BookingDto(
            UUID id,
            UUID ownerId,
            String offerTitle,
            String ownerName,
            String sitterName,
            String petName,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal pricePerDay,
            BigDecimal totalPrice,
            BookingStatus status,
            LocalDateTime bookedAt,
            PaymentStatus paymentStatus,
            LocalDateTime releaseRequestedAt,
            LocalDateTime automaticReleaseAt
    ) {
        this(id, ownerId, offerTitle, ownerName, sitterName, petName, startDate, endDate, pricePerDay,
                totalPrice, status, bookedAt, paymentStatus, releaseRequestedAt, automaticReleaseAt, List.of());
    }
}
