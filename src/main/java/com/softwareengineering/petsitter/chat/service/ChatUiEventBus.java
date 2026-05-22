package com.softwareengineering.petsitter.chat.service;

import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatRefreshEventDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstraktion fuer Chat-UI-Events.
 *
 * Ermoeglicht spaeteren Austausch des In-Memory EventBus gegen Redis Pub/Sub,
 * ohne UI und Service-Aufrufer anzupassen.
 */
public interface ChatUiEventBus {

    Registration register(UUID userId, Consumer<ChatMessageDto> listener);

    void publish(ChatMessageDto message);

    Registration registerTyping(UUID userId, Consumer<ChatTypingEventDto> listener);

    void publishTyping(ChatTypingEventDto event);

    Registration registerRefresh(UUID userId, Consumer<ChatRefreshEventDto> listener);

    void publishRefresh(ChatRefreshEventDto event);
}

