package com.softwareengineering.petsitter.chat.service;

import com.softwareengineering.petsitter.booking.service.BookingCreatedEvent;
import com.softwareengineering.petsitter.booking.service.BookingCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Application Event Listener für Booking-Erstellung und -Abschluss.
 *
 * Reagiert auf {@link BookingCreatedEvent} und {@link BookingCompletedEvent}:
 * - Bei Erstellung: automatisch Chat-Konversation anlegen
 * - Bei Abschluss: Review-Erinnerungskarte im Chat posten
 *
 * Diese Entkopplung hält den BookingService sauber ohne zirkuläre Abhängigkeiten.
 */
@Component
public class ChatBookingListener {

    private static final Logger log = LoggerFactory.getLogger(ChatBookingListener.class);

    private final ChatService chatService;

    public ChatBookingListener(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Listener für neue Bookings.
     * Erstellt automatisch eine Chat-Konversation nach erfolgreichem Booking-Commit.
     *
     * Verwendet @TransactionalEventListener mit AFTER_COMMIT Phase, um sicherzustellen,
     * dass die Chat nur erstellt wird, wenn die Booking-Transaktion erfolgreich committed wurde.
     *
     * @param event Das BookingCreatedEvent mit der Booking-ID
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("BookingCreatedEvent received for booking {}", event.getBookingId());
        try {
            chatService.createConversationForBooking(event.getBookingId());
            log.info("Chat conversation created successfully for booking {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to create chat conversation for booking {}: {}",
                event.getBookingId(), e.getMessage(), e);
        }
    }

    /**
     * Listener für abgeschlossene Bookings.
     * Postet automatisch eine Review-Erinnerungskarte im Chat.
     *
     * Verwendet @TransactionalEventListener mit AFTER_COMMIT Phase, um sicherzustellen,
     * dass die Reminder-Card nur gepostet wird, wenn die Booking-Transaktion erfolgreich committed wurde.
     *
     * @param event Das BookingCompletedEvent mit der Booking-ID
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCompleted(BookingCompletedEvent event) {
        log.info("BookingCompletedEvent received for booking {}", event.getBookingId());
        try {
            var conversationId = chatService.getConversationIdForBooking(event.getBookingId());
            if (conversationId.isPresent()) {
                chatService.postReviewReminderCard(conversationId.get(), event.getBookingId());
                log.info("Review reminder card posted in chat for booking {}", event.getBookingId());
            } else {
                log.warn("No conversation found for booking {} – skipping review reminder", event.getBookingId());
            }
        } catch (Exception e) {
            log.error("Failed to post review reminder for booking {}: {}",
                event.getBookingId(), e.getMessage(), e);
        }
    }

}


