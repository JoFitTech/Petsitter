package com.softwareengineering.petsitter.ui.offer;

import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "offers", layout = MainLayout.class)
@PageTitle("Angebote | Petsitter")
@PermitAll
public class OfferView extends VerticalLayout {

    public OfferView(OfferService offerService) {
        add(new H2("Aktuelle Angebote"));
        add(new Paragraph("Hier finden Sie bald alle verfügbaren Sitter-Angebote."));
        
        if (offerService.getOffers().isEmpty()) {
            add(new Paragraph("Aktuell sind keine Angebote vorhanden."));
        }
    }
}
