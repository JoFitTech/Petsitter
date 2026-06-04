package com.softwareengineering.petsitter.chat.dto;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
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
    String offerId,
    ImageRefDto ownerProfileImage,
    ImageRefDto sitterProfileImage,
    UserRatingSummary ownerRatingSummary,
    UserRatingSummary sitterRatingSummary
) {
    public ChatConversationDto(
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
    ) {
        this(conversationId, bookingId, ownerId, sitterId, ownerDisplayName, sitterDisplayName, createdAt,
                lastMessageAt, lastMessagePreview, requestId, offerId, null, null, null, null);
    }

    public ChatConversationDto(
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
            String offerId,
            ImageRefDto ownerProfileImage,
            ImageRefDto sitterProfileImage
    ) {
        this(conversationId, bookingId, ownerId, sitterId, ownerDisplayName, sitterDisplayName, createdAt,
                lastMessageAt, lastMessagePreview, requestId, offerId, ownerProfileImage, sitterProfileImage, null, null);
    }
}
