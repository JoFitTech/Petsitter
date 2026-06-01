package com.softwareengineering.petsitter.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.review.domain.UserReview;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.review.repository.UserReviewRepository;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

class UserReviewServiceTest {

    private UserReviewRepository userReviewRepository;
    private BookingRepository bookingRepository;
    private UserReviewService userReviewService;

    @BeforeEach
    void setUp() {
        userReviewRepository = Mockito.mock(UserReviewRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        userReviewService = new UserReviewService(userReviewRepository, bookingRepository);
    }

    @Test
    void submitReview_ownerCanRateSitter_afterCompletedBooking() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        User owner = user(ownerId, "Anna", "Owner");
        User sitter = user(UUID.randomUUID(), "Ben", "Sitter");
        Booking booking = booking(bookingId, owner, sitter, BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, ownerId)).thenReturn(false);
        when(userReviewRepository.save(any(UserReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserReview review = userReviewService.submitReview(bookingId, ownerId, 5, " Super Betreuung ");

        assertThat(review.getBooking()).isSameAs(booking);
        assertThat(review.getReviewer()).isSameAs(owner);
        assertThat(review.getReviewee()).isSameAs(sitter);
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Super Betreuung");
        verify(userReviewRepository).save(any(UserReview.class));
    }

    @Test
    void submitReview_failsWhenBookingNotCompleted() {
        UUID bookingId = UUID.randomUUID();
        User owner = user(UUID.randomUUID(), "Anna", "Owner");
        User sitter = user(UUID.randomUUID(), "Ben", "Sitter");
        Booking booking = booking(bookingId, owner, sitter, BookingStatus.CREATED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> userReviewService.submitReview(bookingId, owner.getId(), 4, null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("erst nach Abschluss");
    }

    @Test
    void submitReview_failsWhenReviewerNotBookingParticipant() {
        UUID bookingId = UUID.randomUUID();
        User owner = user(UUID.randomUUID(), "Anna", "Owner");
        User sitter = user(UUID.randomUUID(), "Ben", "Sitter");
        Booking booking = booking(bookingId, owner, sitter, BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> userReviewService.submitReview(bookingId, UUID.randomUUID(), 4, null))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Teilnehmer");
    }

    @Test
    void submitReview_failsWhenRatingOutOfRange() {
        assertThatThrownBy(() -> userReviewService.submitReview(UUID.randomUUID(), UUID.randomUUID(), 0, null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("zwischen 1 und 5");
    }

    @Test
    void submitReview_failsWhenReviewerAlreadyReviewedBooking() {
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        Booking booking = booking(
                bookingId,
                user(reviewerId, "Anna", "Owner"),
                user(UUID.randomUUID(), "Ben", "Sitter"),
                BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)).thenReturn(true);

        assertThatThrownBy(() -> userReviewService.submitReview(bookingId, reviewerId, 4, "Gut"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("bereits eine Bewertung");
    }

    @Test
    void submitReview_rejectsTooLongComment() {
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        Booking booking = booking(
                bookingId,
                user(reviewerId, "Anna", "Owner"),
                user(UUID.randomUUID(), "Ben", "Sitter"),
                BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)).thenReturn(false);

        assertThatThrownBy(() -> userReviewService.submitReview(bookingId, reviewerId, 4, "x".repeat(101)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("maximal 100");
    }

    @Test
    void getUserRatingSummary_returnsRoundedAverage() {
        UUID userId = UUID.randomUUID();
        when(userReviewRepository.countByReviewee_Id(userId)).thenReturn(3L);
        when(userReviewRepository.findAverageRatingByRevieweeId(userId)).thenReturn(4.333333d);

        UserRatingSummary summary = userReviewService.getUserRatingSummary(userId);

        assertThat(summary.ratingCount()).isEqualTo(3L);
        assertThat(summary.averageRating()).isEqualTo(4.3d);
    }

    @Test
    void submitReview_failsWhenBookingNotFound() {
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userReviewService.submitReview(bookingId, reviewerId, 4, "Gut"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking nicht gefunden");
    }

    @Test
    void submitReview_normalizesCommentToNullWhenBlank() {
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        User owner = user(reviewerId, "Anna", "Owner");
        User sitter = user(UUID.randomUUID(), "Ben", "Sitter");
        Booking booking = booking(bookingId, owner, sitter, BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)).thenReturn(false);
        when(userReviewRepository.save(any(UserReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserReview review = userReviewService.submitReview(bookingId, reviewerId, 3, "   ");

        assertThat(review.getComment()).isNull();
    }

    @Test
    void hasUserReviewedBooking_returnsFalseWhenNotReviewed() {
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)).thenReturn(false);

        boolean result = userReviewService.hasUserReviewedBooking(bookingId, reviewerId);

        assertThat(result).isFalse();
    }

    @Test
    void hasUserReviewedBooking_returnsTrueWhenReviewed() {
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId)).thenReturn(true);

        boolean result = userReviewService.hasUserReviewedBooking(bookingId, reviewerId);

        assertThat(result).isTrue();
    }

    @Test
    void submitReview_handlesRaceConditionGracefully() {
        // Simuliert eine Race-Condition: existsBy gibt false zurück, aber save wirft Exception
        UUID bookingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        User owner = user(reviewerId, "Anna", "Owner");
        User sitter = user(UUID.randomUUID(), "Ben", "Sitter");
        Booking booking = booking(bookingId, owner, sitter, BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        // Erste Prüfung: false (kein Duplikat erkannt)
        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, reviewerId))
                .thenReturn(false)  // Pre-check returns false
                .thenReturn(true);  // Post-exception check returns true

        // Save wirft DataIntegrityViolationException (andere Request speichert parallel)
        when(userReviewRepository.save(any(UserReview.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Das sollte trotzdem eine fachliche Exception werfen statt der DB-Exception
        assertThatThrownBy(() -> userReviewService.submitReview(bookingId, reviewerId, 4, "Gut"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("bereits eine Bewertung");
    }

    private Booking booking(UUID id, User owner, User sitter, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setStatus(status);
        return booking;
    }

    private User user(UUID id, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }
}






