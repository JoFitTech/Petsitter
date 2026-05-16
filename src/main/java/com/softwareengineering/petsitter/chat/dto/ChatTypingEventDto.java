package com.softwareengineering.petsitter.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Typing-Event fuer Echtzeit-Anzeige in der ChatView.
 */
public record ChatTypingEventDto(
        String conversationId,
        UUID senderId,
        UUID recipientId,
        boolean typing,
        LocalDateTime createdAt
) {
}

