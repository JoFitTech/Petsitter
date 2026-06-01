package com.softwareengineering.petsitter.ui.booking;

import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Buchungen | Petsitter")
@PermitAll
public class BookingView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo("profile", QueryParameters.of("tab", "bookings"));
    }
}
