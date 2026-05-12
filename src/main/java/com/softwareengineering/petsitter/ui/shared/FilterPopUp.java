package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

import java.util.List;

/**
 * FilterPopUp – Popup-Dialog für Filteroptionen in der PetsitterFilterView.
 *
 * <p>Verwendung:</p>
 * <pre>
 *   new FilterPopUp().open();
 * </pre>
 */
public class FilterPopUp extends Dialog {

    private static final String DARK      = "#4a3428";
    private static final String BROWN     = "#7b5236";
    private static final String CREAM_BG  = "#fdf6ec";
    private static final String CARD_BG   = "#ffffff";
    private static final String BORDER    = "#e8d8bf";
    private static final String STAR_COLOR = "#f5a623";

    /** Currently selected minimum star rating (1–5). */
    private int selectedStarRating = 1;

    public FilterPopUp() {
        setWidth("520px");
        setCloseOnOutsideClick(true);
        getElement().getStyle().set("border-radius", "24px");

        // ── Outer container ────────────────────────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", CREAM_BG)
                .set("border-radius", "20px")
                .set("padding", "28px 28px 24px 28px")
                .set("gap", "18px");

        // ── Header: title + close button ──────────────────────────────────
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 dialogTitle = new H2("Filteroptionen");
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

        // ── Section 1: Art der Betreuung ──────────────────────────────────
        Div betreuungSection = buildCard();

        Span betreuungLabel = sectionLabel("Art der Betreuung");

        RadioButtonGroup<String> betreuungGroup = new RadioButtonGroup<>();
        betreuungGroup.setItems("Tiersitting", "Haussitting");
        betreuungGroup.setValue("Tiersitting");
        applyRadioStyle(betreuungGroup);
        betreuungGroup.getStyle().set("display", "flex").set("flex-direction", "row").set("gap", "32px");
        betreuungGroup.addValueChangeListener(e -> {
            System.out.println("Art der Betreuung gewählt: " + e.getValue());
            // TODO: filterService.setBetreuungsart(e.getValue());
        });

        betreuungSection.add(betreuungLabel, betreuungGroup);

        // ── Section 2: Tierarten ──────────────────────────────────────────
        Div tierartenSection = buildCard();

        Span tierartenLabel = sectionLabel("Tierarten");

        ComboBox<String> tierartenBox = new ComboBox<>();
        tierartenBox.setItems(List.of("Hunde", "Katzen", "Kleintiere", "Vögel", "Reptilien"));
        tierartenBox.setPlaceholder("");
        tierartenBox.setWidthFull();
        tierartenBox.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("background", CARD_BG);
        tierartenBox.addValueChangeListener(e -> {
            System.out.println("Tierart gewählt: " + e.getValue());
            // TODO: filterService.setTierart(e.getValue());
        });

        tierartenSection.add(tierartenLabel, tierartenBox);

        // ── Section 3: Dienstleistung ─────────────────────────────────────
        Div dienstleistungSection = buildCard();

        Span dienstleistungLabel = sectionLabel("Dienstleistung");

        RadioButtonGroup<String> dienstleistungGroup = new RadioButtonGroup<>();
        dienstleistungGroup.setItems("einmalig", "regelmäßig");
        dienstleistungGroup.setValue("einmalig");
        applyRadioStyle(dienstleistungGroup);
        dienstleistungGroup.getStyle().set("display", "flex").set("flex-direction", "row").set("gap", "32px");
        dienstleistungGroup.addValueChangeListener(e -> {
            System.out.println("Dienstleistung gewählt: " + e.getValue());
            // TODO: filterService.setDienstleistungstyp(e.getValue());
        });

        dienstleistungSection.add(dienstleistungLabel, dienstleistungGroup);

        // ── Section 4: Bewertung (min.) ───────────────────────────────────
        Div bewertungSection = buildCard();

        Span bewertungLabel = sectionLabel("Bewertung (min.)");

        // Interactive star rating row
        HorizontalLayout starRow = buildStarRow();

        bewertungSection.add(bewertungLabel, starRow);

        // ── Footer buttons ────────────────────────────────────────────────
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle().set("margin-top", "6px");

        Button resetBtn = new Button("Alle Filter zurücksetzen");
        resetBtn.getStyle()
                .set("background", "transparent")
                .set("color", DARK)
                .set("border", "1.5px solid " + BORDER)
                .set("border-radius", "28px")
                .set("height", "48px")
                .set("padding", "0 24px")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        resetBtn.addClickListener(e -> {
            System.out.println("Alle Filter zurückgesetzt");
            // TODO: filterService.resetAllFilters();
            betreuungGroup.setValue("Tiersitting");
            tierartenBox.clear();
            dienstleistungGroup.setValue("einmalig");
            selectedStarRating = 1;
            refreshStarRow(starRow);
        });

        Button applyBtn = new Button("Filter anwenden");
        applyBtn.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("height", "48px")
                .set("padding", "0 28px")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        applyBtn.addClickListener(e -> {
            System.out.println("Filter anwenden geklickt");
            System.out.println("  Art der Betreuung: " + betreuungGroup.getValue());
            System.out.println("  Tierart:           " + tierartenBox.getValue());
            System.out.println("  Dienstleistung:    " + dienstleistungGroup.getValue());
            System.out.println("  Bewertung min.:    " + selectedStarRating + " Sterne");
            // TODO: filterService.applyFilters(betreuungGroup.getValue(),
            //           tierartenBox.getValue(), dienstleistungGroup.getValue(),
            //           selectedStarRating);
            close();
        });

        footer.add(resetBtn, applyBtn);

        content.add(header, betreuungSection, tierartenSection, dienstleistungSection,
                bewertungSection, footer);
        add(content);
    }

    // ── Star row builder ──────────────────────────────────────────────────────
    private HorizontalLayout buildStarRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("gap", "12px").set("margin-top", "4px");
        refreshStarRow(row);
        return row;
    }

    /**
     * Clears and re-renders the star buttons based on {@link #selectedStarRating}.
     * Called on initial build and whenever the rating changes or is reset.
     */
    private void refreshStarRow(HorizontalLayout row) {
        row.removeAll();
        for (int i = 1; i <= 5; i++) {
            final int starValue = i;
            Button starBtn = new Button(i <= selectedStarRating ? "★" : "★");
            starBtn.getStyle()
                    .set("background", "transparent")
                    .set("box-shadow", "none")
                    .set("font-size", "32px")
                    .set("padding", "0")
                    .set("min-width", "unset")
                    .set("width", "40px")
                    .set("height", "40px")
                    .set("cursor", "pointer")
                    .set("color", i <= selectedStarRating ? STAR_COLOR : "#d9c8b0");
            starBtn.addClickListener(e -> {
                selectedStarRating = starValue;
                System.out.println("Mindest-Bewertung gewählt: " + selectedStarRating + " Sterne");
                // TODO: filterService.setMinRating(selectedStarRating);
                refreshStarRow(row);
            });
            row.add(starBtn);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private Div buildCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "16px")
                .set("padding", "18px 20px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "10px")
                .set("width", "100%")
                .set("box-sizing", "border-box");
        return card;
    }

    private Span sectionLabel(String text) {
        Span label = new Span(text);
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", DARK);
        return label;
    }

    private void applyRadioStyle(RadioButtonGroup<String> group) {
        group.getStyle()
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("font-size", "14px");
    }
}
