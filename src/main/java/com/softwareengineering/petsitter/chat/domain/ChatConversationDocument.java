package com.softwareengineering.petsitter.chat.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chat-Konversation – gehört zu genau einem Booking.
 *
 * Speichert Metadaten über einen Chatkanal zwischen Owner und Sitter.
 * Die eigentlichen Messages werden in {@link ChatMessageDocument} gespeichert.
 */
@Document("chat_conversations")
public class ChatConversationDocument {

    /**
     * MongoDB Document ID (auto-generated).
     */
    @Id
    private String id;

    /**
     * Eindeutige Zuordnung zum Booking.
     * Unique Index: pro Booking maximal eine Conversation.
     */
    @Indexed(unique = true)
    private UUID bookingId;

    /**
     * ID des Pet-Besitzers (Owner).
     */
    private UUID ownerId;

    /**
     * ID des Tiersitters (Sitter).
     */
    private UUID sitterId;

    /**
     * Anzeigename des Owners für Schnellzugriff.
     */
    private String ownerDisplayName;

    /**
     * Anzeigename des Sitters für Schnellzugriff.
     */
    private String sitterDisplayName;

    /**
     * Zeitstempel der Konversationserstellung.
     */
    private LocalDateTime createdAt;

    /**
     * Zeitstempel der letzten Nachricht (für Sortierung & Schnellzugriff).
     */
    private LocalDateTime lastMessageAt;

    /**
     * Preview der letzten Nachricht (z.B. erste 100 Zeichen).
     */
    private String lastMessagePreview;

    public ChatConversationDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getSitterId() {
        return sitterId;
    }

    public void setSitterId(UUID sitterId) {
        this.sitterId = sitterId;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public String getSitterDisplayName() {
        return sitterDisplayName;
    }

    public void setSitterDisplayName(String sitterDisplayName) {
        this.sitterDisplayName = sitterDisplayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }
}


