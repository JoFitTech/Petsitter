package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.ui.user.UserView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MainLayout extends AppLayout {

    private static final String DARK      = "#4a3428";
    private static final String BROWN     = "#7b5236";
    private static final String LIGHT_BG  = "#fbf8f1";
    private static final String CARD_SHADOW = "0 12px 30px rgba(74, 52, 40, 0.10)";

    public MainLayout() {
        // ── Global page background & font ─────────────────────────────────
        getElement().getStyle()
                .set("background", LIGHT_BG)
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK);

        // ── Navbar (Header) ───────────────────────────────────────────────
        addToNavbar(true, buildHeader());
    }

    // ── Override showRouterLayoutContent to append the footer ─────────────
    @Override
    public void showRouterLayoutContent(HasElement content) {
        // Wrap page content + footer in a flex column that is at least full-height
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidthFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.getStyle()
                .set("min-height", "100vh")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("background", LIGHT_BG);

        // The actual routed view
        Component contentComponent = (Component) content;
        wrapper.add(contentComponent);
        wrapper.setFlexGrow(1, contentComponent);

        // Global footer below every page
        wrapper.add(buildFooter());

        setContent(wrapper);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        header.getStyle()
                .set("background", "rgba(255,255,255,0.92)")
                .set("backdrop-filter", "blur(8px)")
                .set("padding", "0 28px")
                .set("height", "72px")
                .set("box-shadow", "0 2px 18px rgba(74, 52, 40, 0.07)")
                .set("box-sizing", "border-box");

        // Left: Logo (PNG image – rahmenlos via Div)
        Image logoImg = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        logoImg.getStyle()
                .set("height", "52px")
                .set("width", "auto")
                .set("display", "block");

        Div logoWrapper = new Div(logoImg);
        logoWrapper.getStyle()
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center");
        logoWrapper.addClickListener(e -> UI.getCurrent().navigate(""));

        // Center: Navigation Pills
        HorizontalLayout nav = new HorizontalLayout();
        nav.setSpacing(true);
        nav.setAlignItems(FlexComponent.Alignment.CENTER);

        Button findSitterBtn = pillButton("Tiersitter finden", "#f6e3bd", DARK);
        findSitterBtn.addClickListener(e -> UI.getCurrent().navigate("tierhalter-finden"));

        Button findOwnerBtn = pillButton("Tierhalter finden", "#fff6e6", DARK);
        findOwnerBtn.getStyle().set("border", "1px solid #ead5ae");
        findOwnerBtn.addClickListener(e -> UI.getCurrent().navigate(""));

        nav.add(findSitterBtn, findOwnerBtn);

        // Right: Icon buttons (Nachrichten, Favoriten, Profil)
        HorizontalLayout rightIcons = new HorizontalLayout();
        rightIcons.setSpacing(false);
        rightIcons.setAlignItems(FlexComponent.Alignment.CENTER);
        rightIcons.getStyle().set("gap", "8px");

        Button mailBtn = headerIconButton(VaadinIcon.ENVELOPE_O, "transparent", DARK);
        mailBtn.addClickListener(e -> UI.getCurrent().navigate("chat"));

        Button heartBtn = headerIconButton(VaadinIcon.HEART_O, "transparent", DARK);
        heartBtn.addClickListener(e -> {
            // TODO: UI.getCurrent().navigate("favorites");
            System.out.println("TODO: Favoriten-Seite");
        });

        Button profileBtn = headerIconButton(VaadinIcon.USER, "#8db3c3", "white");
        profileBtn.addClickListener(e -> UI.getCurrent().navigate("profile"));

        rightIcons.add(mailBtn, heartBtn, profileBtn);
        header.add(logoWrapper, nav, rightIcons);
        return header;
    }


    // ── Footer (global, shown on every page) ─────────────────────────────
    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        footer.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("padding", "34px 76px")
                .set("box-sizing", "border-box")
                .set("margin-top", "auto");

        // Brand column
        VerticalLayout brand = new VerticalLayout();
        brand.setPadding(false);
        brand.setSpacing(false);

        Image footerLogo = new Image("images/Pawsitter_logo_transparent.png", "Pawsitter Logo");
        footerLogo.getStyle()
                .set("height", "44px")
                .set("width", "auto")
                .set("display", "block")
                .set("margin-bottom", "8px");

        Paragraph claim = new Paragraph("Freundliche Plattform für zuverlässige Tierbetreuung in deiner Nähe.");
        claim.getStyle()
                .set("margin", "0 0 18px 0")
                .set("font-size", "14px")
                .set("color", "#e8d8c6");

        Span copyright = new Span("© 2026 Pawsitter");
        copyright.getStyle()
                .set("font-size", "12px")
                .set("color", "#e8d8c6");

        brand.add(footerLogo, claim, copyright);

        // Links
        HorizontalLayout links = new HorizontalLayout();
        links.setSpacing(true);
        links.getStyle().set("gap", "28px");

        links.add(
                footerLink("Über uns",    "ueber-uns"),
                footerLink("Kontakt",     "kontakt"),
                footerLink("Datenschutz", "datenschutz"),
                footerLink("Impressum",   "impressum"),
                footerLink("Hilfe",       "hilfe")
        );

        // Social buttons
        HorizontalLayout socials = new HorizontalLayout();
        socials.setSpacing(true);

        Button facebookBtn = socialButton("f");
        facebookBtn.addClickListener(e -> {
            // TODO: UI.getCurrent().getPage().open("https://facebook.com/pawsitter");
        });

        Button instagramBtn = socialButton("◎");
        instagramBtn.addClickListener(e -> {
            // TODO: UI.getCurrent().getPage().open("https://instagram.com/pawsitter");
        });

        socials.add(facebookBtn, instagramBtn);

        footer.add(brand, links, socials);
        return footer;
    }

    // ── Shared helpers ────────────────────────────────────────────────────
    private Button headerIconButton(VaadinIcon icon, String background, String color) {
        Button btn = new Button(new Icon(icon));
        btn.getStyle()
                .set("width", "42px")
                .set("height", "42px")
                .set("border-radius", "50%")
                .set("background", background)
                .set("color", color)
                .set("box-shadow", "none")
                .set("cursor", "pointer")
                .set("flex-shrink", "0");
        return btn;
    }

    private Button pillButton(String text, String background, String color) {
        Button button = new Button(text);
        button.getStyle()
                .set("height", "44px")
                .set("padding", "0 36px")
                .set("border-radius", "28px")
                .set("background", background)
                .set("color", color)
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        return button;
    }

    private Component footerLink(String text, String route) {
        Button btn = new Button(text);
        btn.getStyle()
                .set("background", "transparent")
                .set("color", "white")
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("cursor", "pointer");
        btn.addClickListener(e -> {
            // TODO: UI.getCurrent().navigate(route);
            System.out.println("Footer-Link: " + route);
        });
        return btn;
    }

    private Button socialButton(String value) {
        Button btn = new Button(value);
        btn.getStyle()
                .set("width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("background", "#87b2c3")
                .set("color", "white")
                .set("font-weight", "800")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        return btn;
    }
}
