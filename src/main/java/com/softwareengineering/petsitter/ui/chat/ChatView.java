package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.booking.dto.BookingAcceptancePreview;
import com.softwareengineering.petsitter.chat.dto.ChatConversationDto;
import com.softwareengineering.petsitter.chat.dto.ChatMessageDto;
import com.softwareengineering.petsitter.chat.dto.ChatRefreshEventDto;
import com.softwareengineering.petsitter.chat.dto.ChatTypingEventDto;
import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.chat.service.Registration;
import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.offerrequest.dto.OfferRequestChatCardDto;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.ui.shared.ExternalPaymentMethods;
import com.softwareengineering.petsitter.ui.shared.ImageComponents;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.ui.shared.RatingComponents;
import com.softwareengineering.petsitter.shared.exception.InsufficientBalanceException;
import com.softwareengineering.petsitter.user.service.UserService;
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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String CREAM = "#e8d9c8";
    private static final String CARD_BG = "#ffffff";
    private static final String SIDEBAR_ITEM_BG = "#fdf6e8";
    private static final String SIDEBAR_ITEM_ACTIVE = "#f0e0c4";
    private static final String BUBBLE_SENT = "#f5ead6";
    private static final String BUBBLE_RECEIVED = "#e8d4b4";
    private static final Pattern REVIEW_RATING_PATTERN = Pattern.compile("\\[RATING:(\\d)]\\s*(.*)");

    private final ChatService chatService;
    private final ChatEventBus eventBus;
    private final RequestService requestService;
    private final BookingService bookingService;
    private final UserService userService;

    // UI Components
    private VerticalLayout conversationList;
    private VerticalLayout messageList;
    private TextField messageInput;
    private Button sendButton;
    private Span chatTitle;
    private Span typingIndicator;
    private Div chatHeaderAvatar;
    private HorizontalLayout chatContainer;

    // State
    private String activeConversationId;
    private UUID activeRecipientId;
    private String activeCounterpartName;
    private ImageRefDto activeCurrentUserImage;
    private ImageRefDto activeCounterpartImage;
    private UUID currentUserId;
    private final Map<String, ChatConversationDto> conversationsById = new HashMap<>();
    private final Map<String, Boolean> typingByConversationId = new HashMap<>();
    private Registration eventBusRegistration;
    private Registration typingRegistration;
    private Registration refreshRegistration;
    private final ScheduledExecutorService typingScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> stopTypingFuture;

    public ChatView(
            ChatService chatService,
            ChatEventBus eventBus,
            AuthenticatedUser authenticatedUser,
            RequestService requestService,
            BookingService bookingService,
            UserService userService
    ) {
        this.chatService = chatService;
        this.eventBus = eventBus;
        this.requestService = requestService;
        this.bookingService = bookingService;
        this.userService = userService;

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
        this.chatContainer = new HorizontalLayout();
        chatContainer.addClassName("chat-main-container");
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
        layout.addClassName("chat-conversation-sidebar");
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
        layout.addClassName("chat-window-pane");
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

        Button backBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addClassName("chat-back-button");
        backBtn.getStyle()
                .set("background", "transparent")
                .set("color", DARK)
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0")
                .set("margin-right", "8px")
                .set("min-width", "unset")
                .set("width", "36px")
                .set("height", "36px");
        backBtn.addClickListener(e -> {
            activeConversationId = null;
            if (chatContainer != null) {
                chatContainer.removeClassName("conversation-selected");
            }
            messageList.removeAll();
            messageList.add(new Paragraph("Wähle ein Gespräch aus der Liste, um den Chat zu starten."));
            chatTitle.setText("Wähle ein Gespräch");
            chatHeaderAvatar.getStyle().set("display", "none");
            refreshConversationList();
        });

        // Avatar in header
        chatHeaderAvatar = createChatAvatar(38);
        chatHeaderAvatar.getStyle()
            .set("display", "none")
            .set("cursor", "pointer");
        chatHeaderAvatar.addClickListener(event -> openProfilePopUp());
        header.add(backBtn, chatHeaderAvatar);

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


    private UserRatingSummary counterpartRatingSummary(ChatConversationDto conv) {
        if (currentUserId == null || conv == null) {
            return null;
        }
        return currentUserId.equals(conv.ownerId())
            ? conv.sitterRatingSummary()
            : conv.ownerRatingSummary();
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
        Div avatar = createChatAvatar(counterpartImage(conv), 42);
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

        UserRatingSummary counterpartRatingSummary = counterpartRatingSummary(conv);
        if (counterpartRatingSummary != null && counterpartRatingSummary.ratingCount() > 0) {
            Component rating = RatingComponents.compactRating(counterpartRatingSummary);
            rating.getElement().getStyle().set("transform", "scale(0.75)").set("transform-origin", "left center");
            nameRow.add(rating);
        } else {
            Span newRating = new Span("Neu");
            newRating.getStyle()
                .set("font-size", "11px")
                .set("color", "#9a7a62")
                .set("font-weight", "600");
            nameRow.add(newRating);
        }

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
        if (chatContainer != null) {
            chatContainer.addClassName("conversation-selected");
        }
        ChatConversationDto selected = conversationsById.get(conversationId);
        if (selected != null) {
            boolean currentUserIsOwner = currentUserId.equals(selected.ownerId());
            activeRecipientId = currentUserIsOwner ? selected.sitterId() : selected.ownerId();
            activeCounterpartName = currentUserIsOwner ? selected.sitterDisplayName() : selected.ownerDisplayName();
            activeCurrentUserImage = currentUserIsOwner ? selected.ownerProfileImage() : selected.sitterProfileImage();
            activeCounterpartImage = currentUserIsOwner ? selected.sitterProfileImage() : selected.ownerProfileImage();
            chatTitle.setText(activeCounterpartName != null ? activeCounterpartName : "Chat");
            // Show avatar in header
            if (chatHeaderAvatar != null) {
                replaceAvatarContent(chatHeaderAvatar, activeCounterpartImage, 38);
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
        if ("REVIEW_CARD".equals(msg.type())) {
            return buildReviewCardBubble(msg);
        }
        if ("REVIEW_REMINDER_CARD".equals(msg.type())) {
            return buildReviewReminderCardBubble(msg);
        }

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);

        if (msg.senderId().equals(currentUserId)) {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            row.add(buildAvatar(activeCurrentUserImage), createBubble(msg.message(), true));
        } else {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            row.add(buildAvatar(activeCounterpartImage), createBubble(msg.message(), false));
        }

        return row;
    }

    private Component buildReviewReminderCardBubble(ChatMessageDto msg) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f0f4ff")
            .set("border", "1px solid #c5d0e8")
            .set("border-radius", "12px")
            .set("padding", "14px 16px")
            .set("max-width", "60%")
            .set("width", "fit-content")
            .set("text-align", "center");

        Span title = new Span("⏰ Termin abgeschlossen");
        title.getStyle()
            .set("font-weight", "700")
            .set("font-size", "14px")
            .set("color", DARK)
            .set("display", "block")
            .set("margin-bottom", "6px");
        card.add(title);

        Span subtitle = new Span("Ihr könnt euch jetzt gegenseitig bewerten.");
        subtitle.getStyle()
            .set("font-size", "13px")
            .set("color", "#7a6050")
            .set("display", "block");
        card.add(subtitle);

        // Center the card in the view
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        row.setSpacing(false);
        row.add(card);

        return row;
    }

    private Component buildReviewCardBubble(ChatMessageDto msg) {
        boolean ownReview = msg.senderId().equals(currentUserId);
        int rating = extractReviewRating(msg.message());
        String comment = extractReviewComment(msg.message());

        Div card = new Div();
        card.getStyle()
            .set("background", "#fff6e6")
            .set("border", "1px solid #f0d8a8")
            .set("border-radius", "12px")
            .set("padding", "12px 14px")
            .set("max-width", "60%")
            .set("width", "fit-content");

        Span title = new Span("Neue Bewertung");
        title.getStyle().set("font-weight", "700").set("font-size", "13px").set("color", DARK);
        card.add(title);

        Span stars = new Span("Sterne: " + "★".repeat(Math.max(0, rating)) + "☆".repeat(Math.max(0, 5 - rating)));
        stars.getStyle().set("display", "block").set("margin-top", "4px").set("font-size", "13px").set("color", "#7a5a00");
        card.add(stars);

        if (comment != null && !comment.isBlank()) {
            Span commentSpan = new Span("\"" + comment + "\"");
            commentSpan.getStyle().set("display", "block").set("margin-top", "6px").set("font-size", "13px").set("color", "#7a6050");
            card.add(commentSpan);
        }

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);

        if (ownReview) {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            row.add(buildAvatar(activeCurrentUserImage), card);
        } else {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            row.add(buildAvatar(activeCounterpartImage), card);
        }

        return row;
    }

    private int extractReviewRating(String encodedMessage) {
        if (encodedMessage == null) {
            return 0;
        }
        Matcher matcher = REVIEW_RATING_PATTERN.matcher(encodedMessage);
        if (!matcher.matches()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String extractReviewComment(String encodedMessage) {
        if (encodedMessage == null) {
            return "";
        }
        Matcher matcher = REVIEW_RATING_PATTERN.matcher(encodedMessage);
        if (!matcher.matches()) {
            return encodedMessage;
        }
        return matcher.group(2);
    }

    private Component buildRequestCardBubble(ChatMessageDto msg) {
        boolean isOwnRequest = msg.senderId().equals(currentUserId);
        boolean isRecipient = msg.recipientId().equals(currentUserId);

        Div card = new Div();
        card.getStyle()
            .set("border-radius", "20px")
            .set("padding", "0")
            .set("max-width", "380px")
            .set("width", "min(380px, 100%)")
            .set("background", "#ffffff")
            .set("box-shadow", "0 10px 26px rgba(74, 52, 40, 0.12)")
            .set("overflow", "hidden");

        String requestId = msg.requestId();
        UUID reqId = parseRequestId(requestId);
        OfferRequestChatCardDto details = loadRequestCardDetails(reqId);
        RequestStatus status = details != null ? details.status() : null;
        String offerTitle = details != null && !isBlank(details.offerTitle())
                ? details.offerTitle()
                : fallbackOfferTitle(msg.offerTitle());

        // Styling and content based on status
        if (status == RequestStatus.ACCEPTED) {
            boolean cancelled = false;
            if (reqId != null) {
                try {
                    cancelled = bookingService.isBookingCancelledForRequest(reqId);
                } catch (Exception e) {
                    log.warn("Could not check booking cancellation: {}", e.getMessage());
                }
            }
            if (cancelled) {
                addRequestOfferCard(card, requestCardStyle("Buchung storniert", "#d0c8c0", "#f5f0ea", "#7a6050"),
                        offerTitle, details);
            } else {
                addRequestOfferCard(card, requestCardStyle("Anfrage angenommen", "#b8ddb8", "#edf7ed", "#2e7d32"),
                        offerTitle, details);
            }
        } else if (status == RequestStatus.DENIED) {
            addRequestOfferCard(card, requestCardStyle("Anfrage abgelehnt", "#d0c8c0", "#f5f0ea", "#7a6050"),
                    offerTitle, details);
        } else if (status == RequestStatus.PENDING && isRecipient) {
            addRequestOfferCard(card, requestCardStyle("Neue Anfrage", "#f0d8a8", "#fff3d6", "#4a3428"),
                    offerTitle, details);

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setWidthFull();
            buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            buttons.getStyle()
                .set("margin", "0")
                .set("padding", "0 16px 16px 16px")
                .set("gap", "8px")
                .set("box-sizing", "border-box");

            Button acceptBtn = new Button("Annehmen", e -> handleAcceptRequest(reqId));
            acceptBtn.getStyle()
                .set("background", "#774f35")
                .set("color", "white")
                .set("border-radius", "20px")
                .set("height", "36px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("padding", "0 18px");

            Button denyBtn = new Button("Ablehnen", e -> {
                try {
                    requestService.denyRequest(reqId, currentUserId);
                    eventBus.publishRefresh(new ChatRefreshEventDto(activeConversationId, currentUserId, activeRecipientId));
                    selectConversation(activeConversationId);
                } catch (Exception ex) {
                    Notification.show("Fehler: " + ex.getMessage());
                }
            });
            denyBtn.getStyle()
                .set("background", "#f7efe4")
                .set("color", "#4a3428")
                .set("border", "1px solid #e7d8c4")
                .set("border-radius", "20px")
                .set("height", "36px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("padding", "0 18px");

            buttons.add(acceptBtn, denyBtn);
            card.add(buttons);
        } else {
            // PENDING as sender, or unknown status
            String statusText = status == RequestStatus.PENDING ? "Ausstehend" : "Unbekannter Status";
            addRequestOfferCard(card, requestCardStyle(statusText, "#c5d0e8", "#eef3ff", "#445b8a"),
                    offerTitle, details);
        }

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);

        if (isOwnRequest) {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            row.add(buildAvatar(activeCurrentUserImage), card);
        } else {
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            row.add(buildAvatar(activeCounterpartImage), card);
        }

        return row;
    }

    private OfferRequestChatCardDto loadRequestCardDetails(UUID requestId) {
        if (requestId == null) {
            return null;
        }
        try {
            return requestService.findChatCardDetails(requestId);
        } catch (Exception e) {
            log.warn("Could not load request card details: {}", e.getMessage());
            return null;
        }
    }

    private UUID parseRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(requestId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request id on chat card: {}", requestId);
            return null;
        }
    }

    private void addRequestOfferCard(
            Div card,
            RequestCardStyle style,
            String offerTitle,
            OfferRequestChatCardDto details
    ) {
        card.getStyle().set("border", "1px solid " + style.borderColor());

        Div header = new Div();
        header.getStyle()
            .set("height", "54px")
            .set("position", "relative")
            .set("background", offerAccentColor(details));

        Span badge = new Span(style.label());
        badge.getStyle()
            .set("position", "absolute")
            .set("top", "12px")
            .set("right", "12px")
            .set("background", style.badgeBackground())
            .set("color", style.badgeColor())
            .set("font-size", "12px")
            .set("font-weight", "800")
            .set("border-radius", "14px")
            .set("padding", "5px 11px")
            .set("box-shadow", "0 4px 12px rgba(74, 52, 40, 0.10)");
        header.add(badge);

        Div body = new Div();
        body.getStyle()
            .set("padding", "14px 16px 16px 16px")
            .set("box-sizing", "border-box");

        Span eyebrow = new Span("Angebot");
        eyebrow.getStyle()
            .set("display", "block")
            .set("font-size", "11px")
            .set("font-weight", "800")
            .set("color", "#9e8c7b")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0");

        H3 title = new H3(offerTitle);
        title.getStyle()
            .set("font-size", "16px")
            .set("font-weight", "800")
            .set("line-height", "1.2")
            .set("margin", "2px 0 0 0")
            .set("color", DARK);

        body.add(eyebrow, title);

        if (details != null) {
            HorizontalLayout facts = new HorizontalLayout();
            facts.setWidthFull();
            facts.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            facts.setAlignItems(FlexComponent.Alignment.START);
            facts.getStyle()
                .set("gap", "10px")
                .set("margin-top", "14px")
                .set("flex-wrap", "wrap");

            facts.add(
                requestFactItem("Zeitraum", OfferCardComponent.formatSchedule(
                    details.frequency(),
                    details.startDate(),
                    details.endDate(),
                    details.recurringWeekdays(),
                    details.timeSlot())),
                requestFactItem(petFactLabel(details), petFactValue(details))
            );
            body.add(facts);
        }

        card.add(header, body);
    }

    private VerticalLayout requestFactItem(String label, String value) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);
        box.getStyle()
            .set("min-width", "128px")
            .set("flex", "1 1 128px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "11px")
            .set("color", "#9e8c7b");

        Span valueSpan = new Span(isBlank(value) ? "–" : value);
        valueSpan.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "800")
            .set("line-height", "1.25")
            .set("color", DARK);

        box.add(labelSpan, valueSpan);
        return box;
    }

    private String petFactLabel(OfferRequestChatCardDto details) {
        return isBlank(details.petSummary()) && details.animalType() != null ? "Tierart" : "Tiere";
    }

    private String petFactValue(OfferRequestChatCardDto details) {
        if (!isBlank(details.petSummary())) {
            return details.petSummary();
        }
        if (details.animalType() != null) {
            return details.animalType().label();
        }
        return "Nicht angegeben";
    }

    private String offerAccentColor(OfferRequestChatCardDto details) {
        if (details == null || details.animalType() == null) {
            return "#f1dfb9";
        }
        return switch (details.animalType()) {
            case DOG -> "#dec18d";
            case CAT -> "#f1b47a";
            case BIRD -> "#93b8c9";
            case SMALL_ANIMAL -> "#94b883";
            case REPTILE -> "#a8c89b";
            case FISH -> "#bad6df";
            case OTHER -> "#f1dfb9";
        };
    }

    private RequestCardStyle requestCardStyle(String label, String borderColor, String badgeBackground, String badgeColor) {
        return new RequestCardStyle(label, borderColor, badgeBackground, badgeColor);
    }

    private String fallbackOfferTitle(String offerTitle) {
        return isBlank(offerTitle) ? "Angebot" : offerTitle;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record RequestCardStyle(
        String label,
        String borderColor,
        String badgeBackground,
        String badgeColor
    ) { }

    private void handleAcceptRequest(UUID requestId) {
        try {
            BookingAcceptancePreview preview = bookingService.previewAcceptance(requestId, currentUserId);
            if (preview.currentUserIsOwner()) {
                openPaymentConfirmation(requestId, preview);
                return;
            }
            acceptRequest(requestId, null);
        } catch (Exception exception) {
            showAcceptError(exception);
        }
    }

    private void openPaymentConfirmation(UUID requestId, BookingAcceptancePreview preview) {
        Dialog dialog = new Dialog();
        dialog.setWidth("460px");
        dialog.setMaxWidth("95vw");
        dialog.getElement().getThemeList().add("no-padding");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.getStyle()
                .set("background-color", "#f3eada")
                .set("padding", "32px 36px")
                .set("border-radius", "16px")
                .set("font-family", "'Inter', sans-serif")
                .set("gap", "12px")
                .set("position", "relative");

        // ── Header: title + X close button ───────────────────────────────
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("margin-bottom", "4px");

        H2 dialogTitle = new H2("Buchung bestätigen");
        dialogTitle.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("margin", "0")
                .set("color", DARK);

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("color", DARK)
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("min-width", "0")
                .set("width", "36px")
                .set("height", "36px");
        closeBtn.addClickListener(e -> dialog.close());

        header.add(dialogTitle, closeBtn);

        // ── Explanation text ──────────────────────────────────────────────
        Paragraph explanation = new Paragraph(
                "Der Gesamtpreis wird jetzt von deinem Guthaben abgezogen und bis zur Auszahlung sicher in Treuhand gehalten.");
        explanation.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#7a6050")
                .set("line-height", "1.5");

        content.add(header, explanation,
                paymentLine("Preis pro Tag", preview.pricePerDay()),
                paymentLine("Gesamtpreis", preview.totalPrice()),
                paymentLine("Dein Guthaben", preview.availableBalance()));

        if (!preview.sufficientBalance()) {
            Paragraph warning = new Paragraph("Dein Guthaben reicht für diese Buchung noch nicht aus.");
            warning.getStyle()
                    .set("margin", "4px 0 0 0")
                    .set("color", "#9a4f36")
                    .set("font-weight", "700")
                    .set("font-size", "14px");

            Button walletButton = new Button("Guthaben aufladen", event -> {
                dialog.close();
                UI.getCurrent().navigate("profile", QueryParameters.of("tab", "wallet"));
            });
            walletButton.setWidthFull();
            walletButton.getStyle()
                    .set("background-color", "#5c3d1e")
                    .set("color", "white")
                    .set("border-radius", "24px")
                    .set("height", "48px")
                    .set("font-size", "15px")
                    .set("font-weight", "700")
                    .set("cursor", "pointer")
                    .set("border", "none")
                    .set("margin-top", "4px");
            content.add(warning, walletButton);
        } else {
            Button confirm = new Button("Mit Guthaben bezahlen und annehmen",
                    event -> acceptRequest(requestId, dialog));
            confirm.setWidthFull();
            confirm.getStyle()
                    .set("background-color", "#5c3d1e")
                    .set("color", "white")
                    .set("border-radius", "24px")
                    .set("height", "48px")
                    .set("font-size", "15px")
                    .set("font-weight", "700")
                    .set("cursor", "pointer")
                    .set("border", "none")
                    .set("margin-top", "4px");
            content.add(confirm);
        }

        content.add(new ExternalPaymentMethods());
        dialog.add(content);
        dialog.open();
    }

    private Component paymentLine(String label, java.math.BigDecimal amount) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Span key = new Span(label);
        key.getStyle()
                .set("color", "#7a6050")
                .set("font-size", "15px");
        Span value = new Span(amount.setScale(2) + " EUR");
        value.getStyle()
                .set("font-weight", "800")
                .set("color", DARK)
                .set("font-size", "15px");
        row.add(key, value);
        return row;
    }

    private void acceptRequest(UUID requestId, Dialog dialog) {
        try {
            bookingService.acceptRequest(requestId, currentUserId);
            if (dialog != null) {
                dialog.close();
            }
            Notification n = Notification.show("Anfrage angenommen · Guthaben wurde in Treuhand reserviert");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            eventBus.publishRefresh(new ChatRefreshEventDto(activeConversationId, currentUserId, activeRecipientId));
            selectConversation(activeConversationId);
        } catch (Exception exception) {
            showAcceptError(exception);
        }
    }

    private void showAcceptError(Exception exception) {
        if (exception instanceof InsufficientBalanceException insufficientBalance) {
            if (currentUserId.equals(insufficientBalance.getOwnerId())) {
                Notification.show("Dein Guthaben reicht nicht aus. Bitte lade zuerst Guthaben auf.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                UI.getCurrent().navigate("profile", QueryParameters.of("tab", "wallet"));
            } else {
                Notification.show("Die Anfrage kann noch nicht angenommen werden. Der Tierhalter wurde informiert.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            return;
        }
        Notification.show("Fehler: " + exception.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
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

    private void openProfilePopUp() {
        if (activeRecipientId == null) {
            return;
        }
        userService.getPublicUserProfile(activeRecipientId).ifPresentOrElse(
            profile -> {
                ProfilePopUp popUp = new ProfilePopUp(profile);
                popUp.open();
            },
            () -> {
                Notification n = Notification.show("Profil konnte nicht geladen werden.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        );
    }

    private Component buildAvatar(ImageRefDto image) {
        return createChatAvatar(image, 32);
    }

    private Div createChatAvatar(int size) {
        return createChatAvatar(null, size);
    }

    private Div createChatAvatar(ImageRefDto image, int size) {
        Div avatar = ImageComponents.avatar(image, size, "#c8dce6");
        avatar.getStyle().set("flex-shrink", "0");
        return avatar;
    }


    private ImageRefDto counterpartImage(ChatConversationDto conversation) {
        return currentUserId.equals(conversation.ownerId())
                ? conversation.sitterProfileImage()
                : conversation.ownerProfileImage();
    }

    private void replaceAvatarContent(Div target, ImageRefDto image, int size) {
        target.removeAll();
        createChatAvatar(image, size).getChildren().toList().forEach(target::add);
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

        this.refreshRegistration = eventBus.registerRefresh(currentUserId, event -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(() -> {
                    if (event.conversationId().equals(activeConversationId)) {
                        selectConversation(activeConversationId);
                    } else {
                        refreshConversationList();
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
        if (refreshRegistration != null) {
            refreshRegistration.remove();
        }
        if (stopTypingFuture != null) {
            stopTypingFuture.cancel(true);
        }
        typingScheduler.shutdownNow();
    }

}
