package com.softwareengineering.petsitter.ui.chat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ProfilePopUp extends Dialog {

    private static final String DARK = "#4a3428";
    private static final String LIGHT_BG = "#f3eada"; // Background of the popup
    private static final String CARD_BG = "#fbf7f0";  // Background of the cards
    private static final String REVIEW_BG = "#f5eee4"; // Background of review items

    public ProfilePopUp() {
        // Style the dialog itself
        this.setWidth("800px");
        this.setMaxWidth("90vw");
        
        // Customizing the dialog overlay background if needed, but we'll focus on the content
        this.getElement().getThemeList().add("no-padding");
        
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.getStyle()
                .set("background-color", LIGHT_BG)
                .set("padding", "30px")
                .set("border-radius", "20px");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Profil von max3010");
        title.getStyle()
                .set("margin", "0")
                .set("color", DARK)
                .set("font-size", "24px")
                .set("font-weight", "800");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.getStyle().set("color", DARK);
        closeButton.addClickListener(e -> {
            System.out.println("Close button clicked in ProfilePopUp");
            this.close();
        });

        header.add(title, closeButton);

        // Card 1: Profile Details
        Div profileCard = new Div();
        profileCard.getStyle()
                .set("background-color", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "30px")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("position", "relative");

        HorizontalLayout profileContent = new HorizontalLayout();
        profileContent.setSpacing(true);
        profileContent.setAlignItems(FlexComponent.Alignment.START);
        profileContent.getStyle().set("gap", "40px");

        // Avatar
        Div avatar = createAvatar(160);
        
        // Info Section
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(false);

        // Stars
        HorizontalLayout starsLayout = new HorizontalLayout();
        starsLayout.setSpacing(false);
        starsLayout.getStyle().set("gap", "4px").set("margin-bottom", "10px");
        for (int i = 0; i < 5; i++) {
            Icon star = new Icon(VaadinIcon.STAR);
            star.setSize("20px");
            star.getStyle().set("color", DARK);
            starsLayout.add(star);
        }

        // Name
        H3 name = new H3("Max Mustermann");
        name.getStyle()
                .set("margin", "0 0 15px 0")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("font-weight", "700");

        // Details (Hunde, Alter, Ort, Sprache)
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);
        detailsLayout.getStyle().set("margin-bottom", "20px").set("gap", "4px");

        Span dogs = new Span("2 Hunde");
        dogs.getStyle().set("font-style", "italic").set("color", DARK).set("font-weight", "600");
        
        Span age = new Span("21 Jahre alt");
        age.getStyle().set("font-style", "italic").set("color", DARK).set("font-weight", "600");

        HorizontalLayout locationLayout = new HorizontalLayout();
        locationLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        locationLayout.setSpacing(false);
        locationLayout.getStyle().set("gap", "6px");
        Icon pinIcon = new Icon(VaadinIcon.MAP_MARKER);
        pinIcon.setSize("16px");
        pinIcon.getStyle().set("color", DARK);
        Span location = new Span("76689 Neuthard");
        location.getStyle().set("font-style", "italic").set("font-weight", "600").set("color", DARK);
        locationLayout.add(pinIcon, location);

        HorizontalLayout langLayout = new HorizontalLayout();
        langLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        langLayout.setSpacing(false);
        langLayout.getStyle().set("gap", "6px");
        Icon globeIcon = new Icon(VaadinIcon.GLOBE);
        globeIcon.setSize("16px");
        globeIcon.getStyle().set("color", DARK);
        Span lang = new Span("deutsch");
        lang.getStyle().set("font-style", "italic").set("font-weight", "600").set("color", DARK);
        langLayout.add(globeIcon, lang);

        detailsLayout.add(dogs, age, locationLayout, langLayout);

        // Description
        Paragraph desc1 = new Paragraph("Hallo, ich bin Max und betreue seit mehreren Jahren Hunde und Katzen.");
        desc1.getStyle().set("margin", "0 0 10px 0").set("color", "#6b5446").set("font-size", "15px");
        
        Paragraph desc2 = new Paragraph("Mir sind Vertrauen, klare Absprachen und ein liebevoller Umgang besonders wichtig.");
        desc2.getStyle().set("margin", "0").set("color", "#6b5446").set("font-size", "15px");

        infoSection.add(starsLayout, name, detailsLayout, desc1, desc2);

        // Verified Badge (absolute positioned top right inside profileCard)
        Div verifiedBadge = new Div();
        verifiedBadge.getStyle()
                .set("position", "absolute")
                .set("top", "30px")
                .set("right", "30px")
                .set("background-color", "#f0faf5") // Very light green
                .set("color", "#2e7d32") // Green text
                .set("padding", "6px 12px")
                .set("border-radius", "20px")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "4px");
        
        Icon checkIcon = new Icon(VaadinIcon.CHECK);
        checkIcon.setSize("14px");
        checkIcon.getStyle().set("color", "#2e7d32");
        verifiedBadge.add(checkIcon, new Span("Verifiziert"));

        profileContent.add(avatar, infoSection);
        profileCard.add(profileContent, verifiedBadge);

        // Card 2: Reviews
        Div reviewsCard = new Div();
        reviewsCard.getStyle()
                .set("background-color", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "30px")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("margin-top", "20px");

        H2 reviewsTitle = new H2("Max's Bewertungen");
        reviewsTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("font-weight", "800");

        VerticalLayout reviewsList = new VerticalLayout();
        reviewsList.setPadding(false);
        reviewsList.setSpacing(true);

        // Review 1
        reviewsList.add(createReviewItem(
                5, 
                "Sehr zuverlässig", 
                "„Bruno war bestens betreut. Kommunikation und Übergabe waren super unkompliziert.“"
        ));

        // Review 2
        reviewsList.add(createReviewItem(
                4, 
                "Freundlich und flexibel", 
                "„Sehr angenehmer Kontakt, unsere Katze Mia hat sich schnell wohlgefühlt.“"
        ));

        reviewsCard.add(reviewsTitle, reviewsList);

        mainContainer.add(header, profileCard, reviewsCard);
        add(mainContainer);
    }

    private Div createAvatar(int size) {
        Div avatar = new Div();
        avatar.getStyle()
                .set("width", size + "px")
                .set("height", size + "px")
                .set("min-width", size + "px")
                .set("border-radius", "50%")
                .set("background-color", "#e0c4a4") // Light brown matching the design
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden");

        Div svgWrap = new Div();
        svgWrap.getElement().setProperty("innerHTML",
                "<svg width='" + (size * 0.7) + "' height='" + (size * 0.7) + "' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
                        "<circle cx='12' cy='8' r='4' fill='white'/>" +
                        "<path d='M4 20c0-4 3.6-7 8-7s8 3 8 7' fill='white'/></svg>");
        avatar.add(svgWrap);
        return avatar;
    }

    private Div createReviewItem(int starsCount, String titleText, String reviewText) {
        Div reviewItem = new Div();
        reviewItem.getStyle()
                .set("background-color", REVIEW_BG)
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("border", "1px solid #efe4d3");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.getStyle().set("gap", "10px").set("margin-bottom", "10px");

        HorizontalLayout starsLayout = new HorizontalLayout();
        starsLayout.setSpacing(false);
        starsLayout.getStyle().set("gap", "2px");
        
        for (int i = 0; i < 5; i++) {
            Icon star = new Icon(i < starsCount ? VaadinIcon.STAR : VaadinIcon.STAR_O);
            star.setSize("14px");
            star.getStyle().set("color", DARK);
            starsLayout.add(star);
        }

        Span title = new Span(titleText);
        title.getStyle()
                .set("color", DARK)
                .set("font-weight", "700")
                .set("font-size", "15px");

        header.add(starsLayout, title);

        Paragraph text = new Paragraph(reviewText);
        text.getStyle()
                .set("margin", "0")
                .set("color", "#6b5446")
                .set("font-size", "14px");

        reviewItem.add(header, text);
        return reviewItem;
    }
}
