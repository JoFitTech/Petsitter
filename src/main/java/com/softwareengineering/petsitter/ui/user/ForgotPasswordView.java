package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.user.dto.PasswordResetConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.PasswordResetRequest;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.service.PasswordResetService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Passwort-vergessen-Seite – kein MainLayout, eigenständige Seite.
 */
@Route("forgot-password")
@PageTitle("Passwort vergessen | Pawsitter")
@AnonymousAllowed
@CssImport(value = "./styles/password-field-reveal-button.css", themeFor = "vaadin-password-field-button")
public class ForgotPasswordView extends VerticalLayout {

    private static final String DARK      = "#4a3428";
    private static final String CREAM     = "#e8d9c8";
    private static final String CARD_BG   = "#ffffff";
    private static final String INPUT_BG  = "#f5e9d6";
    private static final String BROWN_BTN = "#5c3d1e";
    private static final String LINK_CLR  = "#7b5236";
    private static final String DANGER    = "#c73e1d";

    private final PasswordResetService passwordResetService;
    private final Paragraph errorMessage = new Paragraph();
    private final Paragraph statusMessage = new Paragraph();
    private String currentResetEmail = "";

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

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

        // ── Decorative background circles (same as LoginView) ─────────────────
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

    // ── Logo ──────────────────────────────────────────────────────────────────
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

    // ── Title ─────────────────────────────────────────────────────────────────
    private H1 buildTitle() {
        H1 title = new H1("Passwort vergessen?");
        title.getStyle()
                .set("margin", "0 0 12px 0")
                .set("font-size", "24px")
                .set("font-weight", "800")
                .set("color", DARK)
                .set("text-align", "center")
                .set("line-height", "1.3");
        return title;
    }

    // ── Subtitle ──────────────────────────────────────────────────────────────
    private Paragraph buildSubtitle() {
        Paragraph sub = new Paragraph(
                "Gib deine E-Mail Adresse ein und wir senden dir einen Code zum Zurücksetzen deines Passworts.");
        sub.getStyle()
                .set("margin", "0 0 28px 0")
                .set("font-size", "14px")
                .set("color", "#8a7060")
                .set("text-align", "center")
                .set("line-height", "1.5");
        return sub;
    }

    // ── Form ──────────────────────────────────────────────────────────────────
    private VerticalLayout buildForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidthFull();
        form.setPadding(false);
        form.setSpacing(false);
        form.setAlignItems(FlexComponent.Alignment.CENTER);
        form.getStyle().set("gap", "16px");

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
        emailField.addValueChangeListener(e -> emailField.setInvalid(false));

        // "Anfrage senden" button
        Button sendBtn = new Button("Anfrage senden");
        sendBtn.setWidthFull();
        sendBtn.getStyle()
                .set("background", BROWN_BTN)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("height", "52px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("letter-spacing", "0.3px");

        VerticalLayout resetLayout = buildResetLayout(emailField, sendBtn);
        resetLayout.setVisible(false);

        sendBtn.addClickListener(e -> {
            if (!sendBtn.isVisible()) {
                return;
            }
            clearMessages();
            emailField.setInvalid(false);
            UserAuthResult result = passwordResetService.requestPasswordReset(
                    new PasswordResetRequest(emailField.getValue()),
                    currentRequestIp());
            if (result.success()) {
                currentResetEmail = emailField.getValue();
                emailField.setEnabled(false);
                sendBtn.setVisible(false);
                resetLayout.setVisible(true);
                showStatus(result.message());
            } else {
                emailField.setInvalid(true);
                emailField.setErrorMessage(result.message());
                showError(result.message());
            }
        });
        sendBtn.addClickShortcut(Key.ENTER);

        // "Zurück zum Login" link
        Span backBtn = new Span("Zurück zum Login");
        backBtn.getStyle()
                .set("color", LINK_CLR)
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("text-decoration", "underline");
        backBtn.addClickListener(e -> {
            UI.getCurrent().navigate("login");
        });

        form.add(emailField, sendBtn, resetLayout, backBtn);
        return form;
    }

    private VerticalLayout buildResetLayout(EmailField emailField, Button sendBtn) {
        VerticalLayout resetLayout = new VerticalLayout();
        resetLayout.setWidthFull();
        resetLayout.setPadding(false);
        resetLayout.setSpacing(false);
        resetLayout.getStyle().set("gap", "12px");

        TextField codeField = new TextField();
        codeField.setPlaceholder("Reset-Code");
        codeField.setWidthFull();
        styleInputField(codeField.getElement());
        codeField.addValueChangeListener(e -> codeField.setInvalid(false));

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Neues Passwort");
        passwordField.setWidthFull();
        stylePasswordField(passwordField);
        passwordField.addValueChangeListener(e -> passwordField.setInvalid(false));

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPlaceholder("Passwort bestätigen");
        confirmPasswordField.setWidthFull();
        stylePasswordField(confirmPasswordField);
        confirmPasswordField.addValueChangeListener(e -> confirmPasswordField.setInvalid(false));

        Button resetBtn = new Button("Passwort zurücksetzen");
        resetBtn.setWidthFull();
        resetBtn.getStyle()
                .set("background", BROWN_BTN)
                .set("color", "white")
                .set("border-radius", "28px")
                .set("height", "52px")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("letter-spacing", "0.3px");
        resetBtn.addClickListener(e -> {
            clearMessages();
            codeField.setInvalid(false);
            passwordField.setInvalid(false);
            confirmPasswordField.setInvalid(false);

            String email = currentResetEmail == null || currentResetEmail.isBlank()
                    ? emailField.getValue()
                    : currentResetEmail;
            UserAuthResult result = passwordResetService.completePasswordReset(
                    new PasswordResetConfirmationRequest(
                            email,
                            codeField.getValue(),
                            passwordField.getValue(),
                            confirmPasswordField.getValue()));
            if (result.success()) {
                showStatus(result.message());
                resetBtn.setEnabled(false);
                codeField.setEnabled(false);
                passwordField.setEnabled(false);
                confirmPasswordField.setEnabled(false);
                return;
            }
            showError(result.message());
        });

        Span changeEmail = new Span("Andere E-Mail verwenden");
        changeEmail.getStyle()
                .set("color", LINK_CLR)
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("cursor", "pointer")
                .set("text-decoration", "underline")
                .set("align-self", "center");
        changeEmail.addClickListener(e -> {
            currentResetEmail = "";
            emailField.setEnabled(true);
            sendBtn.setVisible(true);
            resetLayout.setVisible(false);
            clearMessages();
        });

        resetLayout.add(codeField, passwordField, confirmPasswordField, resetBtn, changeEmail);
        return resetLayout;
    }

    // ── Helper: pill/capsule styling for Vaadin input fields ──────────────────
    private void styleInputField(com.vaadin.flow.dom.Element el) {
        el.getStyle()
                .set("border-radius", "28px")
                .set("--lumo-border-radius-m", "28px")
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("text-align", "center");
    }

    private void stylePasswordField(PasswordField field) {
        field.getStyle()
                .set("border-radius", "28px")
                .set("--vaadin-input-field-background", INPUT_BG)
                .set("--vaadin-input-field-border-radius", "28px");
        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("--lumo-border-radius-m", "28px");
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
}
