package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserLoginRequest;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Pawsitter")
@AnonymousAllowed
@CssImport(value = "./styles/password-field-reveal-button.css", themeFor = "vaadin-password-field-button")
public class LoginView extends VerticalLayout {

    private static final String DARK       = "#4a3428";
    private static final String CREAM      = "#e8d9c8";    // beige background matching mockup
    private static final String CARD_BG    = "#ffffff";
    private static final String INPUT_BG   = "#f5e9d6";    // warm beige for focused input
    private static final String BROWN_BTN  = "#5c3d1e";    // dark brown login button
    private static final String DANGER     = "#c73e1d";

    private final UserService userService;
    private final Paragraph errorMessage = new Paragraph();
    private final Paragraph statusMessage = new Paragraph();

    public LoginView(UserService userService) {
        this.userService = userService;

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
            .set("min-height", "100vh");

        // ── Decorative background circles ─────────────────────────────────────
        add(decoCircle("220px", "220px", "#d4c4b0", "-60px", null, "-40px", null));     // top-left
        add(decoCircle("300px", "300px", "#b8d4cc", "-40px", "-40px", null, null));     // top-right (mint)
        add(decoCircle("260px", "260px", "#c8b89a", null, null, "-60px", "-50px"));     // bottom-left
        add(decoCircle("200px", "200px", "#d4c4b0", null, "-30px", "-50px", null));     // bottom-right

        // ── Card ─────────────────────────────────────────────────────────────
        add(buildCard());
    }

    // ── Decorative circle helper ─────────────────────────────────────────────
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

    // ── Login card ───────────────────────────────────────────────────────────
    private Div buildCard() {
        Div card = new Div();
        card.getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "24px")
            .set("padding", "48px 44px 40px 44px")
            .set("width", "100%")
            .set("max-width", "500px")
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
        card.add(buildSubtitle());
        configureMessages();
        card.add(errorMessage, statusMessage);
        card.add(buildForm());

        return card;
    }

    // ── Logo row ─────────────────────────────────────────────────────────────
    private HorizontalLayout buildLogo() {
        HorizontalLayout logoRow = new HorizontalLayout();
        logoRow.setAlignItems(FlexComponent.Alignment.CENTER);
        logoRow.setSpacing(false);
        logoRow.getStyle()
            .set("gap", "10px")
            .set("margin-bottom", "20px")
            .set("cursor", "pointer");

        Image logoImg = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        logoImg.getStyle().set("height", "50px").set("width", "auto");

        logoRow.add(logoImg);
        logoRow.addClickListener(e -> UI.getCurrent().navigate(""));
        return logoRow;
    }

    // ── Title ────────────────────────────────────────────────────────────────
    private H1 buildTitle() {
        H1 title = new H1("Willkommen zurück bei Pawsitter!");
        title.getStyle()
            .set("margin", "0 0 8px 0")
            .set("font-size", "22px")
            .set("font-weight", "800")
            .set("color", DARK)
            .set("text-align", "center")
            .set("line-height", "1.3");
        return title;
    }

    // ── Subtitle ─────────────────────────────────────────────────────────────
    private Paragraph buildSubtitle() {
        Paragraph sub = new Paragraph("Melde dich an, um fortzufahren!");
        sub.getStyle()
            .set("margin", "0 0 28px 0")
            .set("font-size", "14px")
            .set("color", "#8a7060")
            .set("text-align", "center");
        return sub;
    }

    // ── Form ─────────────────────────────────────────────────────────────────
    private VerticalLayout buildForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidthFull();
        form.setPadding(false);
        form.setSpacing(false);
        form.getStyle().set("gap", "14px");

        // E-Mail field
        EmailField emailField = new EmailField();
        emailField.setPlaceholder("E-Mail");
        emailField.setWidthFull();
        emailField.getStyle()
            .set("border-radius", "28px")
            .set("--vaadin-input-field-background", INPUT_BG)
            .set("--vaadin-input-field-border-radius", "28px")
            .set("--vaadin-input-field-value-font-size", "15px")
            .set("text-align", "center");
        styleInputField(emailField.getElement());

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Passwort");
        passwordField.setWidthFull();
        
        // Dummy-Icon im Prefix hinzugefügt, um das Augen-Icon im Suffix auszugleichen
        // Dadurch wird der Platzhalter-Text perfekt zentriert
        Icon dummyIcon = VaadinIcon.EYE.create();
        dummyIcon.getStyle().set("visibility", "hidden");
        passwordField.setPrefixComponent(dummyIcon);
        
        passwordField.getStyle()
            .set("border-radius", "28px")
            .set("--vaadin-input-field-background", INPUT_BG)
            .set("--vaadin-input-field-border-radius", "28px")
            .set("--vaadin-input-field-value-font-size", "15px")
            .set("--vaadin-input-field-text-align", "center");
        styleInputField(passwordField.getElement());

        // "Passwort vergessen?" link (right-aligned)
        Span forgotBtn = new Span("Passwort vergessen?");
        forgotBtn.getStyle()
            .set("color", DARK)
            .set("font-size", "13px")
            .set("font-weight", "600")
            .set("cursor", "pointer")
            .set("text-decoration", "underline")
            .set("align-self", "flex-end")
            .set("margin-top", "-4px");
        forgotBtn.addClickListener(e -> {
            UI.getCurrent().navigate("forgot-password");
        });

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setWidthFull();
        loginBtn.getStyle()
            .set("background", BROWN_BTN)
            .set("color", "white")
            .set("border-radius", "28px")
            .set("height", "52px")
            .set("font-size", "16px")
            .set("font-weight", "700")
            .set("box-shadow", "none")
            .set("cursor", "pointer")
            .set("margin-top", "6px")
            .set("letter-spacing", "0.3px");
        loginBtn.addClickListener(e -> {
            clearMessages();
            UserAuthResult result = userService.login(new UserLoginRequest(
                    emailField.getValue(),
                    passwordField.getValue()));
            handleAuthResult(result);
        });

        // "Noch kein Mitglied?" link
        Span registerSpan = new Span();
        registerSpan.getStyle()
            .set("color", DARK)
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("cursor", "pointer")
            .set("align-self", "center")
            .set("margin-top", "8px");

        Span text1 = new Span("Noch kein Mitglied? ");
        Span text2 = new Span("Jetzt registrieren!");
        text2.getStyle().set("text-decoration", "underline");

        registerSpan.add(text1, text2);
        registerSpan.addClickListener(e -> {
            UI.getCurrent().navigate("register");
        });

        form.add(emailField, passwordField, forgotBtn, loginBtn, registerSpan);
        return form;
    }

    // ── Helper: apply pill/capsule styling to Vaadin input field elements ─────
    private void styleInputField(com.vaadin.flow.dom.Element el) {
        el.getStyle()
            .set("border-radius", "28px")
            .set("--lumo-border-radius-m", "28px")
            .set("--lumo-contrast-10pct", INPUT_BG)
            .set("text-align", "center");
        el.setAttribute("theme", "align-center");
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
}
