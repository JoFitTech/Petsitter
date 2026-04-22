package com.softwareengineering.petsitter.ui.views;

import com.softwareengineering.petsitter.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "haustiere", layout = MainLayout.class)
@PageTitle("Haustiere | Petsitter")
public class PetsView extends VerticalLayout {

    public PetsView() {
        add(new H2("Haustiere"));
        add(new Paragraph("Hier verwaltest du Profile von Hunden, Katzen und anderen Tieren."));
        setPadding(true);
    }
}

