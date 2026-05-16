package com.softwareengineering.petsitter.booking.repository;

import com.softwareengineering.petsitter.booking.domain.Booking;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByOwnerIdOrSitterId(UUID ownerId, UUID sitterId);
    java.util.Optional<Booking> findByAcceptedRequest_Id(UUID requestId);
    List<Booking> findAllByAcceptedRequest_Id(UUID requestId);
}

