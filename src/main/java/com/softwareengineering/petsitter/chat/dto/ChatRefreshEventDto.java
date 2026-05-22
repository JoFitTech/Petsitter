package com.softwareengineering.petsitter.chat.dto;

import java.util.UUID;

public record ChatRefreshEventDto(String conversationId, UUID senderId, UUID recipientId) {}
