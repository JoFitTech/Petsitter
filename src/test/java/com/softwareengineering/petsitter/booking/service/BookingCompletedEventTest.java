package com.softwareengineering.petsitter.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Unit-Tests für BookingCompletedEvent Publishing.
 */
class BookingCompletedEventTest {

    private ApplicationEventPublisher eventPublisher;

    @Test
    void bookingCompletedEventIsCreatedWithBookingId() {
        // Given
        BookingCompletedEvent event = new BookingCompletedEvent(this, java.util.UUID.randomUUID());

        // When
        java.util.UUID bookingId = event.getBookingId();

        // Then
        assertThat(bookingId).isNotNull();
    }

    @Test
    void bookingCompletedEventStoresBookingIdCorrectly() {
        // Given
        java.util.UUID expectedId = java.util.UUID.randomUUID();
        BookingCompletedEvent event = new BookingCompletedEvent(this, expectedId);

        // When
        java.util.UUID actualId = event.getBookingId();

        // Then
        assertThat(actualId).isEqualTo(expectedId);
    }
}

