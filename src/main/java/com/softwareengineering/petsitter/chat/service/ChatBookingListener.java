package com.softwareengineering.petsitter.chat.service;

import com.softwareengineering.petsitter.booking.service.BookingCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application Event Listener für Booking-Erstellung.
 *
 * Reagiert auf {@link BookingCreatedEvent} und erstellt automatisch
 * eine Chat-Konversation für das neue Booking.
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
     * Erstellt automatisch eine Chat-Konversation.
     *
     * @param event Das BookingCreatedEvent mit der Booking-ID
     */
    @EventListener
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

}


