package com.softwareengineering.petsitter.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.review.domain.UserReview;
import com.softwareengineering.petsitter.review.repository.UserReviewRepository;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.wallet.service.WalletService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class BookingCompletionReviewFlowTest {

    private BookingRepository bookingRepository;
    private UserReviewRepository userReviewRepository;
    private ChatService chatService;
    private BookingService bookingService;
    private UserReviewService userReviewService;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        userReviewRepository = Mockito.mock(UserReviewRepository.class);
        chatService = Mockito.mock(ChatService.class);

        OfferRequestRepository offerRequestRepository = Mockito.mock(OfferRequestRepository.class);
        OfferRepository offerRepository = Mockito.mock(OfferRepository.class);
        WalletService walletService = Mockito.mock(WalletService.class);
        ApplicationEventPublisher eventPublisher = event -> { };

        bookingService = new BookingService(
                bookingRepository,
                offerRequestRepository,
                offerRepository,
                eventPublisher,
                walletService
        );
        userReviewService = new UserReviewService(userReviewRepository, bookingRepository, chatService);
    }

    @Test
    void flow_createdToCompletedToReview_sendsReviewCard() {
        UUID bookingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User owner = user(ownerId, "Anna", "Owner");
        User sitter = user(UUID.randomUUID(), "Ben", "Sitter");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setStatus(BookingStatus.CREATED);
        booking.setEndDate(LocalDate.now().minusDays(1));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userReviewRepository.existsByBooking_IdAndReviewer_Id(bookingId, ownerId)).thenReturn(false);
        when(userReviewRepository.save(any(UserReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatService.getConversationIdForBooking(bookingId)).thenReturn(Optional.of("conv-flow"));

        Booking completed = bookingService.markBookingCompleted(bookingId, ownerId);
        UserReview review = userReviewService.submitReview(bookingId, ownerId, 5, "Top Betreuung");

        assertThat(completed.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getReviewee().getId()).isEqualTo(sitter.getId());
        verify(chatService).saveReviewNotificationMessage("conv-flow", ownerId, 5, "Top Betreuung");
    }

    private User user(UUID id, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }
}

