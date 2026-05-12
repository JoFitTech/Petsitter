package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Registrierungs-Seite – kein MainLayout, eigenständige Seite.
 *
 * Backend-Schnittstellen (alle als TODO markiert):
 *  - "Registrieren & Starten"-Button: UserService.register(vorname, nachname, email, ...)
 *  - "Bereits registriert? Hier einloggen"-Link: Navigation zur Login-Route
 */
@Route("register")
@PageTitle("Registrierung | Pawsitter")
@AnonymousAllowed
public class RegistrateView extends VerticalLayout {

    private static final String DARK      = "#4a3428";
    private static final String CREAM     = "#e8d9c8";
    private static final String CARD_BG   = "#ffffff";
    private static final String INPUT_BG  = "#f5e9d6";
    private static final String BROWN_BTN = "#5c3d1e";
    private static final String LINK_CLR  = "#7b5236";

    public RegistrateView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        getStyle()
                .set("background", CREAM)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK)
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("min-height", "100vh")
                .set("padding", "32px 0");

        // ── Decorative background circles ─────────────────────────────────────
        add(decoCircle("220px", "220px", "#d4c4b0", "-60px", null,  null,  "-40px")); // top-left
        add(decoCircle("300px", "300px", "#b8d4cc", "-40px", "-40px", null, null));   // top-right (mint)
        add(decoCircle("260px", "260px", "#c8b89a", null,   null,  "-60px", "-50px")); // bottom-left
        add(decoCircle("200px", "200px", "#d4c4b0", null,   "-30px", "-50px", null)); // bottom-right

        // ── Card ──────────────────────────────────────────────────────────────
        add(buildCard());
    }

    // ── Decorative circle ──────────────────────────────────────────────────────
    private Div decoCircle(String w, String h, String color,
                           String top, String right, String bottom, String left) {
        Div circle = new Div();
        circle.getStyle()
                .set("position", "fixed")
                .set("width", w)
                .set("height", h)
                .set("border-radius", "50%")
                .set("background", color)
                .set("opacity", "0.55")
                .set("pointer-events", "none")
                .set("z-index", "0");
        if (top    != null) circle.getStyle().set("top",    top);
        if (right  != null) circle.getStyle().set("right",  right);
        if (bottom != null) circle.getStyle().set("bottom", bottom);
        if (left   != null) circle.getStyle().set("left",   left);
        return circle;
    }

    // ── Card ──────────────────────────────────────────────────────────────────
    private Div buildCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "24px")
                .set("padding", "40px 44px 36px 44px")
                .set("width", "100%")
                .set("max-width", "620px")
                .set("box-shadow", "0 12px 48px rgba(74, 52, 40, 0.13)")
                .set("box-sizing", "border-box")
                .set("position", "relative")
                .set("z-index", "1")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "0");

        card.add(buildLogo());
        card.add(buildTitle());
        card.add(buildForm());

        return card;
    }

    // ── Logo ──────────────────────────────────────────────────────────────────
    private HorizontalLayout buildLogo() {
        HorizontalLayout logoRow = new HorizontalLayout();
        logoRow.setAlignItems(FlexComponent.Alignment.CENTER);
        logoRow.setSpacing(false);
        logoRow.getStyle()
                .set("gap", "10px")
                .set("margin-bottom", "16px")
                .set("cursor", "pointer");

        Image logoImg = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        logoImg.getStyle().set("height", "48px").set("width", "auto");

        Span logoText = new Span("Pawsitter");
        logoText.getStyle()
                .set("font-size", "26px")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("letter-spacing", "-0.5px");

        logoRow.add(logoImg, logoText);
        logoRow.addClickListener(e -> UI.getCurrent().navigate(""));
        return logoRow;
    }

    // ── Title ─────────────────────────────────────────────────────────────────
    private H1 buildTitle() {
        H1 title = new H1("Erstelle jetzt dein kostenloses Pawsitter Konto");
        title.getStyle()
                .set("margin", "0 0 20px 0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("text-align", "left")
                .set("width", "100%")
                .set("line-height", "1.3");
        return title;
    }

    // ── Full form ─────────────────────────────────────────────────────────────
    private VerticalLayout buildForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidthFull();
        form.setPadding(false);
        form.setSpacing(false);
        form.getStyle().set("gap", "12px");

        // ── Persönliche Angaben ────────────────────────────────────────────
        H2 personalHeading = sectionHeading("Persönliche Angaben");

        // Row 1: Vorname | Nachname
        TextField vornameField = pillTextField("Vorname");
        TextField nachnameField = pillTextField("Nachname");
        HorizontalLayout row1 = twoColRow(vornameField, nachnameField);

        // Row 2: E-Mail | Telefonnummer
        EmailField emailField = new EmailField();
        emailField.setPlaceholder("E-Mail");
        styleEmailField(emailField);
        TextField telefonField = pillTextField("Telefonnummer");
        HorizontalLayout row2 = twoColRow(emailField, telefonField);

        // Row 3: Passwort | Passwort bestätigen (warmer Hintergrund – active look)
        PasswordField passwortField = new PasswordField();
        passwortField.setPlaceholder("Passwort");
        passwortField.setWidthFull();
        stylePasswordField(passwortField);

        PasswordField passwortConfirmField = new PasswordField();
        passwortConfirmField.setPlaceholder("Passwort bestätigen");
        passwortConfirmField.setWidthFull();
        stylePasswordField(passwortConfirmField);

        HorizontalLayout row3 = twoColRow(passwortField, passwortConfirmField);

        // Row 4: Geburtstag | Nationalität
        DatePicker geburtstagsField = new DatePicker();
        geburtstagsField.setPlaceholder("Geburtstag");
        geburtstagsField.setWidthFull();
        styleDateField(geburtstagsField.getElement());

        TextField nationalitaetField = pillTextField("Nationalität");
        HorizontalLayout row4 = twoColRow(geburtstagsField, nationalitaetField);

        // ── Standort ──────────────────────────────────────────────────────
        H2 standortHeading = sectionHeading("Standort");

        // Row 5: Straße | Hausnummer
        TextField strasseField = pillTextField("Straße");
        TextField hausnummerField = pillTextField("Hausnummer");
        HorizontalLayout row5 = twoColRow(strasseField, hausnummerField);

        // Row 6: Postleitzahl | Ort (single visual field with divider)
        HorizontalLayout row6 = buildPlzOrtRow();

        // Row 7: Land (half width, centered)
        TextField landField = pillTextField("Land");
        landField.getStyle().set("width", "50%").set("min-width", "200px");

        HorizontalLayout row7 = new HorizontalLayout(landField);
        row7.setWidthFull();
        row7.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        // ── Register button ────────────────────────────────────────────────
        Button registerBtn = new Button("Registrieren & Starten");
        registerBtn.setWidthFull();
        registerBtn.getStyle()
                .set("background", BROWN_BTN)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("height", "52px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("margin-top", "10px")
                .set("letter-spacing", "0.3px");
        registerBtn.addClickListener(e -> {
            // TODO: Backend – UserService.register(vornameField.getValue(), nachnameField.getValue(),
            //                                      emailField.getValue(), telefonField.getValue(),
            //                                      passwortField.getValue(), geburtstagsField.getValue(),
            //                                      nationalitaetField.getValue(), strasseField.getValue(),
            //                                      hausnummerField.getValue(), landField.getValue())
            System.out.println("TODO: UserService.register() – Registrierung wird verarbeitet");
        });

        // ── "Bereits registriert?" link ────────────────────────────────────
        Button loginBtn = new Button("Bereits registriert? Hier einloggen");
        loginBtn.getStyle()
                .set("background", "transparent")
                .set("color", LINK_CLR)
                .set("box-shadow", "none")
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("padding", "0")
                .set("height", "auto")
                .set("cursor", "pointer")
                .set("text-decoration", "underline")
                .set("margin-top", "4px");
        loginBtn.addClickListener(e -> {
            System.out.println("TODO: Navigate to login");
            UI.getCurrent().navigate("login");
        });

        form.add(
                personalHeading,
                row1, row2, row3, row4,
                standortHeading,
                row5, row6, row7,
                registerBtn, loginBtn
        );
        return form;
    }

    // ── Postleitzahl | Ort – single rounded container with divider ────────────
    private HorizontalLayout buildPlzOrtRow() {
        TextField plzField = new TextField();
        plzField.setPlaceholder("Postleitzahl");
        plzField.getStyle()
                .set("flex", "1")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "0")
                .set("border", "none")
                .set("box-shadow", "none");
        plzField.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "0");

        Span divider = new Span("|");
        divider.getStyle()
                .set("color", "#b8a898")
                .set("align-self", "center")
                .set("flex-shrink", "0")
                .set("font-size", "18px")
                .set("line-height", "1")
                .set("padding", "0 4px");

        TextField ortField = new TextField();
        ortField.setPlaceholder("Ort");
        ortField.getStyle()
                .set("flex", "1")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "0")
                .set("border", "none")
                .set("box-shadow", "none");
        ortField.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "0");

        HorizontalLayout combined = new HorizontalLayout(plzField, divider, ortField);
        combined.setWidthFull();
        combined.setAlignItems(FlexComponent.Alignment.CENTER);
        combined.setSpacing(false);
        combined.getStyle()
                .set("background", INPUT_BG)
                .set("border-radius", "28px")
                .set("padding", "0 12px")
                .set("box-sizing", "border-box")
                .set("border", "1.5px solid rgba(74,52,40,0.08)")
                .set("overflow", "hidden");
        return combined;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private H2 sectionHeading(String text) {
        H2 h = new H2(text);
        h.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("margin", "8px 0 4px 0")
                .set("color", DARK)
                .set("width", "100%");
        return h;
    }

    private HorizontalLayout twoColRow(com.vaadin.flow.component.Component left,
                                        com.vaadin.flow.component.Component right) {
        HorizontalLayout row = new HorizontalLayout(left, right);
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("gap", "12px");
        // Make both children fill equal space
        if (left instanceof com.vaadin.flow.component.HasSize hs1) hs1.setWidth("50%");
        if (right instanceof com.vaadin.flow.component.HasSize hs2) hs2.setWidth("50%");
        return row;
    }

    private TextField pillTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPlaceholder(placeholder);
        tf.setWidthFull();
        tf.getStyle()
                .set("border-radius", "28px")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "28px");
        tf.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "28px")
                .set("text-align", "center");
        return tf;
    }

    private void styleEmailField(EmailField ef) {
        ef.setWidthFull();
        ef.getStyle()
                .set("border-radius", "28px")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "28px");
        ef.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "28px")
                .set("text-align", "center");
    }

    private void stylePasswordField(PasswordField pf) {
        pf.getStyle()
                .set("border-radius", "28px")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "28px");
        pf.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "28px");
    }

    private void styleDateField(com.vaadin.flow.dom.Element el) {
        el.getStyle()
                .set("border-radius", "28px")
                .set("--lumo-border-radius-m", "28px")
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "28px");
    }
}
