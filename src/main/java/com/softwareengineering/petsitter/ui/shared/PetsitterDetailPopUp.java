package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;

import java.util.UUID;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Reusable "Auftragsdetails" popup dialog.
 *
 * <p>Usage from any view:</p>
 * <pre>
 *   new PetsitterDetailPopUp(
 *       offerId, "Wochenende mit Nala",
 *       "28.–30. Juni", "160 €", "4,2 km",
 *       "#94b883", 4
 *   ).open();
 * </pre>
 */
public class PetsitterDetailPopUp extends Dialog {

    private static final String DARK  = "#4a3428";
    private static final String BROWN = "#7b5236";

    /**
     * @param offerId   Unique ID of the offer (used in backend hooks)
     * @param title     Display title, e.g. "Wochenende mit Nala"
     * @param date      Date range string, e.g. "28.–30. Juni"
     * @param price     Price string, e.g. "160 €"
     * @param distance  Distance string, e.g. "4,2 km"
     * @param topColor  CSS colour for the image area, e.g. "#94b883"
     * @param stars     Number of filled stars (0–5)
     */
    public PetsitterDetailPopUp(UUID offerId, String title, String date,
                                String price, String distance,
                                String topColor, int stars) {
        setWidth("520px");
        setCloseOnOutsideClick(true);
        this.getElement().getThemeList().add("no-padding");

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
        H3 offerTitle = new H3(title);
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

        // ── Radio buttons: service type + frequency ───────────────────────
        HorizontalLayout radioRow = new HorizontalLayout();
        radioRow.setAlignItems(FlexComponent.Alignment.CENTER);
        radioRow.getStyle().set("gap", "8px").set("flex-wrap", "wrap");

        RadioButtonGroup<String> serviceType = new RadioButtonGroup<>();
        serviceType.setItems("Tiersitting", "Haussitting");
        serviceType.setValue("Tiersitting");
        applyRadioStyle(serviceType);
        serviceType.addValueChangeListener(e ->
                System.out.println("Servicetyp gewählt: " + e.getValue()));

        Div radioSep = new Div();
        radioSep.getStyle()
                .set("width", "1px").set("height", "22px")
                .set("background", "#ccc").set("margin", "0 8px");

        RadioButtonGroup<String> frequency = new RadioButtonGroup<>();
        frequency.setItems("einmalig", "regelmäßig");
        frequency.setValue("einmalig");
        applyRadioStyle(frequency);
        frequency.addValueChangeListener(e ->
                System.out.println("Häufigkeit gewählt: " + e.getValue()));

        radioRow.add(serviceType, radioSep, frequency);

        // ── Input fields ──────────────────────────────────────────────────
        TextField haustierField = styledTextField("Meine Haustiere");
        haustierField.addValueChangeListener(e ->
                System.out.println("Haustiere eingegeben: " + e.getValue()));

        TextField haustierartField = styledTextField("Haustierarten");
        haustierartField.addValueChangeListener(e ->
                System.out.println("Haustierarten eingegeben: " + e.getValue()));

        TextArea zusatzField = new TextArea("Zusatzinfos");
        zusatzField.setWidthFull();
        zusatzField.setMinHeight("90px");
        zusatzField.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("font-family", "Inter, Arial, sans-serif");
        zusatzField.addValueChangeListener(e ->
                System.out.println("Zusatzinfos eingegeben: " + e.getValue()));

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
            onAuftragAnfragenClicked(offerId,
                    serviceType.getValue(), frequency.getValue(),
                    haustierField.getValue(), haustierartField.getValue(),
                    zusatzField.getValue());
            close();
        });

        content.add(header, imageArea, offerTitle, factsRow,
                radioRow, haustierField, haustierartField, zusatzField, anfragenBtn);
        add(content);
    }

    // ── Backend-Interface hook ────────────────────────────────────────────
    /**
     * Called when the user clicks "Auftrag anfragen".
     * TODO: Backend-Team ersetzt den Print durch einen echten Service-Call.
     */
    private void onAuftragAnfragenClicked(UUID offerId, String serviceType,
                                          String frequency, String haustiere,
                                          String haustierarten, String zusatzinfos) {
        System.out.println("Auftrag anfragen geklickt für Offer-ID: " + offerId);
        System.out.println("  Servicetyp:    " + serviceType);
        System.out.println("  Häufigkeit:    " + frequency);
        System.out.println("  Haustiere:     " + haustiere);
        System.out.println("  Haustierarten: " + haustierarten);
        System.out.println("  Zusatzinfos:   " + zusatzinfos);
        // TODO: requestService.createRequest(offerId, serviceType, frequency,
        //           haustiere, haustierarten, zusatzinfos);
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

    private void applyRadioStyle(RadioButtonGroup<String> group) {
        group.getStyle()
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("font-size", "15px");
    }

    private static TextField styledTextField(String label) {
        TextField field = new TextField(label);
        field.setWidthFull();
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
