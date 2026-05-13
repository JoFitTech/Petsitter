package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
public class StartView extends VerticalLayout {

    private static final String DARK       = "#4a3428";
    private static final String BROWN      = "#7b5236";
    private static final String LIGHT_BG   = "#fbf8f1";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";

    private final OfferService offerService;

    @Autowired
    public StartView(OfferService offerService) {
        this.offerService = offerService;
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
                .set("background", "#f6ead5")
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

        HorizontalLayout heroTop = new HorizontalLayout();
        heroTop.setWidthFull();
        heroTop.setAlignItems(FlexComponent.Alignment.START);
        heroTop.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout copy = new VerticalLayout();
        copy.setPadding(false);
        copy.setSpacing(false);
        copy.setWidth("720px");

        H1 title = new H1("Finde liebevolle Tierhalter in deiner Nähe");
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
                "128 neue<br>Halter<br>in deiner<br>Umgebung");
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

        Div searchBar = createSearchBar();

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

        Div wannField = filterPill("📅", "Wann?", "15.–18. Juni", true, this::onFilterWannClicked);
        Div div1 = verticalDivider();
        Div verdienstField = filterPill("€", "Verdienst", "ab 80 €", false, this::onFilterVerdienstClicked);
        Div div2 = verticalDivider();
        Div entfernungField = filterPill("↕", "Entfernung", "bis 5 km", false, this::onFilterEntfernungClicked);

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

    private Div filterPill(String icon, String label, String value, boolean first, Runnable onClick) {
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

        H2 heading = new H2("Aktuelle Tierhalter-Angebote");
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

        List<OfferCardDto> offers = offerService.getOpenOffersByType(OfferType.OWNER_OFFER);
        offers.forEach(dto -> grid.add(new OfferCardComponent(dto, this::openOfferDialog, this::onFavoriteClicked)));

        section.add(heading, grid);
        return section;
    }

    // ── Backend-Interface hooks ───────────────────────────────────────────

    private void onSearchClicked() {
        UI.getCurrent().navigate("petsitter-suche", com.vaadin.flow.router.QueryParameters.of("mode", "tierhalter"));
    }

    private void onFilterWannClicked() {
        // TODO: Datepicker-Dialog öffnen oder direkt Feld aktivieren
    }

    private void onFilterVerdienstClicked() {
        // TODO: Verdienst-Eingabefeld öffnen / fokussieren
    }

    private void onFilterEntfernungClicked() {
        // TODO: Entfernungs-Eingabefeld öffnen / fokussieren
    }

    private void onCreateOfferClicked() {
        UI.getCurrent().navigate("auftrag-erstellen", com.vaadin.flow.router.QueryParameters.of("mode", "offer"));
    }

    private void openOfferDialog(OfferCardDto dto) {
        new PetsitterDetailPopUp(dto, "–", 4).open();
    }

    private void onFavoriteClicked(OfferCardDto dto) {
        // TODO: favoriteService.toggleFavorite(dto.id());
    }
}
