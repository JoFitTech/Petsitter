package com.softwareengineering.petsitter.notification.repository;

import com.softwareengineering.petsitter.notification.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}

