package com.softwareengineering.petsitter.wallet.repository;

import com.softwareengineering.petsitter.wallet.domain.RecurringBookingPayment;
import com.softwareengineering.petsitter.wallet.domain.RecurringPaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringBookingPaymentRepository extends JpaRepository<RecurringBookingPayment, UUID> {

    Optional<RecurringBookingPayment> findByBookingIdAndPeriodStart(UUID bookingId, LocalDate periodStart);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select payment
            from RecurringBookingPayment payment
            where payment.booking.id = :bookingId
              and payment.periodStart = :periodStart
            """)
    Optional<RecurringBookingPayment> findByBookingIdAndPeriodStartForUpdate(
            @Param("bookingId") UUID bookingId,
            @Param("periodStart") LocalDate periodStart);

    List<RecurringBookingPayment> findAllByBookingIdOrderByPeriodStartDesc(UUID bookingId);

    List<RecurringBookingPayment> findAllByBookingIdAndStatusInOrderByPeriodStartAsc(
            UUID bookingId,
            Collection<RecurringPaymentStatus> statuses);

    List<RecurringBookingPayment> findAllByOwnerIdAndStatusIn(
            UUID ownerId,
            Collection<RecurringPaymentStatus> statuses);

    List<RecurringBookingPayment> findAllBySitterIdAndStatusIn(
            UUID sitterId,
            Collection<RecurringPaymentStatus> statuses);

    List<RecurringBookingPayment> findAllByStatusAndReleaseRequestedAtLessThanEqual(
            RecurringPaymentStatus status,
            LocalDateTime threshold);
}
