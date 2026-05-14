package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@CssImport(value = "./styles/filter-search-popover.css", themeFor = "vaadin-popover-overlay")
@CssImport(value = "./styles/filter-search-slider-bubble.css", themeFor = "vaadin-slider-bubble-overlay")
public class FilterSearchBar extends Div {

    private static final String DARK = "#4a3428";
    private static final String BROWN = "#7b5236";
    private static final String BORDER = "#e8ddd0";
    private static final String LABEL = "#9e8c7b";
    private static final String POPOVER_RADIUS = "32px";
    private static final Locale GERMAN = Locale.GERMANY;
    private static final DateTimeFormatter DAY_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("d. MMMM", GERMAN);
    private static final List<Integer> DISTANCE_VALUES = List.of(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            15, 20, 25, 30, 35, 40, 45, 50, 55, 60,
            65, 70, 75, 80, 85, 90, 95, 100
    );
    private static final List<Integer> DATE_FLEX_VALUES = List.of(0, 1, 2, 3, 7);

    private final Span whenValue;
    private final Span earningsValue;
    private final Span distanceValue;
    private final Popover whenPopover;
    private final Popover earningsPopover;
    private final Popover distancePopover;
    private final EarningsMode earningsMode;
    private final Consumer<SearchCriteria> onSearch;
    private final Function<String, Optional<String>> originPostalCodeValidator;

    private TextField originPostalCodeField;
    private LocalDate selectedFrom;
    private LocalDate selectedTo;
    private OfferDateFilterMode selectedDateFilterMode;
    private int selectedDateFlexDays;
    private BigDecimal selectedEarnings;
    private int selectedDistance;
    private String selectedOriginPostalCode;

    public FilterSearchBar(EarningsMode earningsMode, Consumer<SearchCriteria> onSearch) {
        this(earningsMode, defaultCriteria(), onSearch);
    }

    public FilterSearchBar(EarningsMode earningsMode, SearchCriteria initialCriteria,
            Consumer<SearchCriteria> onSearch) {
        this(earningsMode, initialCriteria, postalCode -> Optional.empty(), onSearch);
    }

    public FilterSearchBar(EarningsMode earningsMode, SearchCriteria initialCriteria,
            Function<String, Optional<String>> originPostalCodeValidator,
            Consumer<SearchCriteria> onSearch) {
        this.earningsMode = earningsMode;
        this.onSearch = onSearch;
        this.originPostalCodeValidator = originPostalCodeValidator != null
                ? originPostalCodeValidator
                : postalCode -> Optional.empty();
        SearchCriteria criteria = initialCriteria != null ? initialCriteria : defaultCriteria();
        selectedFrom = criteria.from();
        selectedTo = criteria.to();
        selectedDateFilterMode = criteria.dateFilterMode();
        selectedDateFlexDays = normalizeDateFlexDays(criteria.dateFlexDays());
        selectedEarnings = criteria.earnings();
        selectedDistance = normalizeDistance(criteria.distanceKm());
        selectedOriginPostalCode = normalizePostalCode(criteria.originPostalCode());

        getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("background", "white")
                .set("border-radius", "32px")
                .set("box-shadow", "0 12px 30px rgba(74, 52, 40, 0.08)")
                .set("padding", "16px 24px")
                .set("box-sizing", "border-box")
                .set("margin", "36px auto 0 auto")
                .set("max-width", "860px")
                .set("gap", "0");

        FilterPill whenField = filterPill("📅", "Wann?", formatDateRange(), true);
        FilterPill earningsField = filterPill("€", "Verdienst", formatEarningsValue(), false);
        FilterPill distanceField = filterPill("↕", "Entfernung", "bis " + selectedDistance + " km", false);

        whenValue = whenField.value();
        earningsValue = earningsField.value();
        distanceValue = distanceField.value();

        Button searchBtn = new Button(new Icon(VaadinIcon.SEARCH));
        searchBtn.setAriaLabel("Suche starten");
        searchBtn.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("min-width", "48px")
                .set("border-radius", "50%")
                .set("background", BROWN)
                .set("color", "white")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("margin-left", "16px")
                .set("flex-shrink", "0");
        searchBtn.addClickListener(e -> {
            if (!hasInvalidDateRange() && !hasInvalidOriginPostalCode()) {
                this.onSearch.accept(getCriteria());
            }
        });

        whenPopover = popoverFor(whenField.component(), buildDatePopover(), "460px");
        earningsPopover = popoverFor(earningsField.component(), buildEarningsPopover(), "340px");
        distancePopover = popoverFor(distanceField.component(), buildDistancePopover(), "340px");

        whenField.component().addClickListener(e -> openOnly(whenPopover));
        earningsField.component().addClickListener(e -> openOnly(earningsPopover));
        distanceField.component().addClickListener(e -> openOnly(distancePopover));

        add(
                whenField.component(),
                verticalDivider(),
                earningsField.component(),
                verticalDivider(),
                distanceField.component(),
                searchBtn,
                whenPopover,
                earningsPopover,
                distancePopover
        );
    }

    public SearchCriteria getCriteria() {
        return new SearchCriteria(
                selectedFrom,
                selectedTo,
                selectedDateFilterMode,
                selectedDateFlexDays,
                selectedEarnings,
                selectedDistance,
                selectedOriginPostalCode);
    }

    public static SearchCriteria defaultCriteria() {
        return defaultCriteria(null);
    }

    public static SearchCriteria defaultCriteria(String originPostalCode) {
        int currentYear = LocalDate.now().getYear();
        return new SearchCriteria(
                LocalDate.of(currentYear, 6, 15),
                LocalDate.of(currentYear, 6, 18),
                OfferDateFilterMode.OVERLAP,
                0,
                new BigDecimal("80"),
                5,
                originPostalCode);
    }

    private FilterPill filterPill(String icon, String label, String value, boolean first) {
        Div pill = new Div();
        pill.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("flex", "1")
                .set("padding", first ? "0 20px 0 0" : "0 20px")
                .set("cursor", "pointer");
        pill.getElement().setAttribute("role", "button");
        pill.getElement().setAttribute("tabindex", "0");

        Span iconSpan = new Span(icon);
        iconSpan.getStyle()
                .set("font-size", "20px")
                .set("color", BROWN)
                .set("flex-shrink", "0");

        VerticalLayout text = new VerticalLayout();
        text.setPadding(false);
        text.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "11px")
                .set("color", LABEL)
                .set("font-weight", "700")
                .set("letter-spacing", "0.3px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", DARK);

        text.add(labelSpan, valueSpan);
        pill.add(iconSpan, text);
        return new FilterPill(pill, valueSpan);
    }

    private Div verticalDivider() {
        Div divider = new Div();
        divider.getStyle()
                .set("width", "1px")
                .set("height", "36px")
                .set("background", BORDER)
                .set("flex-shrink", "0");
        return divider;
    }

    private Popover popoverFor(Component target, Component content, String width) {
        Popover popover = new Popover();
        popover.setTarget(target);
        popover.setPosition(PopoverPosition.BOTTOM);
        popover.setCloseOnEsc(true);
        popover.setCloseOnOutsideClick(true);
        popover.setOpenOnClick(false);
        popover.setWidth(width);
        popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);
        popover.getElement().getThemeList().add("filter-search-popover");
        popover.add(content);
        return popover;
    }

    private VerticalLayout popoverContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("background", "white")
                .set("border-radius", POPOVER_RADIUS)
                .set("padding", "24px")
                .set("box-sizing", "border-box")
                .set("gap", "14px")
                .set("box-shadow", "0 12px 28px rgba(74, 52, 40, 0.12)");
        return content;
    }

    private Span popoverTitle(String text) {
        Span title = new Span(text);
        title.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", DARK);
        return title;
    }

    private Component buildDatePopover() {
        VerticalLayout content = popoverContent();
        DatePicker fromPicker = styledDatePicker("Von", selectedFrom);
        DatePicker toPicker = styledDatePicker("Bis", selectedTo);
        toPicker.setMin(selectedFrom);
        RadioButtonGroup<OfferDateFilterMode> dateModeGroup = dateModeGroup();
        RadioButtonGroup<Integer> flexDaysGroup = flexDaysGroup();
        flexDaysGroup.setVisible(selectedDateFilterMode == OfferDateFilterMode.OVERLAP);

        fromPicker.addValueChangeListener(event -> {
            selectedFrom = event.getValue();
            toPicker.setMin(selectedFrom);
            updateDateFilter(fromPicker, toPicker);
        });
        toPicker.addValueChangeListener(event -> {
            selectedTo = event.getValue();
            updateDateFilter(fromPicker, toPicker);
        });
        dateModeGroup.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            selectedDateFilterMode = event.getValue();
            flexDaysGroup.setVisible(selectedDateFilterMode == OfferDateFilterMode.OVERLAP);
            updateDateFilter(fromPicker, toPicker);
        });
        flexDaysGroup.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            selectedDateFlexDays = event.getValue();
            updateDateFilter(fromPicker, toPicker);
        });

        HorizontalLayout row = new HorizontalLayout(fromPicker, toPicker);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.START);
        row.getStyle().set("gap", "12px");

        content.add(popoverTitle("Zeitraum"), dateModeGroup, row, flexDaysGroup);
        return content;
    }

    private RadioButtonGroup<OfferDateFilterMode> dateModeGroup() {
        RadioButtonGroup<OfferDateFilterMode> group = new RadioButtonGroup<>();
        group.setItems(OfferDateFilterMode.ANY, OfferDateFilterMode.EXACT,
                OfferDateFilterMode.CONTAINED, OfferDateFilterMode.OVERLAP);
        group.setValue(selectedDateFilterMode);
        group.setItemLabelGenerator(this::dateModeLabel);
        group.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "700")
                .set("color", DARK);
        return group;
    }

    private RadioButtonGroup<Integer> flexDaysGroup() {
        RadioButtonGroup<Integer> group = new RadioButtonGroup<>();
        group.setLabel("Toleranz");
        group.setItems(DATE_FLEX_VALUES);
        group.setValue(selectedDateFlexDays);
        group.setItemLabelGenerator(days -> "+/- " + days + (days == 1 ? " Tag" : " Tage"));
        group.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "700")
                .set("color", DARK);
        return group;
    }

    private String dateModeLabel(OfferDateFilterMode mode) {
        return switch (mode) {
            case ANY       -> "Zeitlich flexibel";
            case EXACT     -> "Exakter Zeitraum";
            case CONTAINED -> "Innerhalb des Zeitraums";
            case OVERLAP   -> "Zeitraum mit Toleranz";
        };
    }

    private DatePicker styledDatePicker(String label, LocalDate value) {
        DatePicker picker = new DatePicker(label, value);
        picker.setLocale(GERMAN);
        picker.setI18n(germanDatePickerI18n());
        picker.setClearButtonVisible(true);
        picker.setWidthFull();
        picker.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-weight", "700");
        return picker;
    }

    private void updateDateFilter(DatePicker fromPicker, DatePicker toPicker) {
        boolean invalidRange = hasInvalidDateRange();
        toPicker.setInvalid(invalidRange);
        toPicker.setErrorMessage("Bis darf nicht vor Von liegen.");
        if (invalidRange) {
            return;
        }
        fromPicker.setInvalid(false);
        toPicker.setInvalid(false);
        whenValue.setText(formatDateRange());
    }

    private Component buildEarningsPopover() {
        VerticalLayout content = popoverContent();
        TextField field = new TextField(earningsMode.fieldLabel());
        field.setValue(selectedEarnings == null ? "" : formatEuroAmount(selectedEarnings));
        field.setPlaceholder("z. B. 20 €");
        field.setAllowedCharPattern("[0-9,. €]");
        field.setPattern("\\s*\\d+([,.]\\d{1,2})?\\s*€?\\s*");
        field.setErrorMessage("Bitte einen positiven Eurobetrag eingeben.");
        field.setManualValidation(true);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.setWidthFull();
        field.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-weight", "700");
        field.addValueChangeListener(event -> updateEarningsFilter(field));

        HorizontalLayout suggestions = new HorizontalLayout(
                suggestionButton("20 €", field),
                suggestionButton("30 €", field),
                suggestionButton("50 €", field)
        );
        suggestions.setWidthFull();
        suggestions.getStyle().set("gap", "8px");

        content.add(popoverTitle("Verdienst"), field, suggestions);
        return content;
    }

    private Button suggestionButton(String amount, TextField field) {
        Button button = new Button(amount);
        button.getStyle()
                .set("background", "#fdf6ec")
                .set("color", BROWN)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "18px")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("font-weight", "800")
                .set("height", "34px")
                .set("padding", "0 14px");
        button.addClickListener(event -> field.setValue(amount));
        return button;
    }

    private void updateEarningsFilter(TextField field) {
        if (field.getValue() == null || field.getValue().isBlank()) {
            field.setInvalid(false);
            selectedEarnings = null;
            earningsValue.setText(formatEarningsValue());
            return;
        }

        BigDecimal parsedAmount = parseEuroAmount(field.getValue());
        if (parsedAmount == null) {
            field.setInvalid(true);
            return;
        }
        field.setInvalid(false);
        selectedEarnings = parsedAmount;
        earningsValue.setText(formatEarningsValue());
    }

    private Component buildDistancePopover() {
        VerticalLayout content = popoverContent();
        originPostalCodeField = new TextField("Ausgangs-PLZ");
        originPostalCodeField.setValue(selectedOriginPostalCode == null ? "" : selectedOriginPostalCode);
        originPostalCodeField.setPlaceholder("z. B. 10115");
        originPostalCodeField.setAllowedCharPattern("[0-9]");
        originPostalCodeField.setPattern("\\d{5}");
        originPostalCodeField.setMaxLength(5);
        originPostalCodeField.setManualValidation(true);
        originPostalCodeField.setErrorMessage("Bitte eine gültige deutsche Postleitzahl eingeben.");
        originPostalCodeField.setValueChangeMode(ValueChangeMode.EAGER);
        originPostalCodeField.setWidthFull();
        originPostalCodeField.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("font-weight", "700");
        originPostalCodeField.addValueChangeListener(event -> {
            selectedOriginPostalCode = normalizePostalCode(event.getValue());
            if (selectedOriginPostalCode == null || selectedOriginPostalCode.length() <= 5) {
                originPostalCodeField.setInvalid(false);
            }
        });

        Span currentValue = new Span(selectedDistance + " km");
        currentValue.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "800")
                .set("line-height", "1")
                .set("color", DARK);

        Slider slider = new Slider(0, DISTANCE_VALUES.size() - 1);
        slider.setStep(1);
        slider.setValue((double) distanceIndex(selectedDistance));
        slider.setAriaLabel("Entfernung in Kilometern");
        slider.setMinMaxVisible(false);
        slider.setValueAlwaysVisible(false);
        slider.setValueChangeMode(ValueChangeMode.EAGER);
        slider.setWidthFull();
        slider.getElement().getThemeList().add("distance-slider");
        slider.getStyle().set("margin-top", "22px");
        slider.addValueChangeListener(event -> {
            int index = Math.max(0, Math.min(DISTANCE_VALUES.size() - 1, (int) Math.round(event.getValue())));
            selectedDistance = DISTANCE_VALUES.get(index);
            currentValue.setText(selectedDistance + " km");
            distanceValue.setText("bis " + selectedDistance + " km");
        });

        HorizontalLayout scale = new HorizontalLayout(new Span("1 km"), new Span("100 km"));
        scale.setWidthFull();
        scale.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        scale.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("color", LABEL);

        Anchor attribution = new Anchor("https://www.openstreetmap.org/copyright", "Geodaten © OpenStreetMap-Mitwirkende");
        attribution.setTarget("_blank");
        attribution.getStyle()
                .set("font-size", "11px")
                .set("font-weight", "700")
                .set("color", LABEL)
                .set("text-decoration", "none");

        content.add(popoverTitle("Entfernung"), originPostalCodeField, currentValue, slider, scale, attribution);
        return content;
    }

    private boolean hasInvalidOriginPostalCode() {
        if (originPostalCodeField != null) {
            selectedOriginPostalCode = normalizePostalCode(originPostalCodeField.getValue());
        }
        Optional<String> validationError = originPostalCodeValidator.apply(selectedOriginPostalCode);
        if (validationError.isPresent()) {
            if (originPostalCodeField != null) {
                originPostalCodeField.setErrorMessage(validationError.get());
                originPostalCodeField.setInvalid(true);
            }
            if (!distancePopover.isOpened()) {
                openOnly(distancePopover);
            }
            return true;
        }
        if (originPostalCodeField != null) {
            originPostalCodeField.setInvalid(false);
        }
        return false;
    }

    private int distanceIndex(int distance) {
        int index = DISTANCE_VALUES.indexOf(distance);
        return index >= 0 ? index : 0;
    }

    private static int normalizeDistance(int distance) {
        return DISTANCE_VALUES.stream()
                .min((left, right) -> Integer.compare(Math.abs(left - distance), Math.abs(right - distance)))
                .orElse(5);
    }

    private static int normalizeDateFlexDays(int dateFlexDays) {
        return DATE_FLEX_VALUES.contains(dateFlexDays) ? dateFlexDays : 0;
    }

    private static String normalizePostalCode(String postalCode) {
        if (postalCode == null) {
            return null;
        }
        String normalized = postalCode.trim().replace(" ", "");
        return normalized.isBlank() ? null : normalized;
    }

    private void openOnly(Popover popover) {
        boolean wasOpen = popover.isOpened();
        whenPopover.close();
        earningsPopover.close();
        distancePopover.close();
        if (!wasOpen) {
            popover.open();
        }
    }

    private String formatDateRange() {
        if (selectedDateFilterMode == OfferDateFilterMode.ANY) {
            return "Zeitlich flexibel";
        }

        String formattedRange = formatPlainDateRange();
        return switch (selectedDateFilterMode) {
            case ANY -> "Zeitlich flexibel";
            case EXACT -> "Exakt " + formattedRange;
            case CONTAINED -> "Innerhalb " + formattedRange;
            case OVERLAP -> selectedDateFlexDays > 0
                    ? formattedRange + " +/- " + selectedDateFlexDays + " Tage"
                    : formattedRange;
        };
    }

    private String formatPlainDateRange() {
        if (selectedFrom != null && selectedTo != null) {
            if (selectedFrom.getMonth().equals(selectedTo.getMonth())) {
                return selectedFrom.getDayOfMonth() + ".–" + selectedTo.format(DAY_MONTH_FORMATTER);
            }
            return selectedFrom.format(DAY_MONTH_FORMATTER) + " – " + selectedTo.format(DAY_MONTH_FORMATTER);
        }
        if (selectedFrom != null) {
            return "ab " + selectedFrom.format(DAY_MONTH_FORMATTER);
        }
        if (selectedTo != null) {
            return "bis " + selectedTo.format(DAY_MONTH_FORMATTER);
        }
        return "Wann?";
    }

    private boolean hasInvalidDateRange() {
        return selectedDateFilterMode != OfferDateFilterMode.ANY
                && selectedFrom != null
                && selectedTo != null
                && selectedTo.isBefore(selectedFrom);
    }

    private BigDecimal parseEuroAmount(String value) {
        String normalized = value == null ? "" : value
                .trim()
                .replace("€", "")
                .replace(" ", "")
                .replace(",", ".");
        if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
            return null;
        }

        BigDecimal amount = new BigDecimal(normalized);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return amount;
    }

    private String formatEuroAmount(BigDecimal amount) {
        BigDecimal normalized = amount.stripTrailingZeros();
        return normalized.scale() <= 0
                ? normalized.toPlainString() + " €"
                : normalized.toPlainString().replace(".", ",") + " €";
    }

    private String formatEarningsValue() {
        if (selectedEarnings == null) {
            return "beliebig";
        }
        return earningsMode.valuePrefix() + " " + formatEuroAmount(selectedEarnings);
    }

    private DatePicker.DatePickerI18n germanDatePickerI18n() {
        return new DatePicker.DatePickerI18n()
                .setMonthNames(List.of(
                        "Januar", "Februar", "März", "April", "Mai", "Juni",
                        "Juli", "August", "September", "Oktober", "November", "Dezember"))
                .setWeekdays(List.of(
                        "Sonntag", "Montag", "Dienstag", "Mittwoch",
                        "Donnerstag", "Freitag", "Samstag"))
                .setWeekdaysShort(List.of("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"))
                .setFirstDayOfWeek(1)
                .setToday("Heute")
                .setCancel("Abbrechen");
    }

    private record FilterPill(Div component, Span value) {
    }

    public record SearchCriteria(
            LocalDate from,
            LocalDate to,
            OfferDateFilterMode dateFilterMode,
            int dateFlexDays,
            BigDecimal earnings,
            int distanceKm,
            String originPostalCode
    ) {
        public SearchCriteria(LocalDate from, LocalDate to, OfferDateFilterMode dateFilterMode,
                int dateFlexDays, BigDecimal earnings, int distanceKm) {
            this(from, to, dateFilterMode, dateFlexDays, earnings, distanceKm, null);
        }

        public SearchCriteria {
            if (dateFilterMode == null) {
                dateFilterMode = OfferDateFilterMode.OVERLAP;
            }
            if (dateFlexDays < 0) {
                dateFlexDays = 0;
            }
            originPostalCode = normalizePostalCode(originPostalCode);
        }
    }

    public enum EarningsMode {
        MINIMUM("Mindestverdienst", "ab"),
        MAXIMUM("Maximalverdienst", "bis");

        private final String fieldLabel;
        private final String valuePrefix;

        EarningsMode(String fieldLabel, String valuePrefix) {
            this.fieldLabel = fieldLabel;
            this.valuePrefix = valuePrefix;
        }

        private String fieldLabel() {
            return fieldLabel;
        }

        private String valuePrefix() {
            return valuePrefix;
        }
    }
}
