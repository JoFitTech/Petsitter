package com.softwareengineering.petsitter.ui;

import com.softwareengineering.petsitter.ui.views.BookingsView;
import com.softwareengineering.petsitter.ui.views.DashboardView;
import com.softwareengineering.petsitter.ui.views.MessagesView;
import com.softwareengineering.petsitter.ui.views.PetsView;
import com.softwareengineering.petsitter.ui.views.ProfileView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        H3 title = new H3("Petsitter");
        title.getStyle().set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)");

        Tabs navigationTabs = new Tabs(
                createTab("Dashboard", DashboardView.class),
                createTab("Buchungen", BookingsView.class),
                createTab("Haustiere", PetsView.class),
                createTab("Nachrichten", MessagesView.class),
                createTab("Profil", ProfileView.class)
        );
        navigationTabs.setWidthFull();

        addToNavbar(header, navigationTabs);
    }

    private Tab createTab(String text, Class<? extends Component> navigationTarget) {
        RouterLink link = new RouterLink(text, navigationTarget);
        return new Tab(link);
    }
}


