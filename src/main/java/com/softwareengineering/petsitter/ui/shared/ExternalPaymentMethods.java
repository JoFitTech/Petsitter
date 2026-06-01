package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ExternalPaymentMethods extends VerticalLayout {

    private static final String DARK = "#4a3428";
    private final Button applePayButton;

    public ExternalPaymentMethods() {
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "10px");

        Span title = new Span("Weitere Zahlungsmethoden");
        title.getStyle().set("font-size", "15px").set("font-weight", "800").set("color", DARK);

        Paragraph hint = new Paragraph(
                "Diese Optionen sind während der Entwicklungsphase nur als Vorschau sichtbar.");
        hint.getStyle().set("margin", "0").set("font-size", "13px").set("color", "#8a7060");

        Button creditCard = placeholderButton("Kreditkarte");
        Button paypal = placeholderButton("PayPal");
        applePayButton = placeholderButton("Apple Pay");
        applePayButton.setVisible(false);

        add(title, hint, creditCard, paypal, applePayButton);
        addAttachListener(event -> detectAppleDevice());
    }

    private Button placeholderButton(String label) {
        Button button = new Button(label);
        button.setWidthFull();
        button.getStyle()
                .set("height", "42px")
                .set("border-radius", "22px")
                .set("background", "#fffdf8")
                .set("border", "1px solid #ead5ae")
                .set("color", DARK)
                .set("box-shadow", "none")
                .set("font-weight", "700")
                .set("cursor", "pointer");
        button.addClickListener(event -> Notification.show(
                "Während der Entwicklungsphase noch nicht implementiert."));
        return button;
    }

    private void detectAppleDevice() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        ui.getPage().executeJs("""
                return /Mac|iPhone|iPad|iPod/.test(navigator.platform)
                    || /iPhone|iPad|iPod/.test(navigator.userAgent);
                """).then(Boolean.class, applePayButton::setVisible);
    }
}
