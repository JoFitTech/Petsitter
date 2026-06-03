package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.offer.dto.MyOfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.ui.shared.PetsitterDetailPopUp;
import com.softwareengineering.petsitter.ui.shared.ImageComponents;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.user.service.UserService;
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
import com.vaadin.flow.router.QueryParameters;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RolesAllowed("ROLE_SIGNED_IN_USER")
public class MyOffers extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";
    private static final String MUTED = "#8a7060";
    private static final String BORDER = "#ead5ae";

    private final OfferService offerService;
    private final RequestService requestService;
    private final ChatService chatService;
    private final BookingService bookingService;
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final Div offersContainer = new Div();

    private String activeStatusFilter = "ALLE";  // ALLE | OFFEN | GEBUCHT | VERGANGEN
    private String activeTypeFilter   = "ALLE";  // ALLE | HALTER | SITTER

    public MyOffers(OfferService offerService, RequestService requestService,
                    ChatService chatService, BookingService bookingService,
                    AuthenticatedUser authenticatedUser, UserService userService) {
        this.offerService = offerService;
        this.requestService = requestService;
        this.chatService = chatService;
        this.bookingService = bookingService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;

        setWidthFull();
        getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "36px")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
                .set("box-sizing", "border-box");

        offersContainer.setWidthFull();
        add(buildHeader(), offersContainer);
        renderOffers();
    }

    private void renderOffers() {
        offersContainer.removeAll();
        List<MyOfferCardDto> all = offerService.getCurrentUserOffers();

        List<MyOfferCardDto> filtered = all.stream()
                .filter(this::matchesStatusFilter)
                .filter(this::matchesTypeFilter)
                .sorted(Comparator.comparing(
                        (MyOfferCardDto o) -> o.startDate() != null ? o.startDate() : LocalDate.MIN).reversed())
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            offersContainer.add(buildEmptyState());
            return;
        }

        if ("ALLE".equals(activeStatusFilter)) {
            List<MyOfferCardDto> expired  = filtered.stream().filter(this::isExpiredOpenOffer).toList();
            List<MyOfferCardDto> regular  = filtered.stream().filter(o -> !isExpiredOpenOffer(o)).toList();
            if (!regular.isEmpty())  offersContainer.add(buildCardsGrid(regular));
            if (!expired.isEmpty())  offersContainer.add(buildExpiredSection(expired));
        } else {
            offersContainer.add(buildCardsGrid(filtered));
        }
    }

    private boolean matchesStatusFilter(MyOfferCardDto o) {
        return switch (activeStatusFilter) {
            case "OFFEN"     -> o.status() == OfferStatus.OPEN && (o.startDate() == null || !o.startDate().isBefore(LocalDate.now()));
            case "GEBUCHT"   -> o.status() == OfferStatus.BOOKED;
            case "VERGANGEN" -> o.startDate() != null && o.startDate().isBefore(LocalDate.now());
            default          -> true;
        };
    }

    private boolean matchesTypeFilter(MyOfferCardDto o) {
        return switch (activeTypeFilter) {
            case "HALTER" -> o.offerType() == OfferType.OWNER_OFFER;
            case "SITTER" -> o.offerType() == OfferType.SITTER_OFFER;
            default       -> true;
        };
    }

    private Component buildHeader() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.getStyle().set("margin-bottom", "36px").set("gap", "16px");

        // Title row
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 title = new H2("Meine Aufträge");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("color", DARK);
        titleRow.add(title, createAddButton());

        // Filter/sort row
        HorizontalLayout controls = new HorizontalLayout();
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.getStyle().set("gap", "12px").set("flex-wrap", "wrap");

        Select<String> statusSelect = new Select<>();
        statusSelect.setItems("Alle", "Offen", "Gebucht", "Vergangen");
        statusSelect.setValue("Alle");
        statusSelect.setLabel("Status");
        statusSelect.getStyle().set("min-width", "120px");
        statusSelect.addValueChangeListener(e -> {
            activeStatusFilter = switch (e.getValue()) {
                case "Offen"     -> "OFFEN";
                case "Gebucht"   -> "GEBUCHT";
                case "Vergangen" -> "VERGANGEN";
                default          -> "ALLE";
            };
            renderOffers();
        });

        Select<String> typeSelect = new Select<>();
        typeSelect.setItems("Alle", "Halter", "Sitter");
        typeSelect.setValue("Alle");
        typeSelect.setLabel("Perspektive");
        typeSelect.getStyle().set("min-width", "120px");
        typeSelect.addValueChangeListener(e -> {
            activeTypeFilter = switch (e.getValue()) {
                case "Halter" -> "HALTER";
                case "Sitter" -> "SITTER";
                default       -> "ALLE";
            };
            renderOffers();
        });

        controls.add(statusSelect, typeSelect);
        wrapper.add(titleRow, controls);
        return wrapper;
    }

    private Button createAddButton() {
        Button addBtn = new Button("Auftrag anbieten", new Icon(VaadinIcon.PLUS));
        addBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#774f35")
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("padding", "0 20px")
                .set("height", "40px")
                .set("cursor", "pointer");
        addBtn.addClickListener(e -> openCreateOfferDialog());
        return addBtn;
    }

    private Component buildCardsGrid(List<MyOfferCardDto> offers) {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                .set("gap", "24px");

        offers.forEach(offer -> grid.add(buildOfferCard(offer)));
        return grid;
    }

    private Component buildExpiredSection(List<MyOfferCardDto> offers) {
        Div section = new Div();
        section.setWidthFull();
        section.getStyle()
                .set("margin-top", "34px");

        H3 heading = new H3("Abgelaufene offene Angebote");
        heading.getStyle()
                .set("margin", "0 0 6px 0")
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph copy = new Paragraph("Diese offenen Einträge haben ein Startdatum in der Vergangenheit.");
        copy.getStyle()
                .set("margin", "0 0 18px 0")
                .set("font-size", "14px")
                .set("color", MUTED);

        section.add(heading, copy, buildCardsGrid(offers));
        return section;
    }

    private Component buildEmptyState() {
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
                .set("justify-content", "space-between")
                .set("gap", "24px")
                .set("flex-wrap", "wrap");

        Div copy = new Div();
        H3 heading = new H3("Noch keine Aufträge oder Angebote");
        heading.getStyle()
                .set("margin", "0 0 6px 0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph text = new Paragraph("Erstelle deinen ersten Eintrag, um ihn hier in der Übersicht zu sehen.");
        text.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", MUTED);
        copy.add(heading, text);

        empty.add(copy, createAddButton());
        return empty;
    }

    private Component buildOfferCard(MyOfferCardDto offer) {
        Div card = new Div();
        card.getStyle()
                .set("background", "#ffffff")
                .set("border-radius", "20px")
                .set("box-shadow", "0 8px 24px rgba(74,52,40,0.06)")
                .set("padding", "20px")
                .set("box-sizing", "border-box")
                .set("cursor", "pointer");
        card.addClickListener(event -> openOfferDialog(offer));

        Div imagePlaceholder = ImageComponents.offerCover(offer.coverTiles(), "160px", cardColor(offer));
        imagePlaceholder.getStyle()
                .set("width", "100%")
                .set("border-radius", "12px")
                .set("position", "relative")
                .set("margin-bottom", "20px");

        Span typeBadge = badge(typeLabel(offer.offerType()), "#ffffff", DARK);
        typeBadge.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("left", "12px");

        Span statusBadge = badge(statusLabel(offer.status()), statusBackground(offer.status()), statusColor(offer.status()));
        statusBadge.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px");

        imagePlaceholder.add(typeBadge, statusBadge);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getStyle().set("gap", "12px").set("margin", "0 0 16px 0");

        H3 title = new H3(offer.title());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("line-height", "1.2")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("min-width", "0");
        titleRow.add(title);

        if (offer.status() == OfferStatus.OPEN) {
            titleRow.add(createDeleteButton(offer));
        }

        Span subtitle = new Span(subtitleFor(offer));
        subtitle.getStyle()
                .set("display", "block")
                .set("margin", "-8px 0 18px 0")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", MUTED);

        HorizontalLayout detailsRow = new HorizontalLayout();
        detailsRow.setWidthFull();
        detailsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        detailsRow.getStyle().set("gap", "12px");
        detailsRow.add(
                buildDetailColumn("Zeitraum", OfferCardComponent.formatSchedule(
                        offer.frequency(), offer.startDate(), offer.endDate(),
                        offer.recurringWeekdays(), offer.timeSlot()), DARK),
                buildDetailColumn("Verdienst", formatPrice(offer.price()), "#a5663b"),
                buildDetailColumn("Status", statusLabel(offer.status()), DARK)
        );

        card.add(imagePlaceholder, titleRow, subtitle, detailsRow);

        if (offer.status() == OfferStatus.BOOKED) {
            Span cancelLink = new Span("Buchung stornieren");
            cancelLink.getStyle()
                    .set("display", "block")
                    .set("margin-top", "14px")
                    .set("font-size", "13px")
                    .set("font-weight", "600")
                    .set("color", "#9a4f36")
                    .set("cursor", "pointer")
                    .set("text-align", "center")
                    .set("text-decoration", "underline");
            cancelLink.getElement().executeJs("this.addEventListener('click', event => event.stopPropagation());");
            cancelLink.addClickListener(e -> {
                UUID userId = authenticatedUser.get().map(u -> u.getId()).orElse(null);
                if (userId == null) return;
                Optional<UUID> bookingId = bookingService.findActiveBookingIdForOffer(offer.id());
                if (bookingId.isEmpty()) {
                    Notification.show("Keine aktive Buchung gefunden.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                try {
                    bookingService.cancelBooking(bookingId.get(), userId);
                    Notification n = Notification.show("Buchung storniert.");
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    renderOffers();
                } catch (Exception ex) {
                    Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            card.add(cancelLink);
        }

        return card;
    }

    private Button createDeleteButton(MyOfferCardDto offer) {
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.setAriaLabel("Offer löschen");
        deleteBtn.getStyle()
                .set("border-radius", "50%")
                .set("background", "#f4e0d8")
                .set("color", "#9a4f36")
                .set("box-shadow", "none")
                .set("height", "36px")
                .set("width", "36px")
                .set("min-width", "36px")
                .set("padding", "0")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        deleteBtn.getElement().executeJs("this.addEventListener('click', event => event.stopPropagation());");
        deleteBtn.addClickListener(event -> openDeleteConfirmDialog(offer));
        return deleteBtn;
    }

    private void openDeleteConfirmDialog(MyOfferCardDto offer) {
        Dialog confirm = new Dialog();
        confirm.setWidth("400px");
        confirm.getElement().getThemeList().add("no-padding");
        confirm.getElement().getStyle()
                .set("border-radius", "20px")
                .set("font-family", "Inter, Arial, sans-serif");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("padding", "32px 48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
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
        closeBtn.addClickListener(e -> confirm.close());

        H3 title = new H3("Offer löschen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "21px")
                .set("font-weight", "800")
                .set("line-height", "1.2")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("padding-right", "32px")
                .set("color", DARK);

        Paragraph message = new Paragraph("Möchtest du \"" + offer.title()
                + "\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.");
        message.getStyle()
                .set("margin", "0")
                .set("font-size", "13.5px")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("line-height", "1.5")
                .set("color", "#9a8070");

        Button delete = new Button("Löschen");
        delete.setWidthFull();
        delete.getStyle()
                .set("height", "48px")
                .set("border-radius", "24px")
                .set("background", "#5c3d1e")
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-size", "15px")
                .set("box-shadow", "0 2px 8px rgba(74,52,40,0.1)")
                .set("cursor", "pointer")
                .set("border", "none");
        delete.addClickListener(event -> {
            try {
                offerService.deleteCurrentUserOffer(offer.id());
                confirm.close();
                Notification.show("Offer wurde gelöscht.", 2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                renderOffers();
            } catch (RuntimeException exception) {
                Notification.show("Offer konnte nicht gelöscht werden: " + exception.getMessage(),
                        4000,
                        Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        wrapper.add(closeBtn, title, message, delete);
        confirm.add(wrapper);
        confirm.open();
    }


    private Component buildDetailColumn(String labelText, String valueText, String valueColor) {
        VerticalLayout col = new VerticalLayout();
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("gap", "4px").set("min-width", "0");

        Span label = new Span(labelText);
        label.getStyle()
                .set("font-size", "12px")
                .set("color", "#a08060")
                .set("font-weight", "600");

        Span value = new Span(valueText);
        value.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", valueColor)
                .set("white-space", "normal");

        col.add(label, value);
        return col;
    }

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
                .set("padding", "32px 48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "14px")
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
                .set("box-sizing", "border-box");

        // X close button – absolute, top-right corner
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

        H2 title = new H2("Was möchtest du erstellen?");
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

    private void openOfferDialog(MyOfferCardDto offer) {
        OfferCardDto dto = toOfferCardDto(offer);
        new PetsitterDetailPopUp(dto, "–",
                OfferCardComponent.starsForAverage(dto.creatorAverageRating()),
                offerService, requestService, chatService, authenticatedUser, userService, bookingService).open();
    }

    private boolean isExpiredOpenOffer(MyOfferCardDto offer) {
        return offer.status() == OfferStatus.OPEN
                && offer.frequency() != OfferFrequency.REGULAR
                && offer.startDate() != null
                && offer.startDate().isBefore(LocalDate.now());
    }

    private OfferCardDto toOfferCardDto(MyOfferCardDto offer) {
        return new OfferCardDto(
                offer.id(),
                offer.title(),
                offer.startDate(),
                offer.endDate(),
                offer.price(),
                offer.animalType(),
                false,
                offer.description(),
                offer.frequency(),
                offer.recurringWeekdays(),
                offer.timeSlot(),
                offer.careType(),
                offer.petName(),
                offer.petSpecies(),
                offer.petBreed(),
                offer.petTags(),
                offer.pets(),
                null,
                null,
                null,
                false,
                offer.offerType(),
                null,
                null,
                offer.coverTiles(),
                null,
                null,
                null
        );
    }

    private Component createDialogOption(Dialog dialog, VaadinIcon iconType, String titleText, String description,
            String mode) {
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
        icon.getStyle().set("color", DARK).set("flex-shrink", "0");

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
                java.util.Map<String, List<String>> params = new java.util.LinkedHashMap<>();
                params.put("mode", List.of(mode));
                params.put("returnTo", List.of("/profile?tab=offers"));
                ui.navigate("auftrag-erstellen", new QueryParameters(params));
            }
        });
        return option;
    }

    private Span badge(String text, String background, String color) {
        Span badge = new Span(text);
        badge.getStyle()
                .set("background", background)
                .set("color", color)
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "6px 12px")
                .set("border-radius", "20px");
        return badge;
    }

    private String subtitleFor(MyOfferCardDto offer) {
        if (offer.offerType() == OfferType.OWNER_OFFER) {
            if (offer.petName() != null && offer.petSpecies() != null) {
                return offer.petName() + " (" + offer.petSpecies() + ")";
            }
            if (offer.petName() != null) {
                return offer.petName();
            }
            return "Haustierbetreuung gesucht";
        }
        if (offer.animalType() != null) {
            return "Betreuung für " + offer.animalType().label();
        }
        return "Betreuung angeboten";
    }

    private String typeLabel(OfferType offerType) {
        return offerType == OfferType.OWNER_OFFER ? "Halter" : "Sitter";
    }

    private String statusLabel(OfferStatus status) {
        if (status == null) {
            return "Unbekannt";
        }
        return switch (status) {
            case OPEN -> "Offen";
            case BOOKED -> "Gebucht";
            case CANCELLED -> "Storniert";
        };
    }

    private String statusBackground(OfferStatus status) {
        if (status == OfferStatus.BOOKED) {
            return "#e7f0f0";
        }
        if (status == OfferStatus.CANCELLED) {
            return "#f4e0d8";
        }
        return "#edf7e8";
    }

    private String statusColor(OfferStatus status) {
        if (status == OfferStatus.BOOKED) {
            return "#37636b";
        }
        if (status == OfferStatus.CANCELLED) {
            return "#9a4f36";
        }
        return "#4f7f45";
    }

    private String cardColor(MyOfferCardDto offer) {
        if (offer.offerType() == OfferType.OWNER_OFFER) {
            return "#d8ecd8";
        }
        if (offer.animalType() == null) {
            return "#f1dfb9";
        }
        return switch (offer.animalType()) {
            case DOG -> "#dec18d";
            case CAT -> "#f1b47a";
            case BIRD -> "#93b8c9";
            case SMALL_ANIMAL -> "#94b883";
            case REPTILE -> "#a8c89b";
            case FISH -> "#bad6df";
            case OTHER -> "#f1dfb9";
        };
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null) {
            return "–";
        }
        DateTimeFormatter day = DateTimeFormatter.ofPattern("d", Locale.GERMAN);
        DateTimeFormatter dayMonth = DateTimeFormatter.ofPattern("d. MMM", Locale.GERMAN);
        DateTimeFormatter month = DateTimeFormatter.ofPattern("MMMM", Locale.GERMAN);
        if (end == null || start.equals(end)) {
            return start.format(DateTimeFormatter.ofPattern("d. MMMM", Locale.GERMAN));
        }
        if (start.getMonth() == end.getMonth()) {
            return start.format(day) + ".–" + end.format(day) + ". " + start.format(month);
        }
        return start.format(dayMonth) + " – " + end.format(dayMonth);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "–";
        }
        return price.stripTrailingZeros().toPlainString() + " €";
    }
}
