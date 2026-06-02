package com.softwareengineering.petsitter.booking.service;

import java.util.UUID;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when a Booking transitions to COMPLETED status.
 *
 * Listeners can use this to trigger side-effects like:
 * - Posting a review reminder card in the chat
 * - Sending notifications to both parties
 * - Scheduling auto-completion cleanup tasks
 */
public class BookingCompletedEvent extends ApplicationEvent {

    private final UUID bookingId;

    public BookingCompletedEvent(Object source, UUID bookingId) {
        super(source);
        this.bookingId = bookingId;
    }

    public UUID getBookingId() {
        return bookingId;
    }
}

