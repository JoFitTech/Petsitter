package com.softwareengineering.petsitter.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.softwareengineering.petsitter.booking.service.BookingCompletedEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Unit-Tests für ChatBookingListener Review-Reminder-Funktionalität.
 */
class ChatBookingListenerReviewReminderTest {

    private TestChatService chatService;
    private ChatBookingListener listener;

    @BeforeEach
    void setUp() {
        chatService = new TestChatService();
        listener = new ChatBookingListener(chatService);
    }

    @Test
    void onBookingCompleted_postsReviewReminderCard_whenConversationExists() {
        // Given
        UUID bookingId = UUID.randomUUID();
        String conversationId = "conv-123";
        chatService.conversationId = Optional.of(conversationId);

        BookingCompletedEvent event = new BookingCompletedEvent(this, bookingId);

        // When
        listener.onBookingCompleted(event);

        // Then
        assertEquals(1, chatService.getConversationCalls);
        assertEquals(1, chatService.postReminderCalls);
        assertEquals(bookingId, chatService.requestedBookingId);
        assertEquals(conversationId, chatService.postedConversationId);
        assertEquals(bookingId, chatService.postedBookingId);
    }

    @Test
    void onBookingCompleted_skipsPostingReminder_whenNoConversationExists() {
        // Given
        UUID bookingId = UUID.randomUUID();
        chatService.conversationId = Optional.empty();

        BookingCompletedEvent event = new BookingCompletedEvent(this, bookingId);

        // When
        listener.onBookingCompleted(event);

        // Then
        assertEquals(1, chatService.getConversationCalls);
        assertEquals(0, chatService.postReminderCalls);
        assertEquals(bookingId, chatService.requestedBookingId);
    }

    @Test
    void onBookingCompleted_handlesChatServiceException_gracefully() {
        // Given
        UUID bookingId = UUID.randomUUID();
        String conversationId = "conv-123";
        chatService.conversationId = Optional.of(conversationId);
        chatService.postReminderException = new RuntimeException("Chat service error");

        BookingCompletedEvent event = new BookingCompletedEvent(this, bookingId);

        // When - should not throw
        runWithListenerLoggingDisabled(() -> listener.onBookingCompleted(event));

        // Then - both calls were attempted
        assertEquals(1, chatService.getConversationCalls);
        assertEquals(1, chatService.postReminderCalls);
        assertEquals(bookingId, chatService.requestedBookingId);
        assertEquals(conversationId, chatService.postedConversationId);
        assertEquals(bookingId, chatService.postedBookingId);
    }

    private void runWithListenerLoggingDisabled(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger(ChatBookingListener.class);
        Level previousLevel = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            action.run();
        } finally {
            logger.setLevel(previousLevel);
        }
    }

    private static final class TestChatService extends ChatService {

        private Optional<String> conversationId = Optional.empty();
        private RuntimeException postReminderException;
        private int getConversationCalls;
        private int postReminderCalls;
        private UUID requestedBookingId;
        private String postedConversationId;
        private UUID postedBookingId;

        private TestChatService() {
            super(null, null, null, null, null, null, null, null, null);
        }

        @Override
        public Optional<String> getConversationIdForBooking(UUID bookingId) {
            getConversationCalls++;
            requestedBookingId = bookingId;
            return conversationId;
        }

        @Override
        public void postReviewReminderCard(String conversationId, UUID bookingId) {
            postReminderCalls++;
            postedConversationId = conversationId;
            postedBookingId = bookingId;
            if (postReminderException != null) {
                throw postReminderException;
            }
        }
    }
}

