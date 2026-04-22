package com.softwareengineering.petsitter.ui.views;

import com.softwareengineering.petsitter.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "profil", layout = MainLayout.class)
@PageTitle("Profil | Petsitter")
public class ProfileView extends VerticalLayout {

    public ProfileView() {
        add(new H2("Profil"));
        add(new Paragraph("Hier kannst du dein Petsitter-Profil pflegen."));
        setPadding(true);
    }
}

