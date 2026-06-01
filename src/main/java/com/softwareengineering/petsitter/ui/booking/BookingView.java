package com.softwareengineering.petsitter.ui.booking;

import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Buchungen | Petsitter")
@PermitAll
public class BookingView extends VerticalLayout {

    private static final String DARK      = "#4a3428";
    private static final String BROWN     = "#7b5236";
    private static final String CARD_BG   = "#ffffff";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final BookingService bookingService;
    private final AuthenticatedUser authenticatedUser;

    public BookingView(BookingService bookingService, AuthenticatedUser authenticatedUser) {
        this.bookingService = bookingService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("background", "#e8d9c8");

        add(buildDecorativeCircles());
        add(buildMainContent());
    }

    private Component buildDecorativeCircles() {
        Div circles = new Div();
        circles.getStyle()
                .set("position", "absolute").set("top", "0").set("left", "0")
                .set("width", "100%").set("height", "100%")
                .set("pointer-events", "none").set("z-index", "0");

        Div left = new Div();
        left.getStyle()
                .set("position", "absolute").set("width", "400px").set("height", "400px")
                .set("border-radius", "50%").set("background", "#e8ddd4")
                .set("opacity", "0.6").set("top", "-100px").set("left", "-100px");

        Div right = new Div();
        right.getStyle()
                .set("position", "absolute").set("width", "600px").set("height", "600px")
                .set("border-radius", "50%").set("background", "#c8dde6")
                .set("opacity", "0.6").set("top", "-200px").set("right", "-150px");

        circles.add(left, right);
        return circles;
    }

    private Component buildMainContent() {
        VerticalLayout main = new VerticalLayout();
        main.setWidthFull();
        main.setPadding(false);
        main.setSpacing(false);
        main.getStyle()
                .set("padding", "48px 48px 64px 48px")
                .set("position", "relative").set("z-index", "1")
                .set("box-sizing", "border-box")
                .set("max-width", "1200px").set("margin", "0 auto");

        main.add(buildPageHeader());
        main.add(buildBookingsList());
        return main;
    }

    private Component buildPageHeader() {
        Div wrapper = new Div();
        wrapper.getStyle().set("margin-bottom", "32px");

        H1 title = new H1("Meine Buchungen");
        title.getStyle()
                .set("margin", "0 0 6px 0").set("font-size", "28px")
                .set("font-weight", "800").set("color", DARK);

        Paragraph subtitle = new Paragraph(
                "Hier siehst du alle deine aktiven und vergangenen Betreuungsvereinbarungen.");
        subtitle.getStyle()
                .set("margin", "0").set("font-size", "14px").set("color", "#7a6050");

        wrapper.add(title, subtitle);
        return wrapper;
    }

    private Component buildBookingsList() {
        Optional<User> currentUser = authenticatedUser.get();

        if (currentUser.isEmpty()) {
            return buildEmptyState("Du bist nicht angemeldet.", VaadinIcon.USER);
        }

        List<BookingDto> bookings = bookingService.getBookings(currentUser.get().getId());

        if (bookings.isEmpty()) {
            return buildEmptyState(
                    "Du hast noch keine Buchungen. Erstelle ein Angebot oder sende eine Anfrage!",
                    VaadinIcon.CALENDAR_CLOCK);
        }

        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setPadding(false);
        list.setSpacing(false);
        list.getStyle().set("gap", "16px");

        for (BookingDto dto : bookings) {
            list.add(buildBookingCard(dto, currentUser.get().getId()));
        }
        return list;
    }

    private Component buildBookingCard(BookingDto dto, java.util.UUID currentUserId) {
        Div card = new Div();
        card.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "16px")
                .set("box-shadow", "0 4px 18px rgba(74,52,40,0.09)")
                .set("padding", "24px 28px")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px");

        // Header row: Title + Status Badge
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 offerTitle = new H2(dto.offerTitle());
        offerTitle.getStyle()
                .set("margin", "0").set("font-size", "18px")
                .set("font-weight", "700").set("color", DARK);

        Span badge = buildStatusBadge(dto.status());

        headerRow.add(offerTitle, badge);

        // Info row: Persons & Pet
        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.setSpacing(false);
        infoRow.getStyle().set("gap", "28px").set("flex-wrap", "wrap");

        infoRow.add(infoChip(VaadinIcon.USER, "Halter: " + dto.ownerName()));
        infoRow.add(infoChip(VaadinIcon.HEART, "Betreuer: " + dto.sitterName()));
        if (dto.petName() != null) {
            infoRow.add(infoChip(VaadinIcon.RECORDS, "Tier: " + dto.petName()));
        }

        // Date & Price row
        HorizontalLayout dateRow = new HorizontalLayout();
        dateRow.setSpacing(false);
        dateRow.getStyle().set("gap", "28px").set("flex-wrap", "wrap");

        dateRow.add(infoChip(VaadinIcon.CALENDAR,
                dto.startDate().format(DATE_FMT) + " – " + dto.endDate().format(DATE_FMT)));

        if (dto.pricePerWeek() != null) {
            dateRow.add(infoChip(VaadinIcon.EURO,
                    dto.pricePerWeek().toPlainString() + " EUR / Woche"));
        }

        card.add(headerRow, infoRow, dateRow);

        // Cancel-Button nur für CREATED-Bookings
        if (dto.status() == BookingStatus.CREATED) {
            Div buttonRow = new Div();
            buttonRow.getStyle().set("display", "flex").set("justify-content", "flex-end")
                    .set("margin-top", "4px");

            Button cancelBtn = new Button("Buchung stornieren", new Icon(VaadinIcon.CLOSE));
            cancelBtn.getStyle()
                    .set("background", "#fee2e2")
                    .set("color", "#b91c1c")
                    .set("border-radius", "10px")
                    .set("font-weight", "600")
                    .set("border", "none")
                    .set("cursor", "pointer")
                    .set("padding", "8px 18px");

            cancelBtn.addClickListener(e -> {
                try {
                    bookingService.cancelBooking(dto.id(), currentUserId);

                    // Nur die betroffene Karte aktualisieren statt die gesamte Seite neu zu laden.
                    applyStatusBadge(badge, BookingStatus.CANCELLED);
                    card.remove(buttonRow);

                    Notification ok = Notification.show("Buchung storniert.");
                    ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception ex) {
                    Notification err = Notification.show("Fehler: " + ex.getMessage());
                    err.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            buttonRow.add(cancelBtn);
            card.add(buttonRow);
        }

        return card;
    }

    private Span buildStatusBadge(BookingStatus status) {
        Span badge = new Span();
        badge.getStyle()
                .set("padding", "4px 14px")
                .set("border-radius", "20px")
                .set("font-size", "12px")
                .set("font-weight", "700");

        applyStatusBadge(badge, status);
        return badge;
    }

    private void applyStatusBadge(Span badge, BookingStatus status) {
        switch (status) {
            case CREATED -> {
                badge.setText("Aktiv");
                badge.getStyle().set("background", "#dcfce7").set("color", "#15803d");
            }
            case CANCELLED -> {
                badge.setText("Storniert");
                badge.getStyle().set("background", "#fee2e2").set("color", "#b91c1c");
            }
            case COMPLETED -> {
                badge.setText("Abgeschlossen");
                badge.getStyle().set("background", "#e0e7ff").set("color", "#3730a3");
            }
        }
    }

    private Component infoChip(VaadinIcon iconType, String text) {
        HorizontalLayout chip = new HorizontalLayout();
        chip.setAlignItems(FlexComponent.Alignment.CENTER);
        chip.setSpacing(false);
        chip.getStyle().set("gap", "6px");

        Icon icon = new Icon(iconType);
        icon.setSize("14px");
        icon.getStyle().set("color", BROWN);

        Span label = new Span(text);
        label.getStyle().set("font-size", "14px").set("color", "#5a4030");

        chip.add(icon, label);
        return chip;
    }

    private Component buildEmptyState(String message, VaadinIcon iconType) {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(FlexComponent.Alignment.CENTER);
        empty.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        empty.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "16px")
                .set("box-shadow", "0 4px 18px rgba(74,52,40,0.09)")
                .set("padding", "64px 32px")
                .set("margin-top", "16px");

        Icon icon = new Icon(iconType);
        icon.setSize("48px");
        icon.getStyle().set("color", "#c8b8a8").set("margin-bottom", "16px");

        Paragraph msg = new Paragraph(message);
        msg.getStyle()
                .set("font-size", "16px").set("color", "#7a6050")
                .set("text-align", "center").set("margin", "0");

        empty.add(icon, msg);
        return empty;
    }
}
