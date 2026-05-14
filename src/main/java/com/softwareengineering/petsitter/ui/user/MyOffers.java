package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.MyOfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.PetsitterDetailPopUp;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MyOffers extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";
    private static final String MUTED = "#8a7060";
    private static final String BORDER = "#ead5ae";

    private final OfferService offerService;

    public MyOffers(OfferService offerService) {
        this.offerService = offerService;

        setWidthFull();
        getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "36px")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
                .set("box-sizing", "border-box");

        List<MyOfferCardDto> offers = offerService.getCurrentUserOffers();
        add(buildHeader());
        add(offers.isEmpty() ? buildEmptyState() : buildCardsGrid(offers));
    }

    private Component buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "36px");

        H2 title = new H2("Meine Aufträge");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("color", DARK);

        row.add(title, createAddButton());
        return row;
    }

    private Button createAddButton() {
        Button addBtn = new Button("Auftrag anbieten", new Icon(VaadinIcon.PLUS));
        addBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", DARK)
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

        Div imagePlaceholder = new Div();
        imagePlaceholder.getStyle()
                .set("height", "160px")
                .set("width", "100%")
                .set("border-radius", "12px")
                .set("background", cardColor(offer))
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

        H3 title = new H3(offer.title());
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("font-size", "20px")
                .set("line-height", "1.2")
                .set("font-weight", "800")
                .set("color", DARK);

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
                buildDetailColumn("Zeitraum", formatDateRange(offer.startDate(), offer.endDate()), DARK),
                buildDetailColumn("Verdienst", formatPrice(offer.price()), "#a5663b"),
                buildDetailColumn("Status", statusLabel(offer.status()), DARK)
        );

        card.add(imagePlaceholder, title, subtitle, detailsRow);
        return card;
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

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("gap", "18px").set("padding", "8px");

        H2 title = new H2("Was möchtest du erstellen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "24px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph copy = new Paragraph("Wähle aus, ob du Betreuung suchst oder selbst Betreuung anbieten möchtest.");
        copy.getStyle()
                .set("margin", "-8px 0 0 0")
                .set("font-size", "14px")
                .set("color", MUTED);

        content.add(
                title,
                copy,
                createDialogOption(dialog, VaadinIcon.HOME, "Betreuung für mein Haustier suchen",
                        "Erstelle einen Auftrag für dein Haustier.", "request"),
                createDialogOption(dialog, VaadinIcon.HEART, "Betreuung anbieten",
                        "Erstelle ein Angebot als Tiersitter.", "offer")
        );

        dialog.add(content);
        dialog.open();
    }

    private void openOfferDialog(MyOfferCardDto offer) {
        new PetsitterDetailPopUp(toOfferCardDto(offer), "–", 4, offerService).open();
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
                offer.careType(),
                offer.petName(),
                offer.petSpecies(),
                offer.petBreed()
        );
    }

    private Component createDialogOption(Dialog dialog, VaadinIcon iconType, String titleText, String description,
            String mode) {
        Button option = new Button();
        option.setWidthFull();
        option.getStyle()
                .set("height", "auto")
                .set("min-height", "82px")
                .set("padding", "18px")
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "14px")
                .set("background", "#fffdf8")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("text-align", "left");

        Icon icon = new Icon(iconType);
        icon.setSize("22px");
        icon.getStyle().set("color", DARK).set("flex-shrink", "0");

        VerticalLayout copy = new VerticalLayout();
        copy.setPadding(false);
        copy.setSpacing(false);
        copy.getStyle().set("gap", "4px");

        Span title = new Span(titleText);
        title.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", DARK);

        Span desc = new Span(description);
        desc.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "500")
                .set("color", MUTED);

        copy.add(title, desc);
        HorizontalLayout row = new HorizontalLayout(icon, copy);
        row.setPadding(false);
        row.setSpacing(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("gap", "14px");

        option.getElement().appendChild(row.getElement());
        option.addClickListener(event -> {
            dialog.close();
            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.navigate("auftrag-erstellen", QueryParameters.of("mode", mode));
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
        return offerType == OfferType.OWNER_OFFER ? "Auftrag" : "Angebot";
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
