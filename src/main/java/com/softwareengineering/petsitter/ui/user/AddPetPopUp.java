package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.pet.domain.PetTag;
import com.softwareengineering.petsitter.pet.domain.PetVaccinationStatus;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AddPetPopUp extends Dialog {

    private static final String DARK = "#4a3428";
    private static final String BROWN_BTN = "#5c3d1e";
    private static final String CREAM = "#e8d9c8";
    private static final String LIGHT_BG = "#F8EFE4";
    private static final String TYPE_DOG = "Hund";
    private static final String TYPE_CAT = "Katze";
    private static final String TYPE_BIRD = "Vogel";
    private static final String TYPE_RABBIT = "Kaninchen";
    private static final String TYPE_FISH = "Fisch";
    private static final String TYPE_REPTILE = "Reptil";
    private static final String TYPE_RODENT = "Nagetier";
    private static final String TYPE_OTHER = "Sonstiges";

    public AddPetPopUp(PetDto existing, Consumer<PetDto> onSave) {
        this.setWidth("560px");
        this.setMaxWidth("95vw");
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(false);

        this.getElement().getThemeList().add("no-padding");

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(false);
        mainContainer.getStyle()
                .set("background-color", LIGHT_BG)
                .set("padding", "32px 48px")
                .set("border-radius", "16px")
                .set("font-family", "'Inter', sans-serif")
                .set("gap", "16px");

        H2 title = new H2(existing == null ? "Tier hinzufügen" : "Tier bearbeiten");
        title.getStyle()
                .set("color", DARK)
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("margin", "0 0 8px 0");

        TextField nameField = new TextField("Name");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        styleInputField(nameField.getElement());

        ComboBox<String> typeField = new ComboBox<>("Tierart");
        typeField.setItems(TYPE_DOG, TYPE_CAT, TYPE_BIRD, TYPE_RABBIT, TYPE_FISH, TYPE_REPTILE, TYPE_RODENT,
                TYPE_OTHER);
        typeField.setWidthFull();
        typeField.setRequiredIndicatorVisible(true);
        styleInputField(typeField.getElement());

        TextField customTypeField = new TextField("Bitte Tierart angeben");
        customTypeField.setWidthFull();
        customTypeField.setVisible(false);
        styleInputField(customTypeField.getElement());

        typeField.addValueChangeListener(e -> {
            if (TYPE_OTHER.equals(e.getValue())) {
                customTypeField.setVisible(true);
            } else {
                customTypeField.setVisible(false);
                customTypeField.clear();
            }
        });

        TextField breedField = new TextField("Rasse");
        breedField.setWidthFull();
        styleInputField(breedField.getElement());

        DatePicker birthDateField = new DatePicker("Geburtstag");
        birthDateField.setWidthFull();
        birthDateField.setMax(LocalDate.now());
        birthDateField.setLocale(Locale.GERMAN);
        styleInputField(birthDateField.getElement());

        TextArea infoField = new TextArea("Wichtige Infos");
        infoField.setWidthFull();
        infoField.setMinHeight("100px");
        styleInputField(infoField.getElement());

        AtomicReference<PetVaccinationStatus> selectedVaccinationStatus =
                new AtomicReference<>(initialVaccinationStatus(existing));
        Set<PetTag> selectedTags = initialTags(existing);

        VerticalLayout vaccinationStatusTags = buildVaccinationStatusTags(selectedVaccinationStatus);
        VerticalLayout petTagSelector = buildPetTagSelector(selectedTags);

        populateFields(existing, nameField, typeField, customTypeField, breedField, birthDateField, infoField);

        HorizontalLayout footerButtons = new HorizontalLayout();
        footerButtons.setWidthFull();
        footerButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footerButtons.getStyle().set("margin-top", "16px").set("gap", "16px");

        Button btnCancel = new Button("Abbrechen");
        btnCancel.getStyle()
                .set("background-color", CREAM)
                .set("color", DARK)
                .set("border-radius", "24px")
                .set("padding", "0 24px")
                .set("height", "44px")
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("border", "none");
        btnCancel.addClickListener(e -> this.close());

        Button btnSave = new Button("Speichern");
        btnSave.getStyle()
                .set("background-color", BROWN_BTN)
                .set("color", "white")
                .set("border-radius", "24px")
                .set("padding", "0 28px")
                .set("height", "44px")
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("border", "none");
        btnSave.addClickListener(e -> {
            PetDto dto = createDto(existing, nameField, typeField, customTypeField, breedField, birthDateField,
                    infoField, selectedVaccinationStatus.get(), selectedTags);
            if (dto == null) {
                return;
            }
            try {
                onSave.accept(dto);
                this.close();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });

        footerButtons.add(btnCancel, btnSave);

        mainContainer.add(title, nameField, typeField, customTypeField, breedField, birthDateField,
                vaccinationStatusTags, petTagSelector, infoField, footerButtons);
        add(mainContainer);
    }

    private void populateFields(
            PetDto existing,
            TextField nameField,
            ComboBox<String> typeField,
            TextField customTypeField,
            TextField breedField,
            DatePicker birthDateField,
            TextArea infoField) {
        if (existing == null) {
            return;
        }
        setValueIfPresent(nameField, existing.name());
        setValueIfPresent(breedField, existing.breed());
        setValueIfPresent(infoField, existing.notes());
        if (existing.birthDate() != null) {
            birthDateField.setValue(existing.birthDate());
        }
        String displayType = displayType(existing);
        typeField.setValue(displayType);
        if (TYPE_OTHER.equals(displayType)) {
            customTypeField.setVisible(true);
            setValueIfPresent(customTypeField, existing.customSpecies());
        }
    }

    private PetDto createDto(
            PetDto existing,
            TextField nameField,
            ComboBox<String> typeField,
            TextField customTypeField,
            TextField breedField,
            DatePicker birthDateField,
            TextArea infoField,
            PetVaccinationStatus selectedVaccinationStatus,
            Set<PetTag> selectedTags) {
        String name = nameField.getValue();
        String selectedType = typeField.getValue();
        if (name == null || name.isBlank()) {
            Notification.show("Bitte einen Namen eingeben.", 2500, Notification.Position.TOP_CENTER);
            return null;
        }
        if (selectedType == null || selectedType.isBlank()) {
            Notification.show("Bitte eine Tierart auswählen.", 2500, Notification.Position.TOP_CENTER);
            return null;
        }
        if (TYPE_OTHER.equals(selectedType) && customTypeField.getValue().isBlank()) {
            Notification.show("Bitte die Tierart beschreiben.", 2500, Notification.Position.TOP_CENTER);
            return null;
        }

        PetSpecies species = mapSpecies(selectedType);
        return new PetDto(
                existing != null ? existing.id() : null,
                name.trim(),
                species,
                mapCustomSpecies(selectedType, customTypeField),
                blankToNull(breedField.getValue()),
                birthDateField.getValue(),
                blankToNull(infoField.getValue()),
                selectedVaccinationStatus == null
                        ? PetVaccinationStatus.UNBEKANNT
                        : selectedVaccinationStatus,
                selectedTags == null ? Set.of() : new LinkedHashSet<>(selectedTags));
    }

    private PetVaccinationStatus initialVaccinationStatus(PetDto existing) {
        if (existing == null || existing.vaccinationStatus() == null) {
            return PetVaccinationStatus.UNBEKANNT;
        }
        return existing.vaccinationStatus();
    }

    private Set<PetTag> initialTags(PetDto existing) {
        if (existing == null || existing.tags() == null || existing.tags().isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(existing.tags());
    }

    private VerticalLayout buildVaccinationStatusTags(AtomicReference<PetVaccinationStatus> selectedStatus) {
        HorizontalLayout tags = tagRow();
        Map<PetVaccinationStatus, Button> buttons = new LinkedHashMap<>();
        for (PetVaccinationStatus status : PetVaccinationStatus.values()) {
            Button tag = tagButton(status.label());
            buttons.put(status, tag);
            tag.addClickListener(event -> {
                selectedStatus.set(status);
                buttons.forEach((candidate, button) -> styleTagButton(button, candidate == selectedStatus.get()));
            });
            tags.add(tag);
        }
        buttons.forEach((status, button) -> styleTagButton(button, status == selectedStatus.get()));
        return tagSection("Impfstatus", tags);
    }

    private VerticalLayout buildPetTagSelector(Set<PetTag> selectedTags) {
        HorizontalLayout tags = tagRow();
        Map<PetTag, Button> buttons = new LinkedHashMap<>();
        for (PetTag petTag : PetTag.values()) {
            Button tag = tagButton(petTag.label());
            buttons.put(petTag, tag);
            tag.addClickListener(event -> {
                if (selectedTags.contains(petTag)) {
                    selectedTags.remove(petTag);
                } else {
                    selectedTags.add(petTag);
                }
                styleTagButton(tag, selectedTags.contains(petTag));
            });
            tags.add(tag);
        }
        buttons.forEach((petTag, button) -> styleTagButton(button, selectedTags.contains(petTag)));
        return tagSection("Eigenschaften", tags);
    }

    private VerticalLayout tagSection(String labelText, HorizontalLayout tags) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "8px");

        Span label = new Span(labelText);
        label.getStyle()
                .set("color", DARK)
                .set("font-size", "14px")
                .set("font-weight", "700");

        section.add(label, tags);
        return section;
    }

    private HorizontalLayout tagRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setPadding(false);
        row.setSpacing(false);
        row.getStyle()
                .set("gap", "8px")
                .set("flex-wrap", "wrap");
        return row;
    }

    private Button tagButton(String label) {
        Button button = new Button(label);
        button.getStyle()
                .set("border-radius", "999px")
                .set("height", "34px")
                .set("padding", "0 14px")
                .set("font-size", "13px")
                .set("font-weight", "800")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        return button;
    }

    private void styleTagButton(Button button, boolean selected) {
        button.getElement().setAttribute("aria-pressed", String.valueOf(selected));
        button.getStyle()
                .set("background", selected ? BROWN_BTN : "#FCF9F2")
                .set("color", selected ? "white" : DARK)
                .set("border", selected ? "1px solid " + BROWN_BTN : "1px solid #ead5ae");
    }

    private String displayType(PetDto pet) {
        if (pet.species() == PetSpecies.OTHER) {
            return TYPE_RODENT.equals(pet.customSpecies()) ? TYPE_RODENT : TYPE_OTHER;
        }
        return switch (pet.species()) {
            case DOG -> TYPE_DOG;
            case CAT -> TYPE_CAT;
            case BIRD -> TYPE_BIRD;
            case RABBIT -> TYPE_RABBIT;
            case FISH -> TYPE_FISH;
            case REPTILE -> TYPE_REPTILE;
            case OTHER -> TYPE_OTHER;
        };
    }

    private PetSpecies mapSpecies(String selectedType) {
        return switch (selectedType) {
            case TYPE_DOG -> PetSpecies.DOG;
            case TYPE_CAT -> PetSpecies.CAT;
            case TYPE_BIRD -> PetSpecies.BIRD;
            case TYPE_RABBIT -> PetSpecies.RABBIT;
            case TYPE_FISH -> PetSpecies.FISH;
            case TYPE_REPTILE -> PetSpecies.REPTILE;
            default -> PetSpecies.OTHER;
        };
    }

    private String mapCustomSpecies(String selectedType, TextField customTypeField) {
        return switch (selectedType) {
            case TYPE_RODENT -> selectedType;
            case TYPE_OTHER -> customTypeField.getValue().trim();
            default -> null;
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void setValueIfPresent(TextField field, String value) {
        if (value != null) {
            field.setValue(value);
        }
    }

    private void setValueIfPresent(TextArea field, String value) {
        if (value != null) {
            field.setValue(value);
        }
    }

    private void styleInputField(Element el) {
        el.getStyle()
                .set("--vaadin-input-field-background", "#FCF9F2")
                .set("--vaadin-input-field-border", "1px solid #efe4d3")
                .set("--vaadin-input-field-border-radius", "12px")
                .set("--vaadin-input-field-value-color", "#4a3428")
                .set("--lumo-secondary-text-color", "#4a3428")
                .set("--lumo-body-text-color", "#4a3428")
                .set("color", "#4a3428");
    }
}
