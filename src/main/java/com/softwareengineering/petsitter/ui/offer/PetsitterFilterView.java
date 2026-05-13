package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.domain.OfferDateFilterMode;
import com.softwareengineering.petsitter.offer.domain.OfferSearchMode;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.FilterPopUp;
import com.softwareengineering.petsitter.ui.shared.FilterSearchBar;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.ui.shared.PetsitterDetailPopUp;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Route(value = "petsitter-suche", layout = MainLayout.class)
@PageTitle("Suche | Pawsitter")
@PermitAll
public class PetsitterFilterView extends VerticalLayout implements BeforeEnterObserver {

    private static final String DARK       = "#4a3428";
    private static final String CREAM      = "#fbf8f1";
    private static final String ACCENT     = "#7b5236";
    private static final String CARD_BG    = "#ffffff";
    private static final String BORDER_CLR = "#ead5ae";

    private static final String[][] MAP_MARKER_POSITIONS = {
        {"62%", "28%"},
        {"56%", "38%"},
        {"60%", "44%"},
        {"52%", "50%"},
        {"48%", "56%"},
        {"60%", "58%"},
        {"56%", "66%"},
    };

    private final OfferService offerService;

    private H1 pageTitle;
    private Div leftBlob;
    private H2 resultsLabel;
    private Div filterBarContainer;
    private Div offerGrid;
    private Div mapMarkerLayer;
    private OfferSearchCriteria currentCriteria = defaultCriteria(OfferSearchMode.TIERSITTER);

    @Autowired
    public PetsitterFilterView(OfferService offerService) {
        this.offerService = offerService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", CREAM)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK)
                .set("position", "relative");

        add(buildDecoBlobs());
        add(buildPageTitle());
        add(buildFilterBarContainer());
        add(buildResultsLabel());
        add(buildContentArea());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
        currentCriteria = parseCriteria(parameters);

        applyModeChrome();
        renderFilterBar();
        renderOffers();
    }

    private void applyModeChrome() {
        SearchDisplay display = displayFor(currentCriteria.mode());
        pageTitle.setText(display.pageTitle);
        leftBlob.getStyle().set("background", display.leftBlobColor);
    }

    private OfferSearchCriteria parseCriteria(Map<String, List<String>> parameters) {
        OfferSearchMode mode = OfferSearchMode.fromQueryValue(firstValue(parameters, "mode"));
        FilterSearchBar.SearchCriteria defaultCriteria = FilterSearchBar.defaultCriteria();
        if (!containsAnyFilterParameter(parameters)) {
            return new OfferSearchCriteria(
                mode,
                defaultCriteria.from(),
                defaultCriteria.to(),
                defaultCriteria.dateFilterMode(),
                defaultCriteria.dateFlexDays(),
                defaultCriteria.earnings(),
                defaultCriteria.distanceKm());
        }

        return new OfferSearchCriteria(
                mode,
                parseDate(firstValue(parameters, "from")),
                parseDate(firstValue(parameters, "to")),
                OfferDateFilterMode.fromQueryValue(firstValue(parameters, "dateMode")),
                parseFlexDays(firstValue(parameters, "dateFlexDays")),
                parseAmount(firstValue(parameters, "earnings")),
                parseDistance(firstValue(parameters, "distanceKm"), defaultCriteria.distanceKm()));
    }

    private boolean containsAnyFilterParameter(Map<String, List<String>> parameters) {
        return parameters.containsKey("from")
                || parameters.containsKey("to")
                || parameters.containsKey("dateMode")
                || parameters.containsKey("dateFlexDays")
                || parameters.containsKey("earnings")
                || parameters.containsKey("distanceKm");
    }

    private String firstValue(Map<String, List<String>> parameters, String key) {
        List<String> values = parameters.get(key);
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            BigDecimal amount = new BigDecimal(value.replace(",", "."));
            return amount.compareTo(BigDecimal.ZERO) > 0 ? amount : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int parseDistance(String value, int defaultDistance) {
        if (value == null || value.isBlank()) {
            return defaultDistance;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultDistance;
        }
    }

    private int parseFlexDays(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            int parsedFlexDays = Integer.parseInt(value);
            return switch (parsedFlexDays) {
                case 0, 1, 2, 3, 7 -> parsedFlexDays;
                default -> 0;
            };
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private Component buildDecoBlobs() {
        Div container = new Div();
        container.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("overflow", "hidden")
                .set("z-index", "0")
                .set("pointer-events", "none");

        leftBlob = new Div();
        leftBlob.getStyle()
                .set("position", "absolute")
                .set("width", "700px")
                .set("height", "700px")
                .set("border-radius", "50%")
                .set("background", "#e2f2eb")
                .set("top", "-150px")
                .set("left", "-200px");

        Div rightBlob = new Div();
        rightBlob.getStyle()
                .set("position", "absolute")
                .set("width", "800px")
                .set("height", "800px")
                .set("border-radius", "50%")
                .set("background", "#edf1f9")
                .set("top", "50px")
                .set("right", "-150px");

        container.add(leftBlob, rightBlob);
        return container;
    }

    private Component buildPageTitle() {
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("padding", "44px 48px 16px 48px")
                .set("position", "relative")
                .set("z-index", "1");

        pageTitle = new H1(displayFor(OfferSearchMode.TIERSITTER).pageTitle);
        pageTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "32px")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("line-height", "1.25");

        wrapper.add(pageTitle);
        return wrapper;
    }

    private Component buildFilterBarContainer() {
        filterBarContainer = new Div();
        filterBarContainer.getStyle()
                .set("padding", "0 48px 28px 48px")
                .set("position", "relative")
                .set("z-index", "1")
                .set("box-sizing", "border-box");
        return filterBarContainer;
    }

    private void renderFilterBar() {
        filterBarContainer.removeAll();

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(false);
        row.getStyle()
                .set("gap", "16px")
                .set("flex-wrap", "wrap");

        SearchDisplay display = displayFor(currentCriteria.mode());
        FilterSearchBar searchBar = new FilterSearchBar(
                display.earningsMode,
                toFilterCriteria(currentCriteria),
                this::onSearchClicked);
        searchBar.getStyle()
                .set("margin", "0")
                .set("max-width", "860px")
                .set("width", "min(860px, 100%)")
                .set("flex", "1 1 640px");

        Button filterBtn = new Button("Filter");
        Icon filterIcon = new Icon(VaadinIcon.FILTER);
        filterIcon.setSize("16px");
        filterBtn.setIcon(filterIcon);
        filterBtn.getStyle()
                .set("height", "52px")
                .set("padding", "0 32px")
                .set("border-radius", "50px")
                .set("background", ACCENT)
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "15px")
                .set("box-shadow", "0 6px 18px rgba(74,52,40,0.25)")
                .set("cursor", "pointer")
                .set("gap", "8px");
        filterBtn.addClickListener(e -> new FilterPopUp().open());

        row.add(searchBar, filterBtn);
        filterBarContainer.add(row);
    }

    private void onSearchClicked(FilterSearchBar.SearchCriteria criteria) {
        UI.getCurrent().navigate("petsitter-suche", queryParametersFor(currentCriteria.mode(), criteria));
    }

    private FilterSearchBar.SearchCriteria toFilterCriteria(OfferSearchCriteria criteria) {
        return new FilterSearchBar.SearchCriteria(
                criteria.from(),
                criteria.to(),
                criteria.dateFilterMode(),
                criteria.dateFlexDays(),
                criteria.earnings(),
                criteria.distanceKm());
    }

    private QueryParameters queryParametersFor(OfferSearchMode mode, FilterSearchBar.SearchCriteria criteria) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put("mode", List.of(mode.queryValue()));
        if (criteria.from() != null) {
            parameters.put("from", List.of(criteria.from().toString()));
        }
        if (criteria.to() != null) {
            parameters.put("to", List.of(criteria.to().toString()));
        }
        parameters.put("dateMode", List.of(criteria.dateFilterMode().queryValue()));
        parameters.put("dateFlexDays", List.of(String.valueOf(criteria.dateFlexDays())));
        if (criteria.earnings() != null) {
            parameters.put("earnings", List.of(criteria.earnings().stripTrailingZeros().toPlainString()));
        }
        parameters.put("distanceKm", List.of(String.valueOf(criteria.distanceKm())));
        return new QueryParameters(parameters);
    }

    private Component buildResultsLabel() {
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("padding", "0 48px 20px 48px")
                .set("position", "relative")
                .set("z-index", "1");

        resultsLabel = new H2();
        resultsLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK);
        wrapper.add(resultsLabel);
        return wrapper;
    }

    private Component buildContentArea() {
        HorizontalLayout area = new HorizontalLayout();
        area.setWidthFull();
        area.setPadding(false);
        area.setSpacing(false);
        area.getStyle()
                .set("padding", "0 48px 64px 48px")
                .set("gap", "28px")
                .set("align-items", "flex-start")
                .set("box-sizing", "border-box")
                .set("position", "relative")
                .set("z-index", "1");

        offerGrid = new Div();
        offerGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "20px")
                .set("flex", "1");

        Div map = buildMapPanel();
        area.add(offerGrid, map);
        area.setFlexGrow(1, offerGrid);
        return area;
    }

    private void renderOffers() {
        List<OfferCardDto> offers = offerService.searchOpenOffers(currentCriteria);

        resultsLabel.setText(resultText(offers.size()));
        renderMapMarkers(offers);
        offerGrid.removeAll();
        if (offers.isEmpty()) {
            offerGrid.add(buildEmptyState());
            return;
        }

        offers.forEach(dto -> offerGrid.add(new OfferCardComponent(
                dto,
                this::openOfferDialog,
                this::onFavoriteClicked)));
    }

    private void renderMapMarkers(List<OfferCardDto> offers) {
        mapMarkerLayer.removeAll();
        for (int i = 0; i < offers.size() && i < MAP_MARKER_POSITIONS.length; i++) {
            String[] position = MAP_MARKER_POSITIONS[i];
            mapMarkerLayer.add(buildMapMarker(formatPrice(offers.get(i).price()), position[0], position[1], i == 0));
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "–";
        }
        return price.stripTrailingZeros().toPlainString() + " €";
    }

    private String resultText(int count) {
        String resultNoun = displayFor(currentCriteria.mode()).resultNoun;
        return count == 1
                ? "1 " + resultNoun + " gefunden"
                : count + " " + resultNoun + " gefunden";
    }

    private Component buildEmptyState() {
        Div empty = new Div();
        empty.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("box-shadow", "0 6px 24px rgba(74,52,40,0.10)")
                .set("padding", "28px")
                .set("box-sizing", "border-box")
                .set("grid-column", "1 / -1");

        H3 title = new H3("Keine passenden " + displayFor(currentCriteria.mode()).resultNoun + " gefunden");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph copy = new Paragraph("Passe Zeitraum oder Verdienst an, um weitere Angebote zu sehen.");
        copy.getStyle()
                .set("margin", "0")
                .set("color", "#7b7069")
                .set("font-size", "14px");

        empty.add(title, copy);
        return empty;
    }

    private void openOfferDialog(OfferCardDto dto) {
        new PetsitterDetailPopUp(dto, "–", 4).open();
    }

    private void onFavoriteClicked(OfferCardDto dto) {
        // TODO: favoriteService.toggleFavorite(dto.id());
    }

    private Div buildMapPanel() {
        Div mapWrapper = new Div();
        mapWrapper.getStyle()
                .set("width", "380px")
                .set("min-width", "320px")
                .set("border-radius", "20px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.14)")
                .set("position", "sticky")
                .set("top", "24px")
                .set("align-self", "flex-start");

        Div mapArea = new Div();
        mapArea.getStyle()
                .set("width", "100%")
                .set("height", "640px")
                .set("background", "linear-gradient(135deg, #e8f0e8 0%, #d5e8d5 40%, #c8dfd8 70%, #b8cfd8 100%)")
                .set("position", "relative")
                .set("overflow", "hidden");

        addMapGridLines(mapArea);
        addMapRoads(mapArea);

        mapMarkerLayer = new Div();
        mapMarkerLayer.getStyle()
                .set("position", "absolute")
                .set("inset", "0")
                .set("pointer-events", "none");

        mapArea.add(mapMarkerLayer, buildMapControls(), buildMapExpandButton(), buildMapAttribution());
        mapWrapper.add(mapArea);
        return mapWrapper;
    }

    private Div buildMapMarker(String price, String left, String top, boolean highlighted) {
        Div marker = new Div();
        marker.getStyle()
                .set("position", "absolute")
                .set("left", left)
                .set("top", top)
                .set("transform", "translate(-50%, -50%)")
                .set("background", highlighted ? DARK : CARD_BG)
                .set("color", highlighted ? "white" : DARK)
                .set("border", "2px solid " + (highlighted ? DARK : BORDER_CLR))
                .set("border-radius", "20px")
                .set("padding", "5px 12px")
                .set("font-size", "13px")
                .set("font-weight", "800")
                .set("box-shadow", highlighted
                        ? "0 4px 16px rgba(74,52,40,0.45)"
                        : "0 3px 10px rgba(74,52,40,0.18)")
                .set("cursor", "pointer")
                .set("pointer-events", "auto")
                .set("white-space", "nowrap")
                .set("transition", "transform 0.15s ease");

        marker.add(new Span(price));
        marker.getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.transform='translate(-50%,-50%) scale(1.1)');"
                        + "this.addEventListener('mouseleave', () => this.style.transform='translate(-50%,-50%) scale(1)');");
        return marker;
    }

    private Div buildMapControls() {
        Div controls = new Div();
        controls.getStyle()
                .set("position", "absolute")
                .set("right", "12px")
                .set("top", "56px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "2px");

        controls.add(mapControlBtn("+"), mapControlBtn("−"));
        return controls;
    }

    private Div buildMapExpandButton() {
        Div expandBtn = new Div();
        expandBtn.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px")
                .set("width", "34px")
                .set("height", "34px")
                .set("background", CARD_BG)
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.2)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("cursor", "pointer");

        Icon expandIcon = new Icon(VaadinIcon.EXPAND_FULL);
        expandIcon.setSize("16px");
        expandBtn.add(expandIcon);
        return expandBtn;
    }

    private Span buildMapAttribution() {
        Span attribution = new Span("Google Karte — Karzbehler © 2026 Google");
        attribution.getStyle()
                .set("position", "absolute")
                .set("bottom", "6px")
                .set("left", "8px")
                .set("font-size", "10px")
                .set("color", "#555")
                .set("background", "rgba(255,255,255,0.8)")
                .set("padding", "2px 6px")
                .set("border-radius", "4px");
        return attribution;
    }

    private Div mapControlBtn(String symbol) {
        Div button = new Div();
        button.getStyle()
                .set("width", "34px")
                .set("height", "34px")
                .set("background", CARD_BG)
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.2)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "20px")
                .set("font-weight", "700")
                .set("color", DARK)
                .set("cursor", "pointer")
                .set("user-select", "none");
        button.add(new Span(symbol));
        return button;
    }

    private void addMapGridLines(Div mapArea) {
        String[] verticals = {"20%", "40%", "60%", "80%"};
        String[] horizontals = {"15%", "30%", "45%", "60%", "75%", "90%"};

        for (String pos : verticals) {
            Div line = new Div();
            line.getStyle()
                    .set("position", "absolute")
                    .set("left", pos)
                    .set("top", "0")
                    .set("width", "1px")
                    .set("height", "100%")
                    .set("background", "rgba(160,180,160,0.35)");
            mapArea.add(line);
        }
        for (String pos : horizontals) {
            Div line = new Div();
            line.getStyle()
                    .set("position", "absolute")
                    .set("top", pos)
                    .set("left", "0")
                    .set("height", "1px")
                    .set("width", "100%")
                    .set("background", "rgba(160,180,160,0.35)");
            mapArea.add(line);
        }
    }

    private void addMapRoads(Div mapArea) {
        Div road1 = new Div();
        road1.getStyle()
                .set("position", "absolute")
                .set("top", "52%")
                .set("left", "0")
                .set("height", "5px")
                .set("width", "100%")
                .set("background", "rgba(255,255,240,0.7)")
                .set("border-top", "1px solid rgba(200,200,180,0.5)")
                .set("border-bottom", "1px solid rgba(200,200,180,0.5)");

        Div road2 = new Div();
        road2.getStyle()
                .set("position", "absolute")
                .set("top", "20%")
                .set("left", "-10%")
                .set("height", "4px")
                .set("width", "130%")
                .set("background", "rgba(255,255,240,0.6)")
                .set("transform", "rotate(22deg)")
                .set("transform-origin", "left center");

        Div road3 = new Div();
        road3.getStyle()
                .set("position", "absolute")
                .set("left", "55%")
                .set("top", "0")
                .set("width", "5px")
                .set("height", "100%")
                .set("background", "rgba(255,255,240,0.6)");

        mapArea.add(road1, road2, road3);
    }

    private OfferSearchCriteria defaultCriteria(OfferSearchMode mode) {
        FilterSearchBar.SearchCriteria defaultCriteria = FilterSearchBar.defaultCriteria();
        return new OfferSearchCriteria(
                mode,
                defaultCriteria.from(),
                defaultCriteria.to(),
                defaultCriteria.dateFilterMode(),
                defaultCriteria.dateFlexDays(),
                defaultCriteria.earnings(),
                defaultCriteria.distanceKm());
    }

    private SearchDisplay displayFor(OfferSearchMode mode) {
        return switch (mode) {
            case TIERHALTER -> new SearchDisplay(
                    FilterSearchBar.EarningsMode.MINIMUM,
                    "Finde liebevolle Tierhalter in deiner Nähe",
                    "Tierhalter",
                    "#f6ead5");
            case TIERSITTER -> new SearchDisplay(
                    FilterSearchBar.EarningsMode.MAXIMUM,
                    "Finde liebevolle Tiersitter in deiner Nähe",
                    "Tiersitter",
                    "#e2f2eb");
        };
    }

    private record SearchDisplay(
            FilterSearchBar.EarningsMode earningsMode,
            String pageTitle,
            String resultNoun,
            String leftBlobColor
    ) {
    }
}
