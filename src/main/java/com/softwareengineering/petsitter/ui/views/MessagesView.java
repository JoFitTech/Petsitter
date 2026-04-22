package com.softwareengineering.petsitter.ui.views;

import com.softwareengineering.petsitter.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "nachrichten", layout = MainLayout.class)
@PageTitle("Nachrichten | Petsitter")
public class MessagesView extends VerticalLayout {

    public MessagesView() {
        add(new H2("Nachrichten"));
        add(new Paragraph("Hier findest du die Kommunikation mit Tierbesitzern."));
        setPadding(true);
    }
}

