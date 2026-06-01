package com.softwareengineering.petsitter.booking.dto;

import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;

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
        BigDecimal pricePerDay,
        BigDecimal totalPrice,
        BookingStatus status,
        LocalDateTime bookedAt,
        PaymentStatus paymentStatus,
        LocalDateTime releaseRequestedAt,
        LocalDateTime automaticReleaseAt
) {}
