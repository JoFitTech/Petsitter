package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.user.dto.UserAuthResult;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.dto.UserProfileUpdateRequest;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class PersonalDetailView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final List<String> NATIONALITIES = List.of(
            "deutsch",
            "österreichisch",
            "schweizerisch",
            "französisch",
            "italienisch",
            "spanisch",
            "polnisch",
            "niederländisch",
            "türkisch",
            "ukrainisch",
            "andere");

    private final UserService userService;
    private final Consumer<UserProfileDto> profileChanged;
    private UserProfileDto profile;

    public PersonalDetailView(
            UserService userService,
            UserProfileDto profile,
            Consumer<UserProfileDto> profileChanged
    ) {
        this.userService = userService;
        this.profile = profile;
        this.profileChanged = profileChanged;

        setWidthFull();
        getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("padding", "40px")
            .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
            .set("box-sizing", "border-box");

        showViewMode();
    }

    private void showViewMode() {
        removeAll();
        Button bearbeiten = editBtn();
        bearbeiten.addClickListener(e -> showEditMode());
        add(panelHeader("Persönliche Angaben", bearbeiten));

        add(buildDataRow("Name", fullName(), false, null));
        add(buildDataRow("Anzeigename", displayName(), false, null));
        add(buildDataRow("Email", profile.email(), false, null));
        if (!isBlank(profile.pendingEmail())) {
            add(buildDataRow("Neue Email", profile.pendingEmail() + "\nwartet auf Bestätigung", false, null));
        }
        add(buildDataRow("Telefonnummer", displayValue(profile.phone()), false, null));
        add(buildDataRow("Geburtsdatum", formatDate(profile.birthDate()), false, null));
        add(buildDataRow("Nationalität", displayValue(profile.nationality()), false, null));
        add(buildDataRow("Adresse", addressLines(), false, null));
        if (!isBlank(profile.pendingEmail())) {
            add(buildEmailConfirmationBox());
        }
    }

    private void showEditMode() {
        removeAll();
        TextField firstNameField = styledTextField("Vorname", profile.firstName());
        TextField lastNameField = styledTextField("Nachname", profile.lastName());
        TextField displayNameField = styledTextField("Anzeigename", displayName());
        TextField mailField = styledTextField("Email", profile.email());
        TextField phoneField = styledTextField("Telefonnummer", valueOrEmpty(profile.phone()));
        DatePicker birthDatePicker = styledDatePicker("Geburtsdatum", profile.birthDate());
        ComboBox<String> nationalityField = styledComboBox("Nationalität", valueOrEmpty(profile.nationality()));
        TextField streetField = styledTextField("Straße", profile.street());
        TextField houseNumberField = styledTextField("Hausnummer", profile.houseNumber());
        TextField postalCodeField = styledTextField("PLZ", profile.postalCode());
        TextField cityField = styledTextField("Stadt", profile.city());
        TextField addressAdditionField = styledTextField("Adresszusatz", valueOrEmpty(profile.addressAddition()));
        TextField countryField = styledTextField("Land", valueOrDefault(profile.country(), "Deutschland"));

        Button save = saveBtn("Speichern");
        Button cancel = cancelBtn("Abbrechen");

        save.addClickListener(e -> {
            String oldEmail = profile.email();
            UserAuthResult profileResult = userService.updateCurrentUserProfile(new UserProfileUpdateRequest(
                    firstNameField.getValue(),
                    lastNameField.getValue(),
                    displayNameField.getValue(),
                    phoneField.getValue(),
                    birthDatePicker.getValue(),
                    nationalityField.getValue(),
                    profile.language(),
                    profile.bio(),
                    streetField.getValue(),
                    houseNumberField.getValue(),
                    postalCodeField.getValue(),
                    cityField.getValue(),
                    addressAdditionField.getValue(),
                    countryField.getValue()));
            if (!profileResult.success()) {
                showError(profileResult.message());
                return;
            }
            updateProfile(profileResult.userProfile());

            String requestedEmail = normalizeEmail(mailField.getValue());
            if (!requestedEmail.equals(normalizeEmail(oldEmail))) {
                UserAuthResult emailResult = userService.requestCurrentUserEmailChange(requestedEmail, currentRequestIp());
                handleProfileResult(emailResult, () -> {
                    showStatus(emailResult.message());
                    showEditMode();
                });
                return;
            }

            showStatus(profileResult.message());
            showViewMode();
        });
        cancel.addClickListener(e -> showViewMode());

        add(panelHeader("Persönliche Angaben", cancel, save));
        add(buildDataRow("Vorname", null, true, firstNameField));
        add(buildDataRow("Nachname", null, true, lastNameField));
        add(buildDataRow("Anzeigename", null, true, displayNameField));
        add(buildDataRow("Email", null, true, mailField));
        add(buildDataRow("Telefonnummer", null, true, phoneField));
        add(buildDataRow("Geburtsdatum", null, true, birthDatePicker));
        add(buildDataRow("Nationalität", null, true, nationalityField));
        add(buildDataRow("Straße", null, true, streetField));
        add(buildDataRow("Hausnummer", null, true, houseNumberField));
        add(buildDataRow("PLZ", null, true, postalCodeField));
        add(buildDataRow("Stadt", null, true, cityField));
        add(buildDataRow("Adresszusatz", null, true, addressAdditionField));
        add(buildDataRow("Land", null, true, countryField));
        if (!isBlank(profile.pendingEmail())) {
            add(buildEmailConfirmationBox());
        }
    }

    private Component buildEmailConfirmationBox() {
        Div box = new Div();
        box.getStyle()
            .set("margin-top", "24px")
            .set("background", "#fffdf8")
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "16px")
            .set("padding", "20px 22px");

        Span label = new Span("Code für neue Email bestätigen: " + profile.pendingEmail());
        label.getStyle()
            .set("display", "block")
            .set("font-weight", "800")
            .set("font-size", "15px")
            .set("color", DARK);

        TextField codeField = styledTextField("Bestätigungscode", "");
        codeField.setPlaceholder("6-stelliger Code");

        Button confirm = saveBtn("Code bestätigen");
        confirm.addClickListener(event -> {
            UserAuthResult result = userService.confirmCurrentUserEmailChange(codeField.getValue());
            handleProfileResult(result, () -> {
                if (result.userProfile() != null) {
                    UserSessionSupport.authenticate(result.userProfile());
                }
                showStatus(result.message());
                showViewMode();
            });
        });

        VerticalLayout layout = new VerticalLayout(label, codeField, confirm);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "12px");
        box.add(layout);
        return box;
    }

    private HorizontalLayout panelHeader(String titleText, Component... actions) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "32px");

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
            .set("font-size", "14px")
            .set("padding", "0 20px")
            .set("height", "40px")
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
            .set("height", "40px")
            .set("padding", "0 20px")
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
            .set("height", "40px")
            .set("padding", "0 20px")
            .set("cursor", "pointer");
        return btn;
    }

    private Component buildDataRow(String label, String value, boolean editMode, Component editField) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.START);
        row.getStyle().set("padding", "20px 0").set("gap", "24px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-weight", "800")
            .set("font-size", "18px")
            .set("color", DARK)
            .set("width", "220px")
            .set("flex-shrink", "0");

        if (editMode && editField != null) {
            editField.getElement().getStyle().set("flex", "1");
            row.add(labelSpan, editField);
        } else {
            VerticalLayout valLayout = new VerticalLayout();
            valLayout.setPadding(false);
            valLayout.setSpacing(false);
            valLayout.getStyle().set("gap", "6px").set("flex", "1");
            if (value != null) {
                for (String line : value.split("\\R")) {
                    Span v = new Span(line);
                    v.getStyle().set("font-size", "18px").set("color", "#5a4030");
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

    private void handleProfileResult(UserAuthResult result, Runnable onSuccess) {
        if (!result.success()) {
            showError(result.message());
            return;
        }
        updateProfile(result.userProfile());
        onSuccess.run();
    }

    private void updateProfile(UserProfileDto updatedProfile) {
        if (updatedProfile == null) {
            return;
        }
        profile = updatedProfile;
        profileChanged.accept(updatedProfile);
    }

    private String currentRequestIp() {
        if (VaadinService.getCurrentRequest() == null) {
            return "unknown";
        }
        String remoteAddr = VaadinService.getCurrentRequest().getRemoteAddr();
        return isBlank(remoteAddr) ? "unknown" : remoteAddr;
    }

    private String fullName() {
        return (valueOrEmpty(profile.firstName()) + " " + valueOrEmpty(profile.lastName())).trim();
    }

    private String displayName() {
        return valueOrDefault(profile.displayName(), fullName());
    }

    private String addressLines() {
        StringBuilder address = new StringBuilder();
        address.append(valueOrEmpty(profile.street())).append(" ").append(valueOrEmpty(profile.houseNumber()).trim());
        if (!isBlank(profile.addressAddition())) {
            address.append("\n").append(profile.addressAddition());
        }
        address.append("\n").append(locationLine());
        address.append("\n").append(valueOrDefault(profile.country(), "Deutschland"));
        return address.toString().trim();
    }

    private String locationLine() {
        String location = (valueOrEmpty(profile.postalCode()) + " " + valueOrEmpty(profile.city())).trim();
        return valueOrDefault(location, "-");
    }

    private String formatDate(LocalDate date) {
        return displayValue(date == null ? "" : DATE_FORMATTER.format(date));
    }

    private String displayValue(String value) {
        return valueOrDefault(value, "-");
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String normalizeEmail(String email) {
        return valueOrEmpty(email).trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void showError(String message) {
        Notification.show(message, 3500, Notification.Position.TOP_CENTER);
    }

    private void showStatus(String message) {
        Notification.show(message, 2500, Notification.Position.TOP_CENTER);
    }

    private TextField styledTextField(String label, String value) {
        TextField tf = new TextField(label);
        tf.setPlaceholder(valueOrEmpty(value));
        tf.setValue(valueOrEmpty(value));
        tf.setWidthFull();
        return tf;
    }

    private DatePicker styledDatePicker(String label, LocalDate value) {
        DatePicker picker = new DatePicker(label);
        picker.setValue(value);
        picker.setLocale(Locale.GERMANY);
        picker.setPlaceholder("TT.MM.JJJJ");
        picker.setWidthFull();
        picker.setI18n(new DatePicker.DatePickerI18n()
                .setMonthNames(List.of(
                        "Januar", "Februar", "März", "April", "Mai", "Juni",
                        "Juli", "August", "September", "Oktober", "November", "Dezember"))
                .setWeekdays(List.of(
                        "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"))
                .setWeekdaysShort(List.of("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"))
                .setToday("Heute")
                .setCancel("Abbrechen")
                .setFirstDayOfWeek(1));
        return picker;
    }

    private ComboBox<String> styledComboBox(String label, String value) {
        ComboBox<String> comboBox = new ComboBox<>(label);
        comboBox.setItems(NATIONALITIES);
        comboBox.setAllowCustomValue(true);
        comboBox.addCustomValueSetListener(event -> comboBox.setValue(event.getDetail()));
        comboBox.setPlaceholder("Nationalität auswählen");
        if (!isBlank(value)) {
            comboBox.setValue(value);
        }
        comboBox.setWidthFull();
        return comboBox;
    }
}
