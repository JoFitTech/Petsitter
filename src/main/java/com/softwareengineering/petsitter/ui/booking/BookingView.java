package com.softwareengineering.petsitter.ui.booking;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "bookings", layout = MainLayout.class)
@PageTitle("Buchungen | Petsitter")
@PermitAll
public class BookingView extends VerticalLayout {

    public BookingView(BookingService bookingService) {
        add(new H2("Meine Buchungen"));
        add(new Paragraph("Hier sehen Sie bald Ihre getätigten und erhaltenen Buchungen."));
        
        if (bookingService.getBookings().isEmpty()) {
            add(new Paragraph("Aktuell liegen keine Buchungen vor."));
        }
    }
}
