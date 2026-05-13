package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MyPetView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    public MyPetView() {
        setWidthFull();
        getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("padding", "36px")
            .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
            .set("box-sizing", "border-box");

        add(buildHeader());
        add(buildPetCard("Roland", "Hund", "deutsch Drahthaar", "4 Jahre alt"));
        add(buildPetCard("Marie", "Katze", "Persisch", "4 Jahre alt"));
    }

    private Component buildHeader() {
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
        subtitle.getStyle().set("margin", "4px 0 0 0").set("font-size", "13px").set("color", "#a08060").set("font-style", "italic").set("font-weight", "700");

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

        addBtn.addClickListener(e -> {
            AddPetPopUp popUp = new AddPetPopUp();
            popUp.open();
        });

        row.add(titleLayout, addBtn);
        return row;
    }

    private Component buildPetCard(String name, String type, String breed, String age) {
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
        
        H3 nameSpan = new H3(name);
        nameSpan.getStyle().set("margin", "0 0 8px 0").set("font-size", "22px").set("font-weight", "800").set("color", DARK);

        info.add(nameSpan);
        
        for (String detail : new String[]{type, breed, age}) {
            Span s = new Span(detail);
            s.getStyle().set("font-size", "15px").set("color", "#7a6050").set("font-weight", "700").set("font-style", "italic").set("line-height", "1.4");
            info.add(s);
        }

        leftSection.add(avatar, info);

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
        
        editBtn.addClickListener(e -> System.out.println("TODO: Bearbeiten clicked for " + name));

        topRow.add(leftSection, editBtn);

        VerticalLayout bottomSection = new VerticalLayout();
        bottomSection.setPadding(false);
        bottomSection.setSpacing(false);
        bottomSection.getStyle().set("margin-top", "24px");

        Span infosLabel = new Span("Wichtige Infos");
        infosLabel.getStyle().set("font-weight", "800").set("font-size", "18px").set("color", DARK).set("margin-bottom", "10px");

        Div infosBox = new Div();
        infosBox.getStyle()
            .set("border", "1px solid #ead5ae")
            .set("border-radius", "12px")
            .set("min-height", "110px")
            .set("width", "100%")
            .set("background", "#fbf8f1");

        bottomSection.add(infosLabel, infosBox);

        card.add(topRow, bottomSection);
        return card;
    }
}
