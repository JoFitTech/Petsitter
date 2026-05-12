package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MyOffers extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    public MyOffers() {
        setWidthFull();
        getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("padding", "36px")
            .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
            .set("box-sizing", "border-box");

        add(buildHeader());
        
        HorizontalLayout cardsContainer = new HorizontalLayout();
        cardsContainer.setWidthFull();
        cardsContainer.setSpacing(false);
        cardsContainer.getStyle().set("gap", "24px").set("flex-wrap", "wrap");

        cardsContainer.add(
            buildOfferCard("Marie, 31, zeitlich flexibel", "20.–22. Juni", "145 €", "2,4 km", false),
            buildOfferCard("Gassiservice", "?", "?", "?", true)
        );

        add(cardsContainer);
    }

    private Component buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "36px");
        
        H2 title = new H2("Meine Aufträge");
        title.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);

        Button addBtn = new Button("Auftrag anbieten", new Icon(VaadinIcon.PLUS));
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

        addBtn.addClickListener(e -> System.out.println("TODO: Auftrag anbieten clicked"));

        row.add(title, addBtn);
        return row;
    }

    private Component buildOfferCard(String title, String time, String earnings, String distance, boolean isDraft) {
        Div card = new Div();
        card.getStyle()
            .set("width", "calc(50% - 12px)")
            .set("min-width", "280px")
            .set("background", "#ffffff")
            .set("border-radius", "20px")
            .set("box-shadow", "0 8px 24px rgba(74,52,40,0.06)")
            .set("padding", "20px")
            .set("box-sizing", "border-box")
            .set("cursor", "pointer");

        card.addClickListener(e -> System.out.println("TODO: Card clicked: " + title));

        Div imagePlaceholder = new Div();
        imagePlaceholder.getStyle()
            .set("height", "180px")
            .set("width", "100%")
            .set("border-radius", "12px")
            .set("background", "#d4b896")
            .set("position", "relative")
            .set("margin-bottom", "20px");
            
        if (isDraft) {
            Span draftBadge = new Span("Entwurf");
            draftBadge.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px")
                .set("background", "white")
                .set("color", DARK)
                .set("font-size", "13px")
                .set("font-weight", "700")
                .set("padding", "6px 16px")
                .set("border-radius", "20px");
            imagePlaceholder.add(draftBadge);
        }

        H3 titleSpan = new H3(title);
        titleSpan.getStyle()
            .set("margin", "0 0 20px 0")
            .set("font-size", "20px")
            .set("font-weight", "800")
            .set("color", DARK);

        HorizontalLayout detailsRow = new HorizontalLayout();
        detailsRow.setWidthFull();
        detailsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        detailsRow.add(
            buildDetailColumn("Zeitraum", time, DARK),
            buildDetailColumn("Verdienst", earnings, "#a5663b"),
            buildDetailColumn("Entfernung", distance, DARK)
        );

        card.add(imagePlaceholder, titleSpan, detailsRow);
        
        return card;
    }

    private Component buildDetailColumn(String labelText, String valueText, String valueColor) {
        VerticalLayout col = new VerticalLayout();
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("gap", "4px");
        
        Span label = new Span(labelText);
        label.getStyle()
            .set("font-size", "12px")
            .set("color", "#a08060")
            .set("font-weight", "600");
            
        Span value = new Span(valueText);
        value.getStyle()
            .set("font-size", "15px")
            .set("font-weight", "800")
            .set("color", valueColor);
            
        col.add(label, value);
        return col;
    }
}
