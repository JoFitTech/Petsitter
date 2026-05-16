package com.softwareengineering.petsitter.chat.repository;

import com.softwareengineering.petsitter.chat.domain.ChatConversationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Chat-Konversationen in MongoDB.
 */
@Repository
public interface ChatConversationRepository extends MongoRepository<ChatConversationDocument, String> {

    /**
     * Findet die Konversation zu einem Booking.
     * Da bookingId unique ist, gibt es maximal eine.
     */
    Optional<ChatConversationDocument> findById(String id);

    Optional<ChatConversationDocument> findByBookingId(UUID bookingId);

    Optional<ChatConversationDocument> findByRequestId(String requestId);

    /**
     * Findet alle Konversationen eines Users (als Owner oder Sitter),
     * sortiert nach letzter Nachricht (neueste zuerst).
     */
    List<ChatConversationDocument> findAllByOwnerIdOrSitterIdOrderByLastMessageAtDesc(
            UUID ownerId,
            UUID sitterId
    );

}


