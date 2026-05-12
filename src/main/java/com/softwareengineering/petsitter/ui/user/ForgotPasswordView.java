package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Passwort-vergessen-Seite – kein MainLayout, eigenständige Seite.
 *
 * Backend-Schnittstellen (alle als TODO markiert):
 *  - "Anfrage senden"-Button: AuthService.sendPasswordResetMail(email)
 *  - "Zurück zum Login"-Link: Navigation zur Login-Route
 */
@Route("forgot-password")
@PageTitle("Passwort vergessen | Pawsitter")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    private static final String DARK      = "#4a3428";
    private static final String CREAM     = "#e8d9c8";
    private static final String CARD_BG   = "#ffffff";
    private static final String INPUT_BG  = "#f5e9d6";
    private static final String BROWN_BTN = "#5c3d1e";
    private static final String LINK_CLR  = "#7b5236";

    public ForgotPasswordView() {
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
                "Gib deine E-Mail Adresse ein und wir senden dir einen Link zum Zurücksetzen deines Passworts.");
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
        sendBtn.addClickListener(e -> {
            // TODO: Backend – AuthService.sendPasswordResetMail(emailField.getValue())
            System.out.println("TODO: AuthService.sendPasswordResetMail(" + emailField.getValue() + ")");
        });

        // "Zurück zum Login" link
        Button backBtn = new Button("Zurück zum Login");
        backBtn.getStyle()
                .set("background", "transparent")
                .set("color", LINK_CLR)
                .set("box-shadow", "none")
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("padding", "0")
                .set("height", "auto")
                .set("cursor", "pointer")
                .set("text-decoration", "underline");
        backBtn.addClickListener(e -> {
            System.out.println("TODO: Navigate back to login");
            UI.getCurrent().navigate("login");
        });

        form.add(emailField, sendBtn, backBtn);
        return form;
    }

    // ── Helper: pill/capsule styling for Vaadin input fields ──────────────────
    private void styleInputField(com.vaadin.flow.dom.Element el) {
        el.getStyle()
                .set("border-radius", "28px")
                .set("--lumo-border-radius-m", "28px")
                .set("--lumo-contrast-10pct", INPUT_BG)
                .set("text-align", "center");
    }
}
