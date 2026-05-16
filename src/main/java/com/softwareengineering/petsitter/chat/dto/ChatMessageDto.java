package com.softwareengineering.petsitter.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO für eine einzelne Chat-Nachricht.
 *
 * Wird von {@link com.softwareengineering.petsitter.chat.service.ChatService}
 * zurückgegeben und über {@link com.softwareengineering.petsitter.chat.service.ChatEventBus}
 * gepusht.
 */
public record ChatMessageDto(
    String messageId,
    String conversationId,
    UUID bookingId,
    UUID senderId,
    UUID recipientId,
    String message,
    LocalDateTime createdAt,
    boolean read
) { }

