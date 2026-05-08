package com.softwareengineering.petsitter.booking.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class BookingService {

    public Booking acceptRequest(Long requestId, Long offerCreatorId) {
        throw new UnsupportedOperationException("acceptRequest noch nicht implementiert");
    }

    public void cancelBooking(Long bookingId, Long userId) {
        throw new UnsupportedOperationException("cancelBooking noch nicht implementiert");
    }

    public List<String> getBookings() {
        return Collections.emptyList();
    }
}
