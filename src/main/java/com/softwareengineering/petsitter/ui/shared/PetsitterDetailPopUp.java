package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;

import java.util.UUID;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class PetsitterDetailPopUp extends Dialog {

    private static final String DARK  = "#4a3428";
    private static final String BROWN = "#7b5236";

    public PetsitterDetailPopUp(OfferCardDto dto, String distance, int stars) {
        setWidth("520px");
        setCloseOnOutsideClick(true);
        this.getElement().getThemeList().add("no-padding");

        String topColor = OfferCardComponent.colorFor(dto.animalType());
        String date     = OfferCardComponent.formatDateRange(dto.startDate(), dto.endDate());
        String price    = OfferCardComponent.formatPrice(dto.price());

        // ── Outer container ───────────────────────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", "#fdf8f0")
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
        closeBtn.getStyle()
                .set("background", "transparent")
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

        // ── Offer title ───────────────────────────────────────────────────
        H3 offerTitle = new H3(dto.title());
        offerTitle.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("margin", "4px 0 0 0")
                .set("color", DARK);

        // ── Facts row: Zeitraum | Verdienst | Entfernung ──────────────────
        Div factsRow = new Div();
        factsRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "14px 18px")
                .set("gap", "0");

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
        content.add(header, imageArea, offerTitle, factsRow, infoRow);

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
            content.add(readOnlyTextField("Haustier", petValue.toString()));
        } else if (dto.animalType() != null) {
            // SITTER_OFFER: zeige bevorzugte Tierart
            content.add(readOnlyTextField("Bevorzugte Tierart", dto.animalType().label()));
        }

        // ── Beschreibung ──────────────────────────────────────────────────
        TextArea zusatzField = new TextArea("Beschreibung");
        zusatzField.setWidthFull();
        zusatzField.setMinHeight("90px");
        zusatzField.setValue(dto.description() != null ? dto.description() : "");
        zusatzField.setReadOnly(true);
        zusatzField.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("font-family", "Inter, Arial, sans-serif");
        content.add(zusatzField);

        // ── "Auftrag anfragen" button ─────────────────────────────────────
        Button anfragenBtn = new Button("Auftrag anfragen");
        anfragenBtn.setWidthFull();
        anfragenBtn.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("height", "52px")
                .set("border-radius", "28px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("margin-top", "8px");
        anfragenBtn.addClickListener(e -> {
            onAuftragAnfragenClicked(dto.id());
            close();
        });
        content.add(anfragenBtn);

        add(content);
    }

    // ── Backend-Interface hook ────────────────────────────────────────────
    private void onAuftragAnfragenClicked(UUID offerId) {
        System.out.println("Auftrag anfragen geklickt für Offer-ID: " + offerId);
        // TODO: requestService.createRequest(offerId);
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
                .set("background", "white")
                .set("border-radius", "10px")
                .set("font-family", "Inter, Arial, sans-serif");
        return field;
    }

    private static String buildStars(int filled) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < filled ? "★" : "☆");
        }
        return sb.toString();
    }
}
