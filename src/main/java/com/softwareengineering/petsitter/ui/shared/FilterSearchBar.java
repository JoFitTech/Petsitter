package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.button.Button;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
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
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM yyyy", GERMAN);
    private static final List<Integer> DISTANCE_VALUES = List.of(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            15, 20, 25, 30, 35, 40, 45, 50, 55, 60,
            65, 70, 75, 80, 85, 90, 95, 100, OfferSearchCriteria.ANY_DISTANCE_KM
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
    private RadioButtonGroup<OfferDateFilterMode> dateModeGroup;
    private HorizontalLayout dateFlexDaysRow;
    private Div dateCalendar;
    private Span dateRangeStatus;
    private Span distancePostalCodeValue;
    private YearMonth displayedMonth;
    private LocalDate selectedFrom;
    private LocalDate selectedTo;
    private OfferDateFilterMode selectedDateFilterMode;
    private int selectedDateFlexDays;
    private BigDecimal selectedEarnings;
    private int selectedDistance;
    private String selectedOriginPostalCode;
    private boolean selectingRangeEnd;

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
                .set("width", "100%")
                .set("max-width", "100%")
                .set("gap", "0");

        FilterPill whenField = filterPill("📅", "Wann?", formatDateRange(), true);
        FilterPill earningsField = filterPill("€", "Verdienst", formatEarningsValue(), false);
        FilterPill distanceField = distancePill();

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

        whenPopover = popoverFor(whenField.component(), buildDatePopover(), "500px",
                PopoverPosition.TOP_START);
        earningsPopover = popoverFor(earningsField.component(), buildEarningsPopover(), "340px");
        distancePopover = popoverFor(distanceField.component(), buildDistancePopover(), "340px");

        searchBtn.addClickListener(e -> {
            if (hasInvalidDateRange()) {
                updateDateFilter();
                if (!whenPopover.isOpened()) {
                    openOnly(whenPopover);
                }
                return;
            }
            if (hasInvalidOriginPostalCode()) {
                return;
            }
            this.onSearch.accept(getCriteria());
        });

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
        LocalDate today = LocalDate.now();
        LocalDate defaultFrom = LocalDate.of(today.getYear(), 6, 15);
        if (defaultFrom.isBefore(today)) {
            defaultFrom = today;
        }
        return new SearchCriteria(
                defaultFrom,
                defaultFrom.plusDays(3),
                OfferDateFilterMode.OVERLAP,
                0,
                new BigDecimal("80"),
                5,
                originPostalCode);
    }

    private FilterPill distancePill() {
        Div pill = new Div();
        pill.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("flex", "1")
                .set("padding", "0 20px")
                .set("cursor", "pointer");
        pill.getElement().setAttribute("role", "button");
        pill.getElement().setAttribute("tabindex", "0");

        Span iconSpan = new Span("↕");
        iconSpan.getStyle()
                .set("font-size", "20px")
                .set("color", BROWN)
                .set("flex-shrink", "0");

        VerticalLayout text = new VerticalLayout();
        text.setPadding(false);
        text.setSpacing(false);

        Span labelSpan = new Span("Entfernung");
        labelSpan.getStyle()
                .set("font-size", "11px")
                .set("color", LABEL)
                .set("font-weight", "700")
                .set("letter-spacing", "0.3px");

        Span valueSpan = new Span(formatDistancePillValue(selectedDistance));
        valueSpan.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", DARK);

        distancePostalCodeValue = new Span(formatDistancePostalCode());
        distancePostalCodeValue.getStyle()
                .set("font-size", "11px")
                .set("color", LABEL)
                .set("font-weight", "700")
                .set("letter-spacing", "0.3px");

        text.add(labelSpan, valueSpan, distancePostalCodeValue);
        pill.add(iconSpan, text);
        return new FilterPill(pill, valueSpan);
    }

    private String formatDistancePostalCode() {
        return selectedOriginPostalCode != null ? "ab " + selectedOriginPostalCode : "PLZ eingeben";
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
        return popoverFor(target, content, width, PopoverPosition.BOTTOM);
    }

    private Popover popoverFor(Component target, Component content, String width, PopoverPosition position) {
        Popover popover = new Popover();
        popover.setTarget(target);
        popover.setPosition(position);
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
        content.getStyle()
                .set("gap", "5px")
                .set("min-height", "370px")
                .set("padding", "12px 16px");
        displayedMonth = initialDisplayedMonth();
        dateRangeStatus = new Span();
        dateRangeStatus.getStyle()
                .set("background", "#fdf6ec")
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "18px")
                .set("box-sizing", "border-box")
                .set("color", DARK)
                .set("font-size", "12px")
                .set("font-weight", "800")
                .set("padding", "4px 11px")
                .set("width", "100%");
        dateCalendar = new Div();
        dateCalendar.setWidthFull();
        dateModeGroup = dateModeGroup();
        dateFlexDaysRow = flexDaysControl();
        dateFlexDaysRow.setVisible(selectedDateFilterMode == OfferDateFilterMode.OVERLAP);

        dateModeGroup.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            selectedDateFilterMode = event.getValue();
            dateFlexDaysRow.setVisible(selectedDateFilterMode == OfferDateFilterMode.OVERLAP);
            updateDateFilter();
        });

        updateDateFilter();

        content.add(popoverTitle("Zeitraum"), dateModeGroup, dateFlexDaysRow, dateRangeStatus, dateCalendar);
        return content;
    }

    private RadioButtonGroup<OfferDateFilterMode> dateModeGroup() {
        RadioButtonGroup<OfferDateFilterMode> group = new RadioButtonGroup<>();
        group.setItems(OfferDateFilterMode.ANY,
                OfferDateFilterMode.CONTAINED, OfferDateFilterMode.OVERLAP);
        group.setValue(selectedDateFilterMode);
        group.setItemLabelGenerator(this::dateModeLabel);
        group.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "700")
                .set("color", DARK);
        return group;
    }

    private HorizontalLayout flexDaysControl() {
        HorizontalLayout row = new HorizontalLayout();
        row.setPadding(false);
        row.setSpacing(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle()
                .set("gap", "5px")
                .set("flex-wrap", "wrap")
                .set("width", "100%");

        Span label = new Span("Toleranz");
        label.getStyle()
                .set("color", LABEL)
                .set("font-size", "11px")
                .set("font-weight", "900")
                .set("margin-right", "2px");
        row.add(label);

        DATE_FLEX_VALUES.forEach(days -> row.add(flexDaysButton(days)));
        return row;
    }

    private Button flexDaysButton(int days) {
        Button button = new Button(days == 0 ? "0" : "+/- " + days);
        button.setAriaLabel(days + (days == 1 ? " Tag Toleranz" : " Tage Toleranz"));
        button.getStyle()
                .set("background", days == selectedDateFlexDays ? BROWN : "#fdf6ec")
                .set("border", "1px solid " + (days == selectedDateFlexDays ? BROWN : BORDER))
                .set("border-radius", "999px")
                .set("box-shadow", "none")
                .set("color", days == selectedDateFlexDays ? "white" : BROWN)
                .set("cursor", "pointer")
                .set("font-size", "12px")
                .set("font-weight", "900")
                .set("height", "28px")
                .set("min-width", "0")
                .set("padding", "0 10px");
        button.addClickListener(event -> {
            selectedDateFlexDays = days;
            refreshFlexDaysControl();
            updateDateFilter();
        });
        return button;
    }

    private void refreshFlexDaysControl() {
        if (dateFlexDaysRow == null) {
            return;
        }
        dateFlexDaysRow.removeAll();
        Span label = new Span("Toleranz");
        label.getStyle()
                .set("color", LABEL)
                .set("font-size", "11px")
                .set("font-weight", "900")
                .set("margin-right", "2px");
        dateFlexDaysRow.add(label);
        DATE_FLEX_VALUES.forEach(days -> dateFlexDaysRow.add(flexDaysButton(days)));
    }

    private String dateModeLabel(OfferDateFilterMode mode) {
        return switch (mode) {
            case ANY       -> "Zeitlich flexibel";
            case CONTAINED -> "Innerhalb des Zeitraums";
            case OVERLAP   -> "Zeitraum mit Toleranz";
        };
    }

    private void updateDateFilter() {
        boolean showDateControls = selectedDateFilterMode != OfferDateFilterMode.ANY;
        dateRangeStatus.setVisible(showDateControls);
        dateCalendar.setVisible(showDateControls);
        if (dateFlexDaysRow != null) {
            dateFlexDaysRow.setVisible(selectedDateFilterMode == OfferDateFilterMode.OVERLAP);
        }
        if (!showDateControls) {
            whenValue.setText(formatDateRange());
            dateCalendar.removeAll();
            return;
        }

        boolean invalidFrom = selectedDateFilterMode != OfferDateFilterMode.ANY && isPastDate(selectedFrom);
        boolean invalidTo = selectedDateFilterMode != OfferDateFilterMode.ANY && isPastDate(selectedTo);
        boolean invalidOrder = selectedDateFilterMode != OfferDateFilterMode.ANY && hasInvalidDateOrder();

        if (invalidFrom || invalidTo || invalidOrder) {
            dateRangeStatus.setText(invalidFrom || invalidTo
                    ? "Der Zeitraum darf nicht in der Vergangenheit liegen."
                    : "Bis darf nicht vor Von liegen.");
            dateRangeStatus.getStyle()
                    .set("background", "#fff3f0")
                    .set("border", "1px solid #efb5a8")
                    .set("color", "#8a2f1f");
            refreshDateCalendar();
            return;
        }
        dateRangeStatus.setText(selectedDateFilterMode == OfferDateFilterMode.ANY
                ? "Zeitlich flexibel"
                : formatPlainDateRange());
        dateRangeStatus.getStyle()
                .set("background", "#fdf6ec")
                .set("border", "1px solid " + BORDER)
                .set("color", DARK);
        whenValue.setText(formatDateRange());
        refreshDateCalendar();
    }

    private YearMonth initialDisplayedMonth() {
        LocalDate today = LocalDate.now();
        if (selectedFrom != null && !selectedFrom.isBefore(today)) {
            return YearMonth.from(selectedFrom);
        }
        if (selectedTo != null && !selectedTo.isBefore(today)) {
            return YearMonth.from(selectedTo);
        }
        return YearMonth.from(today);
    }

    private void refreshDateCalendar() {
        if (dateCalendar == null) {
            return;
        }
        dateCalendar.removeAll();
        dateCalendar.add(calendarHeader(), weekdayHeader(), dayGrid());
    }

    private Component calendarHeader() {
        Button previousMonth = calendarNavButton(VaadinIcon.CHEVRON_LEFT, "Vorheriger Monat");
        previousMonth.setEnabled(displayedMonth.isAfter(YearMonth.from(LocalDate.now())));
        previousMonth.addClickListener(event -> {
            displayedMonth = displayedMonth.minusMonths(1);
            refreshDateCalendar();
        });

        Button nextMonth = calendarNavButton(VaadinIcon.CHEVRON_RIGHT, "Nächster Monat");
        nextMonth.addClickListener(event -> {
            displayedMonth = displayedMonth.plusMonths(1);
            refreshDateCalendar();
        });

        Span monthLabel = new Span(displayedMonth.atDay(1).format(MONTH_YEAR_FORMATTER));
        monthLabel.getStyle()
                .set("color", DARK)
                .set("font-size", "15px")
                .set("font-weight", "900")
                .set("text-transform", "capitalize");

        HorizontalLayout header = new HorizontalLayout(previousMonth, monthLabel, nextMonth);
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }

    private Button calendarNavButton(VaadinIcon icon, String ariaLabel) {
        Button button = new Button(new Icon(icon));
        button.setAriaLabel(ariaLabel);
        button.getStyle()
                .set("background", "#fdf6ec")
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "50%")
                .set("box-shadow", "none")
                .set("color", BROWN)
                .set("cursor", "pointer")
                .set("height", "28px")
                .set("min-width", "28px")
                .set("padding", "0")
                .set("width", "28px");
        return button;
    }

    private Component weekdayHeader() {
        Div weekdays = calendarGrid();
        List<String> labels = List.of("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So");
        for (String label : labels) {
            Span day = new Span(label);
            day.getStyle()
                    .set("color", LABEL)
                    .set("font-size", "11px")
                    .set("font-weight", "900")
                    .set("line-height", "20px")
                    .set("text-align", "center");
            weekdays.add(day);
        }
        return weekdays;
    }

    private Component dayGrid() {
        Div grid = calendarGrid();
        LocalDate firstDay = displayedMonth.atDay(1);
        int leadingBlankDays = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        for (int i = 0; i < leadingBlankDays; i++) {
            grid.add(blankCalendarCell());
        }
        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
            grid.add(calendarDayButton(displayedMonth.atDay(day)));
        }
        return grid;
    }

    private Div calendarGrid() {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(7, minmax(0, 1fr))")
                .set("gap", "0")
                .set("width", "100%");
        return grid;
    }

    private Component blankCalendarCell() {
        Div blank = new Div();
        blank.getStyle().set("height", "30px");
        return blank;
    }

    private Component calendarDayButton(LocalDate date) {
        Div cell = new Div();
        Button button = new Button(String.valueOf(date.getDayOfMonth()));
        boolean disabled = date.isBefore(LocalDate.now());
        boolean selectedStart = date.equals(selectedFrom);
        boolean selectedEnd = date.equals(selectedTo);
        boolean insideRange = isInsideSelectedRange(date);
        boolean hasCompleteRange = hasCompleteSelectedRange();

        cell.getStyle()
                .set("align-items", "center")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("height", "30px")
                .set("justify-content", "center")
                .set("width", "100%");
        applyRangeCellBackground(cell, selectedStart, selectedEnd, insideRange, hasCompleteRange);

        button.setEnabled(!disabled);
        button.setAriaLabel(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        button.getStyle()
                .set("border", "none")
                .set("box-shadow", "none")
                .set("font-size", "13px")
                .set("font-weight", "800")
                .set("height", "30px")
                .set("margin", "0")
                .set("min-width", "0")
                .set("padding", "0")
                .set("width", "100%");

        if (disabled) {
            button.getStyle()
                    .set("background", "transparent")
                    .set("color", "#c8b9aa")
                    .set("cursor", "not-allowed");
            cell.add(button);
            return cell;
        }

        button.getStyle()
                .set("background", "transparent")
                .set("color", DARK)
                .set("cursor", "pointer");
        if (date.equals(LocalDate.now())) {
            button.getStyle().set("box-shadow", "inset 0 0 0 1px " + BROWN);
        }
        if (insideRange) {
            button.getStyle()
                    .set("background", "transparent")
                    .set("border-radius", "0")
                    .set("color", DARK);
        }
        if (selectedStart || selectedEnd) {
            button.getStyle()
                    .set("background", BROWN)
                    .set("border-radius", "999px")
                    .set("box-shadow", "none")
                    .set("color", "white");
        }

        button.addClickListener(event -> selectCalendarDate(date));
        cell.add(button);
        return cell;
    }

    private void applyRangeCellBackground(Div cell, boolean selectedStart, boolean selectedEnd,
            boolean insideRange, boolean hasCompleteRange) {
        if (!hasCompleteRange) {
            cell.getStyle().set("background", "transparent");
            return;
        }
        String rangeColor = "#f1dfcf";
        if (insideRange) {
            cell.getStyle().set("background", rangeColor);
        } else if (selectedStart && !selectedEnd) {
            cell.getStyle().set("background",
                    "linear-gradient(to right, transparent 0 50%, " + rangeColor + " 50% 100%)");
        } else if (selectedEnd && !selectedStart) {
            cell.getStyle().set("background",
                    "linear-gradient(to right, " + rangeColor + " 0 50%, transparent 50% 100%)");
        } else {
            cell.getStyle().set("background", "transparent");
        }
    }

    private void selectCalendarDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return;
        }
        boolean switchFromFlexibleMode = selectedDateFilterMode == OfferDateFilterMode.ANY;
        if (selectedFrom == null || selectedTo != null || !selectingRangeEnd) {
            selectedFrom = date;
            selectedTo = null;
            selectingRangeEnd = true;
        } else if (date.isBefore(selectedFrom)) {
            selectedFrom = date;
            selectedTo = null;
            selectingRangeEnd = true;
        } else {
            selectedTo = date;
            selectingRangeEnd = false;
        }
        displayedMonth = YearMonth.from(date);
        if (switchFromFlexibleMode && dateModeGroup != null) {
            dateModeGroup.setValue(OfferDateFilterMode.OVERLAP);
            return;
        }
        updateDateFilter();
    }

    private boolean isInsideSelectedRange(LocalDate date) {
        return selectedFrom != null
                && selectedTo != null
                && date.isAfter(selectedFrom)
                && date.isBefore(selectedTo);
    }

    private boolean hasCompleteSelectedRange() {
        return selectedFrom != null
                && selectedTo != null
                && !selectedFrom.equals(selectedTo);
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
            distancePostalCodeValue.setText(formatDistancePostalCode());
        });

        Span currentValue = new Span(formatDistanceValue(selectedDistance));
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
            currentValue.setText(formatDistanceValue(selectedDistance));
            distanceValue.setText(formatDistancePillValue(selectedDistance));
        });

        HorizontalLayout scale = new HorizontalLayout(new Span("1 km"), new Span("egal"));
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
        if (distance == OfferSearchCriteria.ANY_DISTANCE_KM) {
            return distance;
        }
        return DISTANCE_VALUES.stream()
                .filter(value -> value != OfferSearchCriteria.ANY_DISTANCE_KM)
                .min((left, right) -> Integer.compare(Math.abs(left - distance), Math.abs(right - distance)))
                .orElse(5);
    }

    private static String formatDistanceValue(int distance) {
        return distance == OfferSearchCriteria.ANY_DISTANCE_KM ? "egal" : distance + " km";
    }

    private static String formatDistancePillValue(int distance) {
        return distance == OfferSearchCriteria.ANY_DISTANCE_KM ? "egal" : "bis " + formatDistanceValue(distance);
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
                && (isPastDate(selectedFrom) || isPastDate(selectedTo) || hasInvalidDateOrder());
    }

    private boolean hasInvalidDateOrder() {
        return selectedFrom != null
                && selectedTo != null
                && selectedTo.isBefore(selectedFrom);
    }

    private boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
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
