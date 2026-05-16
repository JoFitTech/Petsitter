package com.softwareengineering.petsitter.booking.service;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Spring Application Event für neu erstellte Bookings.
 *
 * Wird von {@link BookingService#acceptRequest} publiziert, damit
 * der com.softwareengineering.petsitter.chat.service.ChatBookingListener automatisch eine Chat-Konversation
 * anlegen kann (entkopplung).
 */
public class BookingCreatedEvent extends ApplicationEvent {

    private final UUID bookingId;

    public BookingCreatedEvent(Object source, UUID bookingId) {
        super(source);
        this.bookingId = bookingId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

}


