package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "petsitter-suche", layout = MainLayout.class)
@PageTitle("Tierhalter finden | Pawsitter")
@PermitAll
public class PetsitterFilterView extends VerticalLayout {

    // ── Design tokens ──────────────────────────────────────────────────────────
    private static final String DARK        = "#4a3428";
    private static final String CREAM       = "#fbf8f1";
    private static final String ACCENT      = "#7b5236";
    private static final String WARM_GOLD   = "#d4a24c";
    private static final String CARD_BG     = "#ffffff";
    private static final String BORDER_CLR  = "#ead5ae";

    // ── Sample offer data (will be replaced by backend calls) ─────────────────
    private static final String[][] SAMPLE_OFFERS = {
        {"Wochenende mit Nala",   "15.–18. Juni",   "145 €",  "2,4 km",  "5",  "true",  "#c8a97a"},
        {"Katzenbetreuung Mia",   "20.–22. Juni",   "90 €",   "1,8 km",  "4",  "true",  "#d4a96a"},
        {"Wochenende mit Nala",   "28.–30. Juni",   "160 €",  "4,2 km",  "5",  "false", "#7a9e6a"},
        {"Kleintierpflege zuhause","12.–16. Juli",  "110 €",  "2,9 km",  "3",  "false", "#e8d5a0"},
        {"Wochenende mit Nala",   "02.–05. Aug.",   "145 €",  "3,1 km",  "5",  "true",  "#c8a97a"},
        {"Katzenbetreuung Mia",   "10.–12. Aug.",   "90 €",   "1,2 km",  "4",  "true",  "#d4a96a"},
    };

    // ── Sample map marker data ─────────────────────────────────────────────────
    private static final String[][] MAP_MARKERS = {
        {"145 €",    "62%",  "28%", "false"},
        {"2.506 €",  "56%",  "38%", "false"},
        {"3.578 €",  "60%",  "44%", "false"},
        {"3.947 €",  "52%",  "50%", "false"},
        {"3.355 €",  "48%",  "56%", "false"},
        {"1.801 €",  "60%",  "58%", "false"},
        {"1.988 €",  "56%",  "66%", "true" },
    };

    public PetsitterFilterView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
            .set("background", CREAM)
            .set("font-family", "Inter, Arial, sans-serif")
            .set("color", DARK)
            .set("position", "relative");

        add(buildDecoCircle());
        add(buildPageTitle());
        add(buildFilterBar());
        add(buildResultsLabel());
        add(buildContentArea());
    }

    // ── Decorative background circle ──────────────────────────────────────────
    private Component buildDecoCircle() {
        Div circle = new Div();
        circle.getStyle()
            .set("position", "absolute")
            .set("width", "320px")
            .set("height", "320px")
            .set("border-radius", "50%")
            .set("background", "#c8dde6")
            .set("opacity", "0.45")
            .set("top", "40px")
            .set("right", "0")
            .set("pointer-events", "none")
            .set("z-index", "0");
        return circle;
    }

    // ── Page title ────────────────────────────────────────────────────────────
    private Component buildPageTitle() {
        Div wrapper = new Div();
        wrapper.getStyle()
            .set("padding", "44px 48px 16px 48px")
            .set("position", "relative")
            .set("z-index", "1");

        H1 title = new H1("Finde liebevolle Tierhalter in deiner Nähe");
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "32px")
            .set("font-weight", "800")
            .set("color", DARK)
            .set("line-height", "1.25");

        wrapper.add(title);
        return wrapper;
    }

    // ── Filter bar ────────────────────────────────────────────────────────────
    private Component buildFilterBar() {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(false);
        row.getStyle()
            .set("padding", "0 48px 28px 48px")
            .set("gap", "16px")
            .set("position", "relative")
            .set("z-index", "1")
            .set("flex-wrap", "wrap");

        // ── Filter pill ──────────────────────────────────────────────────────
        Div pill = new Div();
        pill.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("background", CARD_BG)
            .set("border", "1.5px solid " + BORDER_CLR)
            .set("border-radius", "50px")
            .set("padding", "8px 22px")
            .set("gap", "0")
            .set("box-shadow", "0 4px 14px rgba(74,52,40,0.08)")
            .set("cursor", "default");

        // Wann?
        Div wannSection = buildFilterSection("📅", "Wann?", "15.–18. Juni");
        wannSection.addClickListener(e ->
            System.out.println("TODO: filterService.openDatePicker()"));

        // Separator
        Div sep1 = separator();

        // Verdienst
        Div verdienstSection = buildFilterSection("€", "Verdienst", "ab 80 €");
        verdienstSection.addClickListener(e ->
            System.out.println("TODO: filterService.openEarningsPicker()"));

        // Separator
        Div sep2 = separator();

        // Entfernung
        Div entfernungSection = buildFilterSection("↕", "Entfernung", "bis 5 km");
        entfernungSection.addClickListener(e ->
            System.out.println("TODO: filterService.openDistancePicker()"));

        // Search icon
        Div searchIcon = new Div();
        searchIcon.getStyle()
            .set("margin-left", "14px")
            .set("cursor", "pointer")
            .set("display", "flex")
            .set("align-items", "center")
            .set("color", ACCENT);
        Icon magnifier = new Icon(VaadinIcon.SEARCH);
        magnifier.setSize("18px");
        searchIcon.add(magnifier);
        searchIcon.addClickListener(e ->
            System.out.println("TODO: filterService.applyQuickSearch()"));

        pill.add(wannSection, sep1, verdienstSection, sep2, entfernungSection, searchIcon);

        // ── Filter button ────────────────────────────────────────────────────
        Button filterBtn = new Button("Filter");
        Icon filterIcon = new Icon(VaadinIcon.FILTER);
        filterIcon.setSize("16px");
        filterBtn.setIcon(filterIcon);
        filterBtn.getStyle()
            .set("height", "52px")
            .set("padding", "0 32px")
            .set("border-radius", "50px")
            .set("background", DARK)
            .set("color", "white")
            .set("font-weight", "700")
            .set("font-size", "15px")
            .set("box-shadow", "0 6px 18px rgba(74,52,40,0.25)")
            .set("cursor", "pointer")
            .set("gap", "8px");
        filterBtn.addClickListener(e ->
            System.out.println("TODO: filterService.openFilterDialog()"));

        row.add(pill, filterBtn);
        return row;
    }

    /** Creates a single filter segment inside the pill */
    private Div buildFilterSection(String iconText, String label, String value) {
        Div section = new Div();
        section.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "8px")
            .set("padding", "4px 18px")
            .set("cursor", "pointer");

        Span icon = new Span(iconText);
        icon.getStyle().set("font-size", "15px");

        VerticalLayout text = new VerticalLayout();
        text.setPadding(false);
        text.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "10px")
            .set("color", "#a08060")
            .set("font-weight", "600")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0.5px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "13px")
            .set("font-weight", "700")
            .set("color", DARK);

        text.add(labelSpan, valueSpan);
        section.add(icon, text);
        return section;
    }

    private Div separator() {
        Div sep = new Div();
        sep.getStyle()
            .set("width", "1px")
            .set("height", "28px")
            .set("background", BORDER_CLR);
        return sep;
    }

    // ── Results count label ───────────────────────────────────────────────────
    private Component buildResultsLabel() {
        Div wrapper = new Div();
        wrapper.getStyle()
            .set("padding", "0 48px 20px 48px")
            .set("position", "relative")
            .set("z-index", "1");

        H2 label = new H2("Über 40 Tierhalter in deiner nähe");
        label.getStyle()
            .set("margin", "0")
            .set("font-size", "20px")
            .set("font-weight", "800")
            .set("color", DARK);
        wrapper.add(label);
        return wrapper;
    }

    // ── Main content area: offer grid + map ───────────────────────────────────
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

        // Left: grid of offer tiles
        Div grid = buildOfferGrid();

        // Right: map panel
        Div map = buildMapPanel();

        area.add(grid, map);
        area.setFlexGrow(1, grid);
        return area;
    }

    // ── Offer tile grid ───────────────────────────────────────────────────────
    private Div buildOfferGrid() {
        Div grid = new Div();
        grid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(2, 1fr)")
            .set("gap", "20px")
            .set("flex", "1");

        for (String[] offer : SAMPLE_OFFERS) {
            grid.add(buildOfferTile(offer));
        }
        return grid;
    }

    /**
     * Builds a single offer tile.
     * offer[] layout: [title, zeitraum, verdienst, entfernung, stars, verified, color]
     */
    private Div buildOfferTile(String[] offer) {
        String title      = offer[0];
        String zeitraum   = offer[1];
        String verdienst  = offer[2];
        String entfernung = offer[3];
        int    stars      = Integer.parseInt(offer[4]);
        boolean verified  = Boolean.parseBoolean(offer[5]);
        String  cardColor = offer[6];

        Div tile = new Div();
        tile.getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("box-shadow", "0 6px 24px rgba(74,52,40,0.10)")
            .set("overflow", "hidden")
            .set("cursor", "pointer")
            .set("transition", "transform 0.18s ease, box-shadow 0.18s ease");

        // Hover effect via JS
        tile.getElement().executeJs(
            "this.addEventListener('mouseenter', () => { this.style.transform='translateY(-4px)'; this.style.boxShadow='0 12px 36px rgba(74,52,40,0.18)'; });" +
            "this.addEventListener('mouseleave', () => { this.style.transform='translateY(0)'; this.style.boxShadow='0 6px 24px rgba(74,52,40,0.10)'; });"
        );

        tile.addClickListener(e ->
            System.out.println("TODO: offerService.openOfferDetail(title=" + title + ")"));

        // Image placeholder area
        Div imgArea = new Div();
        imgArea.getStyle()
            .set("width", "100%")
            .set("height", "160px")
            .set("background", cardColor)
            .set("position", "relative")
            .set("border-radius", "0");

        // Stars badge (top-left)
        Div starsBadge = new Div();
        starsBadge.getStyle()
            .set("position", "absolute")
            .set("top", "12px")
            .set("left", "12px")
            .set("background", "rgba(255,255,255,0.88)")
            .set("backdrop-filter", "blur(4px)")
            .set("border-radius", "20px")
            .set("padding", "4px 10px")
            .set("font-size", "13px")
            .set("color", "#f5a623")
            .set("font-weight", "700")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < 5; i++) starStr.append(i < stars ? "★" : "☆");
        starsBadge.add(new Span(starStr.toString()));

        imgArea.add(starsBadge);

        // Verified badge (top-right)
        if (verified) {
            Div verifiedBadge = new Div();
            verifiedBadge.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px")
                .set("background", "rgba(255,255,255,0.92)")
                .set("backdrop-filter", "blur(4px)")
                .set("border-radius", "20px")
                .set("padding", "4px 12px")
                .set("font-size", "12px")
                .set("color", "#4caf50")
                .set("font-weight", "700")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
            verifiedBadge.add(new Span("✓ Verifiziert"));
            imgArea.add(verifiedBadge);
        }

        tile.add(imgArea);

        // Content area below image
        Div content = new Div();
        content.getStyle()
            .set("padding", "16px 18px 18px 18px");

        // Title row with heart
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.setPadding(false);
        titleRow.getStyle().set("margin-bottom", "12px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("font-weight", "800")
            .set("font-size", "15px")
            .set("color", DARK);

        Button heartBtn = new Button(new Icon(VaadinIcon.HEART_O));
        heartBtn.getStyle()
            .set("background", "transparent")
            .set("box-shadow", "none")
            .set("color", "#c07050")
            .set("cursor", "pointer")
            .set("padding", "0")
            .set("min-width", "unset")
            .set("width", "32px")
            .set("height", "32px");
        heartBtn.addClickListener(e ->
            System.out.println("TODO: favoriteService.toggleFavorite(offer=" + title + ")"));

        titleRow.add(titleSpan, heartBtn);
        content.add(titleRow);

        // Details: Zeitraum, Verdienst, Entfernung
        HorizontalLayout details = new HorizontalLayout();
        details.setSpacing(false);
        details.getStyle().set("gap", "20px").set("flex-wrap", "wrap");

        details.add(
            buildDetailItem("Zeitraum",   zeitraum),
            buildDetailItem("Verdienst",  verdienst),
            buildDetailItem("Entfernung", entfernung)
        );
        content.add(details);

        tile.add(content);
        return tile;
    }

    private Div buildDetailItem(String label, String value) {
        Div item = new Div();
        item.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "10px")
            .set("color", "#a08060")
            .set("font-weight", "600")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0.5px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "800")
            .set("color", DARK);

        item.add(labelSpan, valueSpan);
        return item;
    }

    // ── Map panel ─────────────────────────────────────────────────────────────
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

        // Map placeholder container
        Div mapArea = new Div();
        mapArea.getStyle()
            .set("width", "100%")
            .set("height", "640px")
            .set("background", "linear-gradient(135deg, #e8f0e8 0%, #d5e8d5 40%, #c8dfd8 70%, #b8cfd8 100%)")
            .set("position", "relative")
            .set("overflow", "hidden");

        // Simulated map grid lines (decorative)
        addMapGridLines(mapArea);

        // Simulated road/path lines (decorative)
        addMapRoads(mapArea);

        // Price markers
        for (String[] m : MAP_MARKERS) {
            mapArea.add(buildMapMarker(m[0], m[1], m[2], Boolean.parseBoolean(m[3])));
        }

        // Map controls (zoom buttons)
        Div controls = buildMapControls();
        mapArea.add(controls);

        // Map expand button (top-right)
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
        expandBtn.addClickListener(e ->
            System.out.println("TODO: mapService.openFullscreenMap()"));
        mapArea.add(expandBtn);

        // Attribution text (bottom)
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
        mapArea.add(attribution);

        mapWrapper.add(mapArea);
        return mapWrapper;
    }

    /** Renders a price bubble marker on the map */
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
            .set("white-space", "nowrap")
            .set("transition", "transform 0.15s ease");

        marker.add(new Span(price));
        marker.addClickListener(e ->
            System.out.println("TODO: mapService.selectMarker(price=" + price + ")"));

        // Hover effect
        marker.getElement().executeJs(
            "this.addEventListener('mouseenter', () => this.style.transform='translate(-50%,-50%) scale(1.1)');" +
            "this.addEventListener('mouseleave', () => this.style.transform='translate(-50%,-50%) scale(1)');"
        );
        return marker;
    }

    /** Zoom + / - controls */
    private Div buildMapControls() {
        Div ctrl = new Div();
        ctrl.getStyle()
            .set("position", "absolute")
            .set("right", "12px")
            .set("top", "56px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "2px");

        Div zoomIn  = mapControlBtn("+");
        Div zoomOut = mapControlBtn("−");

        zoomIn.addClickListener(e ->
            System.out.println("TODO: mapService.zoomIn()"));
        zoomOut.addClickListener(e ->
            System.out.println("TODO: mapService.zoomOut()"));

        ctrl.add(zoomIn, zoomOut);
        return ctrl;
    }

    private Div mapControlBtn(String symbol) {
        Div btn = new Div();
        btn.getStyle()
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
        btn.add(new Span(symbol));
        return btn;
    }

    // ── Decorative map grid lines ─────────────────────────────────────────────
    private void addMapGridLines(Div mapArea) {
        String[] verticals   = {"20%", "40%", "60%", "80%"};
        String[] horizontals = {"15%", "30%", "45%", "60%", "75%", "90%"};

        for (String pos : verticals) {
            Div line = new Div();
            line.getStyle()
                .set("position", "absolute").set("left", pos).set("top", "0")
                .set("width", "1px").set("height", "100%")
                .set("background", "rgba(160,180,160,0.35)");
            mapArea.add(line);
        }
        for (String pos : horizontals) {
            Div line = new Div();
            line.getStyle()
                .set("position", "absolute").set("top", pos).set("left", "0")
                .set("height", "1px").set("width", "100%")
                .set("background", "rgba(160,180,160,0.35)");
            mapArea.add(line);
        }
    }

    /** Adds a few simulated road paths for visual realism */
    private void addMapRoads(Div mapArea) {
        // Horizontal road
        Div road1 = new Div();
        road1.getStyle()
            .set("position", "absolute").set("top", "52%").set("left", "0")
            .set("height", "5px").set("width", "100%")
            .set("background", "rgba(255,255,240,0.7)")
            .set("border-top", "1px solid rgba(200,200,180,0.5)")
            .set("border-bottom", "1px solid rgba(200,200,180,0.5)");
        mapArea.add(road1);

        // Diagonal road
        Div road2 = new Div();
        road2.getStyle()
            .set("position", "absolute").set("top", "20%").set("left", "-10%")
            .set("height", "4px").set("width", "130%")
            .set("background", "rgba(255,255,240,0.6)")
            .set("transform", "rotate(22deg)")
            .set("transform-origin", "left center");
        mapArea.add(road2);

        // Vertical road
        Div road3 = new Div();
        road3.getStyle()
            .set("position", "absolute").set("left", "55%").set("top", "0")
            .set("width", "5px").set("height", "100%")
            .set("background", "rgba(255,255,240,0.6)");
        mapArea.add(road3);
    }
}
