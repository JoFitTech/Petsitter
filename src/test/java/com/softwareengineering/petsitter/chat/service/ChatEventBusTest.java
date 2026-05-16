package com.softwareengineering.petsitter.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ChatEventBusTest {

    @Test
    void chatEventBus_notifiesRecipient() {
        ChatEventBus eventBus = new ChatEventBus();
        UUID recipientId = UUID.randomUUID();

        AtomicInteger callCount = new AtomicInteger();
        eventBus.register(recipientId, ignored -> callCount.incrementAndGet());

        ChatMessageDto dto = new ChatMessageDto(
                "m1",
                "c1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                recipientId,
                "Hallo",
                LocalDateTime.now(),
                false
        );

        eventBus.publish(dto);

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void chatEventBus_doesNotNotifyOtherUsers() {
        ChatEventBus eventBus = new ChatEventBus();
        UUID recipientId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        AtomicInteger recipientCalls = new AtomicInteger();
        AtomicInteger otherCalls = new AtomicInteger();

        eventBus.register(recipientId, ignored -> recipientCalls.incrementAndGet());
        eventBus.register(otherUserId, ignored -> otherCalls.incrementAndGet());

        ChatMessageDto dto = new ChatMessageDto(
                "m2",
                "c2",
                UUID.randomUUID(),
                UUID.randomUUID(),
                recipientId,
                "Test",
                LocalDateTime.now(),
                false
        );

        eventBus.publish(dto);

        assertThat(recipientCalls.get()).isEqualTo(1);
        assertThat(otherCalls.get()).isZero();
    }

    @Test
    void chatEventBus_typingNotifiesOnlyRecipient() {
        ChatEventBus eventBus = new ChatEventBus();
        UUID recipientId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        AtomicInteger recipientCalls = new AtomicInteger();
        AtomicInteger otherCalls = new AtomicInteger();

        eventBus.registerTyping(recipientId, ignored -> recipientCalls.incrementAndGet());
        eventBus.registerTyping(otherUserId, ignored -> otherCalls.incrementAndGet());

        eventBus.publishTyping(new ChatTypingEventDto(
                "conv-typing",
                UUID.randomUUID(),
                recipientId,
                true,
                LocalDateTime.now()
        ));

        assertThat(recipientCalls.get()).isEqualTo(1);
        assertThat(otherCalls.get()).isZero();
    }
}


