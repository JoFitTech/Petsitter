package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mein Profil | Pawsitter")
@PermitAll
public class UserView extends VerticalLayout {

    private static final String DARK     = "#4a3428";
    private static final String CREAM    = "#fbf8f1";
    private static final String CARD_BG  = "#ffffff";

    private final UserService userService;

    private Button btnUeberMich;
    private Button btnMeineTiere;
    private Button btnMeineAuftraege;
    private Button btnMeineFavoriten;
    private Button btnPersAngaben;
    private Button btnLogout;
    private Div contentPanel;

    public UserView(UserService userService) {
        this.userService = userService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
            .set("background", CREAM)
            .set("font-family", "Inter, Arial, sans-serif")
            .set("color", DARK);

        add(buildPageHeader());
        add(buildMainArea());
        showUeberMich();
        setActiveStyle(btnUeberMich);
    }

    // ── Page header ──────────────────────────────────────────────────────────
    private Component buildPageHeader() {
        Div wrapper = new Div();
        wrapper.getStyle()
            .set("padding", "48px 48px 24px 48px")
            .set("background", CREAM);

        H1 title = new H1("Mein Profil");
        title.getStyle()
            .set("margin", "0 0 6px 0")
            .set("font-size", "28px")
            .set("font-weight", "800")
            .set("color", DARK);

        Paragraph subtitle = new Paragraph(
            "Verwalte deine öffentlichen Angaben, Bewertungen und Kontoeinstellungen.");
        subtitle.getStyle()
            .set("margin", "0")
            .set("font-size", "14px")
            .set("color", "#8a7060");

        wrapper.add(title, subtitle);
        return wrapper;
    }

    // ── Main area (sidebar + content) ────────────────────────────────────────
    private Component buildMainArea() {
        // Outer wrapper with decorative circle
        Div outer = new Div();
        outer.setWidthFull();
        outer.getStyle()
            .set("position", "relative")
            .set("padding", "0 48px 64px 48px")
            .set("box-sizing", "border-box");

        // Decorative circle top-right
        Div deco = new Div();
        deco.getStyle()
            .set("position", "absolute")
            .set("width", "260px")
            .set("height", "260px")
            .set("border-radius", "50%")
            .set("background", "#c8dde6")
            .set("opacity", "0.5")
            .set("top", "-60px")
            .set("right", "-30px")
            .set("pointer-events", "none")
            .set("z-index", "0");
        outer.add(deco);

        HorizontalLayout main = new HorizontalLayout();
        main.setWidthFull();
        main.setPadding(false);
        main.setSpacing(false);
        main.getStyle().set("gap", "24px").set("align-items", "flex-start").set("position", "relative").set("z-index", "1");

        contentPanel = new Div();
        contentPanel.setWidthFull();

        main.add(buildSidebar(), contentPanel);
        outer.add(main);
        return outer;
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────
    private Component buildSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setWidth("210px");
        sidebar.setMinWidth("210px");
        sidebar.getStyle()
            .set("gap", "6px")
            .set("padding", "8px 0")
            .set("background", "transparent");

        btnUeberMich      = sidebarBtn("Über mich");
        btnMeineTiere     = sidebarBtn("Meine Tiere");
        btnMeineAuftraege = sidebarBtn("Meine Aufträge");
        btnMeineFavoriten = sidebarBtn("Meine Favoriten");
        btnPersAngaben    = sidebarBtn("Persönliche Angaben");
        btnLogout         = sidebarBtn("Log out");

        btnUeberMich.addClickListener(e      -> { setActiveStyle(btnUeberMich);      showUeberMich(); });
        btnMeineTiere.addClickListener(e     -> { setActiveStyle(btnMeineTiere);     showMeineTiere(); });
        btnMeineAuftraege.addClickListener(e -> { setActiveStyle(btnMeineAuftraege); showMeineAuftraege(); });
        btnMeineFavoriten.addClickListener(e -> { setActiveStyle(btnMeineFavoriten); showMeineFavoriten(); });
        btnPersAngaben.addClickListener(e    -> { setActiveStyle(btnPersAngaben);    showPersAngaben(false); });
        btnLogout.addClickListener(e         -> { setActiveStyle(btnLogout);         handleLogout(); });

        sidebar.add(btnUeberMich, btnMeineTiere, btnMeineAuftraege, btnMeineFavoriten, btnPersAngaben, btnLogout);
        return sidebar;
    }

    private Button sidebarBtn(String text) {
        Button btn = new Button(text);
        btn.setWidthFull();
        btn.getStyle()
            .set("justify-content", "flex-start")
            .set("height", "48px")
            .set("padding", "0 20px")
            .set("border-radius", "12px")
            .set("background", "transparent")
            .set("color", DARK)
            .set("font-weight", "600")
            .set("font-size", "14px")
            .set("box-shadow", "none")
            .set("cursor", "pointer");
        return btn;
    }

    private void setActiveStyle(Button active) {
        for (Button b : new Button[]{btnUeberMich, btnMeineTiere, btnMeineAuftraege, btnMeineFavoriten, btnPersAngaben, btnLogout}) {
            b.getStyle().set("background", "transparent").set("color", DARK);
        }
        active.getStyle().set("background", DARK).set("color", "white");
    }

    // ── Shared panel wrapper ─────────────────────────────────────────────────
    private Div cardPanel() {
        Div panel = new Div();
        panel.setWidthFull();
        panel.getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("padding", "36px")
            .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
            .set("box-sizing", "border-box");
        return panel;
    }

    private HorizontalLayout panelHeader(String titleText, Component... actions) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "28px");

        H2 title = new H2(titleText);
        title.getStyle().set("margin", "0").set("font-size", "22px").set("font-weight", "800");

        HorizontalLayout actionRow = new HorizontalLayout();
        actionRow.setSpacing(false);
        actionRow.getStyle().set("gap", "10px");
        for (Component a : actions) actionRow.add(a);

        row.add(title, actionRow);
        return row;
    }

    private Button editBtn() {
        Button btn = new Button("Bearbeiten", new Icon(VaadinIcon.PENCIL));
        btn.getStyle()
            .set("border-radius", "24px")
            .set("background", DARK)
            .set("color", "white")
            .set("box-shadow", "none")
            .set("font-weight", "600")
            .set("font-size", "13px")
            .set("padding", "0 20px")
            .set("height", "38px")
            .set("cursor", "pointer");
        return btn;
    }

    private Button saveBtn(String label) {
        Button btn = new Button(label);
        btn.getStyle()
            .set("border-radius", "24px")
            .set("background", DARK)
            .set("color", "white")
            .set("box-shadow", "none")
            .set("font-weight", "600")
            .set("height", "38px")
            .set("cursor", "pointer");
        return btn;
    }

    private Button cancelBtn(String label) {
        Button btn = new Button(label);
        btn.getStyle()
            .set("border-radius", "24px")
            .set("background", "#e8ddd4")
            .set("color", DARK)
            .set("box-shadow", "none")
            .set("font-weight", "600")
            .set("height", "38px")
            .set("cursor", "pointer");
        return btn;
    }

    private Hr divider() {
        Hr hr = new Hr();
        hr.getStyle().set("margin", "24px 0").set("border-color", "#ead5ae");
        return hr;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 1 – ÜBER MICH (View mode)
    // ══════════════════════════════════════════════════════════════════════════
    private void showUeberMich() {
        contentPanel.removeAll();
        Div panel = cardPanel();

        Button bearbeiten = editBtn();
        bearbeiten.addClickListener(e -> showUeberMichEdit());
        panel.add(panelHeader("Über mich", bearbeiten));

        panel.add(buildAvatarCard(false, null));
        panel.add(divider());
        panel.add(buildBioSection(false, null));
        panel.add(divider());
        panel.add(buildReviews());

        contentPanel.add(panel);
    }

    // TAB 1 – ÜBER MICH (Edit mode)
    private void showUeberMichEdit() {
        contentPanel.removeAll();
        Div panel = cardPanel();

        // Edit-mode fields
        TextField nameField   = styledTextField("Name", "Max Mustermann");
        TextField petsField   = styledTextField("Meine Haustiere", "2 Hunde");
        TextField locationField = styledTextField("Ort", "76689 Neuthard");
        TextField langField   = styledTextField("Sprache", "deutsch");
        TextArea  bioArea     = styledTextArea("Über mich");
        bioArea.setValue("Hallo, ich bin Max und betreue seit mehreren Jahren Hunde und Katzen.\n" +
                         "Mir sind Vertrauen, klare Absprachen und ein liebevoller Umgang besonders wichtig.");

        Button save = saveBtn("Speichern");
        Button cancel = cancelBtn("Abbrechen");

        // TODO: Backend – save listener
        save.addClickListener(e -> {
            System.out.println("TODO: userService.updateProfile(name=" + nameField.getValue() + ")");
            showUeberMich();
        });
        cancel.addClickListener(e -> showUeberMich());

        panel.add(panelHeader("Über mich", cancel, save));
        panel.add(buildAvatarCard(true, null));
        panel.add(divider());

        // Editable info fields
        Span infoLabel = new Span("Angaben bearbeiten:");
        infoLabel.getStyle().set("font-weight", "700").set("font-size", "15px").set("color", DARK);
        panel.add(infoLabel);

        VerticalLayout fields = new VerticalLayout();
        fields.setPadding(false);
        fields.setSpacing(false);
        fields.getStyle().set("gap", "12px").set("margin-top", "16px");
        fields.add(nameField, petsField, locationField, langField);
        panel.add(fields);

        panel.add(divider());
        panel.add(buildBioSection(true, bioArea));
        panel.add(divider());
        panel.add(buildReviews());

        contentPanel.add(panel);
    }

    // Avatar card (shared between view and edit mode)
    private Component buildAvatarCard(boolean editMode, Object unused) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#fffdf8")
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "16px")
            .set("padding", "24px 28px")
            .set("display", "flex")
            .set("gap", "28px")
            .set("align-items", "flex-start");

        // Avatar
        Div avatarWrap = new Div();
        avatarWrap.getStyle().set("position", "relative").set("flex-shrink", "0");

        Div avatar = new Div();
        avatar.getStyle()
            .set("width", "96px").set("height", "96px")
            .set("border-radius", "50%")
            .set("background", "#d4b896")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("overflow", "hidden");

        // SVG person placeholder
        Div svgWrap = new Div();
        svgWrap.getElement().setProperty("innerHTML",
            "<svg width='56' height='56' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<circle cx='12' cy='8' r='4' fill='#a07850'/>" +
            "<path d='M4 20c0-4 3.6-7 8-7s8 3 8 7' fill='#a07850'/></svg>");
        avatar.add(svgWrap);
        avatarWrap.add(avatar);

        if (editMode) {
            // Camera overlay button
            Div camOverlay = new Div();
            camOverlay.getStyle()
                .set("position", "absolute")
                .set("bottom", "2px").set("right", "2px")
                .set("width", "28px").set("height", "28px")
                .set("border-radius", "50%")
                .set("background", DARK)
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("cursor", "pointer");
            Icon cam = new Icon(VaadinIcon.CAMERA);
            cam.setSize("13px");
            cam.getStyle().set("color", "white");
            camOverlay.add(cam);
            avatarWrap.add(camOverlay);

            // Hidden upload – backend team hooks into addSucceededListener
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
            upload.setMaxFiles(1);
            upload.setMaxFileSize(5 * 1024 * 1024);
            upload.getStyle().set("display", "none");
            // TODO: Backend – upload.addSucceededListener(ev -> userService.updateProfilePicture(buffer.getInputStream(), ev.getFileName()));

            // Trigger file dialog when camera overlay is clicked
            camOverlay.getElement().executeJs(
                "this.addEventListener('click', () => { var inp = $0.querySelector('input[type=file]'); if(inp) inp.click(); })",
                upload.getElement());
            avatarWrap.add(upload);
        }

        // Right side: stars, verified, name, info
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("gap", "4px");

        // Stars (static)
        Span stars = new Span("★★★★★");
        stars.getStyle().set("color", "#f5c842").set("font-size", "20px").set("letter-spacing", "2px");

        // Verified badge
        Span verified = new Span("✓ Verifiziert");
        verified.getStyle()
            .set("color", "#4caf50").set("font-size", "13px").set("font-weight", "600");

        // Top row: stars + verified
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topRow.add(stars, verified);

        H3 name = new H3("Max Mustermann");
        name.getStyle()
            .set("margin", "4px 0 8px 0")
            .set("font-size", "20px")
            .set("font-weight", "800");

        Span pets = styledInfoLine("🐾 Meine Haustiere: 2 Hunde");
        Span loc  = styledInfoLine("📍 76689 Neuthard");
        Span lang = styledInfoLine("🌐 deutsch");

        info.add(topRow, name, pets, loc, lang);
        card.add(avatarWrap, info);
        return card;
    }

    private Span styledInfoLine(String text) {
        Span s = new Span(text);
        s.getStyle().set("font-size", "14px").set("color", "#5a4030").set("font-weight", "500");
        return s;
    }

    // Bio section
    private Component buildBioSection(boolean editMode, TextArea existingArea) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "12px");

        Span label = new Span("Ich und meine Tiere:");
        label.getStyle().set("font-weight", "700").set("font-size", "15px").set("color", DARK);
        section.add(label);

        if (editMode && existingArea != null) {
            existingArea.setWidthFull();
            existingArea.setMinHeight("120px");
            section.add(existingArea);
        } else {
            Div bioBox = new Div();
            bioBox.getStyle()
                .set("background", "#fffdf8")
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "12px")
                .set("padding", "18px 20px")
                .set("font-size", "14px")
                .set("line-height", "1.7")
                .set("color", "#5a4030")
                .set("min-height", "100px");

            Paragraph line1 = new Paragraph("Hallo, ich bin Max und betreue seit mehreren Jahren Hunde und Katzen.");
            Paragraph line2 = new Paragraph("Mir sind Vertrauen, klare Absprachen und ein liebevoller Umgang besonders wichtig.");
            line1.getStyle().set("margin", "0 0 6px 0");
            line2.getStyle().set("margin", "0");
            bioBox.add(line1, line2);
            section.add(bioBox);
        }
        return section;
    }

    // Reviews section
    private Component buildReviews() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "14px");

        H3 reviewTitle = new H3("Meine Bewertungen");
        reviewTitle.getStyle().set("margin", "0 0 8px 0").set("font-size", "20px").set("font-weight", "800");
        section.add(reviewTitle);

        section.add(reviewCard("★★★★★ Sehr zuverlässig",
            "\u201EBruno war bestens betreut. Kommunikation und \u00DCbergabe waren super unkompliziert.\u201C"));
        section.add(reviewCard("★★★★★ Freundlich und flexibel",
            "\u201ESehr angenehmer Kontakt, unsere Katze Mia hat sich schnell wohlgef\u00FChlt.\u201C"));
        return section;
    }

    private Div reviewCard(String headline, String text) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#fffdf8")
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "14px")
            .set("padding", "18px 22px");

        Span h = new Span(headline);
        h.getStyle().set("font-weight", "700").set("font-size", "14px").set("color", DARK).set("display", "block");

        Span t = new Span(text);
        t.getStyle().set("font-size", "13px").set("color", "#7a6050").set("margin-top", "6px").set("display", "block");

        card.add(h, t);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 2 – MEINE TIERE (Placeholder)
    // ══════════════════════════════════════════════════════════════════════════
    private void showMeineTiere() {
        contentPanel.removeAll();
        Div panel = cardPanel();
        panel.add(panelHeader("Meine Tiere"));
        panel.add(placeholder("Dieser Bereich wird noch implementiert."));
        contentPanel.add(panel);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 3 – MEINE AUFTRÄGE (Placeholder)
    // ══════════════════════════════════════════════════════════════════════════
    private void showMeineAuftraege() {
        contentPanel.removeAll();
        Div panel = cardPanel();
        panel.add(panelHeader("Meine Aufträge"));
        panel.add(placeholder("Dieser Bereich wird noch implementiert."));
        // TODO: Backend – userService.getMyOrders() laden und anzeigen
        System.out.println("TODO: userService.getMyOrders()");
        contentPanel.add(panel);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 4 – MEINE FAVORITEN (Placeholder)
    // ══════════════════════════════════════════════════════════════════════════
    private void showMeineFavoriten() {
        contentPanel.removeAll();
        Div panel = cardPanel();
        panel.add(panelHeader("Meine Favoriten"));
        panel.add(placeholder("Dieser Bereich wird noch implementiert."));
        // TODO: Backend – userService.getMyFavorites() laden und anzeigen
        System.out.println("TODO: userService.getMyFavorites()");
        contentPanel.add(panel);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 3 – PERSÖNLICHE ANGABEN (View mode)
    // ══════════════════════════════════════════════════════════════════════════
    private void showPersAngaben(boolean editMode) {
        contentPanel.removeAll();
        Div panel = cardPanel();

        if (!editMode) {
            Button bearbeiten = editBtn();
            bearbeiten.addClickListener(e -> showPersAngaben(true));
            panel.add(panelHeader("Persönliche Angaben", bearbeiten));

            // View rows
            panel.add(buildDataRow("Name",          "Max Mustermann",        false, null));
            panel.add(buildDataRow("Anzeigename",   "Max",                   false, null));
            panel.add(buildDataRow("Email",         "Max@gmail.com",         false, null));
            panel.add(buildDataRow("Telefonnummer", "0151 8765456783",       false, null));
            panel.add(buildDataRow("Geburtsdatum",  "02.05.2004",            false, null));
            panel.add(buildDataRow("Nationalität",  "deutsch",               false, null));
            panel.add(buildDataRow("Adresse",
                "Mustermann Straße 7\n76689 Neuthard\nDeutschland",          false, null));
        } else {
            // Edit fields
            TextField nameField  = styledTextField("Name",          "Max Mustermann");
            TextField nickField  = styledTextField("Anzeigename",   "Max");
            TextField mailField  = styledTextField("Email",         "Max@gmail.com");
            TextField phoneField = styledTextField("Telefonnummer", "0151 8765456783");
            TextField dateField  = styledTextField("Geburtsdatum",  "02.05.2004");
            TextField natField   = styledTextField("Nationalität",  "deutsch");
            TextArea  adrField   = styledTextArea("Adresse");
            adrField.setValue("Mustermann Straße 7\n76689 Neuthard\nDeutschland");

            Button save   = saveBtn("Speichern");
            Button cancel = cancelBtn("Abbrechen");

            // TODO: Backend – save listener
            save.addClickListener(e -> {
                System.out.println("TODO: userService.updatePersonalData(name=" + nameField.getValue() + ", email=" + mailField.getValue() + ")");
                showPersAngaben(false);
            });
            cancel.addClickListener(e -> showPersAngaben(false));

            panel.add(panelHeader("Persönliche Angaben", cancel, save));
            panel.add(buildDataRow("Name",          null, true, nameField));
            panel.add(buildDataRow("Anzeigename",   null, true, nickField));
            panel.add(buildDataRow("Email",         null, true, mailField));
            panel.add(buildDataRow("Telefonnummer", null, true, phoneField));
            panel.add(buildDataRow("Geburtsdatum",  null, true, dateField));
            panel.add(buildDataRow("Nationalität",  null, true, natField));
            panel.add(buildDataRow("Adresse",       null, true, adrField));
        }

        contentPanel.add(panel);
    }

    private Component buildDataRow(String label, String value, boolean editMode, Component editField) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.START);
        row.getStyle().set("padding", "14px 0").set("gap", "16px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-weight", "700")
            .set("font-size", "14px")
            .set("color", DARK)
            .set("min-width", "150px")
            .set("flex-shrink", "0");

        if (editMode && editField != null) {
            editField.getElement().getStyle().set("flex", "1");
            row.add(labelSpan, editField);
        } else {
            // Support multi-line values
            VerticalLayout valLayout = new VerticalLayout();
            valLayout.setPadding(false);
            valLayout.setSpacing(false);
            valLayout.getStyle().set("gap", "2px").set("flex", "1");
            if (value != null) {
                for (String line : value.split("\n")) {
                    Span v = new Span(line);
                    v.getStyle().set("font-size", "14px").set("color", "#5a4030");
                    valLayout.add(v);
                }
            }
            row.add(labelSpan, valLayout);
        }

        Hr hr = new Hr();
        hr.getStyle().set("margin", "0").set("border-color", "#e8ddd4");

        wrapper.add(row, hr);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 6 – LOG OUT
    // ══════════════════════════════════════════════════════════════════════════
    private void handleLogout() {
        // TODO: Backend – AuthService.logout() / SecurityContextHolder.clearContext() aufrufen
        System.out.println("TODO: AuthService.logout()");
        UI.getCurrent().navigate("login");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private Div placeholder(String msg) {
        Div d = new Div();
        d.getStyle()
            .set("padding", "48px 0")
            .set("text-align", "center")
            .set("color", "#a08060")
            .set("font-size", "15px");
        d.add(new Span(msg));
        return d;
    }

    private TextField styledTextField(String label, String placeholder) {
        TextField tf = new TextField(label);
        tf.setPlaceholder(placeholder);
        tf.setValue(placeholder);
        tf.setWidthFull();
        return tf;
    }

    private TextArea styledTextArea(String label) {
        TextArea ta = new TextArea(label);
        ta.setWidthFull();
        ta.setMinHeight("100px");
        return ta;
    }
}
