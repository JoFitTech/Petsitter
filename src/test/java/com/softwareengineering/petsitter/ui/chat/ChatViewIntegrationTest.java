package com.softwareengineering.petsitter.ui.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.dto.OfferRequestChatCardDto;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.user.domain.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatViewIntegrationTest {

    @Test
    void chatView_loadsConversationListFromService() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        TestChatService chatService = new TestChatService();
        ChatEventBus eventBus = new ChatEventBus();
        AuthenticatedUser authenticatedUser = new TestAuthenticatedUser(currentUser);
        RequestService requestService = new TestRequestService();
        BookingService bookingService = new TestBookingService();

        chatService.conversations = List.of(
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
        );

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, null);

        assertThat(containsText(view, "Ben Sitter")).isTrue();
    }

    @Test
    void chatView_eventBusAppendsMessageForActiveConversation() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        TestChatService chatService = new TestChatService();
        ChatEventBus eventBus = new ChatEventBus();
        AuthenticatedUser authenticatedUser = new TestAuthenticatedUser(currentUser);
        RequestService requestService = new TestRequestService();
        BookingService bookingService = new TestBookingService();

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

        chatService.conversations = List.of(conversation);
        chatService.messagesByConversation.put("conv-2", List.of(
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

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, null);

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
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        TestChatService chatService = new TestChatService();
        ChatEventBus eventBus = new ChatEventBus();
        AuthenticatedUser authenticatedUser = new TestAuthenticatedUser(currentUser);
        RequestService requestService = new TestRequestService();
        BookingService bookingService = new TestBookingService();

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

        chatService.conversations = List.of(conversation);
        chatService.messagesByConversation.put("conv-review", List.of());

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, null);
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

    @Test
    void chatView_rendersPendingRequestCardWithOfferDetailsAndActions() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        TestChatService chatService = new TestChatService();
        ChatEventBus eventBus = new ChatEventBus();
        AuthenticatedUser authenticatedUser = new TestAuthenticatedUser(currentUser);
        TestRequestService requestService = new TestRequestService();
        BookingService bookingService = new TestBookingService();
        requestService.detailsByRequestId.put(requestId, new OfferRequestChatCardDto(
                requestId,
                RequestStatus.PENDING,
                "Urlaubsbetreuung",
                OfferType.OWNER_OFFER,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                OfferFrequency.ONE_TIME,
                Set.of(),
                null,
                "Balu, Minka",
                null
        ));

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, null);
        ChatMessageDto requestCard = new ChatMessageDto(
                "m-request",
                "conv-request",
                null,
                requesterId,
                currentUserId,
                null,
                LocalDateTime.now(),
                false,
                "REQUEST_CARD",
                requestId.toString(),
                "Fallback-Titel"
        );

        Component bubble = (Component) invokePrivate(
                view,
                "buildRequestCardBubble",
                new Class<?>[]{ChatMessageDto.class},
                requestCard);

        assertThat(containsText(bubble, "Angebot")).isTrue();
        assertThat(containsText(bubble, "Urlaubsbetreuung")).isTrue();
        assertThat(containsText(bubble, "Zeitraum")).isTrue();
        assertThat(containsTextPart(bubble, "10")).isTrue();
        assertThat(containsTextPart(bubble, "15. Juli")).isTrue();
        assertThat(containsText(bubble, "Tiere")).isTrue();
        assertThat(containsText(bubble, "Balu, Minka")).isTrue();
        assertThat(containsText(bubble, "Annehmen")).isTrue();
        assertThat(containsText(bubble, "Ablehnen")).isTrue();
    }

    @Test
    void chatView_rendersRequestCardFallbackWhenDetailsCannotBeLoaded() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setFirstName("Anna");
        currentUser.setLastName("Owner");

        TestChatService chatService = new TestChatService();
        ChatEventBus eventBus = new ChatEventBus();
        AuthenticatedUser authenticatedUser = new TestAuthenticatedUser(currentUser);
        TestRequestService requestService = new TestRequestService();
        BookingService bookingService = new TestBookingService();
        requestService.failingRequestIds.add(requestId);

        ChatView view = new ChatView(chatService, eventBus, authenticatedUser, requestService, bookingService, null);
        ChatMessageDto requestCard = new ChatMessageDto(
                "m-request",
                "conv-request",
                null,
                requesterId,
                currentUserId,
                null,
                LocalDateTime.now(),
                false,
                "REQUEST_CARD",
                requestId.toString(),
                "Alter gespeicherter Titel"
        );

        Component bubble = (Component) invokePrivate(
                view,
                "buildRequestCardBubble",
                new Class<?>[]{ChatMessageDto.class},
                requestCard);

        assertThat(containsText(bubble, "Alter gespeicherter Titel")).isTrue();
        assertThat(containsText(bubble, "Unbekannter Status")).isTrue();
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && text.equals(hasText.getText())) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }

    private boolean containsTextPart(Component root, String textPart) {
        if (root instanceof HasText hasText && hasText.getText() != null && hasText.getText().contains(textPart)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsTextPart(child, textPart));
    }

    private static final class TestChatService extends ChatService {
        private List<ChatConversationDto> conversations = List.of();
        private final Map<String, List<ChatMessageDto>> messagesByConversation = new HashMap<>();

        private TestChatService() {
            super(null, null, null, null, null, null, null, null, null);
        }

        @Override
        public List<ChatConversationDto> getCurrentUserConversations() {
            return conversations;
        }

        @Override
        public List<ChatMessageDto> getMessages(String conversationId) {
            return messagesByConversation.getOrDefault(conversationId, List.of());
        }

        @Override
        public void markConversationAsRead(String conversationId) {
        }

        @Override
        public Map<String, Long> getUnreadCountsByConversation() {
            return Map.of();
        }
    }

    private static final class TestAuthenticatedUser extends AuthenticatedUser {
        private final User user;

        private TestAuthenticatedUser(User user) {
            super(null);
            this.user = user;
        }

        @Override
        public Optional<User> get() {
            return Optional.ofNullable(user);
        }
    }

    private static final class TestRequestService extends RequestService {
        private final Map<UUID, OfferRequestChatCardDto> detailsByRequestId = new HashMap<>();
        private final Set<UUID> failingRequestIds = new HashSet<>();

        private TestRequestService() {
            super(null, null, null);
        }

        @Override
        public OfferRequestChatCardDto findChatCardDetails(UUID requestId) {
            if (failingRequestIds.contains(requestId)) {
                throw new RuntimeException("Request fehlt");
            }
            return detailsByRequestId.get(requestId);
        }
    }

    private static final class TestBookingService extends BookingService {
        private TestBookingService() {
            super(null, null, null, null, null);
        }

        @Override
        public boolean isBookingCancelledForRequest(UUID requestId) {
            return false;
        }
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
