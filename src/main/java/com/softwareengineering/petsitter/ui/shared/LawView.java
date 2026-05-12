package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "info", layout = MainLayout.class)
@PageTitle("Pawsitter – Informationen")
public class LawView extends VerticalLayout {

    private static final String DARK        = "#4a3428";
    private static final String BROWN       = "#7b5236";
    private static final String LIGHT_BG    = "#fbf8f1";
    private static final String CARD_BG     = "#ffffff";
    private static final String CARD_SHADOW = "0 4px 18px rgba(74, 52, 40, 0.10)";
    private static final String CARD_RADIUS = "14px";
    private static final String CARD_BORDER = "1.5px solid #f0e6d6";
    private static final String TEXT_MUTED  = "#7a6858";

    public LawView() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        getStyle()
                .set("background", LIGHT_BG)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK)
                .set("min-height", "100vh");

        // ── Decorative background circles (same style as StartView) ──────
        Div pageWrapper = new Div();
        pageWrapper.setWidthFull();
        pageWrapper.getStyle()
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("background", LIGHT_BG)
                .set("padding-bottom", "48px");

        Div circleTR = new Div();
        circleTR.getStyle()
                .set("position", "absolute").set("top", "-80px").set("right", "-80px")
                .set("width", "260px").set("height", "260px")
                .set("border-radius", "50%")
                .set("background", "rgba(141, 179, 195, 0.22)")
                .set("pointer-events", "none");

        Div circleBL = new Div();
        circleBL.getStyle()
                .set("position", "absolute").set("bottom", "-60px").set("left", "-60px")
                .set("width", "200px").set("height", "200px")
                .set("border-radius", "50%")
                .set("background", "rgba(246, 227, 189, 0.30)")
                .set("pointer-events", "none");

        // ── Centered content wrapper ──────────────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setMaxWidth("900px");
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("margin", "0 auto")
                .set("padding", "40px 24px 0 24px")
                .set("box-sizing", "border-box");

        // ── Row 1: Über uns + Kontakt ──────────────────────────────────────
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setSpacing(true);
        topRow.getStyle()
                .set("gap", "24px")
                .set("align-items", "stretch")
                .set("margin-bottom", "24px");

        topRow.add(buildUeberUnsCard(), buildKontaktCard());

        // ── Row 2: Impressum ──────────────────────────────────────────────
        Div impressumCard = buildImpressumCard();

        // ── Row 3: Datenschutz ────────────────────────────────────────────
        Div datenschutzCard = buildDatenschutzCard();

        content.add(topRow, impressumCard, datenschutzCard);

        pageWrapper.add(circleTR, circleBL, content);
        add(pageWrapper);
    }

    // ── Über uns Card ───────────────────────────────────────────────────────
    private Div buildUeberUnsCard() {
        Div card = createCard();
        card.getStyle().set("flex", "1");

        H2 title = new H2("Über uns");
        title.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "800")
                .set("margin", "0 0 12px 0")
                .set("color", DARK);

        Paragraph p1 = styledParagraph(
                "Gegründet, um Tierhalter und Sitter sicher und einfach zu verbinden. " +
                "Bei Pawsitter steht das Vertrauen im Mittelpunkt.");

        Paragraph p2 = styledParagraph(
                "Unsere Mission ist es, eine liebevolle Gemeinschaft in der " +
                "Nachbarschaft aufzubauen, in der jedes Haustier die " +
                "Aufmerksamkeit bekommt, die es verdient.");

        UnorderedList list = new UnorderedList();
        list.getStyle().set("margin", "8px 0 16px 18px").set("padding", "0").set("color", TEXT_MUTED).set("font-size", "14px");
        list.add(new ListItem("Liebevolle Betreuung in deiner direkten Nachbarschaft."));
        list.add(new ListItem("Geprüfte Qualität durch verifizierte Profile."));
        list.add(new ListItem("Einfache Prozesse für Tierhalter und Sitter."));

        Paragraph teamLabel = styledParagraph("Das Team hinter Pawsitter:");
        teamLabel.getStyle().set("margin-bottom", "4px");

        UnorderedList teamList = new UnorderedList();
        teamList.getStyle().set("margin", "0 0 0 18px").set("padding", "0").set("color", TEXT_MUTED).set("font-size", "14px");
        teamList.add(new ListItem("Josef Roland Hermann Lautner"));
        teamList.add(new ListItem("Luis Jonah Schirmbeck"));
        teamList.add(new ListItem("Kim Vivien Reger"));

        card.add(title, p1, p2, list, teamLabel, teamList);
        return card;
    }

    // ── Kontakt Card ────────────────────────────────────────────────────────
    private Div buildKontaktCard() {
        Div card = createCard();
        card.getStyle().set("flex", "1");

        H2 title = new H2("Kontakt");
        title.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "800")
                .set("margin", "0 0 12px 0")
                .set("color", DARK);

        Paragraph intro = styledParagraph("Hast du Fragen oder benötigst du Hilfe?\nUnser Team ist für dich da!");
        intro.getStyle().set("white-space", "pre-line");

        Div emailLine  = contactLine("E-Mail: team@software-engineering.de");
        Div phoneLine  = contactLine("Telefon: +49 (0) 123 456789 (Mo–Fr: 9:00 – 17:00 Uhr)");
        Div addressLine = contactLine("Standort: DHBW Heilbronn");

        card.add(title, intro, emailLine, phoneLine, addressLine);
        return card;
    }

    // ── Impressum Card ──────────────────────────────────────────────────────
    private Div buildImpressumCard() {
        Div card = createCard();
        card.setWidthFull();
        card.getStyle().set("margin-bottom", "24px");

        H2 title = new H2("Impressum (gemäß §5 TMG)");
        title.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "800")
                .set("margin", "0 0 18px 0")
                .set("color", DARK);

        Div address = blockText(
                "Pawsitter GmbH\nStreetstreet 1\n37050 Heilbronn");

        Div rep = new Div();
        rep.getStyle().set("margin-top", "18px");
        Paragraph repLabel = styledParagraph("Vertreten durch:");
        repLabel.getStyle().set("margin-bottom", "2px");
        Paragraph repNames = styledParagraph("Josef Roland Herrmann Lautner, Luis Jonah Schirmbeck, Kim Vivien Reger");
        rep.add(repLabel, repNames);

        Div register = new Div();
        register.getStyle().set("margin-top", "18px");
        Paragraph regLabel = styledParagraph("Registereintrag:");
        regLabel.getStyle().set("margin-bottom", "2px");
        Div regDetails = blockText(
                "Handelsregister: NDI: 0244488\nAmtsgericht: Heilbronn\nUST-IdNr.: 11000056759");
        register.add(regLabel, regDetails);

        card.add(title, address, rep, register);
        return card;
    }

    // ── Datenschutz Card ────────────────────────────────────────────────────
    private Div buildDatenschutzCard() {
        Div card = createCard();
        card.setWidthFull();

        H2 title = new H2("Datenschutzerklärung");
        title.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "800")
                .set("margin", "0 0 18px 0")
                .set("color", DARK);

        Paragraph p1 = styledParagraph(
                "Der Schutz Ihrer persönlichen Daten ist uns ein besonderes Anliegen. " +
                "Wir verarbeiten Ihre Daten daher ausschließlich auf Grundlage der gesetzlichen Bestimmungen " +
                "(DSGVO, TMG). In diesen Datenschutzinformationen informieren wir Sie über die wichtigsten " +
                "Aspekte der Datenverarbeitung im Rahmen unserer App.");

        H3 h1 = sectionHeading("1. Verantwortliche Stelle");
        Paragraph resp = styledParagraph("Pawsitter GmbH, Streetstreet 1, 37050 Heilbronn. E-Mail: team@software-engineering.de");

        H3 h2 = sectionHeading("2. Welche Daten wir erheben");
        Paragraph dataTypes = styledParagraph(
                "Wir erheben und verarbeiten folgende Kategorien personenbezogener Daten: " +
                "Name, E-Mail-Adresse, Profilbild, Standortdaten (nur bei aktivierter Standortfreigabe), " +
                "Bewertungen und Nachrichten innerhalb der Plattform.");

        H3 h3 = sectionHeading("3. Zweck der Datenverarbeitung");
        Paragraph purpose = styledParagraph(
                "Ihre Daten werden ausschließlich zur Erbringung unserer Vermittlungsleistung zwischen " +
                "Tierhaltern und Tiersittern verwendet sowie zur Verbesserung unserer Plattform.");

        H3 h4 = sectionHeading("4. Ihre Rechte");
        Paragraph rights = styledParagraph(
                "Sie haben das Recht auf Auskunft, Berichtigung, Löschung und Einschränkung der Verarbeitung " +
                "Ihrer gespeicherten Daten. Wenden Sie sich dazu jederzeit an: team@software-engineering.de");

        card.add(title, p1, h1, resp, h2, dataTypes, h3, purpose, h4, rights);
        return card;
    }

    // ── Shared helpers ───────────────────────────────────────────────────────

    private Div createCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", CARD_RADIUS)
                .set("border", CARD_BORDER)
                .set("box-shadow", CARD_SHADOW)
                .set("padding", "28px 32px")
                .set("box-sizing", "border-box");
        return card;
    }

    private Paragraph styledParagraph(String text) {
        Paragraph p = new Paragraph(text);
        p.getStyle()
                .set("margin", "0 0 10px 0")
                .set("font-size", "14px")
                .set("color", TEXT_MUTED)
                .set("line-height", "1.6");
        return p;
    }

    private Div contactLine(String text) {
        Div line = new Div();
        Paragraph p = new Paragraph(text);
        p.getStyle()
                .set("margin", "12px 0 0 0")
                .set("font-size", "14px")
                .set("color", TEXT_MUTED);
        line.add(p);
        return line;
    }

    private Div blockText(String text) {
        Div block = new Div();
        Paragraph p = new Paragraph(text);
        p.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", TEXT_MUTED)
                .set("white-space", "pre-line")
                .set("line-height", "1.7");
        block.add(p);
        return block;
    }

    private H3 sectionHeading(String text) {
        H3 h = new H3(text);
        h.getStyle()
                .set("font-size", "1rem")
                .set("font-weight", "700")
                .set("margin", "20px 0 6px 0")
                .set("color", DARK);
        return h;
    }
}
