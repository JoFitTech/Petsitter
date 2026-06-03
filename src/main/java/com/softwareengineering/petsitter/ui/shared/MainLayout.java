package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.image.service.ImageAssetService;
import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.Registration;
import com.softwareengineering.petsitter.notification.domain.Notification;
import com.softwareengineering.petsitter.notification.domain.NotificationType;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.user.LoginView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.QueryParameters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.dependency.CssImport;

@CssImport("./styles/responsive-header.css")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private static final String DARK      = "#4a3428";
    private static final String BROWN     = "#7b5236";
    private static final String LIGHT_BG  = "#fbf8f1";
    private static final String NAV_ACTIVE_BG = "#f6e3bd";
    private static final String NAV_INACTIVE_BG = "#fff6e6";
    private static final String NAV_BORDER = "#ead5ae";

    private final AuthenticatedUser authenticatedUser;
    private final NotificationService notificationService;
    private final ChatEventBus chatEventBus;
    private final ImageAssetService imageAssetService;
    private Button findOwnerBtn;
    private Button findSitterBtn;
    private Button profileBtn;
    private Span mailBadge;
    private Span mailTypingIndicator;
    private Span burgerBadge;
    private Span mobileMailTypingIndicator;
    private Span mobileNotificationBadge;
    private Div dropdownMenu;
    private Registration badgeRegistration;
    private Registration typingHeaderRegistration;
    private final ScheduledExecutorService typingHeaderScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> hideTypingHeaderFuture;

    private static final DateTimeFormatter NOTIFICATION_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    public MainLayout(
            AuthenticatedUser authenticatedUser,
            NotificationService notificationService,
            ChatEventBus chatEventBus,
            ImageAssetService imageAssetService
    ) {
        this.authenticatedUser = authenticatedUser;
        this.notificationService = notificationService;
        this.chatEventBus = chatEventBus;
        this.imageAssetService = imageAssetService;

        // ── Global page background & font ─────────────────────────────────
        getElement().getStyle()
                .set("background", LIGHT_BG)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK);

        // ── Navbar (Header) ───────────────────────────────────────────────
        addToNavbar(true, buildHeader());

        registerBadgeListener();
    }

    // ── Override showRouterLayoutContent to append the footer ─────────────
    @Override
    public void showRouterLayoutContent(HasElement content) {
        // Wrap page content + footer in a flex column that is at least full-height
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidthFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.getStyle()
                .set("min-height", "100vh")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("background", LIGHT_BG);

        // The actual routed view
        Component contentComponent = (Component) content;
        wrapper.add(contentComponent);
        wrapper.setFlexGrow(1, contentComponent);

        // Global footer below every page
        wrapper.add(buildFooter());

        setContent(wrapper);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassName("main-header-layout");

        header.getStyle()
                .set("background", "rgba(255,255,255,0.92)")
                .set("backdrop-filter", "blur(8px)")
                .set("padding", "0 clamp(12px, 3vw, 28px)")
                .set("height", "72px")
                .set("box-shadow", "0 2px 18px rgba(74, 52, 40, 0.07)")
                .set("box-sizing", "border-box")
                .set("flex-wrap", "nowrap")
                .set("gap", "8px");

        // Left: Logo (PNG image – rahmenlos via Div)
        Image logoImg = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        logoImg.getStyle()
                .set("height", "clamp(36px, 5vw, 52px)")
                .set("width", "auto")
                .set("display", "block");

        Div logoWrapper = new Div(logoImg);
        logoWrapper.getStyle()
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("flex-shrink", "0");
        logoWrapper.addClickListener(e -> UI.getCurrent().navigate(""));

        // Center: Navigation Pills + fixed Create-Offer button
        HorizontalLayout centerGroup = new HorizontalLayout();
        centerGroup.setSpacing(false);
        centerGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        centerGroup.addClassName("desktop-center-group");
        centerGroup.getStyle()
                .set("gap", "8px")
                .set("flex-wrap", "wrap")
                .set("justify-content", "center")
                .set("flex", "1 1 auto")
                .set("min-width", "0");

        findOwnerBtn = pillButton("Tierhalter finden");
        findOwnerBtn.addClickListener(e -> UI.getCurrent().navigate(""));

        findSitterBtn = pillButton("Tiersitter finden");
        findSitterBtn.addClickListener(e -> UI.getCurrent().navigate("tierhalter-finden"));

        centerGroup.add(findOwnerBtn, findSitterBtn);

        // Fixed "Angebot erstellen" button – only visible for logged-in users
        if (authenticatedUser.get().isPresent()) {
            Button createOfferBtn = new Button("Angebot erstellen");
            createOfferBtn.getStyle()
                    .set("height", "42px")
                    .set("padding", "0 clamp(12px, 2vw, 22px)")
                    .set("border-radius", "22px")
                    .set("background", "#774f35")
                    .set("color", "white")
                    .set("font-weight", "700")
                    .set("font-size", "14px")
                    .set("box-shadow", "none")
                    .set("cursor", "pointer")
                    .set("flex-shrink", "0");
            createOfferBtn.addClickListener(e -> openCreateOfferDialog());
            centerGroup.add(createOfferBtn);
        }

        header.add(logoWrapper, centerGroup, buildHeaderActions());
        return header;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        updateNavigationState(
                event.getLocation().getPath(),
                event.getLocation().getQueryParameters().getParameters()
        );
        if (mailBadge != null) {
            updateMailBadge(mailBadge);
        }
        if (dropdownMenu != null) {
            dropdownMenu.getStyle().set("display", "none");
        }
    }

    private void updateNavigationState(String path, Map<String, List<String>> queryParameters) {
        boolean ownerActive = path == null || path.isBlank();
        boolean sitterActive = "tierhalter-finden".equals(path);

        if ("petsitter-suche".equals(path)) {
            List<String> modes = queryParameters.getOrDefault("mode", List.of());
            ownerActive = modes.contains("tierhalter");
            sitterActive = modes.contains("tiersitter");
        }

        setPillActive(findOwnerBtn, ownerActive);
        setPillActive(findSitterBtn, sitterActive);
    }

    private Component buildHeaderActions() {
        if (authenticatedUser.get().isEmpty()) {
            Button loginBtn = headerLoginButton();
            loginBtn.addClickListener(e -> UI.getCurrent().navigate(LoginView.class));
            return loginBtn;
        }

        HorizontalLayout desktopIcons = new HorizontalLayout();
        desktopIcons.setSpacing(false);
        desktopIcons.setAlignItems(FlexComponent.Alignment.CENTER);
        desktopIcons.getStyle().set("gap", "8px");
        desktopIcons.addClassName("desktop-header-actions");

        desktopIcons.add(buildMailButtonWithTypingIndicator(), buildNotificationButtonWithBadge());

        Button heartBtn = headerIconButton(VaadinIcon.HEART_O, "transparent", DARK);
        heartBtn.addClickListener(e -> UI.getCurrent().navigate("profile", com.vaadin.flow.router.QueryParameters.of("tab", "favorites")));

        profileBtn = new Button(headerProfileAvatar());
        profileBtn.setAriaLabel("Profil öffnen");
        profileBtn.getStyle()
                .set("width", "42px")
                .set("height", "42px")
                .set("min-width", "42px")
                .set("padding", "0")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        desktopIcons.add(heartBtn, profileBtn);

        Component mobileBurger = buildBurgerMenuButton();

        Div wrapper = new Div(desktopIcons, mobileBurger);
        wrapper.getStyle()
                .set("display", "flex")
                .set("align-items", "center");
        return wrapper;
    }

    public void refreshProfileImage() {
        if (profileBtn != null) {
            profileBtn.setIcon(headerProfileAvatar());
        }
    }

    private Component headerProfileAvatar() {
        return ImageComponents.avatar(
                authenticatedUser.get()
                        .flatMap(user -> imageAssetService.findUserImage(user.getId()))
                        .orElse(null),
                42,
                "#8db3c3");
    }

    private Component buildMailButtonWithTypingIndicator() {
        HorizontalLayout container = new HorizontalLayout();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setSpacing(false);
        container.getStyle()
                .set("position", "relative")
                .set("width", "42px")
                .set("height", "42px");

        Button mailBtn = headerIconButton(VaadinIcon.ENVELOPE_O, "transparent", DARK);
        mailBtn.addClickListener(e -> UI.getCurrent().navigate("chat"));
        container.add(mailBtn);

        mailTypingIndicator = new Span("...");
        mailTypingIndicator.getStyle()
                .set("position", "absolute")
                .set("right", "-4px")
                .set("bottom", "-3px")
                .set("display", "none")
                .set("padding", "0 4px")
                .set("border-radius", "8px")
                .set("background", "#8db3c3")
                .set("color", "white")
                .set("font-size", "10px")
                .set("font-weight", "700")
                .set("line-height", "14px");
        container.add(mailTypingIndicator);

        return container;
    }

    private Component buildNotificationButtonWithBadge() {
        HorizontalLayout container = new HorizontalLayout();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setSpacing(false);
        container.getStyle()
            .set("position", "relative")
            .set("width", "42px")
            .set("height", "42px");

        Button notificationBtn = headerIconButton(VaadinIcon.BELL_O, "transparent", DARK);
        notificationBtn.addClickListener(e -> openNotificationDialog());
        container.add(notificationBtn);

        // Badge with unread count
        mailBadge = new Span();
        mailBadge.getStyle()
            .set("position", "absolute")
            .set("top", "0")
            .set("right", "0")
            .set("width", "20px")
            .set("height", "20px")
            .set("background", "#e74c3c")
            .set("color", "white")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-size", "10px")
            .set("font-weight", "700")
            .set("z-index", "10")
            .set("display", "none"); // hidden by default

        updateMailBadge(mailBadge);
        container.add(mailBadge);

        return container;
    }

    private void updateMailBadge(Span badge) {
        long unread = authenticatedUser.get()
                .map(user -> notificationService.countUnread(user.getId()))
                .orElse(0L);

        String text = unread > 99 ? "99+" : Long.toString(unread);
        boolean show = unread > 0;

        if (badge != null) {
            badge.setText(show ? text : "");
            badge.getStyle().set("display", show ? "flex" : "none");
        }
        if (burgerBadge != null) {
            burgerBadge.setText(show ? text : "");
            burgerBadge.getStyle().set("display", show ? "flex" : "none");
        }
        if (mobileNotificationBadge != null) {
            mobileNotificationBadge.setText(show ? text : "");
            mobileNotificationBadge.getStyle().set("display", show ? "flex" : "none");
        }
    }

    private void openNotificationDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");
        dialog.getElement().getThemeList().add("no-padding");
        dialog.getElement().getStyle()
                .set("border-radius", "16px")
                .set("font-family", "Inter, Arial, sans-serif");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("padding", "24px 28px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("background-color", "#f3eada")
                .set("border-radius", "16px")
                .set("box-sizing", "border-box")
                .set("color", DARK);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 title = new H2("Benachrichtigungen");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("color", DARK);

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("color", DARK)
                .set("font-size", "20px")
                .set("padding", "0")
                .set("min-width", "auto")
                .set("height", "auto")
                .set("cursor", "pointer")
                .set("background", "transparent")
                .set("border", "none");
        closeBtn.addClickListener(e -> dialog.close());

        header.add(title, closeBtn);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        List<Notification> inbox = authenticatedUser.get()
                .map(user -> notificationService.getInbox(user.getId()))
                .orElse(List.of());

        if (inbox.isEmpty()) {
            Paragraph emptyText = new Paragraph("Keine Benachrichtigungen vorhanden.");
            emptyText.getStyle()
                    .set("color", "#7A6050")
                    .set("font-size", "15px")
                    .set("margin", "0")
                    .set("font-weight", "500");
            content.add(emptyText);
        } else {
            Map<LocalDate, List<Notification>> grouped = groupNotificationsByDate(inbox);
            for (Map.Entry<LocalDate, List<Notification>> entry : grouped.entrySet()) {
                Span groupHeader = new Span(toDateGroupLabel(entry.getKey()));
                groupHeader.getStyle()
                        .set("font-weight", "800")
                        .set("font-size", "15px")
                        .set("color", DARK)
                        .set("margin", "8px 0 2px 0");
                content.add(groupHeader);

                for (Notification notification : entry.getValue()) {
                    content.add(buildNotificationRow(dialog, notification));
                }
            }
        }

        wrapper.add(header, content);
        dialog.add(wrapper);
        dialog.open();
    }

    private Component buildNotificationRow(Dialog dialog, Notification notification) {
        Button row = new Button();
        row.setWidthFull();
        row.getStyle()
                .set("text-align", "left")
                .set("justify-content", "flex-start")
                .set("background", notification.isRead() ? "#FCF9F2" : "#fff4de")
                .set("border", notification.isRead() ? "1px solid #efe4d3" : "1px solid #e2b56b")
                .set("border-left", notification.isRead() ? "1px solid #efe4d3" : "5px solid #e2b56b")
                .set("border-radius", "12px")
                .set("padding", "12px 16px")
                .set("height", "auto")
                .set("min-height", "60px")
                .set("color", DARK)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("box-sizing", "border-box");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "4px");

        Span message = new Span(notification.getMessage());
        message.getStyle()
                .set("font-size", "15px")
                .set("color", DARK)
                .set("white-space", "normal")
                .set("font-weight", notification.isRead() ? "600" : "800");

        Span meta = new Span(notification.getType().name() + " • "
                + notification.getCreatedAt().format(NOTIFICATION_TIME_FORMAT));
        meta.getStyle().set("font-size", "13px").set("color", "#7A6050");

        if (!notification.isRead()) {
            Span unreadDot = new Span();
            unreadDot.getStyle()
                    .set("width", "8px")
                    .set("height", "8px")
                    .set("border-radius", "50%")
                    .set("display", "inline-block")
                    .set("background", "#d98b2b")
                    .set("margin-bottom", "4px");
            layout.add(unreadDot);
        }

        layout.add(message, meta);
        row.setIcon(layout);

        row.addClickListener(e -> {
            authenticatedUser.get().ifPresent(user -> notificationService.markAsRead(notification.getId(), user.getId()));
            dialog.close();

            if (notification.getType() == NotificationType.WALLET_TOP_UP_REQUIRED) {
                UI.getCurrent().navigate("profile", QueryParameters.of("tab", "wallet"));
            } else if (notification.getType() == NotificationType.PAYOUT_REQUESTED
                    || notification.getType() == NotificationType.PAYOUT_RELEASED) {
                UI.getCurrent().navigate("profile", QueryParameters.of("tab", "bookings"));
            } else if (notification.getType() == NotificationType.CHAT_MESSAGE
                    && notification.getReferenceId() != null
                    && !notification.getReferenceId().isBlank()) {
                UI.getCurrent().navigate("chat", QueryParameters.of("conversation", notification.getReferenceId()));
            } else {
                UI.getCurrent().navigate("chat");
            }
        });

        return row;
    }

    private void registerBadgeListener() {
        authenticatedUser.get().ifPresent(user ->
                badgeRegistration = chatEventBus.register(user.getId(), message -> {
                    UI ui = getUI().orElse(null);
                    if (ui != null && mailBadge != null) {
                        ui.access(() -> updateMailBadge(mailBadge));
                    }
                })
        );

        authenticatedUser.get().ifPresent(user ->
                typingHeaderRegistration = chatEventBus.registerTyping(user.getId(), event -> {
                    UI ui = getUI().orElse(null);
                    if (ui == null || mailTypingIndicator == null) {
                        return;
                    }
                    ui.access(() -> {
                        if (!event.typing()) {
                            hideTypingHeaderIndicator();
                            return;
                        }

                        mailTypingIndicator.getStyle().set("display", "inline-block");
                        if (mobileMailTypingIndicator != null) {
                            mobileMailTypingIndicator.getStyle().set("display", "inline-block");
                        }

                        if (hideTypingHeaderFuture != null) {
                            hideTypingHeaderFuture.cancel(false);
                        }
                        hideTypingHeaderFuture = typingHeaderScheduler.schedule(() -> {
                            UI currentUi = getUI().orElse(null);
                            if (currentUi != null) {
                                currentUi.access(this::hideTypingHeaderIndicator);
                            }
                        }, 2, TimeUnit.SECONDS);
                    });
                })
        );
    }

    private void hideTypingHeaderIndicator() {
        if (mailTypingIndicator != null) {
            mailTypingIndicator.getStyle().set("display", "none");
        }
        if (mobileMailTypingIndicator != null) {
            mobileMailTypingIndicator.getStyle().set("display", "none");
        }
    }

    private Map<LocalDate, List<Notification>> groupNotificationsByDate(List<Notification> notifications) {
        Map<LocalDate, List<Notification>> grouped = new LinkedHashMap<>();
        for (Notification notification : notifications) {
            LocalDate date = notification.getCreatedAt().toLocalDate();
            grouped.computeIfAbsent(date, ignored -> new java.util.ArrayList<>()).add(notification);
        }
        return grouped;
    }

    private String toDateGroupLabel(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) {
            return "Heute";
        }
        if (date.equals(today.minusDays(1))) {
            return "Gestern";
        }
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }


    // ── Create-Offer Dialog ───────────────────────────────────────────────
    private void openCreateOfferDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("520px");
        dialog.getElement().getThemeList().add("no-padding");
        dialog.getElement().getStyle()
                .set("border-radius", "20px")
                .set("font-family", "Inter, Arial, sans-serif");

        // Wrapper with position:relative so X can be placed absolute
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("padding", "24px 24px 22px 24px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "14px")
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
                .set("box-sizing", "border-box");

        // X close button – absolute, top-right corner
        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px")
                .set("width", "28px")
                .set("height", "28px")
                .set("min-width", "28px")
                .set("border-radius", "50%")
                .set("background", "transparent")
                .set("border", "none")
                .set("color", "#9a8070")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("padding", "0")
                .set("z-index", "10");
        closeBtn.addClickListener(e -> dialog.close());

        com.vaadin.flow.component.html.H2 title =
                new com.vaadin.flow.component.html.H2("Was möchtest du erstellen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "21px")
                .set("font-weight", "800")
                .set("line-height", "1.2")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("padding-right", "32px")  // prevent overlap with X
                .set("color", DARK);

        Paragraph copy = new Paragraph(
                "Wähle aus, ob du Betreuung suchst oder selbst Betreuung anbieten möchtest.");
        copy.getStyle()
                .set("margin", "0")
                .set("font-size", "13px")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("line-height", "1.5")
                .set("color", "#9a8070");

        wrapper.add(
                closeBtn,
                title,
                copy,
                createDialogOption(dialog, VaadinIcon.HOME,
                        "Betreuung für mein Haustier suchen",
                        "Erstelle einen Auftrag für dein Haustier.", "request"),
                createDialogOption(dialog, VaadinIcon.HEART,
                        "Betreuung anbieten",
                        "Erstelle ein Angebot als Tiersitter.", "offer")
        );

        dialog.add(wrapper);
        dialog.open();
    }

    private Component createDialogOption(Dialog dialog, VaadinIcon iconType,
            String titleText, String description, String mode) {
        Button option = new Button();
        option.setWidthFull();
        option.getStyle()
                .set("height", "auto")
                .set("min-height", "68px")
                .set("padding", "14px 16px")
                .set("border", "1.5px solid #e8d9c4")
                .set("border-radius", "14px")
                .set("background", "#fefcf8")
                .set("color", DARK)
                .set("box-shadow", "0 2px 8px rgba(74,52,40,0.05)")
                .set("cursor", "pointer")
                .set("text-align", "left");

        Icon icon = new Icon(iconType);
        icon.setSize("22px");
        icon.getStyle()
                .set("color", DARK)
                .set("flex-shrink", "0");

        VerticalLayout optionContent = new VerticalLayout();
        optionContent.setPadding(false);
        optionContent.setSpacing(false);
        optionContent.getStyle().set("gap", "3px");

        Span optTitle = new Span(titleText);
        optTitle.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("letter-spacing", "-0.1px")
                .set("color", DARK);

        Span desc = new Span(description);
        desc.getStyle()
                .set("font-size", "12.5px")
                .set("font-weight", "400")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", "#9a8070");

        optionContent.add(optTitle, desc);

        HorizontalLayout row = new HorizontalLayout(icon, optionContent);
        row.setPadding(false);
        row.setSpacing(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("gap", "14px");

        option.getElement().appendChild(row.getElement());
        option.addClickListener(event -> {
            dialog.close();
            UI ui = UI.getCurrent();
            if (ui != null) {
                Map<String, List<String>> params = new LinkedHashMap<>();
                params.put("mode", List.of(mode));
                params.put("returnTo", List.of("/profile?tab=offers"));
                ui.navigate("auftrag-erstellen", new QueryParameters(params));
            }
        });
        return option;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (badgeRegistration != null) {
            badgeRegistration.remove();
        }
        if (typingHeaderRegistration != null) {
            typingHeaderRegistration.remove();
        }
        if (hideTypingHeaderFuture != null) {
            hideTypingHeaderFuture.cancel(true);
        }
        typingHeaderScheduler.shutdownNow();
    }


    // ── Footer (global, shown on every page) ─────────────────────────────
    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.START);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        footer.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("padding", "clamp(20px, 4vw, 34px) clamp(16px, 5vw, 76px)")
                .set("box-sizing", "border-box")
                .set("margin-top", "auto")
                .set("flex-wrap", "wrap")
                .set("gap", "28px");

        // Brand column
        VerticalLayout brand = new VerticalLayout();
        brand.setPadding(false);
        brand.setSpacing(false);
        brand.getStyle().set("flex", "1 1 180px").set("min-width", "0");

        Image footerLogo = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        footerLogo.getStyle()
                .set("height", "44px")
                .set("width", "auto")
                .set("display", "block")
                .set("margin-bottom", "8px");

        Paragraph claim = new Paragraph("Freundliche Plattform für zuverlässige Tierbetreuung in deiner Nähe.");
        claim.getStyle()
                .set("margin", "0 0 18px 0")
                .set("font-size", "14px")
                .set("color", "#e8d8c6");

        Span copyright = new Span("© 2026 Pawsitter");
        copyright.getStyle()
                .set("font-size", "12px")
                .set("color", "#e8d8c6");

        brand.add(footerLogo, claim, copyright);

        // Links
        HorizontalLayout links = new HorizontalLayout();
        links.setSpacing(false);
        links.getStyle()
                .set("gap", "clamp(10px, 2vw, 28px)")
                .set("flex-wrap", "wrap")
                .set("align-items", "center")
                .set("flex", "2 1 280px")
                .set("min-width", "0")
                .set("justify-content", "center");

        links.add(
                footerLink("Über uns",    "info"),
                footerLink("Kontakt",     "info"),
                footerLink("Datenschutz", "info"),
                footerLink("Impressum",   "info"),
                footerActionLink("Cookie-Einstellungen", () -> new CookiePopUp().open())
        );

        // Social buttons
        HorizontalLayout socials = new HorizontalLayout();
        socials.setSpacing(true);
        socials.getStyle().set("flex-shrink", "0").set("align-items", "center");

        Html facebookIcon = new Html("<svg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='currentColor' viewBox='0 0 16 16' style='display: block;'><path d='M16 8.049c0-4.446-3.582-8.05-8-8.05C3.58 0-.002 3.603-.002 8.05c0 4.017 2.926 7.347 6.75 7.951v-5.625h-2.03V8.05H6.75V6.275c0-2.017 1.195-3.131 3.022-3.131.876 0 1.791.157 1.791.157v1.98h-1.009c-.993 0-1.303.621-1.303 1.258v1.51h2.218l-.354 2.326H9.25V16c3.824-.604 6.75-3.934 6.75-7.951z'/></svg>");
        Button facebookBtn = socialButton(facebookIcon);
        facebookBtn.addClickListener(e -> {
            // TODO: UI.getCurrent().getPage().open("https://facebook.com/pawsitter");
        });

        Html instagramIcon = new Html("<svg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='currentColor' viewBox='0 0 16 16' style='display: block;'><path d='M8 0C5.829 0 5.556.01 4.703.048 3.85.088 3.269.222 2.76.42a3.9 3.9 0 0 0-1.417.923A3.9 3.9 0 0 0 .42 2.76C.222 3.268.087 3.85.048 4.7.01 5.555 0 5.827 0 8.001c0 2.172.01 2.444.048 3.297.04.852.174 1.433.372 1.942.205.526.478.972.923 1.417.444.445.89.719 1.416.923.51.198 1.09.333 1.942.372C5.555 15.99 5.827 16 8 16s2.444-.01 3.298-.048c.851-.04 1.434-.174 1.943-.372a3.9 3.9 0 0 0 1.416-.923c.445-.445.718-.891.923-1.417.197-.509.332-1.09.372-1.942C15.99 10.445 16 10.173 16 8s-.01-2.445-.048-3.299c-.04-.851-.175-1.433-.372-1.941a3.9 3.9 0 0 0-.923-1.417A3.9 3.9 0 0 0 13.24.42c-.51-.198-1.092-.333-1.943-.372C10.443.01 10.172 0 7.998 0zm-.717 1.442h.718c2.136 0 2.389.007 3.232.046.78.035 1.204.166 1.486.275.373.145.64.319.92.599.28.28.453.546.598.92.11.281.24.705.275 1.485.039.843.047 1.096.047 3.231s-.008 2.389-.047 3.232c-.035.78-.166 1.203-.275 1.485a2.5 2.5 0 0 1-.599.919c-.28.28-.546.453-.92.598-.28.11-.704.24-1.485.276-.843.038-1.096.047-3.232.047s-2.39-.009-3.233-.047c-.78-.036-1.203-.166-1.485-.276a2.5 2.5 0 0 1-.92-.598 2.5 2.5 0 0 1-.6-.92c-.109-.281-.24-.705-.275-1.485-.038-.843-.046-1.096-.046-3.233s.008-2.388.046-3.231c.036-.78.166-1.204.276-1.486.145-.373.319-.64.599-.92.28-.28.546-.453.92-.598.282-.11.705-.24 1.485-.276.738-.034 1.024-.044 2.515-.045zm4.988 1.328a.96.96 0 1 0 0 1.92.96.96 0 0 0 0-1.92zm-4.27 1.122a4.109 4.109 0 1 0 0 8.217 4.109 4.109 0 0 0 0-8.217zm0 1.441a2.667 2.667 0 1 1 0 5.334 2.667 2.667 0 0 1 0-5.334z'/></svg>");
        Button instagramBtn = socialButton(instagramIcon);
        instagramBtn.addClickListener(e -> {
            // TODO: UI.getCurrent().getPage().open("https://instagram.com/pawsitter");
        });

        socials.add(facebookBtn, instagramBtn);

        footer.add(brand, links, socials);
        return footer;
    }

    // ── Shared helpers ────────────────────────────────────────────────────
    private Button headerIconButton(VaadinIcon icon, String background, String color) {
        Button btn = new Button(new Icon(icon));
        btn.getStyle()
                .set("width", "42px")
                .set("height", "42px")
                .set("border-radius", "50%")
                .set("background", background)
                .set("color", color)
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        return btn;
    }

    private Button headerLoginButton() {
        Button btn = new Button("Anmelden");
        btn.getStyle()
                .set("height", "42px")
                .set("padding", "0 24px")
                .set("border-radius", "22px")
                .set("background", BROWN)
                .set("color", "white")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        return btn;
    }

    private Button pillButton(String text) {
        Button button = new Button(text);
        button.getStyle()
                .set("height", "44px")
                .set("padding", "0 clamp(14px, 2.5vw, 36px)")
                .set("border-radius", "28px")
                .set("background", NAV_INACTIVE_BG)
                .set("border", "1px solid " + NAV_BORDER)
                .set("color", DARK)
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("white-space", "nowrap")
                .set("flex-shrink", "0");
        return button;
    }

    private void setPillActive(Button button, boolean active) {
        if (button == null) {
            return;
        }

        button.getStyle()
                .set("background", active ? NAV_ACTIVE_BG : NAV_INACTIVE_BG)
                .set("border", active ? "1px solid transparent" : "1px solid " + NAV_BORDER);
    }

    private Component footerLink(String text, String route) {
        Button btn = new Button(text);
        btn.getStyle()
                .set("background", "transparent")
                .set("color", "white")
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("cursor", "pointer");
        btn.addClickListener(e -> {
            System.out.println("Footer-Link geklickt: " + text + " -> navigiert zu: " + route);
            UI.getCurrent().navigate(route);
        });
        return btn;
    }

    private Component footerActionLink(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyle()
                .set("background", "transparent")
                .set("color", "white")
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("cursor", "pointer");
        btn.addClickListener(e -> {
            System.out.println("Footer-Aktion geklickt: " + text);
            action.run();
        });
        return btn;
    }

    private Button socialButton(Component icon) {
        Button btn = new Button(icon);
        btn.getStyle()
                .set("width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("background", "#87b2c3")
                .set("color", "white")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("border", "none")
                .set("padding", "0");
        return btn;
    }

    private Component buildBurgerMenuButton() {
        HorizontalLayout container = new HorizontalLayout();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setSpacing(false);
        container.addClassName("mobile-header-actions");
        container.getStyle()
                .set("position", "relative")
                .set("width", "42px")
                .set("height", "42px");

        Button burgerBtn = headerIconButton(VaadinIcon.MENU, "transparent", DARK);
        container.add(burgerBtn);

        burgerBadge = new Span();
        burgerBadge.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("right", "0")
                .set("width", "20px")
                .set("height", "20px")
                .set("background", "#e74c3c")
                .set("color", "white")
                .set("border-radius", "50%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "10px")
                .set("font-weight", "700")
                .set("z-index", "10")
                .set("display", "none");
        container.add(burgerBadge);

        dropdownMenu = new Div();
        dropdownMenu.addClassName("burger-dropdown-menu");
        dropdownMenu.getStyle()
                .set("position", "absolute")
                .set("top", "50px")
                .set("right", "0")
                .set("width", "260px")
                .set("background-color", "#f3eada")
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "16px")
                .set("padding", "12px")
                .set("box-shadow", "0 4px 24px rgba(74, 52, 40, 0.15)")
                .set("z-index", "10000")
                .set("display", "none")
                .set("flex-direction", "column")
                .set("gap", "8px")
                .set("box-sizing", "border-box");

        boolean[] isOpen = {false};
        burgerBtn.addClickListener(e -> {
            isOpen[0] = !isOpen[0];
            dropdownMenu.getStyle().set("display", isOpen[0] ? "flex" : "none");
            if (isOpen[0]) {
                rebuildBurgerDropdown();
            }
        });

        container.add(dropdownMenu);
        updateMailBadge(burgerBadge);
        return container;
    }

    private void rebuildBurgerDropdown() {
        if (dropdownMenu == null) return;
        dropdownMenu.removeAll();

        Component ownerRow = createMenuRow(VaadinIcon.USERS, "Tierhalter finden", () -> {
            dropdownMenu.getStyle().set("display", "none");
            UI.getCurrent().navigate("");
        }, null);

        Component sitterRow = createMenuRow(VaadinIcon.SEARCH, "Tiersitter finden", () -> {
            dropdownMenu.getStyle().set("display", "none");
            UI.getCurrent().navigate("tierhalter-finden");
        }, null);

        dropdownMenu.add(ownerRow, sitterRow);

        if (authenticatedUser.get().isEmpty()) {
            Component loginRow = createMenuRow(VaadinIcon.SIGN_IN, "Anmelden", () -> {
                dropdownMenu.getStyle().set("display", "none");
                UI.getCurrent().navigate(LoginView.class);
            }, null);
            dropdownMenu.add(loginRow);
        } else {
            Component createOfferRow = createMenuRow(VaadinIcon.PLUS_CIRCLE, "Angebot erstellen", () -> {
                dropdownMenu.getStyle().set("display", "none");
                openCreateOfferDialog();
            }, null);

            mobileMailTypingIndicator = new Span("...");
            mobileMailTypingIndicator.getStyle()
                    .set("padding", "0 6px")
                    .set("border-radius", "8px")
                    .set("background", "#8db3c3")
                    .set("color", "white")
                    .set("font-size", "11px")
                    .set("font-weight", "700")
                    .set("display", mailTypingIndicator != null && "inline-block".equals(mailTypingIndicator.getStyle().get("display")) ? "inline-block" : "none");

            Component chatRow = createMenuRow(VaadinIcon.ENVELOPE_O, "Chat", () -> {
                dropdownMenu.getStyle().set("display", "none");
                UI.getCurrent().navigate("chat");
            }, mobileMailTypingIndicator);

            mobileNotificationBadge = new Span();
            mobileNotificationBadge.getStyle()
                    .set("width", "20px")
                    .set("height", "20px")
                    .set("background", "#e74c3c")
                    .set("color", "white")
                    .set("border-radius", "50%")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("font-size", "10px")
                    .set("font-weight", "700");

            long unread = authenticatedUser.get()
                    .map(user -> notificationService.countUnread(user.getId()))
                    .orElse(0L);
            if (unread > 0) {
                mobileNotificationBadge.setText(unread > 99 ? "99+" : Long.toString(unread));
                mobileNotificationBadge.getStyle().set("display", "flex");
            } else {
                mobileNotificationBadge.getStyle().set("display", "none");
            }

            Component notificationsRow = createMenuRow(VaadinIcon.BELL_O, "Benachrichtigungen", () -> {
                dropdownMenu.getStyle().set("display", "none");
                openNotificationDialog();
            }, mobileNotificationBadge);

            Component favoritesRow = createMenuRow(VaadinIcon.HEART_O, "Favoriten", () -> {
                dropdownMenu.getStyle().set("display", "none");
                UI.getCurrent().navigate("profile", QueryParameters.of("tab", "favorites"));
            }, null);

            Component profileRow = createMenuRow(VaadinIcon.USER, "Profil", () -> {
                dropdownMenu.getStyle().set("display", "none");
                UI.getCurrent().navigate("profile");
            }, null);

            dropdownMenu.add(createOfferRow, chatRow, notificationsRow, favoritesRow, profileRow);
        }
    }

    private Component createMenuRow(VaadinIcon icon, String labelText, Runnable clickAction, Component rightDecoration) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle()
                .set("background", "#fffbf3")
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "12px")
                .set("padding", "12px 18px")
                .set("cursor", "pointer")
                .set("box-sizing", "border-box");

        row.addClassName("menu-item-row");

        Icon menuIcon = new Icon(icon);
        menuIcon.getStyle()
                .set("color", DARK)
                .set("font-size", "20px");

        Span label = new Span(labelText);
        label.getStyle()
                .set("color", DARK)
                .set("font-weight", "700")
                .set("font-size", "16px");

        row.add(menuIcon, label);
        if (rightDecoration != null) {
            row.add(rightDecoration);
            row.setFlexGrow(1, label);
        }

        row.addClickListener(e -> clickAction.run());
        return row;
    }
}
