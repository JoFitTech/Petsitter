package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class AddPetPopUp extends Dialog {

    private static final String DARK = "#4a3428";
    private static final String BROWN_BTN = "#5c3d1e";
    private static final String CREAM = "#e8d9c8";
    private static final String LIGHT_BG = "#F8EFE4"; 

    public AddPetPopUp() {
        this.setWidth("560px");
        this.setMaxWidth("95vw");
        
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

        // Header
        H2 title = new H2("Tier hinzufügen");
        title.getStyle()
                .set("color", DARK)
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("margin", "0 0 8px 0");

        // Fields
        TextField nameField = new TextField("Name *");
        nameField.setWidthFull();
        styleInputField(nameField.getElement());

        ComboBox<String> typeField = new ComboBox<>("Tierart *");
        typeField.setItems("Hund", "Katze", "Vogel", "Fisch", "Reptil", "Nagetier", "Sonstiges");
        typeField.setWidthFull();
        styleInputField(typeField.getElement());

        TextField customTypeField = new TextField("Bitte Tierart angeben *");
        customTypeField.setWidthFull();
        customTypeField.setVisible(false);
        styleInputField(customTypeField.getElement());

        typeField.addValueChangeListener(e -> {
            if ("Sonstiges".equals(e.getValue())) {
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
        styleInputField(birthDateField.getElement());

        TextArea infoField = new TextArea("Wichtige Infos");
        infoField.setWidthFull();
        infoField.setMinHeight("100px");
        styleInputField(infoField.getElement());

        // Footer Buttons
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
            System.out.println("AddPetPopUp: Speichern clicked");
            this.close();
        });

        footerButtons.add(btnCancel, btnSave);

        mainContainer.add(title, nameField, typeField, customTypeField, breedField, birthDateField, infoField, footerButtons);
        add(mainContainer);
    }

    private void styleInputField(com.vaadin.flow.dom.Element el) {
        el.getStyle()
            .set("--vaadin-input-field-background", "#FCF9F2") // TOGGLE_BG from CookiePopUp
            .set("--vaadin-input-field-border", "1px solid #efe4d3")
            .set("--vaadin-input-field-border-radius", "12px")
            .set("--vaadin-input-field-value-color", "#4a3428") // DARK
            .set("--lumo-secondary-text-color", "#4a3428") // Label color
            .set("--lumo-body-text-color", "#4a3428")
            .set("color", "#4a3428");
    }
}
