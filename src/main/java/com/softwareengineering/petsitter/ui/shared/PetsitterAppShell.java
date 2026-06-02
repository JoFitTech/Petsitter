package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.dependency.CssImport;

/**
 * Vaadin AppShell configuration for global UI features such as Push.
 */
@Push
@CssImport(value = "./styles/custom-notification.css", themeFor = "vaadin-notification-card")
public class PetsitterAppShell implements AppShellConfigurator {
}

