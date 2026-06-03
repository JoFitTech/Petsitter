package com.softwareengineering.petsitter.booking.repository;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByOwnerIdOrSitterId(UUID ownerId, UUID sitterId);
    List<Booking> findAllByAcceptedRequest_Id(UUID requestId);
    java.util.Optional<Booking> findByOffer_OfferIdAndStatus(UUID offerId, BookingStatus status);

    /** Alle CREATED Bookings, deren endDate vor (oder am) gegebenen Datum liegt – für Auto-Complete-Scheduler. */
    List<Booking> findAllByStatusAndEndDateLessThanEqual(BookingStatus status, LocalDate cutoff);

    @Query("""
            select distinct booking
            from Booking booking
            left join fetch booking.recurringWeekdays
            where booking.status = :status
              and booking.frequency = :frequency
            """)
    List<Booking> findAllActiveRecurring(
            @Param("status") BookingStatus status,
            @Param("frequency") OfferFrequency frequency);
}
