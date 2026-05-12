package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class PersonalDetailView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    public PersonalDetailView() {
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

        add(buildDataRow("Name",          "Max Mustermann",        false, null));
        add(buildDataRow("Anzeigename",   "Max",                   false, null));
        add(buildDataRow("Email",         "Max@gmail.com",         false, null));
        add(buildDataRow("Telefonnummer", "0151 8765456783",       false, null));
        add(buildDataRow("Geburtsdatum",  "02.05.2004",            false, null));
        add(buildDataRow("Nationalität",  "deutsch",               false, null));
        add(buildDataRow("Adresse",
            "Mustermann Straße 7\n76689 Neuthard\nDeutschland",          false, null));
    }

    private void showEditMode() {
        removeAll();
        TextField nameField  = styledTextField("Max Mustermann");
        TextField nickField  = styledTextField("Max");
        TextField mailField  = styledTextField("Max@gmail.com");
        TextField phoneField = styledTextField("0151 8765456783");
        TextField dateField  = styledTextField("02.05.2004");
        TextField natField   = styledTextField("deutsch");
        TextArea  adrField   = styledTextArea();
        adrField.setValue("Mustermann Straße 7\n76689 Neuthard\nDeutschland");

        Button save   = saveBtn("Speichern");
        Button cancel = cancelBtn("Abbrechen");

        save.addClickListener(e -> {
            System.out.println("TODO: userService.updatePersonalData(name=" + nameField.getValue() + ")");
            showViewMode();
        });
        cancel.addClickListener(e -> showViewMode());

        add(panelHeader("Persönliche Angaben", cancel, save));
        add(buildDataRow("Name",          null, true, nameField));
        add(buildDataRow("Anzeigename",   null, true, nickField));
        add(buildDataRow("Email",         null, true, mailField));
        add(buildDataRow("Telefonnummer", null, true, phoneField));
        add(buildDataRow("Geburtsdatum",  null, true, dateField));
        add(buildDataRow("Nationalität",  null, true, natField));
        add(buildDataRow("Adresse",       null, true, adrField));
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
        for (Component a : actions) actionRow.add(a);

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
                for (String line : value.split("\n")) {
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

    private TextField styledTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPlaceholder(placeholder);
        tf.setValue(placeholder);
        tf.setWidthFull();
        return tf;
    }

    private TextArea styledTextArea() {
        TextArea ta = new TextArea();
        ta.setWidthFull();
        ta.setMinHeight("100px");
        return ta;
    }
}
