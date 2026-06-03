package com.softwareengineering.petsitter.booking.repository;

import com.softwareengineering.petsitter.booking.domain.BookingPause;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingPauseRepository extends JpaRepository<BookingPause, UUID> {

    List<BookingPause> findAllByBookingIdOrderByStartDateAsc(UUID bookingId);

    @Query("""
            select pause
            from BookingPause pause
            where pause.booking.id = :bookingId
              and pause.endDate >= :periodStart
              and pause.startDate <= :periodEnd
            order by pause.startDate asc
            """)
    List<BookingPause> findAllOverlapping(
            @Param("bookingId") UUID bookingId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
