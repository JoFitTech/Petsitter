package com.softwareengineering.petsitter.notification.service;

import com.softwareengineering.petsitter.notification.domain.Notification;
import com.softwareengineering.petsitter.notification.domain.NotificationType;
import com.softwareengineering.petsitter.notification.repository.NotificationRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Notification-Service – verwaltet Benachrichtigungen in MySQL.
 *
 * <p>Zentrale Methoden:
 * <ul>
 *   <li>Inbox laden (Top-20)</li>
 *   <li>Ungelesene Notifications zählen</li>
 *   <li>Notifications als gelesen markieren</li>
 *   <li>Chat-Notifications erstellen</li>
 * </ul>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final AuthenticatedUser authenticatedUser;

    public NotificationService(
            NotificationRepository notificationRepository,
            AuthenticatedUser authenticatedUser
    ) {
        this.notificationRepository = notificationRepository;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Lädt die Top-20 Notifications für einen User (Inbox-Ansicht).
     *
     * @param userId ID des Users
     * @return Liste der Notifications, sortiert nach Erstellung (neueste zuerst)
     */
    public List<Notification> getInbox(UUID userId) {
        log.debug("Loading inbox for user {}", userId);
        return notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Zählt ungelesene Notifications für einen User.
     * Wird z.B. für das Badge im Header verwendet.
     *
     * @param userId ID des Users
     * @return Anzahl der ungelesenen Notifications
     */
    public long countUnread(UUID userId) {
        long count = notificationRepository.countByRecipientIdAndReadFalse(userId);
        log.debug("User {} has {} unread notifications", userId, count);
        return count;
    }

    /**
     * Erstellt eine Notification für eine neue Chat-Nachricht.
     *
     * @param recipient        User, der die Nachricht erhält
     * @param sender           User, der die Nachricht sendet
     * @param conversationId   ID der Chat-Konversation
     */
    @Transactional
    public void createChatMessageNotification(User recipient, User sender, String conversationId) {
        if (recipient == null || sender == null || conversationId == null) {
            log.warn("Cannot create notification: recipient={}, sender={}, conversationId={}",
                recipient, sender, conversationId);
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(NotificationType.CHAT_MESSAGE);
        notification.setMessage(
            sender.getFirstName() + " " + sender.getLastName() + " hat dir eine Nachricht gesendet."
        );
        notification.setReferenceId(conversationId);
        notification.setRead(false);

        notificationRepository.save(notification);
        log.info("Chat notification created for recipient {} from {} in conversation {}",
            recipient.getId(), sender.getId(), conversationId);
    }

    /**
     * Markiert eine einzelne Notification als gelesen.
     *
     * @param notificationId ID der Notification
     * @param currentUserId  ID des anfragenden Users (muss owner sein)
     * @throws NotFoundException           wenn Notification nicht existiert
     * @throws ForbiddenOperationException wenn User nicht owner ist
     */
    @Transactional
    public void markAsRead(UUID notificationId, UUID currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException("Notification nicht gefunden: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new ForbiddenOperationException(
                "Du kannst nur deine eigenen Notifications als gelesen markieren."
            );
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        log.debug("Notification {} marked as read for user {}", notificationId, currentUserId);
    }

    /**
     * Markiert alle Chat-Notifications einer Konversation als gelesen
     * für einen bestimmten User.
     *
     * @param conversationId ID der Chat-Konversation
     * @param currentUserId  ID des Users
     */
    @Transactional
    public void markChatNotificationsAsRead(String conversationId, UUID currentUserId) {
        List<Notification> notifications = notificationRepository
            .findAllByRecipientIdAndReferenceId(currentUserId, conversationId);

        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }

        log.info("Marked {} chat notifications as read for user {} in conversation {}",
            notifications.size(), currentUserId, conversationId);
    }

    /**
     * Spezielle Methode zum Laden der Inbox für den aktuellen User.
     * Falls nicht eingeloggt, wird eine leere Liste zurückgegeben.
     *
     * @return Inbox-Notifications für den aktuellen User
     */
    public List<Notification> getCurrentUserInbox() {
        return authenticatedUser.get()
            .map(User::getId)
            .map(this::getInbox)
            .orElse(List.of());
    }

    /**
     * Zählt ungelesene Notifications für den aktuellen User.
     *
     * @return Anzahl der ungelesenen Notifications
     */
    public long countCurrentUserUnread() {
        return authenticatedUser.get()
            .map(User::getId)
            .map(this::countUnread)
            .orElse(0L);
    }

}
