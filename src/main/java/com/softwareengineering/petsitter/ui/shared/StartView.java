package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
public class StartView extends VerticalLayout {

    private static final String DARK = "#4a3428";
    private static final String BROWN = "#7b5236";
    private static final String LIGHT_BG = "#fbf8f1";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";

    private DatePicker startDateField;
    private DatePicker endDateField;
    private NumberField minPriceField;
    private IntegerField maxDistanceField;
    private Button searchButton;

    private Div offerGrid;

    public StartView() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        getStyle()
                .set("overflow-x", "hidden");

        add(createPageWrapper());
    }

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
                createOfferSection(),
                createTrustBanner()
        );

        return wrapper;
    }

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
        copy.setWidth("760px");

        H1 title = new H1("Finde liebevolle Tiersitter in deiner Nähe");
        title.getStyle()
                .set("font-size", "42px")
                .set("line-height", "1.12")
                .set("letter-spacing", "1px")
                .set("margin", "0 0 14px 0")
                .set("color", DARK)
                .set("font-weight", "800");

        Paragraph subtitle = new Paragraph("Vertrauenswürdige Betreuung für Hunde, Katzen und Kleintiere.");
        subtitle.getStyle()
                .set("font-size", "18px")
                .set("margin", "0 0 28px 0")
                .set("color", "#7b7069");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button primaryButton = new Button("Jetzt Sitter finden");
        primaryButton.getStyle()
                .set("background", BROWN)
                .set("color", "white")
                .set("border-radius", "26px")
                .set("padding", "0 34px")
                .set("height", "50px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        primaryButton.addClickListener(event -> onFindSitterClicked());

        Button secondaryButton = new Button("Auftrag anbieten");
        secondaryButton.getStyle()
                .set("background", "white")
                .set("color", BROWN)
                .set("border", "1px solid #eadfce")
                .set("border-radius", "26px")
                .set("padding", "0 30px")
                .set("height", "50px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        secondaryButton.addClickListener(event -> onCreateOfferClicked());

        actions.add(primaryButton, secondaryButton);

        copy.add(title, subtitle, actions);

        Div statBox = new Div();
        statBox.getStyle()
                .set("background", "white")
                .set("border-radius", "28px")
                .set("padding", "26px 34px")
                .set("width", "210px")
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
        statHeadline.getElement().setProperty("innerHTML", "128 neue<br>Betreuer<br>in deiner<br>Umgebung");
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

        Hr line = new Hr();
        line.getStyle()
                .set("margin", "44px 0 0 0")
                .set("border", "none")
                .set("border-top", "1px solid #eadfce");

        hero.add(heroTop, createSearchBar(), line);

        return hero;
    }

    private Component createSearchBar() {
        HorizontalLayout search = new HorizontalLayout();
        search.setAlignItems(FlexComponent.Alignment.END);
        search.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        search.setSpacing(true);

        search.getStyle()
                .set("width", "100%")
                .set("max-width", "920px")
                .set("min-height", "82px")
                .set("background", "white")
                .set("border-radius", "32px")
                .set("box-shadow", "0 12px 30px rgba(74, 52, 40, 0.08)")
                .set("padding", "14px 18px")
                .set("box-sizing", "border-box")
                .set("margin", "36px auto 0 auto");

        startDateField = new DatePicker("Von");
        startDateField.setPlaceholder("Startdatum");
        startDateField.setValue(LocalDate.now().plusDays(7));
        styleSearchField(startDateField);

        endDateField = new DatePicker("Bis");
        endDateField.setPlaceholder("Enddatum");
        endDateField.setValue(LocalDate.now().plusDays(10));
        styleSearchField(endDateField);

        minPriceField = new NumberField("Verdienst ab");
        minPriceField.setPlaceholder("80 €");
        minPriceField.setValue(80.0);
        minPriceField.setStep(5.0);
        minPriceField.setSuffixComponent(new Span("€"));
        styleSearchField(minPriceField);

        maxDistanceField = new IntegerField("Umkreis");
        maxDistanceField.setPlaceholder("5 km");
        maxDistanceField.setValue(5);
        maxDistanceField.setMin(1);
        maxDistanceField.setMax(100);
        maxDistanceField.setStepButtonsVisible(true);
        maxDistanceField.setSuffixComponent(new Span("km"));
        styleSearchField(maxDistanceField);

        searchButton = new Button(new Icon(VaadinIcon.SEARCH));
        searchButton.setAriaLabel("Suche starten");
        searchButton.getStyle()
                .set("width", "52px")
                .set("height", "52px")
                .set("min-width", "52px")
                .set("border-radius", "50%")
                .set("background", BROWN)
                .set("color", "white")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("margin-bottom", "2px");
        searchButton.addClickListener(event -> onSearchClicked());

        search.add(
                startDateField,
                endDateField,
                minPriceField,
                maxDistanceField,
                searchButton
        );

        return search;
    }

    private void styleSearchField(Component component) {
        component.getStyle()
                .set("min-width", "150px")
                .set("flex", "1");
    }

    private Component createOfferSection() {
        Div section = new Div();

        section.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("max-width", "1180px")
                .set("margin", "34px auto 0 auto")
                .set("padding", "0 32px 40px 32px")
                .set("box-sizing", "border-box");

        H2 heading = new H2("Aktuelle Tiersitter-Angebote");
        heading.getStyle()
                .set("font-size", "30px")
                .set("margin", "0 0 22px 0")
                .set("color", DARK);

        offerGrid = new Div();
        offerGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(310px, 1fr))")
                .set("gap", "38px");

        renderOffers(getDemoOffers());

        section.add(heading, offerGrid);
        return section;
    }

    private void renderOffers(List<Offer> offers) {
        offerGrid.removeAll();

        offers.forEach(offer -> offerGrid.add(offerCard(offer)));
    }

    private Component offerCard(Offer offer) {
        Div card = new Div();

        card.getStyle()
                .set("background", "white")
                .set("border-radius", "26px")
                .set("box-shadow", CARD_SHADOW)
                .set("overflow", "hidden")
                .set("min-height", "310px")
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

        Div imageArea = new Div();
        imageArea.getStyle()
                .set("height", "150px")
                .set("background", offer.topColor())
                .set("position", "relative")
                .set("border-radius", "16px")
                .set("margin", "14px 14px 0 14px")
                .set("overflow", "hidden");

        Span rating = new Span(stars(offer.stars()));
        rating.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("left", "14px")
                .set("background", "rgba(69, 77, 62, 0.55)")
                .set("color", "#ffdf4a")
                .set("font-size", "13px")
                .set("letter-spacing", "2px")
                .set("border-radius", "16px")
                .set("padding", "6px 13px")
                .set("z-index", "2");

        Span verified = new Span("✓ Verifiziert");
        verified.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px")
                .set("background", "white")
                .set("color", "#6b9a75")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("border-radius", "16px")
                .set("padding", "7px 14px")
                .set("z-index", "2");

        Span animalSpan = new Span(offer.animal());
        animalSpan.getStyle()
                .set("position", "absolute")
                .set("top", "42px")
                .set("left", "50%")
                .set("transform", "translateX(-50%)")
                .set("font-size", "56px")
                .set("z-index", "2");

        Div stripe = new Div();
        stripe.getStyle()
                .set("position", "absolute")
                .set("left", "0")
                .set("right", "0")
                .set("bottom", "0")
                .set("height", "54px")
                .set("background", offer.stripeColor())
                .set("border-radius", "18px 18px 0 0")
                .set("z-index", "1");

        imageArea.add(rating, verified, animalSpan, stripe);

        Div body = new Div();
        body.getStyle()
                .set("padding", "18px 22px 20px 22px");

        H3 cardTitle = new H3(offer.title());
        cardTitle.getStyle()
                .set("font-size", "21px")
                .set("line-height", "1.2")
                .set("margin", "0 0 14px 0")
                .set("color", DARK);

        HorizontalLayout facts = new HorizontalLayout();
        facts.setWidthFull();
        facts.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        facts.setAlignItems(FlexComponent.Alignment.START);

        facts.add(
                fact("Zeitraum", offer.date()),
                fact("Verdienst", offer.price()),
                fact("Entfernung", offer.distance())
        );

        HorizontalLayout bottom = new HorizontalLayout();
        bottom.setWidthFull();
        bottom.setAlignItems(FlexComponent.Alignment.CENTER);
        bottom.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottom.getStyle().set("margin-top", "18px");

        Button detailsButton = new Button("Details ansehen");
        detailsButton.getStyle()
                .set("height", "32px")
                .set("background", "#f5e3bf")
                .set("color", DARK)
                .set("border-radius", "18px")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "0 20px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        detailsButton.addClickListener(event -> onOfferDetailsClicked(offer));

        Button favoriteButton = new Button(new Icon(VaadinIcon.HEART_O));
        favoriteButton.setAriaLabel("Als Favorit markieren");
        favoriteButton.getStyle()
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("background", "transparent")
                .set("color", BROWN)
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        favoriteButton.addClickListener(event -> onFavoriteClicked(offer));

        bottom.add(detailsButton, favoriteButton);

        body.add(cardTitle, facts, bottom);
        card.add(imageArea, body);

        return card;
    }

    private Component fact(String label, String value) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#7d746c");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "800")
                .set("color", DARK);

        box.add(labelSpan, valueSpan);
        return box;
    }

    private Component createTrustBanner() {
        HorizontalLayout banner = new HorizontalLayout();
        banner.setAlignItems(FlexComponent.Alignment.CENTER);
        banner.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        banner.getStyle()
                .set("position", "relative")
                .set("z-index", "3")
                .set("max-width", "1180px")
                .set("width", "calc(100% - 64px)")
                .set("margin", "40px auto 18px auto")
                .set("background", "#fff1d8")
                .set("border-radius", "28px")
                .set("box-shadow", "0 14px 40px rgba(74, 52, 40, 0.10)")
                .set("padding", "28px 36px")
                .set("box-sizing", "border-box");

        VerticalLayout text = new VerticalLayout();
        text.setPadding(false);
        text.setSpacing(false);

        H2 title = new H2("Sicher betreut, transparent bezahlt");
        title.getStyle()
                .set("font-size", "28px")
                .set("margin", "0 0 8px 0")
                .set("color", DARK);

        Paragraph desc = new Paragraph(
                "Pawsitter kombiniert verifizierte Accounts, Bewertungsprofile und klare Auftragsdetails, damit Tierhalter und Tiersitter schnell Vertrauen aufbauen."
        );
        desc.getStyle()
                .set("font-size", "16px")
                .set("max-width", "680px")
                .set("margin", "0")
                .set("color", "#7b7069");

        text.add(title, desc);

        VerticalLayout checks = new VerticalLayout();
        checks.setPadding(false);
        checks.setSpacing(true);

        checks.add(
                checkItem("Verifizierte Profile", "#6f9b6e"),
                checkItem("Sternebewertungen", "#5e8ca0"),
                checkItem("Faire Vergütung", "#e5a36f")
        );

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.setAriaLabel("Hinweis schließen");
        closeButton.getStyle()
                .set("background", "transparent")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("font-size", "20px")
                .set("cursor", "pointer");

        closeButton.addClickListener(event -> banner.setVisible(false));

        banner.add(text, checks, closeButton);
        return banner;
    }

    private Component checkItem(String text, String color) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);

        Span dot = new Span("✓");
        dot.getStyle()
                .set("width", "24px")
                .set("height", "24px")
                .set("border-radius", "50%")
                .set("background", color)
                .set("color", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "13px")
                .set("font-weight", "800");

        Span label = new Span(text);
        label.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "800")
                .set("color", DARK);

        item.add(dot, label);
        return item;
    }


    private Button pillButton(String text, String background, String color) {
        Button button = new Button(text);

        button.getStyle()
                .set("height", "46px")
                .set("padding", "0 42px")
                .set("border-radius", "28px")
                .set("background", background)
                .set("color", color)
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");

        return button;
    }

    private String stars(int filled) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            builder.append(i < filled ? "★" : "☆");
        }

        return builder.toString();
    }

    private List<Offer> getDemoOffers() {
        return List.of(
                new Offer(1L, "🐕", "Hundesitting für Bruno", "15.–18. Juni", "145 €", "2,4 km entfernt", "#dec18d", "#b69d70", 4),
                new Offer(2L, "🐈", "Katzenbetreuung Mia", "20.–22. Juni", "90 €", "1,8 km entfernt", "#f1b47a", "#c49368", 3),
                new Offer(3L, "🦮", "Gassi-Service vormittags", "Mo–Fr, 8–10 Uhr", "120 €", "3,1 km entfernt", "#93b8c9", "#7595a5", 4),
                new Offer(4L, "🐕‍🦺", "Hundebetreuung am Wochenende", "22.–23. Juni", "110 €", "4,0 km entfernt", "#94b883", "#6f9164", 4),
                new Offer(5L, "🐰", "Kleintierpflege gesucht", "24.–27. Juni", "75 €", "2,9 km entfernt", "#f1dfb9", "#c1b399", 2),
                new Offer(6L, "🐱", "Katzenbesuch abends", "18.–21. Juni", "85 €", "1,2 km entfernt", "#bad6df", "#91aab3", 3),
                new Offer(7L, "🐹", "Kleintierbetreuung am Abend", "28.–30. Juni", "65 €", "3,7 km entfernt", "#f4d6a7", "#c9a77b", 4),
                new Offer(8L, "🐶", "Urlaubsbetreuung für Hund", "01.–07. Juli", "260 €", "5,0 km entfernt", "#d7c59a", "#aa9367", 5),
                new Offer(9L, "🐈‍⬛", "Katzenfütterung morgens", "03.–06. Juli", "70 €", "0,9 km entfernt", "#a9c6bd", "#7f9f94", 4)
        );
    }

    /*
     * Backend-/Service-Hooks:
     * Diese Methoden sind bewusst vorbereitet.
     * Hier kann dein Team später Navigation, Service-Calls, Repository-Zugriffe
     * oder State-Updates ergänzen.
     */

    private void onSearchClicked() {
        LocalDate startDate = startDateField.getValue();
        LocalDate endDate = endDateField.getValue();
        Double minPrice = minPriceField.getValue();
        Integer maxDistance = maxDistanceField.getValue();

        System.out.println("Suche gestartet:");
        System.out.println("Von: " + startDate);
        System.out.println("Bis: " + endDate);
        System.out.println("Verdienst ab: " + minPrice);
        System.out.println("Umkreis: " + maxDistance);

        // TODO: Filter-Parameter an Backend übergeben, z. B.:
        // offerService.searchOffers(startDate, endDate, minPrice, maxDistance);
        UI.getCurrent().navigate("petsitter-suche");
    }

    private void onOfferDetailsClicked(Offer offer) {
        System.out.println("Details geöffnet für Offer-ID: " + offer.id());

        // TODO:
        // UI.getCurrent().navigate("angebote/" + offer.id());
    }

    private void onFavoriteClicked(Offer offer) {
        System.out.println("Favorit geklickt für Offer-ID: " + offer.id());

        // TODO:
        // favoriteService.toggleFavorite(offer.id());
    }

    private void onFindSitterClicked() {
        System.out.println("Tiersitter finden geklickt");

        // TODO:
        // UI.getCurrent().navigate("tiersitter-finden");
    }

    private void onFindOwnerClicked() {
        System.out.println("Tierhalter finden geklickt");

        // TODO:
        // UI.getCurrent().navigate("tierhalter-finden");
    }

    private void onCreateOfferClicked() {
        System.out.println("Auftrag anbieten geklickt");
        UI.getCurrent().navigate("auftrag-erstellen");
    }

    private void onProfileClicked() {
        System.out.println("Profil geklickt");

        // TODO:
        // UI.getCurrent().navigate("profil");
    }

    private void onLogoClicked() {
        System.out.println("Logo geklickt");

        // TODO:
        // UI.getCurrent().navigate("");
    }

    private void onFooterLinkClicked(String route) {
        System.out.println("Footer-Link geklickt: " + route);

        // TODO:
        // UI.getCurrent().navigate(route);
    }

    private void onSocialClicked(String platform) {
        System.out.println("Social-Link geklickt: " + platform);

        // TODO:
        // Externe Links öffnen, z. B.:
        // UI.getCurrent().getPage().open("https://...");
    }

    private record Offer(
            Long id,
            String animal,
            String title,
            String date,
            String price,
            String distance,
            String topColor,
            String stripeColor,
            int stars
    ) {
    }
}