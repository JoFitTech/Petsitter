package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.dto.BookingDto;
import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MyBookings extends Div {

    private static final String DARK    = "#4a3428";
    private static final String CARD_BG = "#ffffff";
    private static final String MUTED   = "#8a7060";
    private static final String BORDER  = "#ead5ae";
    private static final String SESSION_KEY_HIDDEN = "myBookings_hiddenIds";

    private final BookingService bookingService;
    private final ChatService chatService;
    private final AuthenticatedUser authenticatedUser;
    private final Div bookingsContainer = new Div();

    private String activeFilter = "ALLE";   // ALLE | AKTIV | STORNIERT | VERGANGEN
    private String activeSort   = "STARTDATUM"; // STARTDATUM | BUCHUNGSDATUM

    public MyBookings(BookingService bookingService, ChatService chatService, AuthenticatedUser authenticatedUser) {
        this.bookingService = bookingService;
        this.chatService = chatService;
        this.authenticatedUser = authenticatedUser;

        setWidthFull();
        getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "36px")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
                .set("box-sizing", "border-box");

        bookingsContainer.setWidthFull();
        add(buildHeader(), bookingsContainer);
        renderBookings();
    }

    // ── Hidden IDs persisted in VaadinSession ─────────────────────────────

    @SuppressWarnings("unchecked")
    private Set<UUID> hiddenIds() {
        Object attr = VaadinSession.getCurrent().getAttribute(SESSION_KEY_HIDDEN);
        if (attr instanceof Set) return (Set<UUID>) attr;
        Set<UUID> set = new HashSet<>();
        VaadinSession.getCurrent().setAttribute(SESSION_KEY_HIDDEN, set);
        return set;
    }

    private void hideBooking(UUID id) {
        hiddenIds().add(id);
        renderBookings();
    }

    // ── Render ────────────────────────────────────────────────────────────

    private void renderBookings() {
        bookingsContainer.removeAll();
        UUID userId = authenticatedUser.get().map(u -> u.getId()).orElse(null);
        if (userId == null) {
            bookingsContainer.add(buildEmptyState("Bitte melde dich an."));
            return;
        }

        List<BookingDto> all = bookingService.getBookings(userId);
        Set<UUID> hidden = hiddenIds();

        // Remove hidden
        List<BookingDto> visible = all.stream()
                .filter(b -> !hidden.contains(b.id()))
                .collect(Collectors.toList());

        // Apply filter
        List<BookingDto> filtered = applyFilter(visible);

        // Apply sort
        List<BookingDto> sorted = applySort(filtered);

        if (sorted.isEmpty()) {
            bookingsContainer.add(buildEmptyState());
        } else {
            bookingsContainer.add(buildCardsGrid(sorted, userId));
        }
    }

    private List<BookingDto> applyFilter(List<BookingDto> list) {
        return switch (activeFilter) {
            case "AKTIV"     -> list.stream().filter(b -> b.status() == BookingStatus.CREATED && !isPast(b)).toList();
            case "STORNIERT" -> list.stream().filter(b -> b.status() == BookingStatus.CANCELLED).toList();
            case "VERGANGEN" -> list.stream().filter(b -> b.status() == BookingStatus.COMPLETED || (b.status() == BookingStatus.CREATED && isPast(b))).toList();
            default          -> list; // ALLE
        };
    }

    private List<BookingDto> applySort(List<BookingDto> list) {
        Comparator<BookingDto> comparator = "BUCHUNGSDATUM".equals(activeSort)
                ? Comparator.comparing(b -> b.bookedAt() != null ? b.bookedAt() : java.time.LocalDateTime.MIN)
                : Comparator.comparing(b -> b.startDate() != null ? b.startDate() : LocalDate.MIN);
        return list.stream().sorted(comparator.reversed()).collect(Collectors.toList());
    }

    private boolean isPast(BookingDto b) {
        return b.startDate() != null && b.startDate().isBefore(LocalDate.now());
    }

    // ── Header with filter/sort controls ─────────────────────────────────

    private Component buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "36px").set("flex-wrap", "wrap").set("gap", "12px");

        H2 title = new H2("Meine Buchungen");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("color", DARK);

        HorizontalLayout controls = new HorizontalLayout();
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.getStyle().set("gap", "12px").set("flex-wrap", "wrap");

        Select<String> filterSelect = new Select<>();
        filterSelect.setItems("Alle", "Aktiv", "Storniert", "Vergangen");
        filterSelect.setValue("Alle");
        filterSelect.setLabel("Anzeigen");
        filterSelect.getStyle().set("min-width", "130px");
        filterSelect.addValueChangeListener(e -> {
            activeFilter = switch (e.getValue()) {
                case "Aktiv"     -> "AKTIV";
                case "Storniert" -> "STORNIERT";
                case "Vergangen" -> "VERGANGEN";
                default          -> "ALLE";
            };
            renderBookings();
        });

        Select<String> sortSelect = new Select<>();
        sortSelect.setItems("Nach Startdatum", "Nach Buchungsdatum");
        sortSelect.setValue("Nach Startdatum");
        sortSelect.setLabel("Sortierung");
        sortSelect.getStyle().set("min-width", "170px");
        sortSelect.addValueChangeListener(e -> {
            activeSort = "Nach Buchungsdatum".equals(e.getValue()) ? "BUCHUNGSDATUM" : "STARTDATUM";
            renderBookings();
        });

        controls.add(filterSelect, sortSelect);
        row.add(title, controls);
        return row;
    }

    // ── Grid ──────────────────────────────────────────────────────────────

    private Component buildCardsGrid(List<BookingDto> bookings, UUID currentUserId) {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                .set("gap", "24px");

        bookings.forEach(b -> grid.add(buildBookingCard(b, currentUserId)));
        return grid;
    }

    // ── Card ──────────────────────────────────────────────────────────────

    private Component buildBookingCard(BookingDto dto, UUID currentUserId) {
        boolean isOwner    = dto.ownerId().equals(currentUserId);
        String roleLabel   = isOwner ? "Tierhalter" : "Tiersitter";
        String partnerName = isOwner ? dto.sitterName() : dto.ownerName();
        String partnerKey  = isOwner ? "Betreuer" : "Auftraggeber";

        Div card = new Div();
        card.getStyle()
                .set("background", "#ffffff")
                .set("border-radius", "20px")
                .set("box-shadow", "0 8px 24px rgba(74,52,40,0.06)")
                .set("padding", "20px")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0");

        Div colorBar = new Div();
        colorBar.getStyle()
                .set("height", "120px")
                .set("width", "100%")
                .set("border-radius", "12px")
                .set("background", cardColor(dto.status()))
                .set("position", "relative")
                .set("margin-bottom", "20px");

        Span roleBadge = badge(roleLabel, "#ffffff", DARK);
        roleBadge.getStyle().set("position", "absolute").set("top", "12px").set("left", "12px");

        Span statusBadge = badge(statusLabel(dto.status()), statusBackground(dto.status()), statusColor(dto.status()));
        statusBadge.getStyle().set("position", "absolute").set("top", "12px").set("right", "12px");

        colorBar.add(roleBadge, statusBadge);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getStyle().set("gap", "12px").set("margin", "0 0 4px 0");

        H3 title = new H3(dto.offerTitle());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("min-width", "0");
        titleRow.add(title);

        Span subtitle = new Span(partnerKey + ": " + partnerName);
        subtitle.getStyle()
                .set("display", "block")
                .set("margin-bottom", "4px")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", MUTED);

        Span petSpan = null;
        if (dto.petName() != null) {
            petSpan = new Span("Tier: " + dto.petName());
            petSpan.getStyle()
                    .set("display", "block")
                    .set("margin-bottom", "16px")
                    .set("font-size", "13px")
                    .set("font-weight", "600")
                    .set("color", MUTED);
        } else {
            subtitle.getStyle().set("margin-bottom", "16px");
        }

        HorizontalLayout detailsRow = new HorizontalLayout();
        detailsRow.setWidthFull();
        detailsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        detailsRow.getStyle().set("gap", "12px");
        detailsRow.add(
                buildDetailColumn("Zeitraum",    formatDateRange(dto.startDate(), dto.endDate()), DARK),
                buildDetailColumn("Preis/Woche", formatPrice(dto.pricePerWeek()), "#a5663b"),
                buildDetailColumn("Status",      statusLabel(dto.status()), DARK)
        );

        Button chatBtn = new Button("Zum Chat", new Icon(VaadinIcon.CHAT));
        chatBtn.setWidthFull();
        chatBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", DARK)
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("font-size", "13px")
                .set("height", "40px")
                .set("cursor", "pointer")
                .set("margin-top", "16px");
        chatBtn.addClickListener(e -> {
            Optional<String> convId = chatService.getConversationIdForBooking(dto.id());
            if (convId.isPresent()) {
                UI.getCurrent().navigate("chat", QueryParameters.of("conversation", convId.get()));
            } else {
                UI.getCurrent().navigate("chat");
            }
        });

        card.add(colorBar, titleRow, subtitle);
        if (petSpan != null) card.add(petSpan);
        card.add(detailsRow);

        if (dto.status() == BookingStatus.CREATED) {
            card.add(chatBtn, buildCancelLink(dto));
        } else {
            card.add(chatBtn, buildHideLink(dto.id()));
        }

        return card;
    }

    private Span buildCancelLink(BookingDto dto) {
        Span link = new Span("Buchung stornieren");
        styleActionLink(link, "#9a4f36");
        link.addClickListener(e -> {
            UUID userId = authenticatedUser.get().map(u -> u.getId()).orElse(null);
            if (userId == null) return;
            try {
                bookingService.cancelBooking(dto.id(), userId);
                Notification.show("Buchung storniert.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                renderBookings();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return link;
    }

    private Span buildHideLink(UUID bookingId) {
        Span link = new Span("Ausblenden");
        styleActionLink(link, MUTED);
        link.addClickListener(e -> hideBooking(bookingId));
        return link;
    }

    private void styleActionLink(Span link, String color) {
        link.getStyle()
                .set("display", "block")
                .set("margin-top", "10px")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", color)
                .set("cursor", "pointer")
                .set("text-align", "center")
                .set("text-decoration", "underline");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Component buildDetailColumn(String labelText, String valueText, String valueColor) {
        VerticalLayout col = new VerticalLayout();
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("gap", "4px").set("min-width", "0");

        Span label = new Span(labelText);
        label.getStyle().set("font-size", "12px").set("color", "#a08060").set("font-weight", "600");

        Span value = new Span(valueText);
        value.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", valueColor)
                .set("white-space", "normal");

        col.add(label, value);
        return col;
    }

    private Component buildEmptyState() {
        return buildEmptyState("Du hast noch keine Buchungen.");
    }

    private Component buildEmptyState(String message) {
        Div empty = new Div();
        empty.setWidthFull();
        empty.getStyle()
                .set("background", "#fffdf8")
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "16px")
                .set("padding", "32px")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "24px");

        H3 heading = new H3("Keine Buchungen");
        heading.getStyle().set("margin", "0 0 6px 0").set("font-size", "20px").set("font-weight", "800").set("color", DARK);
        Paragraph text = new Paragraph(message);
        text.getStyle().set("margin", "0").set("font-size", "14px").set("color", MUTED);

        Div copy = new Div();
        copy.add(heading, text);
        empty.add(copy);
        return empty;
    }

    private Span badge(String text, String background, String color) {
        Span b = new Span(text);
        b.getStyle()
                .set("background", background)
                .set("color", color)
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "6px 12px")
                .set("border-radius", "20px");
        return b;
    }

    private String statusLabel(BookingStatus status) {
        if (status == null) return "Unbekannt";
        return switch (status) {
            case CREATED   -> "Aktiv";
            case CANCELLED -> "Storniert";
            case COMPLETED -> "Abgeschlossen";
        };
    }

    private String statusBackground(BookingStatus status) {
        if (status == BookingStatus.CANCELLED) return "#f4e0d8";
        if (status == BookingStatus.COMPLETED) return "#e0e7ff";
        return "#edf7e8";
    }

    private String statusColor(BookingStatus status) {
        if (status == BookingStatus.CANCELLED) return "#9a4f36";
        if (status == BookingStatus.COMPLETED) return "#3730a3";
        return "#4f7f45";
    }

    private String cardColor(BookingStatus status) {
        if (status == BookingStatus.CANCELLED) return "#f1dfb9";
        if (status == BookingStatus.COMPLETED) return "#c8dde6";
        return "#d8ecd8";
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null) return "–";
        DateTimeFormatter day      = DateTimeFormatter.ofPattern("d", Locale.GERMAN);
        DateTimeFormatter dayMonth = DateTimeFormatter.ofPattern("d. MMM", Locale.GERMAN);
        DateTimeFormatter month    = DateTimeFormatter.ofPattern("MMMM", Locale.GERMAN);
        if (end == null || start.equals(end)) {
            return start.format(DateTimeFormatter.ofPattern("d. MMMM", Locale.GERMAN));
        }
        if (start.getMonth() == end.getMonth()) {
            return start.format(day) + ".–" + end.format(day) + ". " + start.format(month);
        }
        return start.format(dayMonth) + " – " + end.format(dayMonth);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "–";
        return price.stripTrailingZeros().toPlainString() + " €";
    }
}
