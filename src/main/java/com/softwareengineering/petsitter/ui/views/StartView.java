package com.softwareengineering.petsitter.ui.views;

import com.softwareengineering.petsitter.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Start | Petsitter")
@PermitAll
public class StartView extends VerticalLayout {

    public StartView() {
        add(new H2("Petsitter / Pawsitters"));
        add(new Paragraph("Projektgeruest initialisiert"));
        setPadding(true);
        setSpacing(true);
    }
}


