package com.softwareengineering.petsitter.chat.service;

import com.softwareengineering.petsitter.booking.domain.Booking;
import com.softwareengineering.petsitter.booking.repository.BookingRepository;
import com.softwareengineering.petsitter.chat.domain.ChatConversationDocument;
import com.softwareengineering.petsitter.chat.repository.ChatConversationRepository;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Zugriffschutz für Chat-Operationen.
 *
 * Stellt sicher, dass nur Owner und Sitter eines Bookings den Chat sehen und darin
 * Nachrichten schreiben dürfen.
 */
@Service
public class ChatAccessService {

    private static final Logger log = LoggerFactory.getLogger(ChatAccessService.class);

    private final ChatConversationRepository conversationRepository;
    private final BookingRepository bookingRepository;

    public ChatAccessService(
            ChatConversationRepository conversationRepository,
            BookingRepository bookingRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Prüft, ob ein User auf eine Konversation zugreifen darf.
     *
     * @param conversationId ID der Konversation
     * @param userId         ID des anfragenden Users
     * @return die Konversation, wenn Zugriff erlaubt
     * @throws NotFoundException               wenn Konversation nicht existiert
     * @throws ForbiddenOperationException     wenn User nicht authorized ist
     */
    public ChatConversationDocument verifyAccess(String conversationId, UUID userId) {
        ChatConversationDocument conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new NotFoundException("Chat-Konversation nicht gefunden: " + conversationId));

        boolean isOwner = conversation.getOwnerId().equals(userId);
        boolean isSitter = conversation.getSitterId().equals(userId);

        if (!isOwner && !isSitter) {
            log.warn("Access denied to conversation {} for user {}. Owner: {}, Sitter: {}",
                conversationId, userId, conversation.getOwnerId(), conversation.getSitterId());
            throw new ForbiddenOperationException(
                "Du darfst diesen Chat nicht sehen. Nur Owner und Sitter dürfen zugreifen."
            );
        }

        return conversation;
    }

    /**
     * Prüft, ob ein User Zugriff auf ein Booking hat.
     *
     * @param bookingId ID des Bookings
     * @param userId    ID des anfragenden Users
     * @return das Booking, wenn Zugriff erlaubt
     * @throws NotFoundException           wenn Booking nicht existiert
     * @throws ForbiddenOperationException wenn User nicht authorized ist
     */
    public Booking verifyBookingAccess(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking nicht gefunden: " + bookingId));

        boolean isOwner = booking.getOwner().getId().equals(userId);
        boolean isSitter = booking.getSitter().getId().equals(userId);

        if (!isOwner && !isSitter) {
            log.warn("Access denied to booking {} for user {}", bookingId, userId);
            throw new ForbiddenOperationException(
                "Du darfst dieses Booking nicht einsehen. Nur Owner und Sitter dürfen zugreifen."
            );
        }

        return booking;
    }

}



