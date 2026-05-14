package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.pet.service.PetService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.dto.UserProfileUpdateRequest;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.time.LocalDate;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mein Profil | Pawsitter")
@PermitAll
public class UserView extends VerticalLayout implements BeforeEnterObserver {

    private static final String DARK     = "#4a3428";
    private static final String CREAM    = "#fbf8f1";
    private static final String CARD_BG  = "#ffffff";

    private final UserService userService;
    private final PetService petService;
    private final OfferService offerService;
    private UserProfileDto currentProfile;

    private Button btnUeberMich;
    private Button btnMeineTiere;
    private Button btnMeineAuftraege;
    private Button btnMeineFavoriten;
    private Button btnPersAngaben;
    private Button btnLogout;
    private Div contentPanel;

    public UserView(UserService userService, PetService petService, OfferService offerService) {
        this.userService = userService;
        this.petService = petService;
        this.offerService = offerService;
        reloadProfile();

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
            .set("background", CREAM)
            .set("font-family", "Inter, Arial, sans-serif")
            .set("color", DARK);

        add(buildPageHeader());
        add(buildMainArea());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        java.util.List<String> tabs = event.getLocation().getQueryParameters().getParameters().get("tab");
        String tab = (tabs != null && !tabs.isEmpty()) ? tabs.get(0) : null;
        if ("favorites".equals(tab)) {
            setActiveStyle(btnMeineFavoriten);
            showMeineFavoriten();
            return;
        }
        if ("offers".equals(tab)) {
            setActiveStyle(btnMeineAuftraege);
            showMeineAuftraege();
            return;
        }
        setActiveStyle(btnUeberMich);
        showUeberMich();
    }

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

    private Component buildMainArea() {
        Div outer = new Div();
        outer.setWidthFull();
        outer.getStyle()
            .set("position", "relative")
            .set("padding", "0 48px 64px 48px")
            .set("box-sizing", "border-box");

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
        main.getStyle()
            .set("gap", "24px")
            .set("align-items", "flex-start")
            .set("position", "relative")
            .set("z-index", "1");

        contentPanel = new Div();
        contentPanel.setWidthFull();

        main.add(buildSidebar(), contentPanel);
        outer.add(main);
        return outer;
    }

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
        btnPersAngaben.addClickListener(e    -> { setActiveStyle(btnPersAngaben);    showPersAngaben(); });
        btnLogout.addClickListener(e         -> handleLogout());

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
        title.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);

        HorizontalLayout actionRow = new HorizontalLayout();
        actionRow.setSpacing(false);
        actionRow.getStyle().set("gap", "10px");
        for (Component a : actions) {
            actionRow.add(a);
        }

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

    private void showUeberMich() {
        if (!ensureProfileLoaded()) {
            return;
        }
        contentPanel.removeAll();
        Div panel = cardPanel();

        Button bearbeiten = editBtn();
        bearbeiten.addClickListener(e -> showUeberMichEdit());
        panel.add(panelHeader("Über mich", bearbeiten));

        panel.add(buildAvatarCard(false));
        panel.add(divider());
        panel.add(buildBioSection(false, null));
        panel.add(divider());
        panel.add(buildReviews());

        contentPanel.add(panel);
    }

    private void showUeberMichEdit() {
        if (!ensureProfileLoaded()) {
            return;
        }
        contentPanel.removeAll();
        Div panel = cardPanel();

        TextField displayNameField = styledTextField("Anzeigename", displayName());
        TextField langField = styledTextField("Sprache", valueOrDefault(currentProfile.language(), "deutsch"));
        TextArea bioArea = styledTextArea("Über mich");
        bioArea.setValue(valueOrEmpty(currentProfile.bio()));

        Button save = saveBtn("Speichern");
        Button cancel = cancelBtn("Abbrechen");

        save.addClickListener(e -> {
            UserAuthResult result = userService.updateCurrentUserProfile(profileUpdateRequest(
                    currentProfile.firstName(),
                    currentProfile.lastName(),
                    displayNameField.getValue(),
                    currentProfile.phone(),
                    currentProfile.birthDate(),
                    currentProfile.nationality(),
                    langField.getValue(),
                    bioArea.getValue(),
                    currentProfile.street(),
                    currentProfile.houseNumber(),
                    currentProfile.postalCode(),
                    currentProfile.city(),
                    currentProfile.addressAddition(),
                    currentProfile.country()));
            handleProfileResult(result, () -> {
                showStatus(result.message());
                showUeberMich();
            });
        });
        cancel.addClickListener(e -> showUeberMich());

        panel.add(panelHeader("Über mich", cancel, save));
        panel.add(buildAvatarCard(true));
        panel.add(divider());

        Span infoLabel = new Span("Angaben bearbeiten:");
        infoLabel.getStyle().set("font-weight", "700").set("font-size", "15px").set("color", DARK);
        panel.add(infoLabel);

        VerticalLayout fields = new VerticalLayout(displayNameField, langField);
        fields.setPadding(false);
        fields.setSpacing(false);
        fields.getStyle().set("gap", "12px").set("margin-top", "16px");
        panel.add(fields);

        panel.add(divider());
        panel.add(buildBioSection(true, bioArea));
        panel.add(divider());
        panel.add(buildReviews());

        contentPanel.add(panel);
    }

    private Component buildAvatarCard(boolean editMode) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
            .set("box-sizing", "border-box")
            .set("background", "#fffdf8")
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "16px")
            .set("padding", "24px 28px")
            .set("display", "flex")
            .set("gap", "28px")
            .set("align-items", "flex-start");

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

        Div svgWrap = new Div();
        svgWrap.getElement().setProperty("innerHTML",
            "<svg width='56' height='56' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<circle cx='12' cy='8' r='4' fill='#a07850'/>" +
            "<path d='M4 20c0-4 3.6-7 8-7s8 3 8 7' fill='#a07850'/></svg>");
        avatar.add(svgWrap);
        avatarWrap.add(avatar);

        if (editMode) {
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

            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
            upload.setMaxFiles(1);
            upload.setMaxFileSize(5 * 1024 * 1024);
            upload.getStyle().set("display", "none");
            camOverlay.getElement().executeJs(
                "this.addEventListener('click', () => { var inp = $0.querySelector('input[type=file]'); if(inp) inp.click(); })",
                upload.getElement());
            avatarWrap.add(upload);
        }

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("gap", "4px");

        Span stars = new Span("★★★★★");
        stars.getStyle().set("color", "#f5c842").set("font-size", "20px").set("letter-spacing", "2px");

        Span verified = new Span("✓ Verifiziert");
        verified.getStyle()
            .set("color", "#4caf50").set("font-size", "13px").set("font-weight", "600");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topRow.add(stars, verified);

        H3 name = new H3(displayName());
        name.getStyle()
            .set("margin", "4px 0 8px 0")
            .set("font-size", "20px")
            .set("font-weight", "800");

        Component pets = styledInfoLineWithIcon(VaadinIcon.HEART, valueOrDefault(currentProfile.petSummary(), "Keine Haustiere"));
        
        int age = currentProfile.birthDate() != null ? java.time.Period.between(currentProfile.birthDate(), java.time.LocalDate.now()).getYears() : 0;
        String ageStr = age > 0 ? age + " Jahre" : "Alter nicht angegeben";
        Component userAge = styledInfoLineWithIcon(VaadinIcon.USER, ageStr);
        
        Component loc  = styledInfoLineWithIcon(VaadinIcon.MAP_MARKER, locationLine());
        Component lang = styledInfoLineWithIcon(VaadinIcon.GLOBE, valueOrDefault(currentProfile.language(), "deutsch"));

        info.add(topRow, name, pets, userAge, loc, lang);
        card.add(avatarWrap, info);
        return card;
    }

    private Component styledInfoLineWithIcon(VaadinIcon icon, String text) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("gap", "8px");

        Icon i = new Icon(icon);
        i.setSize("16px");
        i.setColor("#5a4030");

        Span s = new Span(text);
        s.getStyle().set("font-size", "14px").set("color", "#5a4030").set("font-weight", "500");
        
        layout.add(i, s);
        return layout;
    }

    private Component buildBioSection(boolean editMode, TextArea existingArea) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();
        section.getStyle().set("gap", "12px");

        H3 label = new H3("Ich und meine Tiere:");
        label.getStyle().set("margin", "0").set("font-weight", "800").set("font-size", "20px").set("color", DARK);
        section.add(label);

        if (editMode && existingArea != null) {
            existingArea.setWidthFull();
            existingArea.setMinHeight("120px");
            section.add(existingArea);
            return section;
        }

        Div bioBox = new Div();
        bioBox.setWidthFull();
        bioBox.getStyle()
            .set("box-sizing", "border-box")
            .set("background", "#fffdf8")
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "12px")
            .set("padding", "18px 20px")
            .set("font-size", "14px")
            .set("line-height", "1.7")
            .set("color", "#5a4030")
            .set("min-height", "100px");

        String bio = valueOrDefault(currentProfile.bio(), "Noch keine Beschreibung hinterlegt.");
        for (String line : bio.split("\\R")) {
            Paragraph paragraph = new Paragraph(line);
            paragraph.getStyle().set("margin", "0 0 6px 0");
            bioBox.add(paragraph);
        }
        section.add(bioBox);
        return section;
    }

    private Component buildReviews() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();
        section.getStyle().set("gap", "14px");

        H3 reviewTitle = new H3("Meine Bewertungen");
        reviewTitle.getStyle().set("margin", "0").set("font-size", "20px").set("font-weight", "800").set("color", DARK);
        section.add(reviewTitle);

        section.add(reviewCard("★★★★★", "Sehr zuverlässig",
            "Bruno war bestens betreut. Kommunikation und Übergabe waren super unkompliziert."));
        section.add(reviewCard("★★★★★", "Freundlich und flexibel",
            "Sehr angenehmer Kontakt, unsere Katze Mia hat sich schnell wohlgefühlt."));
        return section;
    }

    private Div reviewCard(String starsText, String headline, String text) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
            .set("box-sizing", "border-box")
            .set("background", "#fffdf8")
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "14px")
            .set("padding", "18px 22px");

        HorizontalLayout top = new HorizontalLayout();
        top.setAlignItems(FlexComponent.Alignment.CENTER);
        top.setSpacing(false);
        top.getStyle().set("gap", "8px");

        Span stars = new Span(starsText);
        stars.getStyle().set("color", "#f5c842").set("font-size", "16px").set("letter-spacing", "1px");

        Span h = new Span(headline);
        h.getStyle().set("font-weight", "700").set("font-size", "14px").set("color", DARK);

        top.add(stars, h);

        Span t = new Span(text);
        t.getStyle().set("font-size", "13px").set("color", "#7a6050").set("margin-top", "6px").set("display", "block");

        card.add(top, t);
        return card;
    }

    private void showMeineTiere() {
        contentPanel.removeAll();
        contentPanel.add(new MyPetView(petService));
    }

    private void showMeineAuftraege() {
        contentPanel.removeAll();
        contentPanel.add(new MyOffers(offerService));
    }

    private void showMeineFavoriten() {
        contentPanel.removeAll();
        contentPanel.add(new MyFavoritesView());
    }

    private void showPersAngaben() {
        if (!ensureProfileLoaded()) {
            return;
        }
        contentPanel.removeAll();
        contentPanel.add(new PersonalDetailView(userService, currentProfile, profile -> currentProfile = profile));
    }

    private void handleLogout() {
        UserSessionSupport.logout();
        UI.getCurrent().getPage().setLocation("/logout");
    }

    private void reloadProfile() {
        currentProfile = userService.getCurrentUserProfile().orElse(null);
    }

    private boolean ensureProfileLoaded() {
        if (currentProfile != null) {
            return true;
        }
        reloadProfile();
        if (currentProfile == null) {
            showMissingProfile();
            return false;
        }
        return true;
    }

    private void showMissingProfile() {
        contentPanel.removeAll();
        Div panel = cardPanel();
        panel.add(panelHeader("Profil"));
        panel.add(placeholder("Bitte melde dich an, um dein Profil zu verwalten."));
        contentPanel.add(panel);
    }

    private void handleProfileResult(UserAuthResult result, Runnable onSuccess) {
        if (!result.success()) {
            showError(result.message());
            return;
        }
        if (result.userProfile() != null) {
            currentProfile = result.userProfile();
        }
        onSuccess.run();
    }

    private UserProfileUpdateRequest profileUpdateRequest(
            String firstName,
            String lastName,
            String displayName,
            String phone,
            LocalDate birthDate,
            String nationality,
            String language,
            String bio,
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String addressAddition,
            String country
    ) {
        return new UserProfileUpdateRequest(
                firstName,
                lastName,
                displayName,
                phone,
                birthDate,
                nationality,
                language,
                bio,
                street,
                houseNumber,
                postalCode,
                city,
                addressAddition,
                country);
    }

    private String fullName() {
        return (valueOrEmpty(currentProfile.firstName()) + " " + valueOrEmpty(currentProfile.lastName())).trim();
    }

    private String displayName() {
        return valueOrDefault(currentProfile.displayName(), fullName());
    }

    private String locationLine() {
        String location = (valueOrEmpty(currentProfile.postalCode()) + " " + valueOrEmpty(currentProfile.city())).trim();
        return valueOrDefault(location, "-");
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void showError(String message) {
        Notification.show(message, 3500, Notification.Position.TOP_CENTER);
    }

    private void showStatus(String message) {
        Notification.show(message, 2500, Notification.Position.TOP_CENTER);
    }

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

    private TextField styledTextField(String label, String value) {
        TextField tf = new TextField(label);
        tf.setValue(valueOrEmpty(value));
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
