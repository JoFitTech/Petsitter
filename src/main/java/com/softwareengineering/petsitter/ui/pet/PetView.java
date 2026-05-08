package com.softwareengineering.petsitter.ui.pet;

import com.softwareengineering.petsitter.pet.service.PetService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "pets", layout = MainLayout.class)
@PageTitle("Haustiere | Petsitter")
@PermitAll
public class PetView extends VerticalLayout {

    public PetView(PetService petService) {
        add(new H2("Meine Haustiere"));
        add(new Paragraph("Hier werden bald Ihre Haustiere aufgelistet."));
        
        if (petService.getPets().isEmpty()) {
            add(new Paragraph("Aktuell sind keine Haustiere registriert."));
        }
    }
}
