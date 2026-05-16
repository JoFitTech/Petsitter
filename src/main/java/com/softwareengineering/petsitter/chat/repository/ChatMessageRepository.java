package com.softwareengineering.petsitter.chat.repository;

import com.softwareengineering.petsitter.chat.domain.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository für Chat-Nachrichten in MongoDB.
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    /**
     * Findet alle Nachrichten einer Konversation, sortiert chronologisch aufsteigend
     * (älteste zuerst).
     */
    List<ChatMessageDocument> findAllByConversationIdOrderByCreatedAtAsc(String conversationId);

    /**
     * Zählt ungelesene Nachrichten für einen User.
     */
    long countByRecipientIdAndReadFalse(UUID recipientId);

    /**
     * Findet alle ungelesenen Nachrichten einer Konversation für einen Empfänger.
     */
    List<ChatMessageDocument> findAllByConversationIdAndRecipientIdAndReadFalse(
            String conversationId,
            UUID recipientId
    );

}

