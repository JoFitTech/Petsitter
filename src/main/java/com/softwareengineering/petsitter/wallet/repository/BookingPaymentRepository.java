package com.softwareengineering.petsitter.wallet.repository;

import com.softwareengineering.petsitter.wallet.domain.BookingPayment;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingPaymentRepository extends JpaRepository<BookingPayment, UUID> {
    Optional<BookingPayment> findByBookingId(UUID bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from BookingPayment payment where payment.booking.id = :bookingId")
    Optional<BookingPayment> findByBookingIdForUpdate(@Param("bookingId") UUID bookingId);

    List<BookingPayment> findAllByOwnerIdAndStatusIn(UUID ownerId, Collection<PaymentStatus> statuses);

    List<BookingPayment> findAllBySitterIdAndStatusIn(UUID sitterId, Collection<PaymentStatus> statuses);

    List<BookingPayment> findAllByStatusAndReleaseRequestedAtLessThanEqual(
            PaymentStatus status, LocalDateTime threshold);
}
