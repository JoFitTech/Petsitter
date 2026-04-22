package com.softwareengineering.petsitter.ui.views;

import com.softwareengineering.petsitter.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Petsitter")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        add(new H2("Willkommen bei Petsitter"));
        add(new Paragraph("Hier entsteht dein Dashboard mit den wichtigsten Uebersichten."));
        setPadding(true);
    }
}

