package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Petsitter")
@PermitAll
public class UserView extends VerticalLayout {

    public UserView(UserService userService) {
        add(new H2("Benutzerprofil"));
        userService.getCurrentUserProfile()
                .ifPresentOrElse(
                        profile -> {
                            add(new Paragraph("Name: " + profile.firstName() + " " + profile.lastName()));
                            add(new Paragraph("E-Mail: " + profile.email()));
                            add(new Paragraph("Adresse: " + formatAddress(profile)));
                            add(new Paragraph("Rolle: " + profile.accountRole()));
                        },
                        () -> add(new Paragraph("Kein angemeldeter Benutzer gefunden."))
                );
    }

    private String formatAddress(UserProfileDto profile) {
        String address = profile.street() + " " + profile.houseNumber()
                + ", " + profile.postalCode() + " " + profile.city();
        if (profile.addressAddition() == null || profile.addressAddition().isBlank()) {
            return address;
        }
        return address + ", " + profile.addressAddition();
    }
}
