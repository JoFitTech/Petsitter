package com.softwareengineering.petsitter.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.softwareengineering.petsitter.booking.service.BookingCompletedEvent;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit-Tests für ChatBookingListener Review-Reminder-Funktionalität.
 */
class ChatBookingListenerReviewReminderTest {

    private ChatService chatService;
    private ChatBookingListener listener;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        listener = new ChatBookingListener(chatService);
    }

    @Test
    void onBookingCompleted_postsReviewReminderCard_whenConversationExists() {
        // Given
        UUID bookingId = UUID.randomUUID();
        String conversationId = "conv-123";
        when(chatService.getConversationIdForBooking(bookingId))
            .thenReturn(Optional.of(conversationId));

        BookingCompletedEvent event = new BookingCompletedEvent(this, bookingId);

        // When
        listener.onBookingCompleted(event);

        // Then
        verify(chatService, times(1)).getConversationIdForBooking(bookingId);
        verify(chatService, times(1)).postReviewReminderCard(conversationId, bookingId);
    }

    @Test
    void onBookingCompleted_skipsPostingReminder_whenNoConversationExists() {
        // Given
        UUID bookingId = UUID.randomUUID();
        when(chatService.getConversationIdForBooking(bookingId))
            .thenReturn(Optional.empty());

        BookingCompletedEvent event = new BookingCompletedEvent(this, bookingId);

        // When
        listener.onBookingCompleted(event);

        // Then
        verify(chatService, times(1)).getConversationIdForBooking(bookingId);
        verify(chatService, times(0)).postReviewReminderCard(any(), any());
    }

    @Test
    void onBookingCompleted_handlesChatServiceException_gracefully() {
        // Given
        UUID bookingId = UUID.randomUUID();
        String conversationId = "conv-123";
        when(chatService.getConversationIdForBooking(bookingId))
            .thenReturn(Optional.of(conversationId));

        // Mocking a void method to throw an exception
        Mockito.doThrow(new RuntimeException("Chat service error"))
            .when(chatService).postReviewReminderCard(conversationId, bookingId);

        BookingCompletedEvent event = new BookingCompletedEvent(this, bookingId);

        // When - should not throw
        listener.onBookingCompleted(event);

        // Then - both calls were attempted
        verify(chatService, times(1)).getConversationIdForBooking(bookingId);
        verify(chatService, times(1)).postReviewReminderCard(conversationId, bookingId);
    }
}



