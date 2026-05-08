package com.softwareengineering.petsitter.booking.repository;

import com.softwareengineering.petsitter.booking.domain.Booking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByOwnerIdOrSitterId(Long ownerId, Long sitterId);
}

