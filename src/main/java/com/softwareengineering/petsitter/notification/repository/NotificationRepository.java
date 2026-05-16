package com.softwareengineering.petsitter.notification.repository;

import com.softwareengineering.petsitter.notification.domain.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    /**
     * Findet alle Notifications eines Recipients, sortiert nach neuesten zuerst.
     */
    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    /**
     * Findet die Top-N Notifications für einen User (für Inbox).
     */
    List<Notification> findTop20ByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    /**
     * Zählt ungelesene Notifications für einen User.
     */
    long countByRecipientIdAndReadFalse(UUID recipientId);

    /**
     * Findet Notifications mit bestimmter referenceId (z.B. conversationId).
     */
    List<Notification> findAllByReferenceId(String referenceId);

    /**
     * Findet Notifications mit bestimmter referenceId für einen Recipient.
     */
    List<Notification> findAllByRecipientIdAndReferenceId(UUID recipientId, String referenceId);
}
