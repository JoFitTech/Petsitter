package com.softwareengineering.petsitter.chat.service.redis;

import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatRefreshEventDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import com.softwareengineering.petsitter.chat.service.ChatUiEventBus;
import com.softwareengineering.petsitter.chat.service.Registration;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Phase-9 Skeleton fuer Redis-basierten EventBus.
 *
 * Bewusst noch nicht als Spring-Bean registriert, damit die aktuelle
 * In-Memory-Implementierung aktiv bleibt.
 */
public class RedisChatEventBus implements ChatUiEventBus {

    @Override
    public Registration register(UUID userId, Consumer<ChatMessageDto> listener) {
        throw new UnsupportedOperationException("RedisChatEventBus ist als Skeleton angelegt und noch nicht aktiv.");
    }

    @Override
    public void publish(ChatMessageDto message) {
        throw new UnsupportedOperationException("RedisChatEventBus ist als Skeleton angelegt und noch nicht aktiv.");
    }

    @Override
    public Registration registerTyping(UUID userId, Consumer<ChatTypingEventDto> listener) {
        throw new UnsupportedOperationException("RedisChatEventBus ist als Skeleton angelegt und noch nicht aktiv.");
    }

    @Override
    public void publishTyping(ChatTypingEventDto event) {
        throw new UnsupportedOperationException("RedisChatEventBus ist als Skeleton angelegt und noch nicht aktiv.");
    }

    @Override
    public Registration registerRefresh(UUID userId, Consumer<ChatRefreshEventDto> listener) {
        throw new UnsupportedOperationException("RedisChatEventBus ist als Skeleton angelegt und noch nicht aktiv.");
    }

    @Override
    public void publishRefresh(ChatRefreshEventDto event) {
        throw new UnsupportedOperationException("RedisChatEventBus ist als Skeleton angelegt und noch nicht aktiv.");
    }
}

