package com.softwareengineering.petsitter.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO fuer die Darstellung einer einzelnen Bewertung in der UI.
 *
 * @param id          Review-ID
 * @param reviewerName Anzeigename des Bewerters
 * @param rating      Bewertung 1-5
 * @param comment     Optionaler Kommentar
 * @param createdAt   Zeitpunkt der Bewertung
 */
public record UserReviewDto(
        UUID id,
        String reviewerName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {}

