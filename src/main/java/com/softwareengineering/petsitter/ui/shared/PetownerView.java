package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "tierhalter-finden", layout = MainLayout.class)
public class PetownerView extends VerticalLayout {

    private static final String DARK        = "#4a3428";
    private static final String BROWN       = "#7b5236";
    private static final String LIGHT_BG    = "#fbf8f1";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";

    public PetownerView() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("overflow-x", "hidden");

        add(createPageWrapper());
    }

    // ── Page wrapper ──────────────────────────────────────────────────────
    private Component createPageWrapper() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.getStyle()
                .set("position", "relative")
                .set("overflow-x", "hidden")
                .set("background", LIGHT_BG);

        wrapper.add(
                createBackgroundBlobs(),
                createHeroSection(),
                createOfferSection()
        );
        return wrapper;
    }

    // ── Background decorative blobs ───────────────────────────────────────
    private Component createBackgroundBlobs() {
        Div container = new Div();

        Div leftBlob = new Div();
        leftBlob.getStyle()
                .set("position", "absolute")
                .set("left", "-130px")
                .set("top", "80px")
                .set("width", "460px")
                .set("height", "460px")
                .set("background", "#d8ecd8")
                .set("border-radius", "50%")
                .set("z-index", "0");

        Div rightBlob = new Div();
        rightBlob.getStyle()
                .set("position", "absolute")
                .set("right", "-100px")
                .set("top", "130px")
                .set("width", "470px")
                .set("height", "470px")
                .set("background", "#e7f0f0")
                .set("border-radius", "50%")
                .set("z-index", "0");

        Div bottomBlob = new Div();
        bottomBlob.getStyle()
                .set("position", "absolute")
                .set("right", "-170px")
                .set("bottom", "120px")
                .set("width", "430px")
                .set("height", "430px")
                .set("background", "#fff0dd")
                .set("border-radius", "50%")
                .set("z-index", "0");

        container.add(leftBlob, rightBlob, bottomBlob);
        return container;
    }

    // ── Hero section ──────────────────────────────────────────────────────
    private Component createHeroSection() {
        Div hero = new Div();
        hero.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("max-width", "1180px")
                .set("margin", "60px auto 0 auto")
                .set("padding", "0 32px")
                .set("box-sizing", "border-box");

        // Top row: copy text + stat box
        HorizontalLayout heroTop = new HorizontalLayout();
        heroTop.setWidthFull();
        heroTop.setAlignItems(FlexComponent.Alignment.START);
        heroTop.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Left: headline, subtitle, button
        VerticalLayout copy = new VerticalLayout();
        copy.setPadding(false);
        copy.setSpacing(false);
        copy.setWidth("720px");

        H1 title = new H1("Finde liebevolle Tiersitter in deiner Nähe");
        title.getStyle()
                .set("font-size", "42px")
                .set("line-height", "1.12")
                .set("letter-spacing", "0.5px")
                .set("margin", "0 0 12px 0")
                .set("color", DARK)
                .set("font-weight", "800");

        Paragraph subtitle = new Paragraph("Vertrauenswürdige Betreuung für Hunde, Katzen und Kleintiere.");
        subtitle.getStyle()
                .set("font-size", "17px")
                .set("margin", "0 0 32px 0")
                .set("color", "#7b7069");

        // "Auftrag anbieten" button
        Button createOfferBtn = new Button();
        Span plusIcon = new Span("+ ");
        plusIcon.getStyle().set("font-size", "18px").set("font-weight", "900");
        Span btnLabel = new Span("Auftrag anbieten");
        createOfferBtn.getElement().appendChild(plusIcon.getElement(), btnLabel.getElement());
        createOfferBtn.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("padding", "0 30px")
                .set("height", "52px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "4px");
        createOfferBtn.addClickListener(event -> onCreateOfferClicked());

        copy.add(title, subtitle, createOfferBtn);

        // Right: stat box
        Div statBox = new Div();
        statBox.getStyle()
                .set("background", "white")
                .set("border-radius", "28px")
                .set("padding", "26px 34px")
                .set("width", "210px")
                .set("min-width", "210px")
                .set("margin-top", "10px")
                .set("box-shadow", CARD_SHADOW);

        Span available = new Span("Heute verfügbar");
        available.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("color", "#71946e")
                .set("margin-bottom", "8px");

        H2 statHeadline = new H2();
        statHeadline.getElement().setProperty("innerHTML",
                "12 neue<br>Sitter<br>in deiner<br>Umgebung");
        statHeadline.getStyle()
                .set("font-size", "26px")
                .set("line-height", "1.12")
                .set("margin", "0 0 14px 0")
                .set("color", DARK);

        Span rating = new Span("Ø 4,9 Sterne");
        rating.getStyle()
                .set("font-size", "15px")
                .set("color", "#6f6862");

        statBox.add(available, statHeadline, rating);
        heroTop.add(copy, statBox);

        // Search / filter bar
        Div searchBar = createSearchBar();

        // Separator
        Hr line = new Hr();
        line.getStyle()
                .set("margin", "44px 0 0 0")
                .set("border", "none")
                .set("border-top", "1px solid #eadfce");

        hero.add(heroTop, searchBar, line);
        return hero;
    }

    // ── Compact filter / search bar ───────────────────────────────────────
    private Div createSearchBar() {
        Div bar = new Div();
        bar.getStyle()
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

        // Wann? field
        Div wannField = filterPill(
                "📅", "Wann?", "15.–18. Juni", true,
                () -> onFilterWannClicked());

        // Divider
        Div div1 = verticalDivider();

        // Verdienst field
        Div verdienstField = filterPill(
                "€", "Verdienst", "ab 80 €", false,
                () -> onFilterVerdienstClicked());

        // Divider
        Div div2 = verticalDivider();

        // Entfernung field
        Div entfernungField = filterPill(
                "↕", "Entfernung", "bis 5 km", false,
                () -> onFilterEntfernungClicked());

        // Search icon button
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
        searchBtn.addClickListener(e -> onSearchClicked());

        bar.add(wannField, div1, verdienstField, div2, entfernungField, searchBtn);
        return bar;
    }

    /** A single "pill" item inside the search bar (icon + label + value). */
    private Div filterPill(String icon, String label, String value, boolean first,
                           Runnable onClick) {
        Div pill = new Div();
        pill.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("flex", "1")
                .set("padding", first ? "0 20px 0 0" : "0 20px")
                .set("cursor", "pointer");
        pill.addClickListener(e -> onClick.run());

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
                .set("color", "#9e8c7b")
                .set("font-weight", "700")
                .set("letter-spacing", "0.3px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", DARK);

        text.add(labelSpan, valueSpan);
        pill.add(iconSpan, text);
        return pill;
    }

    private Div verticalDivider() {
        Div d = new Div();
        d.getStyle()
                .set("width", "1px")
                .set("height", "36px")
                .set("background", "#e8ddd0")
                .set("flex-shrink", "0");
        return d;
    }

    // ── Offer cards section ───────────────────────────────────────────────
    private Component createOfferSection() {
        Div section = new Div();
        section.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("max-width", "1180px")
                .set("margin", "34px auto 0 auto")
                .set("padding", "0 32px 60px 32px")
                .set("box-sizing", "border-box");

        H2 heading = new H2("Aktuelle Tiersitter-Angebote");
        heading.getStyle()
                .set("font-size", "28px")
                .set("margin", "0 0 22px 0")
                .set("color", DARK)
                .set("font-weight", "800");

        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "32px");

        getDemoOffers().forEach(offer -> grid.add(offerCard(offer)));

        section.add(heading, grid);
        return section;
    }

    // ── Single offer card ─────────────────────────────────────────────────
    private Component offerCard(PetsitterOffer offer) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "24px")
                .set("box-shadow", CARD_SHADOW)
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        card.getElement().executeJs("""
                this.addEventListener('mouseenter', () => {
                    this.style.transform = 'translateY(-4px)';
                    this.style.boxShadow = '0 18px 42px rgba(74, 52, 40, 0.15)';
                });
                this.addEventListener('mouseleave', () => {
                    this.style.transform = 'translateY(0)';
                    this.style.boxShadow = '0 12px 30px rgba(74, 52, 40, 0.10)';
                });
                """);

        // Image area (solid colour block)
        Div imageArea = new Div();
        imageArea.getStyle()
                .set("height", "148px")
                .set("background", offer.topColor())
                .set("position", "relative")
                .set("border-radius", "14px")
                .set("margin", "12px 12px 0 12px")
                .set("overflow", "hidden");

        // Stars badge (top-left)
        Span starsBadge = new Span(stars(offer.stars()));
        starsBadge.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("left", "12px")
                .set("background", "rgba(60, 60, 50, 0.50)")
                .set("color", "#ffdf4a")
                .set("font-size", "12px")
                .set("letter-spacing", "1.5px")
                .set("border-radius", "14px")
                .set("padding", "5px 11px");

        // Verified badge (top-right) – only when verified
        if (offer.verified()) {
            Span verifiedBadge = new Span("✓ Verifiziert");
            verifiedBadge.getStyle()
                    .set("position", "absolute")
                    .set("top", "10px")
                    .set("right", "10px")
                    .set("background", "white")
                    .set("color", "#6b9a75")
                    .set("font-size", "12px")
                    .set("font-weight", "700")
                    .set("border-radius", "14px")
                    .set("padding", "5px 12px");
            imageArea.add(starsBadge, verifiedBadge);
        } else {
            imageArea.add(starsBadge);
        }

        // Card body
        Div body = new Div();
        body.getStyle().set("padding", "16px 18px 18px 18px");

        // Title row: title + heart icon
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getStyle().set("margin-bottom", "10px");

        H3 cardTitle = new H3(offer.title());
        cardTitle.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("line-height", "1.2")
                .set("margin", "0")
                .set("color", DARK);

        Button heartBtn = new Button(new Icon(VaadinIcon.HEART_O));
        heartBtn.setAriaLabel("Als Favorit markieren");
        heartBtn.getStyle()
                .set("width", "34px")
                .set("height", "34px")
                .set("min-width", "34px")
                .set("border-radius", "50%")
                .set("background", "transparent")
                .set("color", "#b0a090")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        heartBtn.addClickListener(e -> onFavoriteClicked(offer));
        // Prevent heart click from bubbling up to the card click
        heartBtn.getElement().addEventListener("click", ev -> {}).addEventData("event.stopPropagation()");

        titleRow.add(cardTitle, heartBtn);

        // Facts row: Zeitraum | Verdienst | Entfernung
        HorizontalLayout facts = new HorizontalLayout();
        facts.setWidthFull();
        facts.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        facts.setAlignItems(FlexComponent.Alignment.START);
        facts.getStyle().set("gap", "4px");

        facts.add(
                factItem("Zeitraum",   offer.date()),
                factItem("Verdienst",  offer.price()),
                factItem("Entfernung", offer.distance())
        );

        body.add(titleRow, facts);
        card.add(imageArea, body);

        // Open detail dialog when clicking anywhere on the card
        card.addClickListener(e -> openOfferDialog(offer));

        return card;
    }

    private Component factItem(String label, String value) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "11px")
                .set("color", "#9e8c7b");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "800")
                .set("color", DARK);

        box.add(labelSpan, valueSpan);
        return box;
    }

    // ── Star helper ───────────────────────────────────────────────────────
    private String stars(int filled) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < filled ? "★" : "☆");
        }
        return sb.toString();
    }

    // ── Demo data ─────────────────────────────────────────────────────────
    private List<PetsitterOffer> getDemoOffers() {
        return List.of(
                new PetsitterOffer(1L, "Marie, 31, zeitlich flexibel",   "15.–18. Juni",    "145 €", "2,4 km", "#dec18d", 5, true),
                new PetsitterOffer(2L, "Katzenbetreuung Mia, sonntags",  "20.–22. Juni",    "90 €",  "1,8 km", "#f1b47a", 4, true),
                new PetsitterOffer(3L, "Gassi-Service vormittags",        "Mo–Fr, 8–10 Uhr", "120 €", "3,1 km", "#93b8c9", 5, true),
                new PetsitterOffer(4L, "Wochenende mit Nala",             "28.–30. Juni",    "160 €", "4,2 km", "#94b883", 5, true),
                new PetsitterOffer(5L, "Kleintierpflege zuhause",         "12.–16. Juli",    "110 €", "2,9 km", "#f1dfb9", 3, false),
                new PetsitterOffer(6L, "Urlaubsbetreuung Katze",          "01.–07. Juli",    "210 €", "5,0 km", "#bad6df", 3, true)
        );
    }

    // ── Backend-Interface hooks ───────────────────────────────────────────
    /*
     * Diese Methoden sind bewusst als Schnittstellen vorbereitet.
     * Das Backend-Team kann hier später Service-Calls oder Repository-Zugriffe ergänzen.
     */

    private void onSearchClicked() {
        System.out.println("Suche (Lupe) geklickt – Filter anwenden");
        // TODO: Filterwerte auslesen und Tiersitter-Liste vom Backend laden
    }

    private void onFilterWannClicked() {
        System.out.println("Filter 'Wann?' geklickt");
        // TODO: Datepicker-Dialog öffnen
    }

    private void onFilterVerdienstClicked() {
        System.out.println("Filter 'Verdienst' geklickt");
        // TODO: Verdienst-Range-Eingabe öffnen
    }

    private void onFilterEntfernungClicked() {
        System.out.println("Filter 'Entfernung' geklickt");
        // TODO: Entfernungs-Slider öffnen
    }

    private void onCreateOfferClicked() {
        System.out.println("Auftrag anbieten geklickt (PetownerView)");
        UI.getCurrent().navigate("auftrag-erstellen");
    }

    private void openOfferDialog(PetsitterOffer offer) {
        System.out.println("Kachel angeklickt – öffne Details für Tiersitter-ID: " + offer.id());
        // TODO: PetsitterDetailPopUp (oder einen spezifischen Tierhalter-Detail-Dialog) öffnen
        new PetsitterDetailPopUp(
                offer.id(), offer.title(), offer.date(),
                offer.price(), offer.distance(),
                offer.topColor(), offer.stars()
        ).open();
    }

    private void onFavoriteClicked(PetsitterOffer offer) {
        System.out.println("Favorit geklickt für Tiersitter-ID: " + offer.id());
        // TODO: favoriteService.toggleFavorite(offer.id());
    }

    // ── Data record ───────────────────────────────────────────────────────
    private record PetsitterOffer(
            Long    id,
            String  title,
            String  date,
            String  price,
            String  distance,
            String  topColor,
            int     stars,
            boolean verified
    ) {}
}
