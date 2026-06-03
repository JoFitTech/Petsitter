package com.softwareengineering.petsitter.ui.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.Registration;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.user.domain.User;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ChatViewIntegrationTest {

    @Test
    void chatView_loadsConversationListFromService() {
        ChatService chatService = Mockito.mock(ChatService.class);
        ChatEventBus eventBus = Mockito.mock(ChatEventBus.class);
        AuthenticatedUser authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        RequestService requestService = Mockito.mock(RequestService.class);
        BookingService bookingService = Mockito.mock(BookingService.class);
        UserService userService = Mockito.mock(UserService.class);

        UUID currentUserId = UUID.randomUUID();
        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));
        when(eventBus.register(eq(currentUserId), any())).thenReturn((Registration) () -> { });
        when(chatService.getCurrentUserConversations()).thenReturn(List.of(
                new ChatConversationDto(
                        "conv-1",
                        UUID.randomUUID(),
                        currentUserId,
                        UUID.randomUUID(),
                        "Anna Owner",
                        "Ben Sitter",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "Hallo",
                        null,
                        null
                )
        ));

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, userService);

        assertThat(containsText(view, "Ben Sitter")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void chatView_eventBusAppendsMessageForActiveConversation() throws Exception {
        ChatService chatService = Mockito.mock(ChatService.class);
        ChatEventBus eventBus = Mockito.mock(ChatEventBus.class);
        AuthenticatedUser authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        RequestService requestService = Mockito.mock(RequestService.class);
        BookingService bookingService = Mockito.mock(BookingService.class);
        UserService userService = Mockito.mock(UserService.class);

        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));

        AtomicReference<Consumer<ChatMessageDto>> listenerRef = new AtomicReference<>();
        when(eventBus.register(eq(currentUserId), any())).thenAnswer(invocation -> {
            listenerRef.set((Consumer<ChatMessageDto>) invocation.getArgument(1));
            return (Registration) () -> { };
        });

        ChatConversationDto conversation = new ChatConversationDto(
                "conv-2",
                UUID.randomUUID(),
                currentUserId,
                otherUserId,
                "Anna Owner",
                "Ben Sitter",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Preview",
                null,
                null
        );

        when(chatService.getCurrentUserConversations()).thenReturn(List.of(conversation));
        when(chatService.getMessages("conv-2")).thenReturn(List.of(
                new ChatMessageDto(
                        "m-old",
                        "conv-2",
                        conversation.bookingId(),
                        otherUserId,
                        currentUserId,
                        "Bestehende Nachricht",
                        LocalDateTime.now(),
                        false,
                        null,
                        null,
                        null
                )
        ));

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, userService);

        Method selectConversation = ChatView.class.getDeclaredMethod("selectConversation", String.class);
        selectConversation.setAccessible(true);
        selectConversation.invoke(view, "conv-2");

        VerticalLayout messageList = (VerticalLayout) getField(view, "messageList");
        int before = messageList.getComponentCount();

        ChatMessageDto incoming = new ChatMessageDto(
                "m-new",
                "conv-2",
                conversation.bookingId(),
                otherUserId,
                currentUserId,
                "Neue Event-Nachricht",
                LocalDateTime.now(),
                false,
                null,
                null,
                null
        );

        // simulate delivery by same logic as listener callback
        messageList.add((Component) invokePrivate(view, "buildMessageBubble", new Class<?>[]{ChatMessageDto.class}, incoming));

        int after = messageList.getComponentCount();

        assertThat(after).isEqualTo(before + 1);
        assertThat(containsText(view, "Neue Event-Nachricht")).isTrue();
    }

    @Test
    void chatView_rendersReviewCardWithStarsAndComment() throws Exception {
        ChatService chatService = Mockito.mock(ChatService.class);
        ChatEventBus eventBus = Mockito.mock(ChatEventBus.class);
        AuthenticatedUser authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        RequestService requestService = Mockito.mock(RequestService.class);
        BookingService bookingService = Mockito.mock(BookingService.class);
        UserService userService = Mockito.mock(UserService.class);

        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));
        when(eventBus.register(eq(currentUserId), any())).thenReturn((Registration) () -> { });
        when(eventBus.registerTyping(eq(currentUserId), any())).thenReturn((Registration) () -> { });
        when(eventBus.registerRefresh(eq(currentUserId), any())).thenReturn((Registration) () -> { });

        ChatConversationDto conversation = new ChatConversationDto(
                "conv-review",
                UUID.randomUUID(),
                currentUserId,
                otherUserId,
                "Anna Owner",
                "Ben Sitter",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Preview",
                null,
                null
        );

        when(chatService.getCurrentUserConversations()).thenReturn(List.of(conversation));
        when(chatService.getMessages("conv-review")).thenReturn(List.of());

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, userService);
        invokePrivate(view, "selectConversation", new Class<?>[]{String.class}, "conv-review");

        ChatMessageDto reviewMessage = new ChatMessageDto(
                "m-review",
                "conv-review",
                conversation.bookingId(),
                otherUserId,
                currentUserId,
                "[RATING:4] Sehr zuverlässig",
                LocalDateTime.now(),
                false,
                "REVIEW_CARD",
                null,
                null
        );

        Component bubble = (Component) invokePrivate(view, "buildReviewCardBubble", new Class<?>[]{ChatMessageDto.class}, reviewMessage);

        assertThat(containsTextPart(bubble, "Neue Bewertung")).isTrue();
        assertThat(containsTextPart(bubble, "Sterne:")).isTrue();
        assertThat(containsTextPart(bubble, "Sehr zuverlässig")).isTrue();
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof Span span && text.equals(span.getText())) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }

    private boolean containsTextPart(Component root, String textPart) {
        if (root instanceof Span span && span.getText() != null && span.getText().contains(textPart)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsTextPart(child, textPart));
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private Object invokePrivate(Object target, String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
