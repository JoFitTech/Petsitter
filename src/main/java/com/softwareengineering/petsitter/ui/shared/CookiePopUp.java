package com.softwareengineering.petsitter.ui.shared;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CookiePopUp extends Dialog {

    private static final String DARK = "#4a3428";
    private static final String BROWN_BTN = "#5c3d1e";
    private static final String CREAM = "#e8d9c8";
    private static final String LIGHT_BG = "#f3eada";
    private static final String TOGGLE_BG = "#FCF9F2";
    
    public CookiePopUp() {
        this.setWidth("720px");
        this.setMaxWidth("95vw");
        
        this.getElement().getThemeList().add("no-padding");

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(false);
        mainContainer.getStyle()
                .set("background-color", LIGHT_BG)
                .set("padding", "32px 48px")
                .set("border-radius", "16px")
                .set("font-family", "'Inter', sans-serif");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.getStyle()
                .set("margin-bottom", "16px")
                .set("position", "relative");
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        Image logoImg = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        logoImg.setHeight("40px");
        H2 logoText = new H2("– Cookie-Einstellungen");
        logoText.getStyle()
                .set("color", DARK)
                .set("font-size", "26px")
                .set("font-weight", "800")
                .set("margin", "0 0 0 8px");
        titleLayout.add(logoImg, logoText);

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.getStyle()
                .set("color", DARK)
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("position", "absolute")
                .set("right", "0")
                .set("top", "50%")
                .set("transform", "translateY(-50%)");
        closeButton.addClickListener(e -> {
            System.out.println("CookiePopUp: Close button clicked");
            this.close();
        });

        header.add(titleLayout, closeButton);

        // Subtitle
        H2 subtitle = new H2("Deine Privatsphäre ist uns wichtig!");
        subtitle.getStyle()
                .set("color", DARK)
                .set("font-size", "34px")
                .set("font-weight", "800")
                .set("margin", "12px 0 16px 0")
                .set("text-align", "center")
                .set("width", "100%");

        // Description text
        Paragraph description = new Paragraph(
            "Wir nutzen Cookies und ähnliche Technologien, um deine Erfahrung auf Pawsitter zu verbessern, " +
            "Inhalte zu personalisieren und Werbung zu optimieren. Mit deiner Zustimmung hilfst du uns, die Plattform " +
            "für alle noch besser zu machen. Detaillierte Informationen findest du in unserer Datenschutzerklärung."
        );
        description.getStyle()
                .set("color", "#7A6050")
                .set("font-size", "15px")
                .set("text-align", "center")
                .set("line-height", "1.6")
                .set("margin", "0 0 32px 0")
                .set("font-weight", "500");

        // Toggles Layout
        VerticalLayout togglesLayout = new VerticalLayout();
        togglesLayout.setPadding(false);
        togglesLayout.setSpacing(true);
        togglesLayout.setWidthFull();
        togglesLayout.getStyle().set("gap", "12px");

        togglesLayout.add(createToggleItem("Notwendige Cookies", "Diese Cookies sind für die Grundfunktion der Website erforderlich.", true, true));
        togglesLayout.add(createToggleItem("Funktionale Cookies", "Verbessern die Performance und Funktionalität (z.B. Spracheinstellungen).", true, false));
        togglesLayout.add(createToggleItem("Analytische Cookies", "Helfen uns zu verstehen, wie Nutzer mit der Website interagieren.", true, false));
        togglesLayout.add(createToggleItem("Marketing Cookies", "Werden verwendet, um personalisierte Werbung anzuzeigen.", true, false));

        // Footer Buttons
        HorizontalLayout footerButtons = new HorizontalLayout();
        footerButtons.setWidthFull();
        footerButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footerButtons.getStyle().set("margin-top", "36px").set("gap", "20px");

        Button btnOnlyNecessary = new Button("Nur notwendige akzeptieren");
        btnOnlyNecessary.getStyle()
                .set("background-color", CREAM)
                .set("color", BROWN_BTN)
                .set("border", "1px solid " + BROWN_BTN)
                .set("border-radius", "24px")
                .set("padding", "0 24px")
                .set("height", "48px")
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("cursor", "pointer");
        btnOnlyNecessary.addClickListener(e -> {
            System.out.println("CookiePopUp: Nur notwendige akzeptieren clicked");
            this.close();
        });

        Button btnAcceptAll = new Button("Alle akzeptieren");
        btnAcceptAll.getStyle()
                .set("background-color", BROWN_BTN)
                .set("color", "white")
                .set("border-radius", "24px")
                .set("padding", "0 28px")
                .set("height", "48px")
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("border", "none");
        btnAcceptAll.addClickListener(e -> {
            System.out.println("CookiePopUp: Alle akzeptieren clicked");
            this.close();
        });

        footerButtons.add(btnOnlyNecessary, btnAcceptAll);

        mainContainer.add(header, subtitle, description, togglesLayout, footerButtons);
        add(mainContainer);
    }

    private Div createToggleItem(String titleText, String descText, boolean activeInitially, boolean isReadOnly) {
        Div item = new Div();
        item.getStyle()
                .set("background-color", TOGGLE_BG)
                .set("border-radius", "12px")
                .set("padding", "16px 24px")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("border", "1px solid #efe4d3")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        H3 title = new H3(titleText);
        title.getStyle()
                .set("margin", "0 0 6px 0")
                .set("color", DARK)
                .set("font-size", "18px")
                .set("font-weight", "800");

        Span desc = new Span(descText);
        desc.getStyle()
                .set("color", "#7A6050")
                .set("font-size", "14px");

        textLayout.add(title, desc);

        // Custom Toggle Switch
        Div toggleSwitch = new Div();
        toggleSwitch.getStyle()
                .set("width", "56px")
                .set("height", "28px")
                .set("border-radius", "14px")
                .set("position", "relative")
                .set("cursor", isReadOnly ? "default" : "pointer")
                .set("flex-shrink", "0")
                .set("transition", "background-color 0.2s");
        
        if (isReadOnly) {
            toggleSwitch.getStyle().set("opacity", "0.6");
        }

        Div lineIndicator = new Div();
        lineIndicator.getStyle()
                .set("width", "2px")
                .set("height", "10px")
                .set("background-color", "white")
                .set("position", "absolute")
                .set("top", "9px")
                .set("left", "12px")
                .set("border-radius", "1px");

        Div knob = new Div();
        knob.getStyle()
                .set("width", "22px")
                .set("height", "22px")
                .set("border-radius", "50%")
                .set("background-color", "white")
                .set("position", "absolute")
                .set("top", "3px")
                .set("transition", "left 0.2s")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        toggleSwitch.add(lineIndicator, knob);

        boolean[] isActive = {activeInitially};

        Runnable updateToggleStyle = () -> {
            if (isActive[0]) {
                knob.getStyle().set("left", "31px");
                toggleSwitch.getStyle().set("background-color", BROWN_BTN);
                lineIndicator.getStyle().set("display", "block");
            } else {
                knob.getStyle().set("left", "3px");
                toggleSwitch.getStyle().set("background-color", "#d4c8bd");
                lineIndicator.getStyle().set("display", "none");
            }
        };
        updateToggleStyle.run();

        if (!isReadOnly) {
            toggleSwitch.addClickListener(e -> {
                isActive[0] = !isActive[0];
                updateToggleStyle.run();
                System.out.println("CookiePopUp: " + titleText + " turned " + (isActive[0] ? "ON" : "OFF"));
            });
        }

        item.add(textLayout, toggleSwitch);
        return item;
    }
}
