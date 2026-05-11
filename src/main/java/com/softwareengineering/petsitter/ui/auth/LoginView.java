package com.softwareengineering.petsitter.ui.auth;

import com.softwareengineering.petsitter.auth.service.AuthService;
import com.softwareengineering.petsitter.user.domain.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * LoginView – Vaadin-UI für passwortlose Email+Code Authentifizierung.
 *
 * <p>Fluss:
 * 1. User gibt Email ein -> "Code anfordern"
 * 2. Code wird per Mail versendet (bzw. geloggt in Dev)
 * 3. User gibt 6-stelligen Code ein -> "Anmelden"
 * 4. Bei Erfolg: Session setzen und zur StartView navigieren
 *
 * <p>Design: Clean, fokussiert auf Eingabe ohne ablenkende Elemente.
 */
@Route("/login")
@PageTitle("Anmelden – Petsitter")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(LoginView.class);

    private static final String COLOR_PRIMARY = "#7b5236";
    private static final String COLOR_BG = "#fbf8f1";
    private static final String COLOR_DANGER = "#c73e1d";

    @Autowired
    private AuthService authService;

    // UI Components
    private EmailField emailField;
    private TextField codeField;
    private Button requestCodeButton;
    private Button verifyCodeButton;
    private Paragraph errorMessage;
    private Span statusMessage;

    // State
    private String currentEmail = "";
    private String clientIp = "";

    public LoginView(@Autowired AuthService authService) {
        this.authService = authService;

        setWidthFull();
        setHeightFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        getStyle()
                .set("background", COLOR_BG)
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center");

        add(createLoginPanel());
    }

    private Div createLoginPanel() {
        Div panel = new Div();
        panel.getStyle()
                .set("width", "100%")
                .set("max-width", "400px")
                .set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "40px 32px")
                .set("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.1)");

        H1 title = new H1("Anmelden");
        title.getStyle()
                .set("font-size", "28px")
                .set("margin", "0 0 8px 0")
                .set("color", COLOR_PRIMARY)
                .set("text-align", "center");

        Paragraph subtitle = new Paragraph("Mit E-Mail und Code");
        subtitle.getStyle()
                .set("margin", "0 0 24px 0")
                .set("text-align", "center")
                .set("color", "#7d746c")
                .set("font-size", "14px");

        Paragraph accountHint = new Paragraph("Hinweis: Falls noch kein Konto existiert, wird bei der ersten Anmeldung automatisch eines erstellt.");
        accountHint.getStyle()
                .set("margin", "0 0 18px 0")
                .set("padding", "10px 12px")
                .set("font-size", "12px")
                .set("line-height", "1.4")
                .set("color", "#5f5a56")
                .set("background", "#f7f3ec")
                .set("border", "1px solid #eadfce")
                .set("border-radius", "8px");

        errorMessage = new Paragraph();
        errorMessage.getStyle()
                .set("color", COLOR_DANGER)
                .set("margin", "16px 0")
                .set("padding", "12px")
                .set("background", "#fce8e0")
                .set("border-radius", "6px")
                .set("display", "none");

        statusMessage = new Span();
        statusMessage.getStyle()
                .set("color", "#71946e")
                .set("margin", "16px 0")
                .set("padding", "12px")
                .set("background", "#e8f5e5")
                .set("border-radius", "6px")
                .set("display", "none");

        // STEP 1: Email eingeben
        VerticalLayout step1 = createEmailStep();

        // STEP 2: Code eingeben
        VerticalLayout step2 = createCodeStep();
        step2.setVisible(false);

        panel.add(
                title,
                subtitle,
                accountHint,
                errorMessage,
                statusMessage,
                step1,
                step2
        );

        return panel;
    }

    private VerticalLayout createEmailStep() {
        VerticalLayout step = new VerticalLayout();
        step.setPadding(false);
        step.setSpacing(true);

        H2 stepTitle = new H2("Schritt 1: E-Mail eingeben");
        stepTitle.getStyle()
                .set("font-size", "16px")
                .set("margin", "0 0 16px 0")
                .set("color", COLOR_PRIMARY);

        emailField = new EmailField("E-Mail-Adresse");
        emailField.setWidthFull();
        emailField.setPlaceholder("deine@email.de");
        emailField.setRequired(true);
        emailField.setErrorMessage("Bitte gib eine gültige E-Mail ein");

        requestCodeButton = new Button("Code anfordern");
        requestCodeButton.setWidthFull();
        requestCodeButton.getStyle()
                .set("background", COLOR_PRIMARY)
                .set("color", "white")
                .set("font-weight", "700")
                .set("padding", "10px 16px")
                .set("border-radius", "6px")
                .set("cursor", "pointer");

        requestCodeButton.addClickListener(e -> onRequestCodeClicked(step.getParent().get()));

        step.add(stepTitle, emailField, requestCodeButton);
        return step;
    }

    private VerticalLayout createCodeStep() {
        VerticalLayout step = new VerticalLayout();
        step.setPadding(false);
        step.setSpacing(true);

        H2 stepTitle = new H2("Schritt 2: Code eingeben");
        stepTitle.getStyle()
                .set("font-size", "16px")
                .set("margin", "0 0 16px 0")
                .set("color", COLOR_PRIMARY);

        Paragraph instruction = new Paragraph("Der Code wurde an deine E-Mail versendet. Er ist 10 Minuten lang gültig.");
        instruction.getStyle()
                .set("font-size", "13px")
                .set("color", "#7d746c")
                .set("margin", "0 0 12px 0");

        codeField = new TextField("Sicherheitscode");
        codeField.setWidthFull();
        codeField.setPlaceholder("123456");
        codeField.setMaxLength(6);
        codeField.setRequired(true);

        verifyCodeButton = new Button("Anmelden");
        verifyCodeButton.setWidthFull();
        verifyCodeButton.getStyle()
                .set("background", COLOR_PRIMARY)
                .set("color", "white")
                .set("font-weight", "700")
                .set("padding", "10px 16px")
                .set("border-radius", "6px")
                .set("cursor", "pointer");

        verifyCodeButton.addClickListener(e -> onVerifyCodeClicked(step.getParent().get()));

        step.add(stepTitle, instruction, codeField, verifyCodeButton);
        return step;
    }

    /**
     * Handler: Code anfordern (Schritt 1 -> Schritt 2)
     */
    private void onRequestCodeClicked(com.vaadin.flow.component.Component parent) {
        clearMessages();
        String email = emailField.getValue().trim();

        if (email.isEmpty()) {
            showError("Bitte gib eine E-Mail-Adresse ein");
            return;
        }

        try {
            // IP extrahieren (für Audit)
            clientIp = getClientIp();
            currentEmail = email;

            authService.requestCodeForEmail(email, clientIp);
            log.info("Code angefordert für {}", email);

            showStatus("✓ Code versendet! Prüfe deine E-Mail (oder logs für Tests).");

            // UI: Schritt 1 verstecken, Schritt 2 zeigen
            if (parent instanceof Div) {
                parent.getChildren()
                        .filter(c -> c instanceof VerticalLayout)
                        .forEach(c -> {
                            if (((VerticalLayout) c).getComponentCount() > 0) {
                                Object first = ((VerticalLayout) c).getComponentAt(0);
                                if (first instanceof H2) {
                                    H2 h2 = (H2) first;
                                    if (h2.getText().contains("Schritt 1")) {
                                        c.setVisible(false);
                                    } else if (h2.getText().contains("Schritt 2")) {
                                        c.setVisible(true);
                                    }
                                }
                            }
                        });
            }


        } catch (Exception ex) {
            log.error("Fehler beim Anfordern des Codes: {}", ex.getMessage());
            showError("Fehler beim Versenden. Bitte versuche es später erneut.");
        }
    }

    /**
     * Handler: Code validieren und anmelden (Schritt 2 -> Session -> StartView)
     */
    private void onVerifyCodeClicked(com.vaadin.flow.component.Component parent) {
        clearMessages();
        String code = codeField.getValue().trim();

        if (code.isEmpty() || code.length() != 6) {
            showError("Bitte gib einen gültigen 6-stelligen Code ein");
            return;
        }

        try {
            Optional<User> userOpt = authService.verifyCodeAndGetUser(currentEmail, code);

            if (userOpt.isEmpty()) {
                showError("Ungültiger oder abgelaufener Code. Bitte versuche es erneut.");
                return;
            }

            User user = userOpt.get();
            log.info("User {} erfolgreich authentifiziert", user.getEmail());

            // Spring Security Session setzen
            setSecurityContext(user);

            showStatus("✓ Anmeldung erfolgreich! Leite weiter...");

            // Navigiere zur StartView nach kurzer Verzögerung
            getUI().ifPresent(ui -> {
                ui.access(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    ui.navigate("");
                });
            });

        } catch (Exception ex) {
            log.error("Fehler bei Code-Validierung: {}", ex.getMessage());
            showError("Anmeldung fehlgeschlagen. Bitte versuche es später erneut.");
        }
    }

    /**
     * Setzt die Spring Security Context (Session).
     */
    private void setSecurityContext(User user) {
        UserDetails userDetails = authService.toUserDetails(user);

        // Authentication erstellen
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // WebAuthenticationDetails setzen (falls nötig)
        authentication.setDetails(new WebAuthenticationDetails(clientIp, null));

        // In SecurityContext speichern
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Für Session-Persistierung (wichtig!)
        getUI().ifPresent(ui -> {
            ui.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
        });
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

    /**
     * Extrahiert Client-IP (für Audit und Rate-Limiting).
     * In lokalen Tests: 127.0.0.1, in Production mit X-Forwarded-For konfigurieren.
     */
    private String getClientIp() {
        try {
            // Für Production: würde man X-Forwarded-For o.ä. aus Request-Header ziehen
            // Hier: einfach lokale IP für Tests
            com.vaadin.flow.component.UI ui = getUI().orElse(null);
            if (ui != null && ui.getSession() != null) {
                // In lokaler Umgebung: immer 127.0.0.1 (korrekt für Tests)
                // In Prod: würde man hier aus Servlet-Request extrahieren
                return "127.0.0.1";
            }
        } catch (Exception e) {
            log.debug("IP-Extraktion fehlgeschlagen: {}", e.getMessage());
        }
        return "127.0.0.1"; // Fallback
    }
}

