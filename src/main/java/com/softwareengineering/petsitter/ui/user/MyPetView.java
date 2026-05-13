package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.pet.service.PetService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

public class MyPetView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    private final PetService petService;
    private final Div petListContainer;

    public MyPetView(PetService petService) {
        this.petService = petService;

        setWidthFull();
        getStyle()
                .set("background", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "36px")
                .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
                .set("box-sizing", "border-box");

        add(buildHeader());

        petListContainer = new Div();
        petListContainer.setWidthFull();
        add(petListContainer);

        loadPets();
    }

    private void loadPets() {
        petListContainer.removeAll();
        List<PetDto> pets = petService.getPetDtosForCurrentUser();
        if (pets.isEmpty()) {
            Span empty = new Span("Noch keine Tiere hinterlegt.");
            empty.getStyle().set("color", "#a08060").set("font-style", "italic").set("font-size", "15px");
            petListContainer.add(empty);
        } else {
            pets.forEach(pet -> petListContainer.add(buildPetCard(pet)));
        }
    }

    private Div buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "28px");

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H2 title = new H2("Meine Tiere");
        title.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);

        Span subtitle = new Span("*Diese Inhalte werden automatisch in deine Aufträge übernommen*");
        subtitle.getStyle().set("margin", "4px 0 0 0").set("font-size", "13px").set("color", "#a08060")
                .set("font-style", "italic").set("font-weight", "700");

        titleLayout.add(title, subtitle);

        Button addBtn = new Button("Tier hinzufügen", new Icon(VaadinIcon.PLUS));
        addBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", DARK)
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("padding", "0 20px")
                .set("height", "40px")
                .set("cursor", "pointer");
        addBtn.addClickListener(e -> openPetDialog(null));

        row.add(titleLayout, addBtn);

        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.add(row);
        return wrapper;
    }

    private Div buildPetCard(PetDto pet) {
        Div card = new Div();
        card.getStyle()
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "16px")
                .set("padding", "28px")
                .set("margin-bottom", "20px")
                .set("background", "#ffffff");

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.START);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setSpacing(false);
        leftSection.getStyle().set("gap", "24px");
        leftSection.setAlignItems(FlexComponent.Alignment.START);

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "100px").set("height", "100px")
                .set("border-radius", "50%")
                .set("background", "#e3cda8")
                .set("flex-shrink", "0");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        H3 nameSpan = new H3(pet.name());
        nameSpan.getStyle().set("margin", "0 0 8px 0").set("font-size", "22px").set("font-weight", "800").set("color",
                DARK);
        info.add(nameSpan);

        String speciesText = displaySpeciesLabel(pet);
        String breedText = pet.breed() != null && !pet.breed().isBlank() ? pet.breed() : "";
        String ageText = pet.birthDate() != null ? formatAge(pet.birthDate()) : "";

        for (String detail : new String[] { speciesText, breedText, ageText }) {
            if (!detail.isBlank()) {
                Span s = new Span(detail);
                s.getStyle().set("font-size", "15px").set("color", "#7a6050").set("font-weight", "700")
                        .set("font-style", "italic").set("line-height", "1.4");
                info.add(s);
            }
        }

        leftSection.add(avatar, info);

        HorizontalLayout btnRow = new HorizontalLayout();
        btnRow.setSpacing(false);
        btnRow.getStyle().set("gap", "10px");

        Button editBtn = new Button("Bearbeiten", new Icon(VaadinIcon.PENCIL));
        editBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", DARK)
                .set("color", "white")
                .set("box-shadow", "none")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("padding", "0 20px")
                .set("height", "40px")
                .set("cursor", "pointer");
        editBtn.addClickListener(e -> openPetDialog(pet));

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.getStyle()
                .set("border-radius", "24px")
                .set("background", "#e8ddd4")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("height", "40px")
                .set("width", "40px")
                .set("padding", "0")
                .set("cursor", "pointer");
        deleteBtn.addClickListener(e -> confirmDelete(pet));

        btnRow.add(editBtn, deleteBtn);
        topRow.add(leftSection, btnRow);

        VerticalLayout bottomSection = new VerticalLayout();
        bottomSection.setPadding(false);
        bottomSection.setSpacing(false);
        bottomSection.getStyle().set("margin-top", "24px");

        Span infosLabel = new Span("Wichtige Infos");
        infosLabel.getStyle().set("font-weight", "800").set("font-size", "18px").set("color", DARK).set("margin-bottom",
                "10px");

        Div infosBox = new Div();
        infosBox.getStyle()
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "12px")
                .set("min-height", "80px")
                .set("padding", "14px 16px")
                .set("width", "100%")
                .set("background", "#fbf8f1")
                .set("font-size", "14px")
                .set("color", "#7a6050")
                .set("line-height", "1.6");

        if (pet.notes() != null && !pet.notes().isBlank()) {
            infosBox.add(new Span(pet.notes()));
        } else {
            Span noNotes = new Span("Keine besonderen Infos hinterlegt.");
            noNotes.getStyle().set("font-style", "italic").set("color", "#b0907a");
            infosBox.add(noNotes);
        }

        bottomSection.add(infosLabel, infosBox);
        card.add(topRow, bottomSection);
        return card;
    }

    private void openPetDialog(PetDto existing) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "14px");

        H2 title = new H2(existing == null ? "Tier hinzufügen" : "Tier bearbeiten");
        title.getStyle().set("margin", "0 0 6px 0").set("font-size", "22px").set("font-weight", "800").set("color",
                DARK);

        TextField nameField = new TextField("Name *");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        if (existing != null)
            nameField.setValue(existing.name());

        // Species: ComboBox with all values; OTHER triggers custom text field
        ComboBox<PetSpecies> speciesBox = new ComboBox<>("Tierart *");
        speciesBox.setItems(PetSpecies.values());
        speciesBox.setItemLabelGenerator(PetService::speciesLabel);
        speciesBox.setWidthFull();
        speciesBox.setRequiredIndicatorVisible(true);

        TextField customSpeciesField = new TextField("Tierart (eigene Beschreibung) *");
        customSpeciesField.setWidthFull();
        customSpeciesField.setPlaceholder("z.B. Schildkröte, Hamster, ...");
        customSpeciesField.setVisible(false);

        if (existing != null) {
            speciesBox.setValue(existing.species());
            if (existing.species() == PetSpecies.OTHER) {
                customSpeciesField.setVisible(true);
                if (existing.customSpecies() != null)
                    customSpeciesField.setValue(existing.customSpecies());
            }
        }

        speciesBox.addValueChangeListener(e -> {
            boolean isOther = e.getValue() == PetSpecies.OTHER;
            customSpeciesField.setVisible(isOther);
            if (!isOther)
                customSpeciesField.clear();
        });

        TextField breedField = new TextField("Rasse");
        breedField.setWidthFull();
        if (existing != null && existing.breed() != null)
            breedField.setValue(existing.breed());

        DatePicker birthDatePicker = new DatePicker("Geburtstag");
        birthDatePicker.setWidthFull();
        birthDatePicker.setMax(LocalDate.now());
        birthDatePicker.setLocale(Locale.GERMAN);
        if (existing != null && existing.birthDate() != null)
            birthDatePicker.setValue(existing.birthDate());

        TextArea notesArea = new TextArea("Wichtige Infos");
        notesArea.setWidthFull();
        notesArea.setMinHeight("80px");
        if (existing != null && existing.notes() != null)
            notesArea.setValue(existing.notes());

        Button saveBtn = styledSaveBtn("Speichern");
        Button cancelBtn = styledCancelBtn("Abbrechen");
        cancelBtn.addClickListener(e -> dialog.close());

        saveBtn.addClickListener(e -> {
            String name = nameField.getValue();
            PetSpecies species = speciesBox.getValue();
            if (name == null || name.isBlank()) {
                Notification.show("Bitte einen Namen eingeben.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            if (species == null) {
                Notification.show("Bitte eine Tierart auswählen.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            if (species == PetSpecies.OTHER && customSpeciesField.getValue().isBlank()) {
                Notification.show("Bitte die Tierart beschreiben.", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            try {
                PetDto dto = new PetDto(
                        existing != null ? existing.id() : null,
                        name.trim(),
                        species,
                        species == PetSpecies.OTHER ? customSpeciesField.getValue().trim() : null,
                        breedField.getValue().isBlank() ? null : breedField.getValue().trim(),
                        birthDatePicker.getValue(),
                        notesArea.getValue().isBlank() ? null : notesArea.getValue().trim());
                if (existing == null) {
                    petService.createPetForCurrentUser(dto);
                    Notification.show("Tier hinzugefügt.", 2500, Notification.Position.TOP_CENTER);
                } else {
                    petService.updatePet(existing.id(), dto);
                    Notification.show("Änderungen gespeichert.", 2500, Notification.Position.TOP_CENTER);
                }
                dialog.close();
                loadPets();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });

        HorizontalLayout btns = new HorizontalLayout(cancelBtn, saveBtn);
        btns.setWidthFull();
        btns.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btns.getStyle().set("margin-top", "8px");

        layout.add(title, nameField, speciesBox, customSpeciesField, breedField, birthDatePicker, notesArea, btns);
        dialog.add(layout);
        dialog.open();
    }

    private void confirmDelete(PetDto pet) {
        Dialog confirm = new Dialog();
        confirm.setWidth("360px");
        confirm.setCloseOnEsc(true);
        confirm.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "20px");

        Span msg = new Span("Möchtest du " + pet.name() + " wirklich löschen?");
        msg.getStyle().set("font-size", "15px").set("color", DARK).set("font-weight", "600");

        Button yes = styledSaveBtn("Löschen");
        Button no = styledCancelBtn("Abbrechen");
        no.addClickListener(e -> confirm.close());
        yes.addClickListener(e -> {
            try {
                petService.deletePet(pet.id());
                confirm.close();
                Notification.show(pet.name() + " wurde gelöscht.", 2500, Notification.Position.TOP_CENTER);
                loadPets();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });

        HorizontalLayout btns = new HorizontalLayout(no, yes);
        btns.setWidthFull();
        btns.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btns.getStyle().set("gap", "10px");

        layout.add(msg, btns);
        confirm.add(layout);
        confirm.open();
    }

    private String displaySpeciesLabel(PetDto pet) {
        if (pet.species() == PetSpecies.OTHER && pet.customSpecies() != null && !pet.customSpecies().isBlank()) {
            return pet.customSpecies();
        }
        return PetService.speciesLabel(pet.species());
    }

    private String formatAge(LocalDate birthDate) {
        int years = Period.between(birthDate, LocalDate.now()).getYears();
        if (years == 0) {
            int months = Period.between(birthDate, LocalDate.now()).getMonths();
            return months <= 1 ? "weniger als 1 Monat alt" : months + " Monate alt";
        }
        return years == 1 ? "1 Jahr alt" : years + " Jahre alt";
    }

    private Button styledSaveBtn(String label) {
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

    private Button styledCancelBtn(String label) {
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
}
