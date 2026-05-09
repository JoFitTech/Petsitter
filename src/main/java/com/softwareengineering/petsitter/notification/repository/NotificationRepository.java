package com.softwareengineering.petsitter.notification.repository;

import com.softwareengineering.petsitter.notification.domain.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
}

