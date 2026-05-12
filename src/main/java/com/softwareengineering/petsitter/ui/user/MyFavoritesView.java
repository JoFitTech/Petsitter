package com.softwareengineering.petsitter.ui.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MyFavoritesView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    public MyFavoritesView() {
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
            buildFavoriteCard("Marie, 31, zeitlich flexibel", "20.–22. Juni", "145 €", "2,4 km"),
            buildFavoriteCard("Gassiservice", "20.–22. Juni", "145 €", "2,4 km")
        );

        add(cardsContainer);
    }

    private Component buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "36px");
        
        H2 title = new H2("Meine Favoriten");
        title.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);

        row.add(title);
        return row;
    }

    private Component buildFavoriteCard(String title, String time, String earnings, String distance) {
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

        card.addClickListener(e -> System.out.println("TODO: Favorite Card clicked: " + title));

        Div imagePlaceholder = new Div();
        imagePlaceholder.getStyle()
            .set("height", "180px")
            .set("width", "100%")
            .set("border-radius", "12px")
            .set("background", "#d4b896")
            .set("margin-bottom", "20px");

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.getStyle().set("margin-bottom", "20px");

        H3 titleSpan = new H3(title);
        titleSpan.getStyle()
            .set("margin", "0")
            .set("font-size", "20px")
            .set("font-weight", "800")
            .set("color", DARK);

        Icon heartIcon = new Icon(VaadinIcon.HEART_O);
        heartIcon.getStyle()
            .set("color", DARK)
            .set("cursor", "pointer")
            .set("font-size", "20px")
            .set("flex-shrink", "0")
            .set("margin-left", "12px");
            
        // Prevent click on the heart from triggering the card click
        heartIcon.getElement().addEventListener("click", e -> {
            System.out.println("TODO: Toggle favorite for " + title);
        }).addEventData("event.stopPropagation()");

        titleRow.add(titleSpan, heartIcon);

        HorizontalLayout detailsRow = new HorizontalLayout();
        detailsRow.setWidthFull();
        detailsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        detailsRow.add(
            buildDetailColumn("Zeitraum", time, DARK),
            buildDetailColumn("Verdienst", earnings, "#a5663b"),
            buildDetailColumn("Entfernung", distance, DARK)
        );

        card.add(imagePlaceholder, titleRow, detailsRow);
        
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
