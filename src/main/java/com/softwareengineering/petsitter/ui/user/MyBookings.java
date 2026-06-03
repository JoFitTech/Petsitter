package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.booking.domain.BookingStatus;
import com.softwareengineering.petsitter.booking.dto.BookingDto;
import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.review.service.UserReviewService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.wallet.domain.PaymentStatus;
import com.softwareengineering.petsitter.wallet.domain.RecurringPaymentStatus;
import com.softwareengineering.petsitter.ui.shared.ImageComponents;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final UserReviewService userReviewService;
    private final AuthenticatedUser authenticatedUser;
    private final Div bookingsContainer = new Div();

    private String activeFilter = "ALLE";   // ALLE | AKTIV | STORNIERT | VERGANGEN
    private String activeSort   = "STARTDATUM"; // STARTDATUM | BUCHUNGSDATUM

    public MyBookings(
            BookingService bookingService,
            ChatService chatService,
            UserReviewService userReviewService,
            AuthenticatedUser authenticatedUser
    ) {
        this.bookingService = bookingService;
        this.chatService = chatService;
        this.userReviewService = userReviewService;
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
            case "AKTIV"     -> list.stream()
                    .filter(b -> b.status() == BookingStatus.CREATED && (isRecurring(b) || !isPast(b)))
                    .toList();
            case "STORNIERT" -> list.stream().filter(b -> b.status() == BookingStatus.CANCELLED).toList();
            case "VERGANGEN" -> list.stream()
                    .filter(b -> b.status() == BookingStatus.COMPLETED
                            || b.status() == BookingStatus.ENDED
                            || (b.status() == BookingStatus.CREATED && !isRecurring(b) && isPast(b)))
                    .toList();
            default          -> list; // ALLE
        };
    }

    private List<BookingDto> applySort(List<BookingDto> list) {
        Comparator<BookingDto> comparator = "BUCHUNGSDATUM".equals(activeSort)
                ? Comparator.comparing(b -> b.bookedAt() != null ? b.bookedAt() : java.time.LocalDateTime.MIN)
                : Comparator.comparing(b -> b.startDate() != null ? b.startDate() : LocalDate.MIN);
        return list.stream().sorted(comparator).collect(Collectors.toList());
    }

    private boolean isPast(BookingDto b) {
        if (isRecurring(b)) {
            return false;
        }
        LocalDate reference = b.endDate() != null ? b.endDate() : b.startDate();
        return reference != null && reference.isBefore(LocalDate.now());
    }

    private boolean hasStarted(BookingDto b) {
        if (isRecurring(b)) {
            return true;
        }
        return b.startDate() != null && !b.startDate().isAfter(LocalDate.now());
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

        Div colorBar = ImageComponents.offerCover(dto.coverTiles(), "120px", cardColor(dto.status()));
        colorBar.getStyle()
                .set("width", "100%")
                .set("border-radius", "12px")
                .set("position", "relative")
                .set("margin-bottom", "20px");

        Span roleBadge = badge(roleLabel, "#ffffff", DARK);
        roleBadge.getStyle().set("position", "absolute").set("top", "12px").set("left", "12px");

        Span statusBadge = badge(statusLabel(dto), statusBackground(dto), statusColor(dto));
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
        boolean recurring = isRecurring(dto);
        detailsRow.add(
                buildDetailColumn(recurring ? "Rhythmus" : "Zeitraum", formatBookingSchedule(dto), DARK),
                buildDetailColumn(recurring ? "Preis/Termin" : "Preis/Tag", formatPrice(dto.pricePerDay()), "#a5663b"),
                buildDetailColumn("Status", statusLabel(dto), DARK)
        );

        HorizontalLayout paymentRow = new HorizontalLayout();
        paymentRow.setWidthFull();
        paymentRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        paymentRow.getStyle().set("gap", "12px").set("margin-top", "14px");
        if (recurring) {
            paymentRow.add(
                    buildDetailColumn("Auszahlbar", recurringPayableLabel(dto), DARK),
                    buildDetailColumn("Wochenstatus",
                            recurringPaymentStatusLabel(dto.recurringPaymentStatus()),
                            recurringPaymentStatusColor(dto.recurringPaymentStatus()))
            );
        } else {
            paymentRow.add(
                    buildDetailColumn("Gesamtpreis", formatPrice(dto.totalPrice()), DARK),
                    buildDetailColumn("Zahlung", paymentStatusLabel(dto.paymentStatus()), paymentStatusColor(dto.paymentStatus()))
            );
        }

        Button chatBtn = new Button("Zum Chat", new Icon(VaadinIcon.CHAT));
        chatBtn.setWidthFull();
        chatBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#774f35")
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
        card.add(detailsRow, paymentRow);

        if (dto.status() == BookingStatus.CREATED) {
            card.add(chatBtn);
            if (recurring) {
                addRecurringActions(card, dto, currentUserId, isOwner);
            } else {
                addCompletionAction(card, dto, currentUserId);
                addPaymentActions(card, dto, currentUserId, isOwner);
                if (!hasStarted(dto)) {
                    card.add(buildCancelLink(dto));
                }
            }
        } else {
            card.add(chatBtn);
            addReviewAction(card, dto, currentUserId);
            card.add(buildHideLink(dto.id()));
        }

        return card;
    }

    private void addPaymentActions(Div card, BookingDto dto, UUID currentUserId, boolean isOwner) {
        if (dto.endDate() == null || !LocalDate.now().isAfter(dto.endDate())) {
            return;
        }
        if (dto.paymentStatus() == PaymentStatus.HELD) {
            if (isOwner) {
                card.add(actionButton("Auszahlung freigeben", () -> releasePayment(dto.id(), currentUserId)));
            } else {
                card.add(actionButton("Auszahlung anfordern", () -> requestPayment(dto.id(), currentUserId)));
            }
            return;
        }
        if (dto.paymentStatus() == PaymentStatus.RELEASE_REQUESTED) {
            Span deadline = new Span("Auszahlung angefordert · automatische Freigabe am "
                    + formatDateTime(dto.automaticReleaseAt()));
            deadline.getStyle()
                    .set("display", "block")
                    .set("margin-top", "14px")
                    .set("font-size", "12px")
                    .set("font-weight", "700")
                    .set("color", "#a5663b");
            card.add(deadline);
            if (isOwner) {
                card.add(actionButton("Auszahlung freigeben", () -> releasePayment(dto.id(), currentUserId)));
                card.add(actionButton("Problem melden", () -> Notification.show(
                        "Während der Entwicklungsphase noch nicht implementiert.")));
            }
        }
    }

    private void addRecurringActions(Div card, BookingDto dto, UUID currentUserId, boolean isOwner) {
        card.add(actionButton("Pause eintragen", () -> openPauseDialog(dto, currentUserId)));
        addRecurringPaymentActions(card, dto, currentUserId, isOwner);
        card.add(buildEndRecurringLink(dto));
    }

    private void addRecurringPaymentActions(Div card, BookingDto dto, UUID currentUserId, boolean isOwner) {
        RecurringPaymentStatus status = dto.recurringPaymentStatus();
        if (status == RecurringPaymentStatus.AWAITING_FUNDS) {
            addStatusNote(card, "Fuer mindestens eine Woche fehlt noch Guthaben im Wallet.", "#9a4f36");
        }
        if (dto.payableRecurringOccurrences() <= 0) {
            return;
        }
        if (status == RecurringPaymentStatus.HELD) {
            if (isOwner) {
                card.add(actionButton("Auszahlung freigeben",
                        () -> releaseRecurringPayments(dto.id(), currentUserId)));
            } else {
                card.add(actionButton("Auszahlung anfordern",
                        () -> requestRecurringPayment(dto.id(), currentUserId)));
            }
            return;
        }
        if (status == RecurringPaymentStatus.RELEASE_REQUESTED) {
            addStatusNote(card, "Auszahlung angefordert · automatische Freigabe am "
                    + formatDateTime(dto.recurringAutomaticReleaseAt()), "#a5663b");
            if (isOwner) {
                card.add(actionButton("Auszahlung freigeben",
                        () -> releaseRecurringPayments(dto.id(), currentUserId)));
            }
        }
    }

    private void openPauseDialog(BookingDto dto, UUID currentUserId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pause eintragen");
        dialog.setWidth("420px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("gap", "12px");

        DatePicker start = new DatePicker("Von");
        DatePicker end = new DatePicker("Bis");
        LocalDate today = LocalDate.now();
        start.setMin(today);
        end.setMin(today);
        start.setValue(today);
        end.setValue(today);
        start.setWidthFull();
        end.setWidthFull();
        start.addValueChangeListener(event -> {
            LocalDate value = event.getValue();
            if (value == null) {
                return;
            }
            end.setMin(value);
            if (end.getValue() == null || end.getValue().isBefore(value)) {
                end.setValue(value);
            }
        });

        Button submit = new Button("Pause speichern", event ->
                addRecurringPause(dto.id(), currentUserId, start.getValue(), end.getValue(), dialog));
        submit.setWidthFull();
        submit.getStyle()
                .set("border-radius", "24px")
                .set("background", DARK)
                .set("color", "white")
                .set("font-weight", "700");

        content.add(start, end, submit);
        dialog.add(content);
        dialog.open();
    }

    private void addRecurringPause(
            UUID bookingId,
            UUID currentUserId,
            LocalDate start,
            LocalDate end,
            Dialog dialog
    ) {
        try {
            bookingService.addRecurringPause(bookingId, currentUserId, start, end);
            dialog.close();
            Notification.show("Pause wurde eingetragen.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Span buildEndRecurringLink(BookingDto dto) {
        Span link = new Span("Regelmaessige Buchung beenden");
        styleActionLink(link, "#9a4f36");
        link.addClickListener(e -> {
            UUID userId = authenticatedUser.get().map(u -> u.getId()).orElse(null);
            if (userId == null) return;
            try {
                bookingService.endRecurringBooking(dto.id(), userId);
                Notification.show("Regelmaessige Buchung beendet.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                renderBookings();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return link;
    }

    private void releaseRecurringPayments(UUID bookingId, UUID currentUserId) {
        try {
            bookingService.releaseRecurringPayments(bookingId, currentUserId);
            Notification.show("Auszahlung wurde freigegeben.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void requestRecurringPayment(UUID bookingId, UUID currentUserId) {
        try {
            bookingService.requestRecurringPaymentRelease(bookingId, currentUserId);
            Notification.show("Auszahlung wurde angefordert.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void addStatusNote(Div card, String text, String color) {
        Span note = new Span(text);
        note.getStyle()
                .set("display", "block")
                .set("margin-top", "14px")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("color", color);
        card.add(note);
    }

    private void addCompletionAction(Div card, BookingDto dto, UUID currentUserId) {
        if (dto.endDate() == null || !LocalDate.now().isAfter(dto.endDate())) {
            return;
        }

        card.add(actionButton("Termin als abgeschlossen markieren",
                () -> completeBooking(dto.id(), currentUserId)));
    }

    private Button actionButton(String label, Runnable action) {
        Button button = new Button(label);
        button.setWidthFull();
        button.getStyle()
                .set("border-radius", "24px")
                .set("background", "#f6e3bd")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("font-weight", "700")
                .set("font-size", "13px")
                .set("height", "40px")
                .set("cursor", "pointer")
                .set("margin-top", "10px");
        button.addClickListener(event -> action.run());
        return button;
    }

    private void releasePayment(UUID bookingId, UUID currentUserId) {
        try {
            bookingService.releasePayment(bookingId, currentUserId);
            Notification.show("Auszahlung wurde freigegeben.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void requestPayment(UUID bookingId, UUID currentUserId) {
        try {
            bookingService.requestPaymentRelease(bookingId, currentUserId);
            Notification.show("Auszahlung wurde angefordert.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void completeBooking(UUID bookingId, UUID currentUserId) {
        try {
            bookingService.markBookingCompleted(bookingId, currentUserId);
            Notification.show("Termin als abgeschlossen markiert.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void addReviewAction(Div card, BookingDto dto, UUID currentUserId) {
        if (dto.status() != BookingStatus.COMPLETED) {
            return;
        }

        boolean alreadyReviewed;
        try {
            alreadyReviewed = userReviewService.hasUserReviewedBooking(dto.id(), currentUserId);
        } catch (Exception exception) {
            Notification.show("Bewertungsstatus konnte nicht geladen werden: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (alreadyReviewed) {
            Span done = new Span("Du hast diese Buchung bereits bewertet.");
            done.getStyle()
                    .set("display", "block")
                    .set("margin-top", "12px")
                    .set("font-size", "12px")
                    .set("font-weight", "700")
                    .set("color", "#4f7f45");
            card.add(done);
            return;
        }

        card.add(actionButton("Bewertung abgeben", () -> openReviewDialog(dto.id(), currentUserId)));
    }

    private void openReviewDialog(UUID bookingId, UUID currentUserId) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.getElement().getThemeList().add("no-padding");
        dialog.getElement().getStyle()
                .set("border-radius", "20px")
                .set("font-family", "'Inter', sans-serif");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
                .set("padding", "32px 48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("font-family", "'Inter', sans-serif")
                .set("position", "relative")
                .set("box-sizing", "border-box");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("position", "absolute")
                .set("top", "24px")
                .set("right", "24px")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("z-index", "10");
        closeBtn.addClickListener(e -> dialog.close());

        H2 title = new H2("Buchung bewerten");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("padding-right", "32px")
                .set("color", DARK);

        Select<Integer> rating = new Select<>();
        rating.setLabel("Sterne");
        rating.setItems(5, 4, 3, 2, 1);
        rating.setValue(5);
        rating.setWidthFull();

        TextArea comment = new TextArea("Kommentar (optional)");
        comment.setMaxLength(100);
        comment.setWidthFull();
        comment.setMinHeight("110px");

        Button submit = new Button("Bewertung speichern", e ->
                submitReview(bookingId, currentUserId, rating.getValue(), comment.getValue(), dialog));
        submit.setWidthFull();
        submit.getStyle()
                .set("border-radius", "24px")
                .set("background", "#5c3d1e")
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "15px")
                .set("height", "48px")
                .set("cursor", "pointer")
                .set("border", "none")
                .set("font-family", "'Inter', sans-serif");

        wrapper.add(closeBtn, title, rating, comment, submit);
        dialog.add(wrapper);
        dialog.open();
    }

    private void submitReview(UUID bookingId, UUID currentUserId, Integer rating, String comment, Dialog dialog) {
        if (rating == null) {
            Notification.show("Bitte waehle eine Sternebewertung aus.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            userReviewService.submitReview(bookingId, currentUserId, rating, comment);
            dialog.close();
            Notification.show("Bewertung gespeichert.")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            renderBookings();
        } catch (Exception exception) {
            Notification.show("Fehler: " + exception.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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

    private String statusLabel(BookingDto dto) {
        if (dto.status() == null) return "Unbekannt";
        return switch (dto.status()) {
            case CREATED   -> isPast(dto) ? "Vergangen" : "Aktiv";
            case CANCELLED -> "Storniert";
            case COMPLETED -> "Abgeschlossen";
            case ENDED     -> "Beendet";
        };
    }

    private String statusBackground(BookingDto dto) {
        if (dto.status() == BookingStatus.CANCELLED) return "#f4e0d8";
        if (dto.status() == BookingStatus.COMPLETED) return "#e0e7ff";
        if (dto.status() == BookingStatus.ENDED) return "#e8e8e8";
        if (isPast(dto)) return "#e8e8e8";
        return "#edf7e8";
    }

    private String statusColor(BookingDto dto) {
        if (dto.status() == BookingStatus.CANCELLED) return "#9a4f36";
        if (dto.status() == BookingStatus.COMPLETED) return "#3730a3";
        if (dto.status() == BookingStatus.ENDED) return "#666666";
        if (isPast(dto)) return "#666666";
        return "#4f7f45";
    }

    private String cardColor(BookingStatus status) {
        if (status == BookingStatus.CANCELLED) return "#f1dfb9";
        if (status == BookingStatus.COMPLETED) return "#c8dde6";
        if (status == BookingStatus.ENDED) return "#e5e0d8";
        return "#d8ecd8";
    }

    private boolean isRecurring(BookingDto dto) {
        return dto != null && dto.frequency() == OfferFrequency.REGULAR;
    }

    private String formatBookingSchedule(BookingDto dto) {
        return OfferCardComponent.formatSchedule(
                dto.frequency(),
                dto.startDate(),
                dto.endDate(),
                dto.recurringWeekdays(),
                dto.timeSlot());
    }

    private String recurringPayableLabel(BookingDto dto) {
        int count = dto.payableRecurringOccurrences();
        String unit = count == 1 ? "Termin" : "Termine";
        return count + " " + unit + " · " + formatPrice(dto.payableRecurringAmount());
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

    private String paymentStatusLabel(PaymentStatus status) {
        if (status == null) return "Unbekannt";
        return switch (status) {
            case HELD -> "In Treuhand";
            case RELEASE_REQUESTED -> "Auszahlung angefordert";
            case RELEASED -> "Ausgezahlt";
            case REFUNDED -> "Erstattet";
            case LEGACY_UNFUNDED -> "Legacy-Buchung";
        };
    }

    private String paymentStatusColor(PaymentStatus status) {
        if (status == PaymentStatus.RELEASED) return "#4f7f45";
        if (status == PaymentStatus.REFUNDED) return "#9a4f36";
        return "#a5663b";
    }

    private String recurringPaymentStatusLabel(RecurringPaymentStatus status) {
        if (status == null) return "Noch offen";
        return switch (status) {
            case AWAITING_FUNDS -> "Guthaben fehlt";
            case HELD -> "In Treuhand";
            case RELEASE_REQUESTED -> "Auszahlung angefordert";
            case RELEASED -> "Ausgezahlt";
            case REFUNDED -> "Erstattet";
            case SKIPPED -> "Pausiert";
        };
    }

    private String recurringPaymentStatusColor(RecurringPaymentStatus status) {
        if (status == RecurringPaymentStatus.RELEASED) return "#4f7f45";
        if (status == RecurringPaymentStatus.REFUNDED || status == RecurringPaymentStatus.AWAITING_FUNDS) {
            return "#9a4f36";
        }
        if (status == RecurringPaymentStatus.SKIPPED) return MUTED;
        return "#a5663b";
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) return "–";
        return value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
