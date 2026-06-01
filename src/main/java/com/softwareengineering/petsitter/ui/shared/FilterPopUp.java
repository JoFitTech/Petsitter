package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dependency.CssImport;

import java.util.Set;
import java.util.function.Consumer;

/**
 * FilterPopUp – Popup-Dialog für Filteroptionen in der PetsitterFilterView.
 *
 * <p>Verwendung:</p>
 * <pre>
 *   new FilterPopUp(criteria, onApply).open();
 * </pre>
 */
@CssImport(value = "./styles/custom-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
public class FilterPopUp extends Dialog {

    private static final String DARK      = "#4a3428";
    private static final String BROWN     = "#7b5236";
    private static final String CREAM_BG  = "#f3eada";
    private static final String CARD_BG   = "#ffffff";
    private static final String BORDER    = "#e8d8bf";
    private static final String STAR_COLOR = "#f5a623";

    /** Currently selected minimum star rating (0-5); 0 means no local rating selection. */
    private int selectedStarRating = 0;

    private final Consumer<AdditionalFilters> onApply;

    public FilterPopUp() {
        this(null, filters -> { });
    }

    public FilterPopUp(OfferSearchCriteria initialCriteria, Consumer<AdditionalFilters> onApply) {
        this.onApply = onApply != null ? onApply : filters -> { };

        setWidth("520px");
        setCloseOnOutsideClick(true);
        getElement().getStyle().set("border-radius", "24px");
        getElement().getThemeList().add("no-padding");

        // ── Outer container ────────────────────────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", CREAM_BG)
                .set("border-radius", "20px")
                .set("padding", "28px 28px 24px 28px")
                .set("gap", "18px")
                .set("font-family", "'Inter', sans-serif");

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

        // ── Section 1: Art der Betreuung ──────────────────────────────────
        Div betreuungSection = buildCard();

        Span betreuungLabel = sectionLabel("Art der Betreuung");

        ComboBox<OfferCareType> betreuungBox = new ComboBox<>();
        betreuungBox.setItems(OfferCareType.values());
        betreuungBox.setItemLabelGenerator(OfferCareType::label);
        betreuungBox.setPlaceholder("Egal");
        betreuungBox.setClearButtonVisible(true);
        betreuungBox.setWidthFull();
        applyComboStyle(betreuungBox);
        if (initialCriteria != null && initialCriteria.careType() != null) {
            betreuungBox.setValue(initialCriteria.careType());
        }

        betreuungSection.add(betreuungLabel, betreuungBox);

        // ── Section 2: Tierarten ──────────────────────────────────────────
        Div tierartenSection = buildCard();

        Span tierartenLabel = sectionLabel("Tierarten");

        MultiSelectComboBox<OfferAnimalType> tierartenBox = new MultiSelectComboBox<>();
        tierartenBox.setItems(OfferAnimalType.values());
        tierartenBox.setItemLabelGenerator(OfferAnimalType::label);
        tierartenBox.setPlaceholder("Egal");
        tierartenBox.setClearButtonVisible(true);
        tierartenBox.setWidthFull();
        applyMultiSelectComboStyle(tierartenBox);
        if (initialCriteria != null && !initialCriteria.animalTypes().isEmpty()) {
            tierartenBox.setValue(initialCriteria.animalTypes());
        }

        tierartenSection.add(tierartenLabel, tierartenBox);

        // ── Section 3: Dienstleistung ─────────────────────────────────────
        Div dienstleistungSection = buildCard();

        Span dienstleistungLabel = sectionLabel("Dienstleistung");

        ComboBox<OfferFrequency> dienstleistungBox = new ComboBox<>();
        dienstleistungBox.setItems(OfferFrequency.values());
        dienstleistungBox.setItemLabelGenerator(OfferFrequency::label);
        dienstleistungBox.setPlaceholder("Egal");
        dienstleistungBox.setClearButtonVisible(true);
        dienstleistungBox.setWidthFull();
        applyComboStyle(dienstleistungBox);
        if (initialCriteria != null && initialCriteria.frequency() != null) {
            dienstleistungBox.setValue(initialCriteria.frequency());
        }

        dienstleistungSection.add(dienstleistungLabel, dienstleistungBox);

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
                .set("background", "#fff6e6")
                .set("color", DARK)
                .set("border", "1.5px solid #e9d5ae")
                .set("border-radius", "28px")
                .set("height", "48px")
                .set("padding", "0 24px")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        resetBtn.addClickListener(e -> {
            betreuungBox.clear();
            tierartenBox.clear();
            dienstleistungBox.clear();
            selectedStarRating = 0;
            refreshStarRow(starRow);
        });

        Button applyBtn = new Button("Filter anwenden");
        applyBtn.getStyle()
                .set("background", BROWN)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("height", "48px")
                .set("padding", "0 28px")
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        applyBtn.addClickListener(e -> {
            close();
            this.onApply.accept(new AdditionalFilters(
                    betreuungBox.getValue(),
                    dienstleistungBox.getValue(),
                    tierartenBox.getValue()));
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
                    .set("border", "none")
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

    private void applyComboStyle(ComboBox<?> comboBox) {
        comboBox.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("font-size", "14px")
                .set("background", CARD_BG);
    }

    private void applyMultiSelectComboStyle(MultiSelectComboBox<?> comboBox) {
        comboBox.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-weight", "700")
                .set("color", BROWN)
                .set("font-size", "14px")
                .set("background", CARD_BG);
    }

    public record AdditionalFilters(
            OfferCareType careType,
            OfferFrequency frequency,
            Set<OfferAnimalType> animalTypes
    ) {
        public AdditionalFilters {
            animalTypes = animalTypes == null ? Set.of() : Set.copyOf(animalTypes);
        }
    }
}
