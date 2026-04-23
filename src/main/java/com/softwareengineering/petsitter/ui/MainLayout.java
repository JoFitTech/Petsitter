package com.softwareengineering.petsitter.ui;

import com.softwareengineering.petsitter.ui.views.StartView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        H3 title = new H3("Petsitter / Pawsitters");
        title.getStyle().set("margin", "0");

        Anchor logout = new Anchor("logout", "Logout");
        logout.getElement().setAttribute("router-ignore", true);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, logout);
        header.setWidthFull();
        header.expand(title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)");

        Tabs navigationTabs = new Tabs(new Tab(new RouterLink("Start", StartView.class)));
        navigationTabs.setWidthFull();

        addToNavbar(header, navigationTabs);
    }
}
