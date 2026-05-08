package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.ui.shared.MainLayout;
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
        add(new Paragraph("Angemeldeter Benutzer: " + userService.getCurrentUser()));
    }
}
