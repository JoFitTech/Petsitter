package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.booking.dto.BookingAcceptancePreview;
import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.offerrequest.domain.OfferRequest;
import com.softwareengineering.petsitter.offerrequest.domain.RequestStatus;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.chat.ProfilePopUp;
import com.softwareengineering.petsitter.user.service.UserService;
import java.math.BigDecimal;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.QueryParameters;

import java.util.UUID;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport(value = "./styles/custom-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport(value = "./styles/custom-readonly-field.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/custom-readonly-field.css", themeFor = "vaadin-text-area")
public class PetsitterDetailPopUp extends Dialog {

    private static final String DARK  = "#4a3428";
    private static final String BROWN = "#7b5236";

    private final OfferService offerService;
    private final RequestService requestService;
    private final ChatService chatService;
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final BookingService bookingService;

    public PetsitterDetailPopUp(OfferCardDto dto, String distance, int stars, OfferService offerService,
                                RequestService requestService, ChatService chatService,
                                AuthenticatedUser authenticatedUser, UserService userService,
                                BookingService bookingService) {
        this.offerService = offerService;
        this.requestService = requestService;
        this.chatService = chatService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.bookingService = bookingService;

        setWidth("520px");
        setCloseOnOutsideClick(true);
        this.getElement().getThemeList().add("no-padding");

        String topColor = OfferCardComponent.colorFor(dto.animalType());
        String date     = OfferCardComponent.formatDateRange(dto.startDate(), dto.endDate());
        String price    = OfferCardComponent.formatPrice(dto.price());
        String location = OfferCardComponent.formatLocation(dto.postalCode(), dto.city());

        // ── Outer container ───────────────────────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", "#f3eada")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("border-radius", "20px")
                .set("padding", "28px 28px 24px 28px")
                .set("gap", "16px");

        // ── Header: title + close button ──────────────────────────────────
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 dialogTitle = new H2("Auftragsdetails");
        dialogTitle.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("margin", "0")
                .set("color", DARK);

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("background", "transparent")
                .set("border", "none")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%");
        closeBtn.addClickListener(e -> close());

        header.add(dialogTitle, closeBtn);

        // ── Image area ────────────────────────────────────────────────────
        Div imageArea = new Div();
        imageArea.getStyle()
                .set("width", "100%")
                .set("height", "160px")
                .set("background", topColor)
                .set("border-radius", "16px")
                .set("position", "relative")
                .set("overflow", "hidden");

        Span starsBadge = new Span(buildStars(stars));
        starsBadge.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("left", "14px")
                .set("background", "rgba(74, 52, 40, 0.55)")
                .set("color", "#ffdf4a")
                .set("font-size", "14px")
                .set("letter-spacing", "2px")
                .set("border-radius", "14px")
                .set("padding", "6px 14px");
        imageArea.add(starsBadge);

        // ── Offer title & Creator Link ────────────────────────────────────
        H3 offerTitle = new H3(dto.title());
        offerTitle.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("margin", "4px 0 0 0")
                .set("color", DARK);

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);
        titleLayout.getStyle().set("gap", "4px");
        titleLayout.add(offerTitle);

        Component creatorLink = createCreatorLink(dto);
        if (creatorLink != null) {
            titleLayout.add(creatorLink);
        }

        // ── Facts row: Zeitraum | Verdienst | Entfernung ──────────────────
        Div factsRow = new Div();
        factsRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "14px 18px")
                .set("gap", "0")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        factsRow.add(
                factColumn("Zeitraum",   date),
                factDivider(),
                factColumn("Verdienst",  price),
                factDivider(),
                factColumn("Entfernung", distance)
        );

        // ── Info row: Betreuungsart | Häufigkeit (read-only text) ─────────
        String careLabel      = dto.careType()  != null ? dto.careType().label()  : "–";
        String frequencyLabel = dto.frequency() != null ? dto.frequency().label() : "–";

        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.setAlignItems(FlexComponent.Alignment.CENTER);
        infoRow.getStyle().set("gap", "8px").set("flex-wrap", "wrap");

        Span careSpan = new Span(careLabel);
        careSpan.getStyle()
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("font-size", "15px");

        Div infoSep = new Div();
        infoSep.getStyle()
                .set("width", "1px").set("height", "22px")
                .set("background", "#ccc").set("margin", "0 8px");

        Span freqSpan = new Span(frequencyLabel);
        freqSpan.getStyle()
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("font-size", "15px");

        infoRow.add(careSpan, infoSep, freqSpan);

        // ── Tier-Feld: dynamisch je nach OWNER_OFFER vs SITTER_OFFER ──────
        content.add(header, imageArea, titleLayout, factsRow, infoRow);

        if (dto.petName() != null) {
            // OWNER_OFFER: zeige konkretes Haustier
            StringBuilder petValue = new StringBuilder(dto.petName());
            if (dto.petSpecies() != null || dto.petBreed() != null) {
                petValue.append(" (");
                if (dto.petSpecies() != null) petValue.append(dto.petSpecies());
                if (dto.petSpecies() != null && dto.petBreed() != null) petValue.append(", ");
                if (dto.petBreed() != null) petValue.append(dto.petBreed());
                petValue.append(")");
            }
            content.add(readOnlyTextField(dto.petName().contains(",") ? "Haustiere" : "Haustier", petValue.toString()));
            if (dto.petTags() != null && !dto.petTags().isBlank()) {
                content.add(readOnlyTextField("Eigenschaften", dto.petTags()));
            }
        } else if (dto.animalType() != null) {
            // SITTER_OFFER: zeige bevorzugte Tierart
            content.add(readOnlyTextField("Bevorzugte Tierart", dto.animalType().label()));
        }

        if (!location.isBlank()) {
            content.add(readOnlyTextField("Standort", location));
        }

        // ── Beschreibung ──────────────────────────────────────────────────
        TextArea zusatzField = new TextArea("Beschreibung");
        zusatzField.setWidthFull();
        zusatzField.setMinHeight("90px");
        zusatzField.setValue(dto.description() != null ? dto.description() : "");
        zusatzField.setReadOnly(true);
        zusatzField.getStyle()
                .set("background", "#f3eada")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("--lumo-secondary-text-color", "#7b5236")
                .set("--lumo-body-text-color", "#7b5236")
                .set("color", "#7b5236");
        content.add(zusatzField);

        Button actionButton = createActionButton(dto);
        content.add(actionButton);

        add(content);
    }

    private Button createActionButton(OfferCardDto dto) {
        boolean ownOffer = offerService.isCurrentUserOffer(dto.id());
        boolean editable = offerService.canCurrentUserEditOffer(dto.id());

        if (editable) {
            Button btn = styledButton("Auftrag bearbeiten");
            btn.addClickListener(e -> {
                close();
                UI ui = UI.getCurrent();
                if (ui != null) {
                    ui.navigate("auftrag-erstellen", QueryParameters.of("edit", dto.id().toString()));
                }
            });
            return btn;
        }

        if (ownOffer) {
            Button btn = styledButton("Nicht bearbeitbar");
            btn.setEnabled(false);
            btn.getStyle().set("background", "#d8cec6").set("color", "#7a6050").set("cursor", "default");
            return btn;
        }

        // Check existing request state for this offer
        UUID currentUserId = authenticatedUser.get()
                .map(com.softwareengineering.petsitter.user.domain.User::getId)
                .orElse(null);

        if (currentUserId != null) {
            RequestStatus existingStatus = requestService.findMyRequests(currentUserId).stream()
                    .filter(r -> r.getOffer().getOfferId().equals(dto.id()))
                    .map(OfferRequest::getStatus)
                    .findFirst()
                    .orElse(null);

            if (existingStatus == RequestStatus.PENDING) {
                Button btn = styledButton("Anfrage ausstehend");
                btn.setEnabled(false);
                btn.getStyle().set("background", "#d8cec6").set("color", "#7a6050").set("cursor", "default");
                return btn;
            }
            if (existingStatus == RequestStatus.ACCEPTED) {
                if (!offerService.isOfferOpen(dto.id())) {
                    Button btn = styledButton("Angebot gebucht");
                    btn.setEnabled(false);
                    btn.getStyle().set("background", "#d8cec6").set("color", "#7a6050").set("cursor", "default");
                    return btn;
                }
                // Offer is OPEN again (booking was cancelled) → allow re-requesting
            }
            if (existingStatus == RequestStatus.DENIED) {
                Button btn = styledButton("Anfrage abgelehnt");
                btn.setEnabled(false);
                btn.getStyle().set("background", "#d8cec6").set("color", "#7a6050").set("cursor", "default");
                btn.getElement().setAttribute("title", "Deine Anfrage wurde abgelehnt");
                return btn;
            }
        }

        Button btn = styledButton("Auftrag anfragen");
        btn.getStyle().set("background", BROWN);
        btn.addClickListener(e -> onAuftragAnfragenClicked(dto.id()));
        return btn;
    }

    private Component createCreatorLink(OfferCardDto dto) {
        if (dto.creatorUserId() == null || isBlank(dto.creatorDisplayName())) {
            return null;
        }

        Button nameButton = new Button(dto.creatorDisplayName());
        nameButton.getStyle()
                .set("background", "transparent")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("height", "auto")
                .set("min-width", "0")
                .set("color", BROWN)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("text-decoration", "underline")
                .set("cursor", "pointer")
                .set("margin", "0");
        nameButton.addClickListener(event -> userService.getPublicUserProfile(dto.creatorUserId())
                .ifPresentOrElse(
                        profile -> new ProfilePopUp(profile).open(),
                        () -> Notification.show("Profil konnte nicht geladen werden.")
                ));
        return nameButton;
    }

    private Button styledButton(String label) {
        Button btn = new Button(label);
        btn.setWidthFull();
        btn.getStyle()
                .set("background", "#774f35")
                .set("color", "white")
                .set("height", "52px")
                .set("border-radius", "28px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("margin-top", "8px");
        return btn;
    }

    private void onAuftragAnfragenClicked(UUID offerId) {
        UUID currentUserId = authenticatedUser.get()
                .map(com.softwareengineering.petsitter.user.domain.User::getId)
                .orElse(null);
        if (currentUserId == null) {
            Notification.show("Bitte melde dich an.");
            return;
        }

        try {
            BookingAcceptancePreview preview = bookingService.previewForOffer(offerId, currentUserId);
            if (!preview.sufficientBalance()) {
                openInsufficientBalanceDialog(preview);
                return;
            }
        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage());
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("440px");
        dialog.getElement().getThemeList().add("no-padding");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(true);
        dialogContent.setSpacing(false);
        dialogContent.getStyle()
                .set("background-color", "#f3eada")
                .set("padding", "32px")
                .set("border-radius", "20px")
                .set("font-family", "'Inter', sans-serif")
                .set("gap", "16px");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 dialogTitle = new H2("Anfrage senden");
        dialogTitle.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("margin", "0")
                .set("color", DARK);

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("background", "transparent")
                .set("border", "none")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%");
        closeBtn.addClickListener(e -> dialog.close());

        header.add(dialogTitle, closeBtn);

        TextArea messageArea = new TextArea("Nachricht (optional)");
        messageArea.setWidthFull();
        messageArea.setPlaceholder("Schreibe dem Anbieter eine kurze Nachricht...");
        messageArea.setMaxLength(1000);
        messageArea.getStyle()
                .set("--vaadin-input-field-background", "#FCF9F2")
                .set("--vaadin-input-field-border", "1px solid #efe4d3")
                .set("--vaadin-input-field-border-radius", "12px")
                .set("--vaadin-input-field-value-color", "#4a3428")
                .set("--lumo-secondary-text-color", "#4a3428")
                .set("--lumo-body-text-color", "#4a3428")
                .set("color", "#4a3428");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.getStyle().set("margin-top", "8px");

        Button submitBtn = new Button("Anfragen", e -> {
            try {
                var request = requestService.createRequest(offerId, currentUserId, messageArea.getValue());
                String conversationId = chatService.createConversationForRequest(
                        request.getId(), messageArea.getValue());
                dialog.close();
                close();
                UI ui = UI.getCurrent();
                if (ui != null) {
                    ui.navigate("chat", QueryParameters.of("conversation", conversationId));
                }
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage());
            }
        });
        submitBtn.getStyle()
                .set("background-color", BROWN)
                .set("color", "white")
                .set("border-radius", "24px")
                .set("padding", "0 28px")
                .set("height", "44px")
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("border", "none");

        buttons.add(submitBtn);
        dialogContent.add(header, messageArea, buttons);
        dialog.add(dialogContent);
        dialog.open();
    }

    private void openInsufficientBalanceDialog(BookingAcceptancePreview preview) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Buchung bestätigen");
        dialog.setWidth("460px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("gap", "12px");

        Paragraph explanation = new Paragraph(
                "Der Gesamtpreis wird von deinem Guthaben abgezogen und bis zur Auszahlung sicher in Treuhand gehalten.");
        explanation.getStyle().set("margin", "0").set("font-size", "14px").set("color", "#7a6050");

        content.add(
                explanation,
                paymentLine("Preis pro Tag", preview.pricePerDay()),
                paymentLine("Gesamtpreis", preview.totalPrice()),
                paymentLine("Dein Guthaben", preview.availableBalance()));

        Paragraph warning = new Paragraph("Dein Guthaben reicht für diese Buchung noch nicht aus.");
        warning.getStyle().set("margin", "0").set("color", "#9a4f36").set("font-weight", "700");
        Button walletButton = new Button("Guthaben aufladen", event -> {
            dialog.close();
            close();
            UI.getCurrent().navigate("profile", QueryParameters.of("tab", "wallet"));
        });
        walletButton.getStyle().set("background", DARK).set("color", "white").set("border-radius", "22px");
        content.add(warning, walletButton);
        content.add(new ExternalPaymentMethods());

        dialog.add(content);
        dialog.open();
    }

    private Component paymentLine(String label, BigDecimal amount) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Span key = new Span(label);
        key.getStyle().set("color", "#7a6050");
        Span value = new Span(amount.setScale(2) + " EUR");
        value.getStyle().set("font-weight", "800").set("color", DARK);
        row.add(key, value);
        return row;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private static VerticalLayout factColumn(String label, String value) {
        VerticalLayout col = new VerticalLayout();
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("flex", "1").set("align-items", "center");

        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "11px").set("color", "#9e8c7b");

        Span val = new Span(value);
        val.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", DARK);

        col.add(lbl, val);
        return col;
    }

    private static Div factDivider() {
        Div d = new Div();
        d.getStyle()
                .set("width", "1px")
                .set("height", "32px")
                .set("background", "#e0d5c8");
        return d;
    }

    private static TextField readOnlyTextField(String label, String value) {
        TextField field = new TextField(label);
        field.setWidthFull();
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        field.getStyle()
                .set("background", "#f3eada")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("--lumo-secondary-text-color", "#7b5236")
                .set("--lumo-body-text-color", "#7b5236")
                .set("color", "#7b5236");
        return field;
    }

    private static String buildStars(int filled) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < filled ? "★" : "☆");
        }
        return sb.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
