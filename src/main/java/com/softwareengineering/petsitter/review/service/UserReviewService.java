package com.softwareengineering.petsitter.review.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.review.domain.UserReview;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.review.dto.UserReviewDto;
import com.softwareengineering.petsitter.review.repository.UserReviewRepository;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserReviewService {

    private static final Logger log = LoggerFactory.getLogger(UserReviewService.class);

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_COMMENT_LENGTH = 100;

    private final UserReviewRepository userReviewRepository;
    private final BookingRepository bookingRepository;

    public UserReviewService(UserReviewRepository userReviewRepository, BookingRepository bookingRepository) {
        this.userReviewRepository = userReviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public UserReview submitReview(UUID bookingId, UUID reviewerId, int rating, String comment) {
        requireId(bookingId, "bookingId darf nicht null sein");
        requireId(reviewerId, "reviewerId darf nicht null sein");
        validateRating(rating);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Bewertungen sind erst nach Abschluss des Termins moeglich.");
        }

        User reviewer = resolveParticipant(booking, reviewerId);
        User reviewee = resolveCounterpart(booking, reviewerId);

        // Explizite Validierung 1: Selbstbewertung vor DB-Operation blocken (Hardening)
        // Das verhindert, dass der DB-Constraint aktiviert wird und einen generischen Fehler wirft
        if (reviewer.getId().equals(reviewee.getId())) {
            throw new BusinessRuleViolationException("Selbstbewertungen sind nicht erlaubt.");
        }

        // Explizite Validierung 2: Duplikat vor Save prüfen (hauptsächlich für Performance)
        // Im Catch-Handler prüfen wir nochmal für Race-Conditions
        if (userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)) {
            throw new BusinessRuleViolationException("Du hast fuer diesen Termin bereits eine Bewertung abgegeben.");
        }

        UserReview review = new UserReview();
        review.setBooking(booking);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(rating);
        review.setComment(normalizeComment(comment));

        try {
            return userReviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            // Race-Condition Handling (Hardening): Bei Parallel-Save schlägt Unique-Constraint fehl
            // Wir prüfen nochmal und geben eine fachliche Fehlermeldung statt DB-Exception
            if (userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)) {
                log.warn("Race-condition detected: Duplicate review attempt for booking {} by reviewer {}",
                         bookingId, reviewerId);
                throw new BusinessRuleViolationException("Du hast fuer diesen Termin bereits eine Bewertung abgegeben.");
            }
            // Falls es ein anderer Integrity-Fehler ist, re-throw
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserRatingSummary getUserRatingSummary(UUID revieweeId) {
        requireId(revieweeId, "revieweeId darf nicht null sein");

        long ratingCount = userReviewRepository.countByReviewee_Id(revieweeId);
        if (ratingCount == 0) {
            return new UserRatingSummary(0.0d, 0L);
        }

        Double average = userReviewRepository.findAverageRatingByRevieweeId(revieweeId);
        if (average == null) {
            return new UserRatingSummary(0.0d, ratingCount);
        }

        double rounded = BigDecimal.valueOf(average)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        return new UserRatingSummary(rounded, ratingCount);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewedBooking(UUID bookingId, UUID reviewerId) {
        requireId(bookingId, "bookingId darf nicht null sein");
        requireId(reviewerId, "reviewerId darf nicht null sein");
        return userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId);
    }

    /**
     * Gibt die letzten {@code limit} Bewertungen fuer einen Reviewee zurueck.
     *
     * @param revieweeId ID des bewerteten Users
     * @param limit      maximale Anzahl Bewertungen (z.B. 3 fuer Profilvorschau)
     * @return Liste der DTOs, neueste zuerst
     */
    @Transactional(readOnly = true)
    public List<UserReviewDto> getRecentReviews(UUID revieweeId, int limit) {
        requireId(revieweeId, "revieweeId darf nicht null sein");
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return userReviewRepository
                .findByReviewee_IdOrderByCreatedAtDesc(revieweeId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UserReviewDto toDto(com.softwareengineering.petsitter.review.domain.UserReview review) {
        String reviewerName = buildDisplayName(review.getReviewer());
        return new UserReviewDto(
                review.getId(),
                reviewerName,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private String buildDisplayName(com.softwareengineering.petsitter.user.domain.User user) {
        if (user == null) return "Unbekannt";
        String name = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return name.isBlank() ? "Anonym" : name;
    }

    private User resolveParticipant(Booking booking, UUID reviewerId) {
        User owner = booking.getOwner();
        User sitter = booking.getSitter();

        if (owner != null && owner.getId() != null && owner.getId().equals(reviewerId)) {
            return owner;
        }
        if (sitter != null && sitter.getId() != null && sitter.getId().equals(reviewerId)) {
            return sitter;
        }

        throw new ForbiddenOperationException("Nur Teilnehmer der Buchung duerfen bewerten.");
    }

    private User resolveCounterpart(Booking booking, UUID reviewerId) {
        User owner = booking.getOwner();
        User sitter = booking.getSitter();

        if (owner != null && owner.getId() != null && owner.getId().equals(reviewerId)) {
            if (sitter == null || sitter.getId() == null) {
                throw new BusinessRuleViolationException("Buchung enthaelt keinen gueltigen Gegenpart.");
            }
            return sitter;
        }

        if (sitter != null && sitter.getId() != null && sitter.getId().equals(reviewerId)) {
            if (owner == null || owner.getId() == null) {
                throw new BusinessRuleViolationException("Buchung enthaelt keinen gueltigen Gegenpart.");
            }
            return owner;
        }

        throw new ForbiddenOperationException("Nur Teilnehmer der Buchung duerfen bewerten.");
    }

    private void requireId(UUID value, String message) {
        if (value == null) {
            throw new BusinessRuleViolationException(message);
        }
    }

    private void validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessRuleViolationException("Die Bewertung muss zwischen 1 und 5 Sternen liegen.");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String normalized = comment.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new BusinessRuleViolationException(
                    "Der Kommentar darf maximal " + MAX_COMMENT_LENGTH + " Zeichen enthalten.");
        }
        return normalized;
    }
}











