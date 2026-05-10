package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ProfileMenu extends MenuBar {

    private final String actionLabel;
    private final String actionTarget;

    public ProfileMenu() {
        this(hasAuthenticatedSession());
    }

    ProfileMenu(boolean authenticated) {
        this.actionLabel = authenticated ? "Logout" : "Login";
        this.actionTarget = authenticated ? "/logout" : "login";

        addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("min-width", "48px")
                .set("border-radius", "50%")
                .set("background", "#8db3c3")
                .set("color", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "none")
                .set("cursor", "pointer");

        Icon icon = new Icon(VaadinIcon.USER);
        icon.setSize("20px");

        MenuItem profileItem = addItem(icon);
        profileItem.getElement().setAttribute("aria-label", "Profilmenue");
        profileItem.getElement().setAttribute("title", "Profilmenue");
        profileItem.getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("min-width", "48px")
                .set("border-radius", "50%");

        profileItem.getSubMenu().addItem(actionLabel, event -> openAction());
    }

    static boolean hasAuthenticatedSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    String getActionLabel() {
        return actionLabel;
    }

    String getActionTarget() {
        return actionTarget;
    }

    private void openAction() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        if ("/logout".equals(actionTarget)) {
            ui.getPage().setLocation(actionTarget);
            return;
        }
        ui.navigate(actionTarget);
    }
}
