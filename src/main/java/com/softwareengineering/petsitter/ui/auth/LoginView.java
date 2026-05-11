package com.softwareengineering.petsitter.ui.auth;

import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserLoginRequest;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.dto.UserRegistrationConfirmationRequest;
import com.softwareengineering.petsitter.user.dto.UserRegistrationRequest;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Route("/login")
@PageTitle("Anmelden – Petsitter")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private static final String COLOR_PRIMARY = "#7b5236";
    private static final String COLOR_BG = "#fbf8f1";
    private static final String COLOR_DANGER = "#c73e1d";

    private final UserService userService;

    private final Paragraph errorMessage = new Paragraph();
    private final Span statusMessage = new Span();

    private EmailField loginEmail;
    private PasswordField loginPassword;
    private EmailField registrationEmail;
    private PasswordField registrationPassword;
    private PasswordField registrationConfirmPassword;
    private TextField firstName;
    private TextField lastName;
    private TextField phone;
    private TextField street;
    private TextField houseNumber;
    private TextField postalCode;
    private TextField city;
    private TextField addressAddition;
    private TextField registrationCode;
    private String currentRegistrationEmail = "";

    public LoginView(UserService userService) {
        this.userService = userService;

        setWidthFull();
        setMinHeight("100vh");
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle()
                .set("background", COLOR_BG)
                .set("justify-content", "center");

        add(createPanel());
    }

    private Div createPanel() {
        Div panel = new Div();
        panel.getStyle()
                .set("width", "100%")
                .set("max-width", "520px")
                .set("background", "white")
                .set("border-radius", "8px")
                .set("padding", "32px")
                .set("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.1)");

        H1 title = new H1("Account");
        title.getStyle()
                .set("font-size", "28px")
                .set("margin", "0 0 20px 0")
                .set("color", COLOR_PRIMARY)
                .set("text-align", "center");

        configureMessages();
        panel.add(title, errorMessage, statusMessage, createLoginSection(), createRegistrationSection());
        return panel;
    }

    private VerticalLayout createLoginSection() {
        VerticalLayout section = section();
        H2 title = sectionTitle("Anmelden");

        loginEmail = new EmailField("E-Mail");
        loginEmail.setWidthFull();
        loginPassword = new PasswordField("Passwort");
        loginPassword.setWidthFull();

        Button loginButton = primaryButton("Anmelden");
        loginButton.addClickListener(event -> {
            clearMessages();
            UserAuthResult result = userService.login(new UserLoginRequest(
                    loginEmail.getValue(),
                    loginPassword.getValue()
            ));
            handleAuthResult(result);
        });

        section.add(title, loginEmail, loginPassword, loginButton);
        return section;
    }

    private VerticalLayout createRegistrationSection() {
        VerticalLayout section = section();
        section.getStyle()
                .set("border-top", "1px solid #eadfce")
                .set("margin-top", "24px")
                .set("padding-top", "24px");

        registrationEmail = new EmailField("E-Mail");
        registrationPassword = new PasswordField("Passwort");
        registrationConfirmPassword = new PasswordField("Passwort wiederholen");
        firstName = new TextField("Vorname");
        lastName = new TextField("Nachname");
        phone = new TextField("Telefon");
        street = new TextField("Straße");
        houseNumber = new TextField("Hausnummer");
        postalCode = new TextField("PLZ");
        city = new TextField("Stadt");
        addressAddition = new TextField("Adresszusatz");
        registrationCode = new TextField("Code");

        List.<HasSize>of(
                registrationEmail,
                registrationPassword,
                registrationConfirmPassword,
                firstName,
                lastName,
                phone,
                street,
                houseNumber,
                postalCode,
                city,
                addressAddition,
                registrationCode
        ).forEach(field -> field.setWidthFull());

        Button registerButton = primaryButton("Registrieren");
        registerButton.addClickListener(event -> startRegistration());

        Button confirmButton = primaryButton("Code bestätigen");
        confirmButton.addClickListener(event -> completeRegistration());

        section.add(
                sectionTitle("Registrieren"),
                registrationEmail,
                registrationPassword,
                registrationConfirmPassword,
                firstName,
                lastName,
                phone,
                street,
                houseNumber,
                postalCode,
                city,
                addressAddition,
                registerButton,
                new Paragraph("Nach dem Registrieren kommt der Code per E-Mail."),
                registrationCode,
                confirmButton
        );
        return section;
    }

    private void startRegistration() {
        clearMessages();
        currentRegistrationEmail = registrationEmail.getValue();
        UserAuthResult result = userService.startRegistration(new UserRegistrationRequest(
                registrationEmail.getValue(),
                registrationPassword.getValue(),
                registrationConfirmPassword.getValue(),
                firstName.getValue(),
                lastName.getValue(),
                phone.getValue(),
                street.getValue(),
                houseNumber.getValue(),
                postalCode.getValue(),
                city.getValue(),
                addressAddition.getValue()
        ), "127.0.0.1");
        showResult(result);
    }

    private void completeRegistration() {
        clearMessages();
        String email = currentRegistrationEmail == null || currentRegistrationEmail.isBlank()
                ? registrationEmail.getValue()
                : currentRegistrationEmail;
        UserAuthResult result = userService.completeRegistration(new UserRegistrationConfirmationRequest(
                email,
                registrationCode.getValue()
        ));
        handleAuthResult(result);
    }

    private void handleAuthResult(UserAuthResult result) {
        showResult(result);
        if (result.success() && result.userProfile() != null) {
            setSecurityContext(result.userProfile());
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    private void setSecurityContext(UserProfileDto profile) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                profile.email(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + profile.accountRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        getUI().ifPresent(ui -> ui.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        ));
        if (VaadinService.getCurrentRequest() != null) {
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
        }
    }

    private VerticalLayout section() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        return section;
    }

    private H2 sectionTitle(String text) {
        H2 title = new H2(text);
        title.getStyle()
                .set("font-size", "18px")
                .set("margin", "0 0 8px 0")
                .set("color", COLOR_PRIMARY);
        return title;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setWidthFull();
        button.getStyle()
                .set("background", COLOR_PRIMARY)
                .set("color", "white")
                .set("font-weight", "700")
                .set("border-radius", "6px")
                .set("cursor", "pointer");
        return button;
    }

    private void configureMessages() {
        errorMessage.getStyle()
                .set("color", COLOR_DANGER)
                .set("margin", "0 0 16px 0")
                .set("padding", "12px")
                .set("background", "#fce8e0")
                .set("border-radius", "6px")
                .set("display", "none");

        statusMessage.getStyle()
                .set("color", "#3f7d42")
                .set("display", "none")
                .set("margin", "0 0 16px 0")
                .set("padding", "12px")
                .set("background", "#e8f5e5")
                .set("border-radius", "6px");
    }

    private void showResult(UserAuthResult result) {
        if (result.success()) {
            showStatus(result.message());
        } else {
            showError(result.message());
        }
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
