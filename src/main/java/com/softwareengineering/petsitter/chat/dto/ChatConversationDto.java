package com.softwareengineering.petsitter.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO für Chat-Konversations-Metadaten.
 *
 * Wird von {@link com.softwareengineering.petsitter.chat.service.ChatService}
 * zurückgegeben und in der UI angezeigt.
 */
public record ChatConversationDto(
    String conversationId,
    UUID bookingId,
    UUID ownerId,
    UUID sitterId,
    String ownerDisplayName,
    String sitterDisplayName,
    LocalDateTime createdAt,
    LocalDateTime lastMessageAt,
    String lastMessagePreview,
    String requestId,
    String offerId
) { }

