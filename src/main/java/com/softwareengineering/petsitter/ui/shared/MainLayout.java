package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.chat.service.ChatEventBus;
import com.softwareengineering.petsitter.chat.service.Registration;
import com.softwareengineering.petsitter.notification.domain.Notification;
import com.softwareengineering.petsitter.notification.domain.NotificationType;
import com.softwareengineering.petsitter.notification.service.NotificationService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.user.LoginView;
import com.vaadin.flow.component.Component;
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
    private Button findOwnerBtn;
    private Button findSitterBtn;
    private Span mailBadge;
    private Span mailTypingIndicator;
    private Registration badgeRegistration;
    private Registration typingHeaderRegistration;
    private final ScheduledExecutorService typingHeaderScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> hideTypingHeaderFuture;

    private static final DateTimeFormatter NOTIFICATION_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    public MainLayout(
            AuthenticatedUser authenticatedUser,
            NotificationService notificationService,
            ChatEventBus chatEventBus
    ) {
        this.authenticatedUser = authenticatedUser;
        this.notificationService = notificationService;
        this.chatEventBus = chatEventBus;

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

        header.getStyle()
                .set("background", "rgba(255,255,255,0.92)")
                .set("backdrop-filter", "blur(8px)")
                .set("padding", "0 28px")
                .set("height", "72px")
                .set("box-shadow", "0 2px 18px rgba(74, 52, 40, 0.07)")
                .set("box-sizing", "border-box");

        // Left: Logo (PNG image – rahmenlos via Div)
        Image logoImg = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        logoImg.getStyle()
                .set("height", "52px")
                .set("width", "auto")
                .set("display", "block");

        Div logoWrapper = new Div(logoImg);
        logoWrapper.getStyle()
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center");
        logoWrapper.addClickListener(e -> UI.getCurrent().navigate(""));

        // Center: Navigation Pills + fixed Create-Offer button
        HorizontalLayout centerGroup = new HorizontalLayout();
        centerGroup.setSpacing(false);
        centerGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        centerGroup.getStyle().set("gap", "12px");

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
                    .set("padding", "0 22px")
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

        HorizontalLayout rightIcons = new HorizontalLayout();
        rightIcons.setSpacing(false);
        rightIcons.setAlignItems(FlexComponent.Alignment.CENTER);
        rightIcons.getStyle().set("gap", "8px");

        // Mail button with unread badge (for Chat)
        Component mailBtnWithBadge = buildMailButtonWithBadge();
        rightIcons.add(mailBtnWithBadge);

        Button heartBtn = headerIconButton(VaadinIcon.HEART_O, "transparent", DARK);
        heartBtn.addClickListener(e -> UI.getCurrent().navigate("profile", com.vaadin.flow.router.QueryParameters.of("tab", "favorites")));

        Button profileBtn = headerIconButton(VaadinIcon.USER, "#8db3c3", "white");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        rightIcons.add(heartBtn, profileBtn);
        return rightIcons;
    }

    private Component buildMailButtonWithBadge() {
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

        mailTypingIndicator = new Span("...");
        mailTypingIndicator.getStyle()
                .set("position", "absolute")
                .set("bottom", "-4px")
                .set("right", "-8px")
                .set("padding", "0 6px")
                .set("height", "16px")
                .set("line-height", "16px")
                .set("border-radius", "10px")
                .set("background", "#8db3c3")
                .set("color", "white")
                .set("font-size", "10px")
                .set("font-weight", "700")
                .set("display", "none");
        container.add(mailTypingIndicator);

        return container;
    }

    private void updateMailBadge(Span badge) {
        long unread = authenticatedUser.get()
                .map(user -> notificationService.countUnread(user.getId()))
                .orElse(0L);

        if (unread <= 0) {
            badge.setText("");
            badge.getStyle().set("display", "none");
            return;
        }

        badge.setText(unread > 99 ? "99+" : Long.toString(unread));
        badge.getStyle().set("display", "flex");
    }

    private void openNotificationDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Benachrichtigungen");
        dialog.setWidth("480px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        List<Notification> inbox = authenticatedUser.get()
                .map(user -> notificationService.getInbox(user.getId()))
                .orElse(List.of());

        if (inbox.isEmpty()) {
            content.add(new Paragraph("Keine Benachrichtigungen vorhanden."));
        } else {
            Map<LocalDate, List<Notification>> grouped = groupNotificationsByDate(inbox);
            for (Map.Entry<LocalDate, List<Notification>> entry : grouped.entrySet()) {
                Span groupHeader = new Span(toDateGroupLabel(entry.getKey()));
                groupHeader.getStyle()
                        .set("font-weight", "700")
                        .set("font-size", "13px")
                        .set("color", "#7a6050")
                        .set("margin", "8px 0 2px 0");
                content.add(groupHeader);

                for (Notification notification : entry.getValue()) {
                    content.add(buildNotificationRow(dialog, notification));
                }
            }
        }

        Button chatButton = new Button("Chat öffnen", e -> {
            dialog.close();
            UI.getCurrent().navigate("chat");
        });

        dialog.add(content);
        dialog.getFooter().add(chatButton);
        dialog.open();
    }

    private Component buildNotificationRow(Dialog dialog, Notification notification) {
        Button row = new Button();
        row.setWidthFull();
        row.getStyle()
                .set("text-align", "left")
                .set("justify-content", "flex-start")
                .set("background", notification.isRead() ? "#ffffff" : "#fff4de")
                .set("border", notification.isRead() ? "1px solid #ead5ae" : "1px solid #e2b56b")
                .set("border-left", notification.isRead() ? "1px solid #ead5ae" : "5px solid #e2b56b")
                .set("border-radius", "10px")
                .set("padding", "10px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        Span message = new Span(notification.getMessage());
        message.getStyle().set("font-weight", notification.isRead() ? "500" : "700");

        Span meta = new Span(notification.getType().name() + " • "
                + notification.getCreatedAt().format(NOTIFICATION_TIME_FORMAT));
        meta.getStyle().set("font-size", "12px").set("color", "#7a6050");

        if (!notification.isRead()) {
            Span unreadDot = new Span();
            unreadDot.getStyle()
                    .set("width", "8px")
                    .set("height", "8px")
                    .set("border-radius", "50%")
                    .set("display", "inline-block")
                    .set("background", "#d98b2b")
                    .set("margin-bottom", "6px");
            layout.add(unreadDot);
        }

        layout.add(message, meta);
        row.setIcon(layout);

        row.addClickListener(e -> {
            authenticatedUser.get().ifPresent(user -> notificationService.markAsRead(notification.getId(), user.getId()));
            dialog.close();

            if (notification.getType() == NotificationType.CHAT_MESSAGE
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
        dialog.getElement().getStyle()
                .set("border-radius", "20px")
                .set("padding", "0")
                .set("font-family", "Inter, Arial, sans-serif");

        // Wrapper with position:relative so X can be placed absolute
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("padding", "24px 24px 22px 24px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "14px")
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
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        footer.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("padding", "34px 76px")
                .set("box-sizing", "border-box")
                .set("margin-top", "auto");

        // Brand column
        VerticalLayout brand = new VerticalLayout();
        brand.setPadding(false);
        brand.setSpacing(false);

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
        links.setSpacing(true);
        links.getStyle().set("gap", "28px");

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

        Button facebookBtn = socialButton("f");
        facebookBtn.addClickListener(e -> {
            // TODO: UI.getCurrent().getPage().open("https://facebook.com/pawsitter");
        });

        Button instagramBtn = socialButton("◎");
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
                .set("padding", "0 36px")
                .set("border-radius", "28px")
                .set("background", NAV_INACTIVE_BG)
                .set("border", "1px solid " + NAV_BORDER)
                .set("color", DARK)
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
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

    private Button socialButton(String value) {
        Button btn = new Button(value);
        btn.getStyle()
                .set("width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("background", "#87b2c3")
                .set("color", "white")
                .set("font-weight", "800")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        return btn;
    }
}
