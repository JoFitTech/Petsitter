package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.chat.service.Registration;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
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
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
public class ChatView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);

    private static final String DARK = "#4a3428";
    private static final String CREAM = "#fbf8f1";
    private static final String CARD_BG = "#ffffff";
    private static final String SIDEBAR_ITEM_BG = "#fdf6e8";
    private static final String SIDEBAR_ITEM_ACTIVE = "#f0e0c4";
    private static final String BUBBLE_SENT = "#f5ead6";
    private static final String BUBBLE_RECEIVED = "#e8d4b4";

    private final ChatService chatService;
    private final ChatEventBus eventBus;
    private final RequestService requestService;
    private final BookingService bookingService;

    // UI Components
    private VerticalLayout conversationList;
    private VerticalLayout messageList;
    private TextField messageInput;
    private Button sendButton;
    private Span chatTitle;
    private Span typingIndicator;
    private Div chatHeaderAvatar;

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
        setPadding(true);
        setSpacing(false);
        getStyle()
            .set("background", CREAM)
            .set("font-family", "Inter, Arial, sans-serif")
            .set("padding", "30px 50px")
            .set("box-sizing", "border-box");

        // Determine current user
        this.currentUserId = authenticatedUser.get()
            .map(com.softwareengineering.petsitter.user.domain.User::getId)
            .orElse(null);

        if (currentUserId == null) {
            add(new Paragraph("Bitte melde dich an."));
            return;
        }

        // Inbox header
        H2 inboxTitle = new H2("Inbox");
        inboxTitle.getStyle()
            .set("margin", "0 0 4px 0")
            .set("font-size", "28px")
            .set("font-weight", "800")
            .set("color", DARK);
        add(inboxTitle);

        Paragraph subtitle = new Paragraph("Verwalte hier deine eingehenden Nachrichten und behalte alle wichtigen Unterhaltungen im Blick.");
        subtitle.getStyle()
            .set("margin", "0 0 20px 0")
            .set("font-size", "14px")
            .set("color", "#7a6050");
        add(subtitle);

        // Main container card with fixed height
        HorizontalLayout chatContainer = new HorizontalLayout();
        chatContainer.setWidthFull();
        chatContainer.setHeight("600px");
        chatContainer.setPadding(false);
        chatContainer.setSpacing(false);
        chatContainer.getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("box-shadow", "0 2px 16px rgba(74,52,40,0.07)")
            .set("overflow", "hidden")
            .set("flex-shrink", "0");

        chatContainer.add(buildConversationList());
        chatContainer.add(buildChatWindow());
        chatContainer.setFlexGrow(1, chatContainer.getComponentAt(1));

        add(chatContainer);

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
        layout.setWidth("280px");
        layout.setHeightFull();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getStyle()
            .set("border-right", "1px solid #f0e6da")
            .set("overflow-y", "auto")
            .set("background", CARD_BG)
            .set("box-sizing", "border-box")
            .set("padding", "16px 12px")
            .set("flex-shrink", "0");

        this.conversationList = new VerticalLayout();
        conversationList.setWidthFull();
        conversationList.setPadding(false);
        conversationList.setSpacing(false);
        conversationList.getStyle().set("gap", "8px");
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
        layout.getStyle()
            .set("background", CARD_BG)
            .set("display", "flex")
            .set("flex-direction", "column");

        // Header
        layout.add(buildChatHeader());

        // Messages area (scrollable, fills remaining space)
        this.messageList = new VerticalLayout();
        messageList.setWidthFull();
        messageList.setPadding(true);
        messageList.setSpacing(false);
        messageList.getStyle()
            .set("overflow-y", "auto")
            .set("background", CARD_BG)
            .set("gap", "12px")
            .set("padding", "20px 24px")
            .set("flex", "1 1 0")
            .set("min-height", "0");
        layout.add(messageList);

        // Divider line
        Div divider = new Div();
        divider.getStyle()
            .set("width", "100%")
            .set("height", "1px")
            .set("background", "#e8ddd4");
        layout.add(divider);

        // Input area
        layout.add(buildInputArea());

        return layout;
    }

    private Component buildChatHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setHeight("70px");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("padding", "0 24px")
            .set("background", CARD_BG)
            .set("border-bottom", "1px solid #e8ddd4")
            .set("gap", "14px")
            .set("flex-shrink", "0");

        // Avatar in header
        chatHeaderAvatar = createChatAvatar(38);
        chatHeaderAvatar.getStyle().set("display", "none");
        header.add(chatHeaderAvatar);

        VerticalLayout titleWrap = new VerticalLayout();
        titleWrap.setPadding(false);
        titleWrap.setSpacing(false);

        chatTitle = new Span("Wähle ein Gespräch");
        chatTitle.getStyle()
            .set("font-size", "20px")
            .set("font-weight", "700")
            .set("color", DARK);

        typingIndicator = new Span("");
        typingIndicator.getStyle()
            .set("font-size", "12px")
            .set("color", "#7a6050")
            .set("display", "none");

        titleWrap.add(chatTitle, typingIndicator);
        header.add(titleWrap);
        return header;
    }

    private Component buildInputArea() {
        // Outer wrapper with padding
        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setWidthFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.getStyle()
            .set("padding", "12px 20px")
            .set("box-sizing", "border-box")
            .set("flex-shrink", "0")
            .set("background", CARD_BG);

        // Unified input bar (single rounded container)
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.setPadding(false);
        bar.setSpacing(false);
        bar.getStyle()
            .set("background", "#f5f0e8")
            .set("border-radius", "28px")
            .set("padding", "6px 16px")
            .set("gap", "10px")
            .set("box-sizing", "border-box");

        // Camera icon
        Icon cameraIcon = new Icon(VaadinIcon.CAMERA);
        cameraIcon.setSize("22px");
        cameraIcon.getStyle()
            .set("color", "#8a6d50")
            .set("cursor", "pointer")
            .set("flex-shrink", "0");
        cameraIcon.addClickListener(e -> System.out.println("Camera button clicked"));

        // Text input (borderless, transparent bg)
        this.messageInput = new TextField();
        messageInput.setPlaceholder("Schreibe hier deine Nachricht");
        messageInput.setWidthFull();
        messageInput.getStyle()
            .set("flex", "1")
            .set("--vaadin-input-field-background", "transparent")
            .set("--vaadin-input-field-border-width", "0")
            .set("--vaadin-input-field-border-radius", "0")
            .set("--vaadin-focus-ring-width", "0")
            .set("--vaadin-focus-ring-color", "transparent")
            .set("--lumo-contrast-10pct", "transparent")
            .set("font-size", "14px")
            .set("color", DARK)
            .set("box-shadow", "none");
        messageInput.setValueChangeMode(ValueChangeMode.LAZY);
        messageInput.setValueChangeTimeout(350);
        messageInput.addValueChangeListener(e -> handleTyping());
        messageInput.addKeyDownListener(Key.ENTER, e -> sendMessage());

        // Send icon
        Icon sendIcon = new Icon(VaadinIcon.PAPERPLANE);
        sendIcon.setSize("22px");
        sendIcon.getStyle()
            .set("color", "#8a6d50")
            .set("cursor", "pointer")
            .set("flex-shrink", "0");

        this.sendButton = new Button(sendIcon);
        sendButton.getStyle()
            .set("background", "transparent")
            .set("color", "#8a6d50")
            .set("width", "36px")
            .set("height", "36px")
            .set("min-width", "36px")
            .set("padding", "0")
            .set("border", "none")
            .set("box-shadow", "none")
            .set("cursor", "pointer")
            .set("flex-shrink", "0");
        sendButton.addClickListener(e -> sendMessage());
        sendButton.setEnabled(false);

        bar.add(cameraIcon, messageInput, sendButton);
        wrapper.add(bar);
        return wrapper;
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
        boolean isActive = activeConversationId != null && activeConversationId.equals(conv.conversationId());

        Div item = new Div();
        item.setWidthFull();
        item.getStyle()
            .set("padding", "12px 14px")
            .set("border-radius", "14px")
            .set("cursor", "pointer")
            .set("background", isActive ? SIDEBAR_ITEM_ACTIVE : SIDEBAR_ITEM_BG)
            .set("border", "1px solid " + (isActive ? "#e0c8a0" : "#f0e6da"))
            .set("box-sizing", "border-box")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "12px")
            .set("transition", "background 0.15s ease");

        // Avatar
        Div avatar = createChatAvatar(42);
        item.add(avatar);

        // Determine counterpart name
        String counterpartName = currentUserId.equals(conv.ownerId())
            ? conv.sitterDisplayName()
            : conv.ownerDisplayName();
        if (counterpartName == null || counterpartName.isBlank()) {
            counterpartName = "Unbekannter Kontakt";
        }

        // Middle content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("flex", "1").set("gap", "2px");

        // Name + stars row
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setPadding(false);
        nameRow.setSpacing(false);
        nameRow.setAlignItems(FlexComponent.Alignment.CENTER);
        nameRow.getStyle().set("gap", "6px");

        Span name = new Span(counterpartName);
        name.getStyle()
            .set("font-weight", "700")
            .set("color", DARK)
            .set("font-size", "13px");
        nameRow.add(name);

        // Stars (placeholder, 4 stars)
        HorizontalLayout stars = new HorizontalLayout();
        stars.setPadding(false);
        stars.setSpacing(false);
        stars.getStyle().set("gap", "1px");
        for (int i = 0; i < 4; i++) {
            Icon star = new Icon(VaadinIcon.STAR);
            star.setSize("10px");
            star.getStyle().set("color", "#ffdf4a");
            stars.add(star);
        }
        nameRow.add(stars);

        content.add(nameRow);

        // Preview text or typing indicator
        String previewText = Boolean.TRUE.equals(typingByConversationId.get(conv.conversationId()))
                ? "schreibt gerade..."
                : conv.lastMessagePreview();

        if (previewText != null) {
            Span preview = new Span(previewText);
            preview.getStyle()
                .set("color", "#7a6050")
                .set("font-size", "11px")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("max-width", "140px");
            content.add(preview);
        }

        item.add(content);

        // Unread dot (right side)
        if (unreadCount > 0) {
            Span dot = new Span();
            dot.getStyle()
                .set("width", "10px").set("height", "10px")
                .set("border-radius", "50%")
                .set("background", "#e74c3c")
                .set("flex-shrink", "0");
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
            // Show avatar in header
            if (chatHeaderAvatar != null) {
                chatHeaderAvatar.getStyle().set("display", "flex");
            }
        }
        typingIndicator.setText("");
        typingIndicator.getStyle().set("display", "none");
        log.info("Selected conversation: {}", conversationId);

        // Mark as read first, then refresh list so unread dot disappears
        try {
            chatService.markConversationAsRead(conversationId);
        } catch (Exception e) {
            log.warn("Failed to mark conversation as read: {}", e.getMessage());
        }

        // Refresh conversation list styling (after marking as read)
        refreshConversationList();

        // Load messages
        try {
            List<ChatMessageDto> messages = chatService.getMessages(conversationId);
            messageList.removeAll();

            if (messages.isEmpty()) {
                messageList.add(new Paragraph("Keine Nachrichten in diesem Chat."));
            } else {
                for (ChatMessageDto msg : messages) {
                    messageList.add(buildMessageBubble(msg));
                }
                // Auto-scroll to bottom
                messageList.getElement().executeJs("this.scrollTop = this.scrollHeight");
            }

            // Enable send button
            sendButton.setEnabled(true);
            messageInput.focus();

        } catch (Exception e) {
            log.error("Failed to load messages: {}", e.getMessage());
            messageList.removeAll();
            messageList.add(new Paragraph("Fehler beim Laden der Nachrichten."));
        }
    }

    private Component buildMessageBubble(ChatMessageDto msg) {
        if ("REQUEST_CARD".equals(msg.type())) {
            return buildRequestCardBubble(msg);
        }

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);

        if (msg.senderId().equals(currentUserId)) {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            row.add(buildAvatar("Ich"), createBubble(msg.message(), true));
        } else {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            row.add(buildAvatar(activeCounterpartName), createBubble(msg.message(), false));
        }

        return row;
    }

    private Component buildRequestCardBubble(ChatMessageDto msg) {
        boolean isOwnRequest = msg.senderId().equals(currentUserId);
        boolean isRecipient = msg.recipientId().equals(currentUserId);

        Div card = new Div();
        card.getStyle()
            .set("border-radius", "12px")
            .set("padding", "14px 16px")
            .set("max-width", "60%")
            .set("width", "fit-content");

        String offerTitle = msg.offerTitle() != null ? msg.offerTitle() : "Angebot";
        String requestId = msg.requestId();

        // Determine current request status
        RequestStatus status = null;
        if (requestId != null) {
            try {
                status = requestService.findById(UUID.fromString(requestId)).getStatus();
            } catch (Exception e) {
                log.warn("Could not load request status for card: {}", e.getMessage());
            }
        }

        // Styling and content based on status
        if (status == RequestStatus.ACCEPTED) {
            boolean cancelled = false;
            if (requestId != null) {
                try {
                    cancelled = bookingService.isBookingCancelledForRequest(UUID.fromString(requestId));
                } catch (Exception e) {
                    log.warn("Could not check booking cancellation: {}", e.getMessage());
                }
            }
            if (cancelled) {
                card.getStyle().set("background", "#f5f5f5").set("border", "1px solid #d0c8c0");
                card.add(makeCardTitle("🚫 Buchung storniert", "#7a6050"));
                card.add(makeCardOfferSpan(offerTitle));
            } else {
                card.getStyle().set("background", "#edf7ed").set("border", "1px solid #b8ddb8");
                card.add(makeCardTitle("✅ Anfrage angenommen", "#2e7d32"));
                card.add(makeCardOfferSpan(offerTitle));
            }
        } else if (status == RequestStatus.DENIED) {
            card.getStyle().set("background", "#f5f5f5").set("border", "1px solid #d0c8c0");
            card.add(makeCardTitle("❌ Anfrage abgelehnt", "#7a6050"));
            card.add(makeCardOfferSpan(offerTitle));
        } else if (status == RequestStatus.PENDING && isRecipient) {
            card.getStyle().set("background", "#fff8ec").set("border", "1px solid #f0d8a8");
            card.add(makeCardTitle("📋 Angebotsanfrage", "#4a3428"));
            card.add(makeCardOfferSpan(offerTitle));

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.getStyle().set("margin-top", "10px");

            UUID reqId = UUID.fromString(requestId);
            Button acceptBtn = new Button("Annehmen", e -> {
                try {
                    bookingService.acceptRequest(reqId, currentUserId);
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
                    requestService.denyRequest(reqId, currentUserId);
                    selectConversation(activeConversationId);
                } catch (Exception ex) {
                    Notification.show("Fehler: " + ex.getMessage());
                }
            });
            denyBtn.getStyle().set("border-radius", "8px");

            buttons.add(acceptBtn, denyBtn);
            card.add(buttons);
        } else {
            // PENDING as sender, or unknown status
            card.getStyle().set("background", "#f0f4ff").set("border", "1px solid #c5d0e8");
            card.add(makeCardTitle("📋 Angebotsanfrage", "#4a3428"));
            card.add(makeCardOfferSpan(offerTitle));
            Span statusSpan = new Span(status == RequestStatus.PENDING ? "Status: Ausstehend" : "Status: Unbekannt");
            statusSpan.getStyle().set("font-size", "12px").set("color", "#7a6050").set("font-style", "italic").set("display", "block").set("margin-top", "4px");
            card.add(statusSpan);
        }

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);

        if (isOwnRequest) {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            row.add(buildAvatar("Ich"), card);
        } else {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            row.add(buildAvatar(activeCounterpartName), card);
        }

        return row;
    }

    private Span makeCardTitle(String text, String color) {
        Span s = new Span(text);
        s.getStyle().set("font-weight", "700").set("font-size", "13px").set("color", color)
            .set("display", "block").set("margin-bottom", "4px");
        return s;
    }

    private Span makeCardOfferSpan(String offerTitle) {
        Span s = new Span("Angebot: " + offerTitle);
        s.getStyle().set("font-size", "13px").set("color", "#7a6050").set("display", "block");
        return s;
    }

    private Div createBubble(String text, boolean isOwn) {
        Div bubble = new Div();
        bubble.getStyle()
            .set("max-width", "60%")
            .set("padding", "12px 18px")
            .set("border-radius", isOwn ? "16px 16px 4px 16px" : "16px 16px 16px 4px")
            .set("word-wrap", "break-word")
            .set("background", isOwn ? BUBBLE_SENT : BUBBLE_RECEIVED)
            .set("color", DARK)
            .set("font-size", "14px")
            .set("line-height", "1.5");

        bubble.add(new Span(text));
        return bubble;
    }

    private Component buildAvatar(String displayName) {
        return createChatAvatar(32);
    }

    private Div createChatAvatar(int size) {
        Div avatar = new Div();
        avatar.getStyle()
            .set("width", size + "px")
            .set("height", size + "px")
            .set("min-width", size + "px")
            .set("border-radius", "50%")
            .set("background-color", "#c8dce6")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("overflow", "hidden")
            .set("flex-shrink", "0");

        int svgSize = (int)(size * 0.65);
        Div svgWrap = new Div();
        svgWrap.getElement().setProperty("innerHTML",
            "<svg width='" + svgSize + "' height='" + svgSize + "' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<circle cx='12' cy='8' r='4' fill='#8db3c3'/>" +
            "<path d='M4 20c0-4 3.6-7 8-7s8 3 8 7' fill='#8db3c3'/></svg>");
        avatar.add(svgWrap);
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

