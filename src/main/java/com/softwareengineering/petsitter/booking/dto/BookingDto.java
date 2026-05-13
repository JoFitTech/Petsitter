package com.softwareengineering.petsitter.booking.dto;

import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
 * @param pricePerWeek  Preis pro Woche in EUR
 * @param status        Aktueller Booking-Status
 */
public record BookingDto(
        UUID id,
        String offerTitle,
        String ownerName,
        String sitterName,
        String petName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal pricePerWeek,
        BookingStatus status
) {}

