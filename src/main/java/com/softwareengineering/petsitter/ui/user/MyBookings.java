package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.dto.BookingDto;
import com.softwareengineering.petsitter.booking.service.BookingService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MyBookings extends Div {

    private static final String DARK    = "#4a3428";
    private static final String CARD_BG = "#ffffff";
    private static final String MUTED   = "#8a7060";
    private static final String BORDER  = "#ead5ae";

    private final BookingService bookingService;
    private final AuthenticatedUser authenticatedUser;
    private final Div bookingsContainer = new Div();

    public MyBookings(BookingService bookingService, AuthenticatedUser authenticatedUser) {
        this.bookingService = bookingService;
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

    private void renderBookings() {
        bookingsContainer.removeAll();
        UUID userId = authenticatedUser.get().map(u -> u.getId()).orElse(null);
        if (userId == null) {
            bookingsContainer.add(buildEmptyState("Bitte melde dich an."));
            return;
        }
        List<BookingDto> bookings = bookingService.getBookings(userId);
        if (bookings.isEmpty()) {
            bookingsContainer.add(buildEmptyState());
        } else {
            bookingsContainer.add(buildCardsGrid(bookings, userId));
        }
    }

    private Component buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("margin-bottom", "36px");

        H2 title = new H2("Meine Buchungen");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("color", DARK);

        row.add(title);
        return row;
    }

    private Component buildCardsGrid(List<BookingDto> bookings, UUID currentUserId) {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                .set("gap", "24px");

        bookings.forEach(b -> grid.add(buildBookingCard(b, currentUserId)));
        return grid;
    }

    private Component buildBookingCard(BookingDto dto, UUID currentUserId) {
        boolean isOwner = dto.ownerId().equals(currentUserId);
        String roleLabel   = isOwner ? "Auftraggeber" : "Tiersitter";
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
        chatBtn.addClickListener(e -> UI.getCurrent().navigate("chat"));

        card.add(colorBar, titleRow, subtitle);
        if (petSpan != null) card.add(petSpan);
        card.add(detailsRow);

        if (dto.status() == BookingStatus.CREATED) {
            Span cancelSpan = buildCancelRow(dto, card);
            card.add(chatBtn, cancelSpan);
        } else {
            card.add(chatBtn);
        }

        return card;
    }

    private Span buildCancelRow(BookingDto dto, Div card) {
        Span cancelLink = new Span("Buchung stornieren");
        cancelLink.getStyle()
                .set("display", "block")
                .set("margin-top", "10px")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", "#9a4f36")
                .set("cursor", "pointer")
                .set("text-align", "center")
                .set("text-decoration", "underline");
        cancelLink.addClickListener(e -> {
            UUID userId = authenticatedUser.get().map(u -> u.getId()).orElse(null);
            if (userId == null) return;
            try {
                bookingService.cancelBooking(dto.id(), userId);
                Notification ok = Notification.show("Buchung storniert.");
                ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                renderBookings();
            } catch (Exception ex) {
                Notification err = Notification.show("Fehler: " + ex.getMessage());
                err.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return cancelLink;
    }

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

        Div copy = new Div();
        H3 heading = new H3("Noch keine Buchungen");
        heading.getStyle().set("margin", "0 0 6px 0").set("font-size", "20px").set("font-weight", "800").set("color", DARK);
        Paragraph text = new Paragraph(message);
        text.getStyle().set("margin", "0").set("font-size", "14px").set("color", MUTED);
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
