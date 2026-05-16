package com.softwareengineering.petsitter.chat.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chat-Nachricht – append-only in MongoDB.
 *
 * Jede Nachricht gehört zu einer {@link ChatConversationDocument}.
 * Nachrichten werden niemals gelöscht oder editiert (nur als "gelesen" markiert).
 */
@Document("chat_messages")
public class ChatMessageDocument {

    /**
     * MongoDB Document ID (auto-generated).
     */
    @Id
    private String id;

    /**
     * ID der übergeordneten Konversation (Fremdschlüssel zu ChatConversationDocument).
     * Indexed für schnelle Queries.
     */
    @Indexed
    private String conversationId;

    /**
     * Booking-ID (für schnelle Zugriffskontrolle).
     * Indexed für schnelle Queries.
     */
    @Indexed
    private UUID bookingId;

    /**
     * ID des Absenders der Nachricht.
     */
    private UUID senderId;

    /**
     * ID des Empfängers der Nachricht.
     * Indexed für Queries auf Empfänger-Basis.
     */
    @Indexed
    private UUID recipientId;

    /**
     * Der Nachrichtentext (max. 1000 Zeichen).
     */
    private String message;

    /**
     * Zeitstempel der Erstellung.
     * Indexed für zeitliche Sortierung.
     */
    @Indexed
    private LocalDateTime createdAt;

    /**
     * Markierung: Nachricht vom Empfänger gelesen?
     */
    private boolean read;

    public ChatMessageDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(UUID recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}


