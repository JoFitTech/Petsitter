package com.softwareengineering.petsitter.ui.views;

import com.softwareengineering.petsitter.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "buchungen", layout = MainLayout.class)
@PageTitle("Buchungen | Petsitter")
public class BookingsView extends VerticalLayout {

    public BookingsView() {
        add(new H2("Buchungen"));
        add(new Paragraph("Hier siehst du offene und kommende Betreuungsanfragen."));
        setPadding(true);
    }
}

