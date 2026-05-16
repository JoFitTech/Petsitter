package com.softwareengineering.petsitter.chat.service;

import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * In-Memory Event Bus für Chat-Updates.
 *
 * Ermöglicht Echtzeit-Benachrichtigungen zwischen Services und UI-Komponenten.
 * Listener werden registriert und erhalten neue Messages, wenn diese publiziert werden.
 *
 * Hinweis: Funktioniert nur innerhalb einer App-Instanz. Für Multi-Instance-Setup später
 * auf Redis Pub/Sub oder ähnlich umstellen.
 */
@Component
public class ChatEventBus implements ChatUiEventBus {

    private static final Logger log = LoggerFactory.getLogger(ChatEventBus.class);

    /**
     * Speichert Listener pro Empfänger-User.
     */
    private final Map<UUID, List<Consumer<ChatMessageDto>>> listenersByUserId = new ConcurrentHashMap<>();
    private final Map<UUID, List<Consumer<ChatTypingEventDto>>> typingListenersByUserId = new ConcurrentHashMap<>();

    /**
     * Registriert einen Listener für einen User.
     *
     * @param userId   ID des Users, der Nachrichten empfangen soll
     * @param listener Consumer, der bei neuen Nachrichten aufgerufen wird
     * @return Registration, um den Listener später zu entfernen
     */
    public Registration register(UUID userId, Consumer<ChatMessageDto> listener) {
        List<Consumer<ChatMessageDto>> userListeners = listenersByUserId
            .computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>());
        userListeners.add(listener);

        log.debug("Registered chat listener for user {}", userId);

        return () -> {
            List<Consumer<ChatMessageDto>> listeners = listenersByUserId.get(userId);
            if (listeners != null) {
                listeners.remove(listener);
                log.debug("Deregistered chat listener for user {}", userId);
            }
        };
    }

    /**
     * Publiziert eine neue Nachricht an alle registrierten Listener des Empfängers.
     *
     * @param message Die zu veröffentlichende Nachricht
     */
    public void publish(ChatMessageDto message) {
        List<Consumer<ChatMessageDto>> listeners = listenersByUserId
            .getOrDefault(message.recipientId(), List.of());

        log.debug("Publishing chat message from {} to {}. {} listeners registered.",
            message.senderId(), message.recipientId(), listeners.size());

        listeners.forEach(listener -> {
            try {
                listener.accept(message);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        });
    }

    /**
     * Registriert einen Listener fuer Typing-Events eines Users.
     */
    public Registration registerTyping(UUID userId, Consumer<ChatTypingEventDto> listener) {
        List<Consumer<ChatTypingEventDto>> userListeners = typingListenersByUserId
                .computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>());
        userListeners.add(listener);

        return () -> {
            List<Consumer<ChatTypingEventDto>> listeners = typingListenersByUserId.get(userId);
            if (listeners != null) {
                listeners.remove(listener);
            }
        };
    }

    /**
     * Publiziert ein Typing-Event an alle Listener des Empfaengers.
     */
    public void publishTyping(ChatTypingEventDto event) {
        List<Consumer<ChatTypingEventDto>> listeners = typingListenersByUserId
                .getOrDefault(event.recipientId(), List.of());

        listeners.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.error("Error notifying typing listener", e);
            }
        });
    }

}




