package com.softwareengineering.petsitter.ui.user;

import java.time.LocalDate;

import com.softwareengineering.petsitter.location.service.PostalCodeService;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserRegistrationConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.UserRegistrationRequest;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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
    private static final String DANGER    = "#c73e1d";
    private static final String SUCCESS   = "#3f7d42";
    private static final int MIN_PASSWORD_LENGTH = 14;
    private static final String PASSWORD_RULE_MESSAGE =
            "Das Passwort muss mindestens 14 Zeichen lang sein und Groß- und Kleinbuchstaben, eine Zahl sowie ein Sonderzeichen enthalten.";

    private final UserService userService;
    private final PostalCodeService postalCodeService;
    private final Paragraph errorMessage = new Paragraph();
    private final Paragraph statusMessage = new Paragraph();

    private TextField postalCodeField;
    private TextField cityField;
    private ComboBox<String> landField;
    private String currentRegistrationEmail = "";

    public RegistrateView(UserService userService, PostalCodeService postalCodeService) {
        this.userService = userService;
        this.postalCodeService = postalCodeService;

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
        configureMessages();
        card.add(errorMessage, statusMessage);
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

        logoRow.add(logoImg);
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

        // ── Tabs ──────────────────────────────────────────────────────────
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setSpacing(false);
        tabs.getStyle()
                .set("gap", "8px")
                .set("margin-bottom", "8px");

        Button kontoTab = new Button("Konto");
        Button datenTab = new Button("Daten");

        VerticalLayout kontoLayout = new VerticalLayout();
        kontoLayout.setPadding(false);
        kontoLayout.setSpacing(false);
        kontoLayout.getStyle().set("gap", "12px");

        VerticalLayout datenLayout = new VerticalLayout();
        datenLayout.setPadding(false);
        datenLayout.setSpacing(false);
        datenLayout.getStyle().set("gap", "12px");
        datenLayout.setVisible(false);

        styleTabButton(kontoTab, true);
        styleTabButton(datenTab, false);

        // Tab click listeners are defined further down

        tabs.add(kontoTab, datenTab);

        // ── Konto Fields ──────────────────────────────────────────────────
        EmailField emailField = new EmailField();
        emailField.setPlaceholder("E-Mail");
        styleEmailField(emailField);

        PasswordField passwortField = new PasswordField();
        passwortField.setPlaceholder("Passwort");
        passwortField.setWidthFull();
        passwortField.setValueChangeMode(ValueChangeMode.EAGER);
        stylePasswordField(passwortField);

        PasswordField passwortConfirmField = new PasswordField();
        passwortConfirmField.setPlaceholder("Passwort bestätigen");
        passwortConfirmField.setWidthFull();
        stylePasswordField(passwortConfirmField);

        emailField.addValueChangeListener(e -> emailField.setInvalid(false));
        passwortField.addValueChangeListener(e -> passwortField.setInvalid(false));
        passwortConfirmField.addValueChangeListener(e -> passwortConfirmField.setInvalid(false));

        HorizontalLayout rowPass = twoColRow(passwordInputGroup(passwortField), passwortConfirmField);
        rowPass.setAlignItems(FlexComponent.Alignment.START);

        kontoLayout.add(emailField, rowPass);

        // ── Daten Fields ──────────────────────────────────────────────────
        H2 personalHeading = sectionHeading("Persönliche Angaben");

        TextField vornameField = pillTextField("Vorname");
        TextField nachnameField = pillTextField("Nachname");
        vornameField.addValueChangeListener(e -> vornameField.setInvalid(false));
        nachnameField.addValueChangeListener(e -> nachnameField.setInvalid(false));
        HorizontalLayout rowVorNach = twoColRow(vornameField, nachnameField);

        TextField telefonField = pillTextField("Telefonnummer");
        DatePicker geburtstagsField = new DatePicker();
        geburtstagsField.setPlaceholder("Geburtstag");
        geburtstagsField.setWidthFull();
        styleDateField(geburtstagsField.getElement());
        geburtstagsField.addValueChangeListener(e -> geburtstagsField.setInvalid(false));
        HorizontalLayout rowTelGeb = twoColRow(telefonField, geburtstagsField);

        ComboBox<String> nationalitaetField = pillComboBox("Nationalität",
                "Deutsch", "Österreichisch", "Schweizerisch", "Amerikanisch", "Australisch",
                "Belgisch", "Brasilianisch", "Bulgarisch", "Chinesisch", "Dänisch",
                "Englisch", "Finnisch", "Französisch", "Griechisch", "Indisch",
                "Indonesisch", "Iranisch", "Irakisch", "Irisch", "Israelisch",
                "Italienisch", "Japanisch", "Kanadisch", "Kolumbianisch", "Kroatisch",
                "Luxemburgisch", "Marokkanisch", "Mexikanisch", "Niederländisch", "Nigerianisch",
                "Norwegisch", "Pakistanisch", "Polnisch", "Portugiesisch", "Rumänisch",
                "Russisch", "Schwedisch", "Serbisch", "Slowakisch", "Slowenisch",
                "Spanisch", "Südafrikanisch", "Südkoreanisch", "Tschechisch", "Türkisch",
                "Ukrainisch", "Ungarisch", "Vietnamesisch");
        nationalitaetField.getStyle().set("width", "50%").set("min-width", "200px");
        HorizontalLayout rowNat = new HorizontalLayout(nationalitaetField);
        rowNat.setWidthFull();
        rowNat.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 standortHeading = sectionHeading("Standort");

        TextField strasseField = pillTextField("Straße");
        TextField hausnummerField = pillTextField("Hausnummer");
        strasseField.addValueChangeListener(e -> strasseField.setInvalid(false));
        hausnummerField.addValueChangeListener(e -> hausnummerField.setInvalid(false));
        HorizontalLayout rowStrasseHaus = twoColRow(strasseField, hausnummerField);

        postalCodeField = pillTextField("Postleitzahl");
        cityField = pillTextField("Ort");
        boolean[] cityAutoFilled = {false};
        boolean[] landAutoFilled = {false};
        postalCodeField.addValueChangeListener(e -> {
            postalCodeField.setInvalid(false);
            String plz = e.getValue();
            if (plz != null && plz.matches("\\d{5}")) {
                postalCodeService.findGermanLocation(plz).ifPresent(loc -> {
                    java.util.List<String> names = loc.getPlaceNamesList();
                    if (!names.isEmpty() && (cityField.getValue().isBlank() || cityAutoFilled[0])) {
                        cityField.setValue(names.get(0));
                        cityAutoFilled[0] = true;
                    }
                    if (landField.getValue() == null || landField.getValue().isBlank() || landAutoFilled[0]) {
                        landField.setValue("Deutschland");
                        landAutoFilled[0] = true;
                    }
                });
            } else {
                if (cityAutoFilled[0]) { cityField.setValue(""); cityAutoFilled[0] = false; }
                if (landAutoFilled[0]) { landField.setValue(null); landAutoFilled[0] = false; }
            }
        });
        cityField.addValueChangeListener(e -> {
            cityField.setInvalid(false);
            if (e.isFromClient()) cityAutoFilled[0] = false;
        });
        HorizontalLayout rowPlzOrt = twoColRow(postalCodeField, cityField);

        landField = pillComboBox("Land",
                "Deutschland", "Österreich", "Schweiz", "Frankreich", "Italien",
                "Spanien", "Portugal", "Niederlande", "Belgien", "Luxemburg",
                "Polen", "Tschechien", "Slowakei", "Ungarn", "Rumänien",
                "Bulgarien", "Kroatien", "Serbien", "Slowenien", "Griechenland",
                "Schweden", "Norwegen", "Dänemark", "Finnland", "Irland",
                "Großbritannien", "Türkei", "Russland", "Ukraine",
                "USA", "Kanada", "Australien", "Japan", "China",
                "Indien", "Brasilien", "Argentinien", "Mexiko", "Südafrika",
                "Nigeria", "Ägypten", "Marokko", "Iran", "Saudi-Arabien",
                "Südkorea", "Indonesien", "Vietnam", "Pakistan");
        landField.addValueChangeListener(e -> { if (e.isFromClient()) landAutoFilled[0] = false; });
        landField.getStyle().set("width", "50%").set("min-width", "200px");
        HorizontalLayout rowLand = new HorizontalLayout(landField);
        rowLand.setWidthFull();
        rowLand.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        datenLayout.add(
                personalHeading,
                rowVorNach, rowTelGeb, rowNat,
                standortHeading,
                rowStrasseHaus, rowPlzOrt, rowLand
        );

        // ── Step 3: Code confirmation layout ──────────────────────────────
        VerticalLayout codeLayout = new VerticalLayout();
        codeLayout.setPadding(false);
        codeLayout.setSpacing(false);
        codeLayout.getStyle().set("gap", "12px");
        codeLayout.setVisible(false);

        H2 codeHeading = sectionHeading("Bestätigungscode eingeben");

        Paragraph codeHint = new Paragraph("Der Code wurde per E-Mail gesendet.");
        codeHint.getStyle()
                .set("margin", "0")
                .set("font-size", "13px")
                .set("color", "#8a7060");

        TextField codeField = pillTextField("Bestätigungscode");

        Button confirmBtn = new Button("Code bestätigen");
        confirmBtn.setWidthFull();
        confirmBtn.getStyle()
                .set("background", BROWN_BTN)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("height", "48px")
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        confirmBtn.addClickListener(e -> {
            clearMessages();
            String email = currentRegistrationEmail == null || currentRegistrationEmail.isBlank()
                    ? emailField.getValue()
                    : currentRegistrationEmail;
            UserAuthResult result = userService.completeRegistration(new UserRegistrationConfirmationRequest(
                    email,
                    codeField.getValue()));
            handleAuthResult(result);
        });

        codeLayout.add(codeHeading, codeHint, codeField, confirmBtn);

        // ── "Bereits registriert?" link ────────────────────────────────────
        Span loginBtn = new Span("Bereits registriert? Hier einloggen");
        loginBtn.getStyle()
                .set("color", LINK_CLR)
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("text-decoration", "underline")
                .set("margin-top", "4px")
                .set("align-self", "center");
        loginBtn.addClickListener(e -> UI.getCurrent().navigate("login"));

        // ── Register button ────────────────────────────────────────────────
        Button registerBtn = new Button("Weiter");
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
            if ("Weiter".equals(registerBtn.getText())) {
                boolean valid = true;
                if (emailField.getValue().isBlank() || !emailField.getValue().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    emailField.setInvalid(true);
                    emailField.setErrorMessage("Gültige E-Mail eingeben");
                    valid = false;
                }
                if (!isValidPassword(passwortField.getValue())) {
                    passwortField.setInvalid(true);
                    passwortField.setErrorMessage(PASSWORD_RULE_MESSAGE);
                    valid = false;
                }
                if (!passwortConfirmField.getValue().equals(passwortField.getValue())) {
                    passwortConfirmField.setInvalid(true);
                    passwortConfirmField.setErrorMessage("Passwörter stimmen nicht überein");
                    valid = false;
                }
                if (valid) datenTab.click();
            } else {
                boolean valid = true;
                if (vornameField.getValue().isBlank()) { vornameField.setInvalid(true); vornameField.setErrorMessage("Pflichtfeld"); valid = false; }
                if (nachnameField.getValue().isBlank()) { nachnameField.setInvalid(true); nachnameField.setErrorMessage("Pflichtfeld"); valid = false; }
                if (strasseField.getValue().isBlank()) { strasseField.setInvalid(true); strasseField.setErrorMessage("Pflichtfeld"); valid = false; }
                if (hausnummerField.getValue().isBlank()) { hausnummerField.setInvalid(true); hausnummerField.setErrorMessage("Pflichtfeld"); valid = false; }
                if (postalCodeField.getValue().isBlank()) { postalCodeField.setInvalid(true); postalCodeField.setErrorMessage("Pflichtfeld"); valid = false; }
                if (cityField.getValue().isBlank()) { cityField.setInvalid(true); cityField.setErrorMessage("Pflichtfeld"); valid = false; }
                if (geburtstagsField.getValue() == null) {
                    geburtstagsField.setInvalid(true);
                    geburtstagsField.setErrorMessage("Pflichtfeld");
                    valid = false;
                } else if (geburtstagsField.getValue().isAfter(LocalDate.now().minusYears(18))) {
                    geburtstagsField.setInvalid(true);
                    geburtstagsField.setErrorMessage("Mindestalter 18 Jahre");
                    valid = false;
                }
                if (!valid) return;
                clearMessages();
                currentRegistrationEmail = emailField.getValue();
                UserAuthResult result = userService.startRegistration(new UserRegistrationRequest(
                        emailField.getValue(),
                        passwortField.getValue(),
                        passwortConfirmField.getValue(),
                        vornameField.getValue(),
                        nachnameField.getValue(),
                        telefonField.getValue(),
                        strasseField.getValue(),
                        hausnummerField.getValue(),
                        postalCodeField.getValue(),
                        cityField.getValue(),
                        null,
                        geburtstagsField.getValue(),
                        nationalitaetField.getValue(),
                        landField.getValue()), currentRequestIp());
                if (result.success()) {
                    tabs.setVisible(false);
                    datenLayout.setVisible(false);
                    registerBtn.setVisible(false);
                    loginBtn.setVisible(false);
                    codeLayout.setVisible(true);
                    showStatus(result.message());
                } else {
                    showError(result.message());
                }
            }
        });

        kontoTab.addClickListener(e -> {
            styleTabButton(kontoTab, true);
            styleTabButton(datenTab, false);
            kontoLayout.setVisible(true);
            datenLayout.setVisible(false);
            registerBtn.setText("Weiter");
        });

        datenTab.addClickListener(e -> {
            styleTabButton(kontoTab, false);
            styleTabButton(datenTab, true);
            kontoLayout.setVisible(false);
            datenLayout.setVisible(true);
            registerBtn.setText("Registrieren & Starten");
        });

        form.add(
                tabs,
                kontoLayout,
                datenLayout,
                codeLayout,
                registerBtn, loginBtn
        );
        return form;
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

    private ComboBox<String> pillComboBox(String placeholder, String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPlaceholder(placeholder);
        cb.setItems(items);
        cb.setAllowCustomValue(true);
        cb.addCustomValueSetListener(e -> cb.setValue(e.getDetail()));
        cb.setWidthFull();
        cb.getStyle()
                .set("border-radius", "28px")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "28px");
        cb.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "28px");
        return cb;
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
                .set("--vaadin-input-field-border-radius", "28px")
                .set("text-align", "center");
    }

    private VerticalLayout passwordInputGroup(PasswordField passwordField) {
        Span lengthCriterion = passwordCriterion("Mindestens 14 Zeichen");
        Span uppercaseCriterion = passwordCriterion("Großbuchstabe");
        Span lowercaseCriterion = passwordCriterion("Kleinbuchstabe");
        Span specialCriterion = passwordCriterion("Sonderzeichen");
        Span digitCriterion = passwordCriterion("Zahl");

        VerticalLayout criteria = new VerticalLayout(
                lengthCriterion,
                uppercaseCriterion,
                lowercaseCriterion,
                specialCriterion,
                digitCriterion
        );
        criteria.setPadding(false);
        criteria.setSpacing(false);
        criteria.setWidthFull();
        criteria.getStyle()
                .set("gap", "3px")
                .set("margin", "2px 0 0 12px");

        updatePasswordCriteria(
                "",
                lengthCriterion,
                uppercaseCriterion,
                lowercaseCriterion,
                specialCriterion,
                digitCriterion
        );
        passwordField.addValueChangeListener(e -> updatePasswordCriteria(
                e.getValue(),
                lengthCriterion,
                uppercaseCriterion,
                lowercaseCriterion,
                specialCriterion,
                digitCriterion
        ));

        VerticalLayout group = new VerticalLayout(passwordField, criteria);
        group.setPadding(false);
        group.setSpacing(false);
        group.setWidthFull();
        group.getStyle().set("gap", "4px");
        return group;
    }

    private Span passwordCriterion(String label) {
        Span criterion = new Span(label);
        criterion.getStyle()
                .set("font-size", "12px")
                .set("line-height", "1.25")
                .set("font-weight", "600");
        return criterion;
    }

    private void updatePasswordCriteria(
            String password,
            Span lengthCriterion,
            Span uppercaseCriterion,
            Span lowercaseCriterion,
            Span specialCriterion,
            Span digitCriterion
    ) {
        String value = password == null ? "" : password;
        updatePasswordCriterion(lengthCriterion, "Mindestens 14 Zeichen", value.length() >= MIN_PASSWORD_LENGTH);
        updatePasswordCriterion(uppercaseCriterion, "Großbuchstabe", containsUppercase(value));
        updatePasswordCriterion(lowercaseCriterion, "Kleinbuchstabe", containsLowercase(value));
        updatePasswordCriterion(specialCriterion, "Sonderzeichen", containsSpecialCharacter(value));
        updatePasswordCriterion(digitCriterion, "Zahl", containsDigit(value));
    }

    private void updatePasswordCriterion(Span criterion, String label, boolean fulfilled) {
        criterion.setText((fulfilled ? "Erfüllt: " : "Offen: ") + label);
        criterion.getStyle()
                .set("color", fulfilled ? SUCCESS : DANGER)
                .set("font-weight", fulfilled ? "700" : "600");
    }

    private boolean isValidPassword(String password) {
        String value = password == null ? "" : password;
        return value.length() >= MIN_PASSWORD_LENGTH
                && containsUppercase(value)
                && containsLowercase(value)
                && containsSpecialCharacter(value)
                && containsDigit(value);
    }

    private boolean containsUppercase(String value) {
        return value.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsLowercase(String value) {
        return value.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }

    private boolean containsSpecialCharacter(String value) {
        return value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    }

    private void handleAuthResult(UserAuthResult result) {
        if (result.success()) {
            showStatus(result.message());
            if (result.userProfile() != null) {
                UserSessionSupport.authenticate(result.userProfile());
                UI.getCurrent().navigate("");
            }
            return;
        }
        showError(result.message());
    }

    private void showResult(UserAuthResult result) {
        if (result.success()) {
            showStatus(result.message());
        } else {
            showError(result.message());
        }
    }

    private String currentRequestIp() {
        if (VaadinService.getCurrentRequest() == null) {
            return "unknown";
        }
        String remoteAddr = VaadinService.getCurrentRequest().getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }

    private void configureMessages() {
        errorMessage.setWidthFull();
        errorMessage.getStyle()
                .set("box-sizing", "border-box")
                .set("color", DANGER)
                .set("margin", "0 0 16px 0")
                .set("padding", "12px 16px")
                .set("background", "#fce8e0")
                .set("border-radius", "14px")
                .set("font-size", "14px")
                .set("display", "none");

        statusMessage.setWidthFull();
        statusMessage.getStyle()
                .set("box-sizing", "border-box")
                .set("color", "#3f7d42")
                .set("margin", "0 0 16px 0")
                .set("padding", "12px 16px")
                .set("background", "#e8f5e5")
                .set("border-radius", "14px")
                .set("font-size", "14px")
                .set("display", "none");
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.getStyle().set("display", "block");
        statusMessage.getStyle().set("display", "none");
    }

    private void showStatus(String message) {
        statusMessage.setText(message);
        statusMessage.getStyle().set("display", "block");
        errorMessage.getStyle().set("display", "none");
    }

    private void clearMessages() {
        errorMessage.getStyle().set("display", "none");
        statusMessage.getStyle().set("display", "none");
    }

    private void styleTabButton(Button btn, boolean active) {
        btn.getStyle()
                .set("border-radius", "12px")
                .set("font-size", "16px")
                .set("font-weight", "500")
                .set("cursor", "pointer")
                .set("box-shadow", "none")
                .set("border", "none")
                .set("padding", "0 20px")
                .set("height", "40px");

        if (active) {
            btn.getStyle()
                    .set("background", INPUT_BG)
                    .set("color", DARK);
        } else {
            btn.getStyle()
                    .set("background", "transparent")
                    .set("color", "#8a7060");
        }
    }
}
