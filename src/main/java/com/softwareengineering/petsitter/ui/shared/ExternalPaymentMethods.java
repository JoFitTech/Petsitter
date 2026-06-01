package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ExternalPaymentMethods extends VerticalLayout {

    private static final String DARK = "#4a3428";
    private final NativeButton applePayButton;

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
        NativeButton paypal = paypalButton();
        applePayButton = applePayButton();
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

    private NativeButton paypalButton() {
        NativeButton button = new NativeButton();
        button.addClassName("paypal-payment-button");
        button.getElement().setAttribute("aria-label", "Mit PayPal bezahlen");
        button.getStyle()
                .set("width", "100%")
                .set("height", "42px")
                .set("border-radius", "4px")
                .set("background", "#ffc439")
                .set("border", "1px solid #f4b631")
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-family", "Arial, sans-serif")
                .set("font-size", "19px")
                .set("line-height", "1");

        Span pay = paypalWordmarkPart("Pay", "#003087");
        Span pal = paypalWordmarkPart("Pal", "#009cde");
        button.add(pay, pal);
        addPlaceholderNotification(button);
        return button;
    }

    private Span paypalWordmarkPart(String text, String color) {
        Span part = new Span(text);
        part.getStyle()
                .set("color", color)
                .set("font-style", "italic")
                .set("font-weight", "800")
                .set("letter-spacing", "-1.2px");
        return part;
    }

    private NativeButton applePayButton() {
        NativeButton button = new NativeButton("");
        button.addClassName("apple-pay-button");
        button.getElement().setAttribute("lang", "de-DE");
        button.getElement().setAttribute("aria-label", "Mit Apple Pay bezahlen");
        button.getStyle()
                .set("width", "100%")
                .set("height", "42px")
                .set("min-height", "30px")
                .set("border-radius", "5px")
                .set("background", "#000")
                .set("border", "1px solid #000")
                .set("color", "#fff")
                .set("cursor", "pointer")
                .set("-webkit-appearance", "-apple-pay-button")
                .set("-apple-pay-button-type", "plain")
                .set("-apple-pay-button-style", "black");
        addPlaceholderNotification(button);
        return button;
    }

    private void addPlaceholderNotification(NativeButton button) {
        button.addClickListener(event -> showPlaceholderNotification());
    }

    private void showPlaceholderNotification() {
        Notification.show("Während der Entwicklungsphase noch nicht implementiert.");
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
