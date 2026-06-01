package com.softwareengineering.petsitter.booking.repository;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByOwnerIdOrSitterId(UUID ownerId, UUID sitterId);
    java.util.Optional<Booking> findByAcceptedRequest_Id(UUID requestId);
    List<Booking> findAllByAcceptedRequest_Id(UUID requestId);
    java.util.Optional<Booking> findByOffer_OfferIdAndStatus(UUID offerId, BookingStatus status);

    /** Alle CREATED Bookings, deren endDate vor (oder am) gegebenen Datum liegt – für Auto-Complete-Scheduler. */
    List<Booking> findAllByStatusAndEndDateLessThanEqual(BookingStatus status, LocalDate cutoff);
}

