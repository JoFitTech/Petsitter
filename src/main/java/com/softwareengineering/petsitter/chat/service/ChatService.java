package com.softwareengineering.petsitter.chat.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.chat.domain.ChatConversationDocument;
import com.softwareengineering.petsitter.chat.domain.ChatMessageDocument;
import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.repository.ChatConversationRepository;
import com.softwareengineering.petsitter.chat.repository.ChatMessageRepository;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.repository.OfferRequestRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Chat-Service – zentrale Businesslogik für Chat-Operationen.
 *
 * <p>Verantwortungen:
 * <ul>
 *   <li>Konversationen für Bookings anlegen (idempotent)</li>
 *   <li>Nachrichten lesen und schreiben</li>
 *   <li>Zugriffskontrolle durchsetzen</li>
 *   <li>Events publizieren für UI-Updates</li>
 *   <li>Notifications erstellen</li>
 * </ul>
 *
 * @see ChatAccessService für Zugriffschutz
 * @see ChatEventBus für Event-Publishing
 * @see NotificationService für Benachrichtigungen
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final OfferRequestRepository offerRequestRepository;
    private final ChatAccessService accessService;
    private final ChatEventBus eventBus;
    private final NotificationService notificationService;
    private final AuthenticatedUser authenticatedUser;
    private final UserRepository userRepository;

    public ChatService(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            BookingRepository bookingRepository,
            OfferRequestRepository offerRequestRepository,
            ChatAccessService accessService,
            ChatEventBus eventBus,
            NotificationService notificationService,
            AuthenticatedUser authenticatedUser,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
        this.offerRequestRepository = offerRequestRepository;
        this.accessService = accessService;
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.authenticatedUser = authenticatedUser;
        this.userRepository = userRepository;
    }

    /**
     * Erstellt automatisch eine Konversation für ein neu erstelltes Booking.
     *
     * <p>Diese Methode ist idempotent: Pro Booking wird maximal eine Konversation erstellt.
     * Wenn bereits eine existiert, wird sie zurückgegeben.
     *
     * @param bookingId ID des Bookings
     * @return die neu erstellte oder existierende Konversation
     * @throws NotFoundException wenn Booking nicht existiert
     */
    @Transactional
    public String createConversationForRequest(UUID requestId, String initialMessage) {
        log.info("Creating chat conversation for request {}", requestId);

        OfferRequest request = offerRequestRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Request nicht gefunden: " + requestId));

        var offer = request.getOffer();
        User creator = offer.getCreator();
        User requester = request.getRequester();

        User owner;
        User sitter;
        if (offer.getType() == OfferType.OWNER_OFFER) {
            owner = creator;
            sitter = requester;
        } else {
            sitter = creator;
            owner = requester;
        }

        String pairKey = buildUserPairKey(owner.getId(), sitter.getId());

        String convId;

        var existing = conversationRepository.findByUserPairKey(pairKey);
        if (existing.isPresent()) {
            log.info("Conversation for user pair {} already exists. Reusing {}.", pairKey, existing.get().getId());
            convId = existing.get().getId();
        } else {
            ChatConversationDocument conversation = new ChatConversationDocument();
            conversation.setOwnerId(owner.getId());
            conversation.setSitterId(sitter.getId());
            conversation.setOwnerDisplayName(getDisplayName(owner));
            conversation.setSitterDisplayName(getDisplayName(sitter));
            conversation.setRequestId(requestId.toString());
            conversation.setOfferId(offer.getOfferId().toString());
            conversation.setUserPairKey(pairKey);
            conversation.setCreatedAt(LocalDateTime.now());
            conversation = conversationRepository.save(conversation);
            convId = conversation.getId();
            log.info("Chat conversation created for request {} with id {}", requestId, convId);
        }

        saveRequestCardMessage(convId, requester.getId(), creator.getId(), requestId, offer.getTitle());

        String msgText = (initialMessage != null && !initialMessage.isBlank())
            ? initialMessage
            : "Guten Tag, ich interessiere mich für dieses Angebot.";
        sendMessage(convId, msgText);

        try {
            notificationService.createRequestNotification(creator, requester, convId);
        } catch (Exception e) {
            log.warn("Failed to create request notification: {}", e.getMessage(), e);
        }

        return convId;
    }

    @Transactional
    public ChatConversationDto createConversationForBooking(UUID bookingId) {
        log.info("Creating chat conversation for booking {}", bookingId);

        var existing = conversationRepository.findByBookingId(bookingId);
        if (existing.isPresent()) {
            log.info("Conversation for booking {} already exists. Returning existing.", bookingId);
            return toConversationDto(existing.get());
        }

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));

        // Reuse conversation created at request time if it exists (look up by user pair)
        String pairKey = buildUserPairKey(booking.getOwner().getId(), booking.getSitter().getId());
        var byPair = conversationRepository.findByUserPairKey(pairKey);
        if (byPair.isPresent()) {
            ChatConversationDocument conv = byPair.get();
            conv.setBookingId(bookingId);
            conversationRepository.save(conv);
            log.info("Linked existing conversation {} to booking {}", conv.getId(), bookingId);
            return toConversationDto(conv);
        }

        ChatConversationDocument conversation = new ChatConversationDocument();
        conversation.setBookingId(booking.getId());
        conversation.setOwnerId(booking.getOwner().getId());
        conversation.setSitterId(booking.getSitter().getId());
        conversation.setOwnerDisplayName(getDisplayName(booking.getOwner()));
        conversation.setSitterDisplayName(getDisplayName(booking.getSitter()));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageAt(null);
        conversation.setLastMessagePreview(null);

        conversation = conversationRepository.save(conversation);
        log.info("Chat conversation created for booking {} with id {}", bookingId, conversation.getId());

        return toConversationDto(conversation);
    }

    /**
     * Lädt alle Konversationen des aktuellen Users (als Owner oder Sitter),
     * sortiert nach letzter Nachricht (neueste zuerst).
     *
     * @return Liste der Konversationen als DTOs
     * @throws IllegalStateException wenn User nicht eingeloggt ist
     */
    public List<ChatConversationDto> getCurrentUserConversations() {
        UUID currentUserId = authenticatedUser.get()
            .map(User::getId)
            .orElse(null);

        if (currentUserId == null) {
            log.warn("Skipping conversation load because no authenticated user is available");
            return List.of();
        }

        log.debug("Loading conversations for user {}", currentUserId);

        List<ChatConversationDocument> conversations =
            conversationRepository.findAllByOwnerIdOrSitterIdOrderByLastMessageAtDesc(
                currentUserId, currentUserId
            );

        return conversations.stream()
            .map(this::toConversationDto)
            .toList();
    }

    /**
     * Lädt alle Nachrichten einer Konversation.
     *
     * <p>Der aktuelle User wird auf Zugriff geprüft.
     *
     * @param conversationId ID der Konversation
     * @return sortierte Liste der Nachrichten (älteste zuerst)
     * @throws NotFoundException           wenn Konversation nicht existiert
     * @throws ForbiddenOperationException wenn User keinen Zugriff hat
     */
    public List<ChatMessageDto> getMessages(String conversationId) {
        UUID currentUserId = authenticatedUser.get()
            .map(User::getId)
            .orElseThrow(() -> new IllegalStateException("User must be logged in"));

        // Zugriff prüfen
        accessService.verifyAccess(conversationId, currentUserId);

        log.debug("Loading messages for conversation {}", conversationId);

        List<ChatMessageDocument> messages = messageRepository
            .findAllByConversationIdOrderByCreatedAtAsc(conversationId);

        return messages.stream()
            .map(this::toMessageDto)
            .toList();
    }

    /**
     * Versendet eine neue Nachricht in einer Konversation.
     *
     * <p>Schritte:
     * <ol>
     *   <li>User-Authentifizierung & Zugriffskontrolle</li>
     *   <li>Text validieren</li>
     *   <li>Message in MongoDB speichern</li>
     *   <li>Konversation aktualisieren (lastMessage, preview)</li>
     *   <li>Notification für Empfänger erstellen</li>
     *   <li>Event publizieren für UI-Updates</li>
     * </ol>
     *
     * @param conversationId ID der Konversation
     * @param text          Nachrichtentext (max 1000 Zeichen)
     * @return die neue Nachricht als DTO
     * @throws NotFoundException               wenn Konversation nicht existiert
     * @throws ForbiddenOperationException     wenn User keinen Zugriff hat
     * @throws BusinessRuleViolationException  wenn Text leer oder zu lang ist
     */
    @Transactional
    public ChatMessageDto sendMessage(String conversationId, String text) {
        UUID currentUserId = authenticatedUser.get()
            .map(User::getId)
            .orElseThrow(() -> new IllegalStateException("User must be logged in"));

        // Zugriff prüfen
        ChatConversationDocument conversation = accessService.verifyAccess(conversationId, currentUserId);

        // Text validieren
        if (text == null || text.isBlank()) {
            throw new BusinessRuleViolationException("Nachrichtentext darf nicht leer sein.");
        }
        if (text.length() > 1000) {
            throw new BusinessRuleViolationException("Nachricht darf maximal 1000 Zeichen sein.");
        }

        // Empfänger bestimmen (die andere Person)
        UUID recipientId = conversation.getOwnerId().equals(currentUserId)
            ? conversation.getSitterId()
            : conversation.getOwnerId();

        // Message erstellen und speichern
        ChatMessageDocument message = new ChatMessageDocument();
        message.setConversationId(conversationId);
        message.setBookingId(conversation.getBookingId());
        message.setSenderId(currentUserId);
        message.setRecipientId(recipientId);
        message.setMessage(text);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);

        message = messageRepository.save(message);
        log.info("Message sent in conversation {} from {} to {}", conversationId, currentUserId, recipientId);

        // Konversation aktualisieren
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastMessagePreview(truncatePreview(text));
        conversationRepository.save(conversation);

        ChatMessageDto messageDto = toMessageDto(message);

        // Notification erstellen (mit Fallback auf direkte User-Lookups, falls Booking nicht mehr aufloesbar ist)
        User sender = null;
        User recipient = null;
        if (conversation.getBookingId() != null) {
            try {
                Booking booking = accessService.verifyBookingAccess(conversation.getBookingId(), currentUserId);
                sender = booking.getOwner().getId().equals(currentUserId) ? booking.getOwner() : booking.getSitter();
                recipient = booking.getOwner().getId().equals(currentUserId) ? booking.getSitter() : booking.getOwner();
            } catch (Exception e) {
                log.warn("Booking lookup failed for conversation {}. Falling back to user lookup: {}",
                    conversationId, e.getMessage());
            }
        }
        if (sender == null || recipient == null) {
            sender = userRepository.findById(currentUserId).orElse(null);
            recipient = userRepository.findById(recipientId).orElse(null);
        }

        if (sender != null && recipient != null) {
            try {
                notificationService.createChatMessageNotification(recipient, sender, conversationId);
                log.debug("Notification created for recipient {}", recipientId);
            } catch (Exception e) {
                log.warn("Failed to create notification for message in {}: {}", conversationId, e.getMessage(), e);
                // Nicht kritisch – Nachricht wurde trotzdem gespeichert
            }
        } else {
            log.warn("Skipped notification in conversation {} because sender ({}) or recipient ({}) could not be resolved",
                conversationId, currentUserId, recipientId);
        }

        // Event publizieren für Echtzeit-Updates
        eventBus.publish(messageDto);

        return messageDto;
    }

    /**
     * Markiert alle Nachrichten einer Konversation als gelesen
     * (für den aktuellen User als Empfänger).
     *
     * @param conversationId ID der Konversation
     * @throws NotFoundException           wenn Konversation nicht existiert
     * @throws ForbiddenOperationException wenn User keinen Zugriff hat
     */
    @Transactional
    public void markConversationAsRead(String conversationId) {
        UUID currentUserId = authenticatedUser.get()
            .map(User::getId)
            .orElseThrow(() -> new IllegalStateException("User must be logged in"));

        // Zugriff prüfen
        accessService.verifyAccess(conversationId, currentUserId);

        // Alle ungelesenen Nachrichten des Users als Empfänger markieren
        List<ChatMessageDocument> unread = messageRepository
            .findAllByConversationIdAndRecipientIdAndReadFalse(conversationId, currentUserId);

        for (ChatMessageDocument msg : unread) {
            msg.setRead(true);
            messageRepository.save(msg);
        }

        log.info("Marked {} messages as read in conversation {} for user {}",
            unread.size(), conversationId, currentUserId);

        // Auch in Notifications markieren
        try {
            notificationService.markChatNotificationsAsRead(conversationId, currentUserId);
        } catch (Exception e) {
            log.warn("Failed to mark notifications as read: {}", e.getMessage());
        }
    }

    /**
     * Zählt ungelesene Nachrichten für den aktuellen User.
     *
     * @return Anzahl der ungelesenen Nachrichten
     */
    public long countUnreadMessages() {
        UUID currentUserId = authenticatedUser.get()
            .map(User::getId)
            .orElse(null);

        if (currentUserId == null) {
            return 0;
        }

        return messageRepository.countByRecipientIdAndReadFalse(currentUserId);
    }

    public Map<String, Long> getUnreadCountsByConversation() {
        UUID userId = authenticatedUser.get().map(User::getId).orElse(null);
        if (userId == null) {
            return Map.of();
        }
        return messageRepository.findAllByRecipientIdAndReadFalse(userId).stream()
            .collect(Collectors.groupingBy(
                ChatMessageDocument::getConversationId,
                Collectors.counting()
            ));
    }

    // ── Private Hilfsmethoden ────────────────────────────────────────────

    private ChatConversationDto toConversationDto(ChatConversationDocument doc) {
        return new ChatConversationDto(
            doc.getId(),
            doc.getBookingId(),
            doc.getOwnerId(),
            doc.getSitterId(),
            doc.getOwnerDisplayName(),
            doc.getSitterDisplayName(),
            doc.getCreatedAt(),
            doc.getLastMessageAt(),
            doc.getLastMessagePreview(),
            doc.getRequestId(),
            doc.getOfferId()
        );
    }

    private ChatMessageDto toMessageDto(ChatMessageDocument doc) {
        return new ChatMessageDto(
            doc.getId(),
            doc.getConversationId(),
            doc.getBookingId(),
            doc.getSenderId(),
            doc.getRecipientId(),
            doc.getMessage(),
            doc.getCreatedAt(),
            doc.isRead(),
            doc.getType(),
            doc.getRequestId(),
            doc.getOfferTitle()
        );
    }

    private String getDisplayName(User user) {
        return (user.getFirstName() != null ? user.getFirstName().trim() : "")
            + " "
            + (user.getLastName() != null ? user.getLastName().trim() : "");
    }

    private void saveRequestCardMessage(String conversationId, UUID requesterId, UUID creatorId,
                                        UUID requestId, String offerTitle) {
        ChatMessageDocument card = new ChatMessageDocument();
        card.setConversationId(conversationId);
        card.setSenderId(requesterId);
        card.setRecipientId(creatorId);
        card.setType("REQUEST_CARD");
        card.setRequestId(requestId.toString());
        card.setOfferTitle(offerTitle != null ? offerTitle : "Angebot");
        card.setCreatedAt(LocalDateTime.now());
        card.setRead(false);
        ChatMessageDocument saved = messageRepository.save(card);
        eventBus.publish(toMessageDto(saved));
    }

    private String buildUserPairKey(UUID a, UUID b) {
        String s1 = a.toString(), s2 = b.toString();
        return s1.compareTo(s2) <= 0 ? s1 + "_" + s2 : s2 + "_" + s1;
    }

    private String truncatePreview(String text) {
        if (text.length() > 100) {
            return text.substring(0, 100) + "...";
        }
        return text;
    }

}
