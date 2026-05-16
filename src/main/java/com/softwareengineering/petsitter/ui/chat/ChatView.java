package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.chat.service.Registration;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Chat-View – Echtzeit-Chatfenster für Bookings.
 *
 * Layout:
 * - Linke Spalte: Liste der Konversationen
 * - Rechte Spalte: Chat-Verlauf + Input
 *
 * Automatische Updates via Vaadin Push + ChatEventBus.
 */
@Route(value = "chat", layout = MainLayout.class)
@PageTitle("Chat | Pawsitter")
@PermitAll
public class ChatView extends HorizontalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);

    private static final String DARK = "#4a3428";
    private static final String CREAM = "#fbf8f1";
    private static final String CARD_BG = "#ffffff";

    private final ChatService chatService;
    private final ChatEventBus eventBus;
    private final RequestService requestService;
    private final BookingService bookingService;

    // UI Components
    private VerticalLayout conversationList;
    private VerticalLayout messageList;
    private TextArea messageInput;
    private Button sendButton;
    private Span chatTitle;
    private Span typingIndicator;

    // State
    private String activeConversationId;
    private UUID activeRecipientId;
    private String activeCounterpartName;
    private UUID currentUserId;
    private final Map<String, ChatConversationDto> conversationsById = new HashMap<>();
    private final Map<String, Boolean> typingByConversationId = new HashMap<>();
    private Registration eventBusRegistration;
    private Registration typingRegistration;
    private final ScheduledExecutorService typingScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> stopTypingFuture;

    public ChatView(
            ChatService chatService,
            ChatEventBus eventBus,
            AuthenticatedUser authenticatedUser,
            RequestService requestService,
            BookingService bookingService
    ) {
        this.chatService = chatService;
        this.eventBus = eventBus;
        this.requestService = requestService;
        this.bookingService = bookingService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Determine current user
        this.currentUserId = authenticatedUser.get()
            .map(com.softwareengineering.petsitter.user.domain.User::getId)
            .orElse(null);

        if (currentUserId == null) {
            add(new Paragraph("Bitte melde dich an."));
            return;
        }

        // Build layout
        add(buildConversationList());
        add(buildChatWindow());

        // Load conversations
        refreshConversationList();

        // Register for chat events
        registerEventBusListener();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // ensure user is logged in
        if (currentUserId == null) {
            event.rerouteTo("login");
            return;
        }

        // support query parameter: ?conversation=<conversationId>
        event.getLocation().getQueryParameters()
            .getParameters()
            .getOrDefault("conversation", List.of())
            .stream()
            .findFirst()
            .ifPresent(this::selectConversation);
    }


    // ── Private Methods: UI Building ──

    private Component buildConversationList() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("320px");
        layout.setHeightFull();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getStyle()
            .set("border-right", "1px solid #f0e6da")
            .set("overflow-y", "auto")
            .set("background", "#ffffff")
            .set("box-sizing", "border-box");

        H2 title = new H2("Gespräche");
        title.getStyle().set("margin", "0 0 16px 0").set("font-size", "18px");
        layout.add(title);

        this.conversationList = new VerticalLayout();
        conversationList.setPadding(false);
        conversationList.setSpacing(false);
        layout.add(conversationList);
        layout.setFlexGrow(1, conversationList);

        return layout;
    }

    private Component buildChatWindow() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setHeightFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("background", CARD_BG);

        // Header
        layout.add(buildChatHeader());

        // Messages area
        this.messageList = new VerticalLayout();
        messageList.setWidthFull();
        messageList.setPadding(true);
        messageList.setSpacing(false);
        messageList.getStyle()
            .set("overflow-y", "auto")
            .set("background", CREAM)
            .set("gap", "12px");
        layout.add(messageList);
        layout.setFlexGrow(1, messageList);

        // Input area
        layout.add(buildInputArea());

        return layout;
    }

    private Component buildChatHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setHeight("60px");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("padding", "0 20px")
            .set("background", CARD_BG)
            .set("border-bottom", "1px solid #e8ddd4");

        VerticalLayout titleWrap = new VerticalLayout();
        titleWrap.setPadding(false);
        titleWrap.setSpacing(false);

        chatTitle = new Span("Wähle ein Gespräch");
        chatTitle.getStyle().set("font-size", "16px").set("font-weight", "700").set("color", DARK);

        typingIndicator = new Span("");
        typingIndicator.getStyle().set("font-size", "12px").set("color", "#7a6050").set("display", "none");

        titleWrap.add(chatTitle, typingIndicator);
        header.add(titleWrap);
        return header;
    }

    private Component buildInputArea() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setHeight("120px");
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
            .set("background", "#fcfaf6")
            .set("border-top", "1px solid #e8ddd4")
            .set("box-sizing", "border-box");

        this.messageInput = new TextArea();
        messageInput.setPlaceholder("Schreibe deine Nachricht...");
        messageInput.getStyle()
            .set("flex", "1")
            .set("background", CARD_BG)
            .set("border", "1px solid #e8ddd4")
            .set("border-radius", "12px")
            .set("font-size", "14px")
            .set("color", DARK);
        messageInput.setValueChangeMode(ValueChangeMode.LAZY);
        messageInput.setValueChangeTimeout(350);
        messageInput.addValueChangeListener(e -> handleTyping());

        this.sendButton = new Button("Senden");
        sendButton.setWidth("100px");
        sendButton.getStyle()
            .set("background", "#8db3c3")
            .set("color", "white")
            .set("border-radius", "12px");
        sendButton.addClickListener(e -> sendMessage());
        sendButton.setEnabled(false); // disabled until conversation selected

        layout.add(messageInput, sendButton);
        return layout;
    }

    // ── Conversation & Message Loading ──

    private void refreshConversationList() {
        conversationList.removeAll();
        conversationsById.clear();

        try {
            List<ChatConversationDto> conversations = chatService.getCurrentUserConversations();

            if (conversations.isEmpty()) {
                conversationList.add(new Paragraph("Noch keine Chats vorhanden."));
                return;
            }

            Map<String, Long> unreadCounts = chatService.getUnreadCountsByConversation();

            for (ChatConversationDto conv : conversations) {
                if (conv == null || conv.conversationId() == null || conv.conversationId().isBlank()) {
                    log.warn("Skipping invalid conversation entry: {}", conv);
                    continue;
                }

                try {
                    conversationsById.put(conv.conversationId(), conv);
                    long unread = unreadCounts.getOrDefault(conv.conversationId(), 0L);
                    conversationList.add(buildConversationItem(conv, unread));
                } catch (Exception itemException) {
                    log.warn("Skipping conversation {} due to rendering error: {}",
                        conv.conversationId(), itemException.getMessage(), itemException);
                }
            }

            if (conversationList.getComponentCount() == 0) {
                conversationList.add(new Paragraph("Noch keine nutzbaren Chats vorhanden."));
            }
        } catch (Exception e) {
            log.error("Failed to load conversations", e);
            conversationList.add(new Paragraph("Fehler beim Laden der Gespräche."));
        }
    }

    private Component buildConversationItem(ChatConversationDto conv, long unreadCount) {
        Div item = new Div();
        item.getStyle()
            .set("padding", "14px 16px")
            .set("border-radius", "12px")
            .set("margin-bottom", "10px")
            .set("cursor", "pointer")
            .set("background", activeConversationId != null && activeConversationId.equals(conv.conversationId())
                ? "#ebd7c0"
                : "#fcfaf6")
            .set("border", "1px solid #f0e6da")
            .set("box-sizing", "border-box")
            .set("display", "flex")
            .set("align-items", "center");

        // Determine counterpart name
        String counterpartName = currentUserId.equals(conv.ownerId())
            ? conv.sitterDisplayName()
            : conv.ownerDisplayName();
        if (counterpartName == null || counterpartName.isBlank()) {
            counterpartName = "Unbekannter Kontakt";
        }

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("flex", "1");

        Span name = new Span(counterpartName);
        name.getStyle().set("font-weight", "700").set("color", DARK).set("font-size", "14px");
        content.add(name);

        String previewText = Boolean.TRUE.equals(typingByConversationId.get(conv.conversationId()))
                ? "schreibt gerade..."
                : conv.lastMessagePreview();

        if (previewText != null) {
            Span preview = new Span(previewText);
            preview.getStyle().set("color", "#7a6050").set("font-size", "12px").set("margin-top", "4px");
            content.add(preview);
        }

        item.add(content);

        if (unreadCount > 0) {
            Span dot = new Span();
            dot.getStyle()
                .set("width", "10px").set("height", "10px")
                .set("border-radius", "50%")
                .set("background", "#e74c3c")
                .set("flex-shrink", "0")
                .set("margin-left", "8px");
            item.add(dot);
        }

        item.addClickListener(e -> selectConversation(conv.conversationId()));

        return item;
    }

    private void selectConversation(String conversationId) {
        this.activeConversationId = conversationId;
        ChatConversationDto selected = conversationsById.get(conversationId);
        if (selected != null) {
            boolean currentUserIsOwner = currentUserId.equals(selected.ownerId());
            activeRecipientId = currentUserIsOwner ? selected.sitterId() : selected.ownerId();
            activeCounterpartName = currentUserIsOwner ? selected.sitterDisplayName() : selected.ownerDisplayName();
            chatTitle.setText(activeCounterpartName != null ? activeCounterpartName : "Chat");
        }
        typingIndicator.setText("");
        typingIndicator.getStyle().set("display", "none");
        log.info("Selected conversation: {}", conversationId);

        // Refresh conversation list styling
        refreshConversationList();

        // Load messages
        try {
            List<ChatMessageDto> messages = chatService.getMessages(conversationId);
            messageList.removeAll();

            // Request card as first item (if a request exists between these users)
            if (selected != null) {
                Component card = buildRequestCard(selected);
                if (card != null) {
                    messageList.add(card);
                }
            }

            if (messages.isEmpty() && messageList.getComponentCount() == 0) {
                messageList.add(new Paragraph("Keine Nachrichten in diesem Chat."));
            } else {
                for (ChatMessageDto msg : messages) {
                    messageList.add(buildMessageBubble(msg));
                }
                // Auto-scroll to bottom
                messageList.getElement().executeJs("this.scrollTop = this.scrollHeight");
            }

            // Mark as read
            chatService.markConversationAsRead(conversationId);

            // Enable send button
            sendButton.setEnabled(true);
            messageInput.focus();

        } catch (Exception e) {
            log.error("Failed to load messages: {}", e.getMessage());
            messageList.removeAll();
            messageList.add(new Paragraph("Fehler beim Laden der Nachrichten."));
        }
    }

    private Component buildRequestCard(ChatConversationDto conv) {
        UUID otherUserId = currentUserId.equals(conv.ownerId()) ? conv.sitterId() : conv.ownerId();

        try {
            // Current user is the offer creator → they received the request
            Optional<OfferRequest> pendingAsCreator =
                requestService.findPendingRequestFromRequesterToCreator(currentUserId, otherUserId);
            if (pendingAsCreator.isPresent()) {
                return buildActionableRequestCard(pendingAsCreator.get(), conv);
            }

            // Current user is the requester → they sent the request
            Optional<OfferRequest> pendingAsRequester =
                requestService.findPendingRequestFromRequesterToCreator(otherUserId, currentUserId);
            if (pendingAsRequester.isPresent()) {
                return buildPendingRequesterCard(pendingAsRequester.get());
            }

            // Accepted — current user was the creator
            Optional<OfferRequest> acceptedAsCreator =
                requestService.findAcceptedRequestFromRequesterToCreator(currentUserId, otherUserId);
            if (acceptedAsCreator.isPresent()) {
                return buildAcceptedCard(acceptedAsCreator.get());
            }

            // Accepted — current user was the requester
            Optional<OfferRequest> acceptedAsRequester =
                requestService.findAcceptedRequestFromRequesterToCreator(otherUserId, currentUserId);
            if (acceptedAsRequester.isPresent()) {
                return buildAcceptedCard(acceptedAsRequester.get());
            }

            // Denied — current user was the creator
            Optional<OfferRequest> deniedAsCreator =
                requestService.findDeniedRequestFromRequesterToCreator(currentUserId, otherUserId);
            if (deniedAsCreator.isPresent()) {
                return buildDeniedCard(deniedAsCreator.get());
            }

            // Denied — current user was the requester
            Optional<OfferRequest> deniedAsRequester =
                requestService.findDeniedRequestFromRequesterToCreator(otherUserId, currentUserId);
            if (deniedAsRequester.isPresent()) {
                return buildDeniedCard(deniedAsRequester.get());
            }

        } catch (Exception e) {
            log.warn("Could not build request card: {}", e.getMessage());
        }
        return null;
    }

    private Component buildActionableRequestCard(OfferRequest request, ChatConversationDto conv) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#fff8ec")
            .set("border", "1px solid #f0d8a8")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("margin", "0 auto 4px auto")
            .set("max-width", "80%")
            .set("width", "fit-content");

        String offerTitle = request.getOffer().getTitle() != null ? request.getOffer().getTitle() : "Angebot";
        String requesterName = request.getRequester().getFirstName() + " " + request.getRequester().getLastName();

        Span title = new Span("📋 Angebotsanfrage");
        title.getStyle().set("font-weight", "700").set("font-size", "13px").set("color", "#4a3428").set("display", "block").set("margin-bottom", "4px");

        Span offerSpan = new Span("Angebot: " + offerTitle);
        offerSpan.getStyle().set("font-size", "13px").set("color", "#7a6050").set("display", "block").set("margin-bottom", "4px");

        Span fromSpan = new Span("Von: " + requesterName);
        fromSpan.getStyle().set("font-size", "12px").set("color", "#7a6050").set("display", "block").set("margin-bottom", "12px");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button acceptBtn = new Button("Annehmen", e -> {
            try {
                bookingService.acceptRequest(request.getId(), currentUserId);
                Notification n = Notification.show("Anfrage angenommen – Booking erstellt");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                selectConversation(activeConversationId);
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage());
            }
        });
        acceptBtn.getStyle().set("background", "#4a3428").set("color", "white").set("border-radius", "8px");

        Button denyBtn = new Button("Ablehnen", e -> {
            try {
                requestService.denyRequest(request.getId(), currentUserId);
                selectConversation(activeConversationId);
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage());
            }
        });
        denyBtn.getStyle().set("border-radius", "8px");

        buttons.add(acceptBtn, denyBtn);
        card.add(title, offerSpan, fromSpan, buttons);
        return card;
    }

    private Component buildPendingRequesterCard(OfferRequest request) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f0f4ff")
            .set("border", "1px solid #c5d0e8")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("margin", "0 auto 4px auto")
            .set("max-width", "80%")
            .set("width", "fit-content");

        String offerTitle = request.getOffer().getTitle() != null ? request.getOffer().getTitle() : "Angebot";

        Span title = new Span("📋 Deine Anfrage");
        title.getStyle().set("font-weight", "700").set("font-size", "13px").set("color", "#4a3428").set("display", "block").set("margin-bottom", "4px");

        Span offerSpan = new Span("Angebot: " + offerTitle);
        offerSpan.getStyle().set("font-size", "13px").set("color", "#7a6050").set("display", "block").set("margin-bottom", "4px");

        Span status = new Span("Status: Ausstehend");
        status.getStyle().set("font-size", "12px").set("color", "#7a6050").set("font-style", "italic");

        card.add(title, offerSpan, status);
        return card;
    }

    private Component buildDeniedCard(OfferRequest request) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f5f5f5")
            .set("border", "1px solid #d0c8c0")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("margin", "0 auto 4px auto")
            .set("max-width", "80%")
            .set("width", "fit-content");

        String offerTitle = request.getOffer().getTitle() != null ? request.getOffer().getTitle() : "Angebot";

        Span title = new Span("❌ Anfrage abgelehnt");
        title.getStyle().set("font-weight", "700").set("font-size", "13px").set("color", "#7a6050").set("display", "block").set("margin-bottom", "4px");

        Span offerSpan = new Span("Angebot: " + offerTitle);
        offerSpan.getStyle().set("font-size", "13px").set("color", "#7a6050");

        card.add(title, offerSpan);
        return card;
    }

    private Component buildAcceptedCard(OfferRequest request) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#edf7ed")
            .set("border", "1px solid #b8ddb8")
            .set("border-radius", "12px")
            .set("padding", "16px")
            .set("margin", "0 auto 4px auto")
            .set("max-width", "80%")
            .set("width", "fit-content");

        String offerTitle = request.getOffer().getTitle() != null ? request.getOffer().getTitle() : "Angebot";

        Span title = new Span("✅ Anfrage angenommen");
        title.getStyle().set("font-weight", "700").set("font-size", "13px").set("color", "#2e7d32").set("display", "block").set("margin-bottom", "4px");

        Span offerSpan = new Span("Angebot: " + offerTitle);
        offerSpan.getStyle().set("font-size", "13px").set("color", "#7a6050");

        card.add(title, offerSpan);
        return card;
    }

    private Component buildMessageBubble(ChatMessageDto msg) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);

        if (msg.senderId().equals(currentUserId)) {
            // Own message (right side)
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            row.add(buildAvatar("Ich"), createBubble(msg.message(), true));
        } else {
            // Other's message (left side)
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            row.add(buildAvatar(activeCounterpartName), createBubble(msg.message(), false));
        }

        return row;
    }

    private Div createBubble(String text, boolean isOwn) {
        Div bubble = new Div();
        bubble.getStyle()
            .set("max-width", "60%")
            .set("padding", "12px 16px")
            .set("border-radius", "12px")
            .set("word-wrap", "break-word")
            .set("background", isOwn ? "#f7f5f0" : "#ebd7c0")
            .set("border", "1px solid #e8ddd4")
            .set("color", DARK)
            .set("font-size", "14px")
            .set("line-height", "1.4");

        bubble.add(new Span(text));
        return bubble;
    }

    private Component buildAvatar(String displayName) {
        String initials = initials(displayName);
        Span avatar = new Span(initials);
        avatar.getStyle()
                .set("width", "28px")
                .set("height", "28px")
                .set("border-radius", "50%")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("background", "#8db3c3")
                .set("color", "white");
        return avatar;
    }

    private String initials(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "?";
        }
        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void sendMessage() {
        String text = messageInput.getValue().trim();

        if (text.isEmpty() || activeConversationId == null) {
            return;
        }

        try {
            ChatMessageDto sent = chatService.sendMessage(activeConversationId, text);
            messageInput.setValue("");
            publishTyping(false);

            // Add sender's message immediately to UI (don't wait for EventBus)
            messageList.add(buildMessageBubble(sent));
            messageList.getElement().executeJs("this.scrollTop = this.scrollHeight");

            // Refresh conversation list for updated timestamp
            refreshConversationList();
        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage());
            messageInput.setValue("Fehler: Nachricht konnte nicht gesendet werden.");
        }
    }

    // ── Event Bus Listener ──

    private void registerEventBusListener() {
        this.eventBusRegistration = eventBus.register(currentUserId, message -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(() -> {
                    // If the message is for the active conversation, add it to the view
                    if (message.conversationId().equals(activeConversationId)) {
                        messageList.add(buildMessageBubble(message));
                        messageList.getElement().executeJs("this.scrollTop = this.scrollHeight");
                    } else {
                        // Otherwise, just refresh the conversation list
                        refreshConversationList();
                    }
                });
            }
        });

        this.typingRegistration = eventBus.registerTyping(currentUserId, event -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(() -> {
                    typingByConversationId.put(event.conversationId(), event.typing());
                    refreshConversationList();

                    if (!event.conversationId().equals(activeConversationId)) {
                        return;
                    }
                    if (event.typing()) {
                        typingIndicator.setText(initials(activeCounterpartName) + " schreibt gerade...");
                        typingIndicator.getStyle().set("display", "block");
                    } else {
                        typingIndicator.setText("");
                        typingIndicator.getStyle().set("display", "none");
                    }
                });
            }
        });
    }

    private void handleTyping() {
        if (activeConversationId == null || activeRecipientId == null) {
            return;
        }
        if (messageInput.getValue() == null || messageInput.getValue().isBlank()) {
            publishTyping(false);
            return;
        }

        publishTyping(true);

        if (stopTypingFuture != null) {
            stopTypingFuture.cancel(false);
        }
        stopTypingFuture = typingScheduler.schedule(() -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(() -> publishTyping(false));
            }
        }, 1200, TimeUnit.MILLISECONDS);
    }

    private void publishTyping(boolean typing) {
        if (activeConversationId == null || activeRecipientId == null) {
            return;
        }
        eventBus.publishTyping(new ChatTypingEventDto(
                activeConversationId,
                currentUserId,
                activeRecipientId,
                typing,
                LocalDateTime.now()
        ));
    }

    @Override
    protected void onDetach(DetachEvent event) {
        super.onDetach(event);
        // Cleanup: remove listener from event bus
        if (eventBusRegistration != null) {
            eventBusRegistration.remove();
            log.info("EventBus listener unregistered");
        }
        if (typingRegistration != null) {
            typingRegistration.remove();
        }
        if (stopTypingFuture != null) {
            stopTypingFuture.cancel(true);
        }
        typingScheduler.shutdownNow();
    }

}

