package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.ui.shared.ImageComponents;
import com.softwareengineering.petsitter.ui.shared.RatingComponents;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.dto.PublicUserProfileDto;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.dependency.CssImport;
import java.time.LocalDate;
import java.time.Period;

@CssImport(value = "./styles/custom-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
public class ProfilePopUp extends Dialog {

    private static final String DARK = "#4a3428";
    private static final String LIGHT_BG = "#f3eada"; // Background of the popup
    private static final String CARD_BG = "#fbf7f0";  // Background of the cards
    private static final String REVIEW_BG = "#f5eee4"; // Background of review items

    public ProfilePopUp() {
        this(new PublicUserProfileDto(
                null,
                "Max Mustermann",
                LocalDate.now().minusYears(21),
                "deutsch",
                "Hallo, ich bin Max und betreue seit mehreren Jahren Hunde und Katzen.",
                "76689",
                "Neuthard",
                "2 Hunde",
                AccountStatus.VERIFIED
        ));
    }

    public ProfilePopUp(PublicUserProfileDto profile) {
        PublicUserProfileDto safeProfile = profile != null ? profile : emptyProfile();
        String displayName = valueOrDefault(safeProfile.displayName(), "Unbekannter Nutzer");

        // Style the dialog itself
        this.setWidth("800px");
        this.setMaxWidth("90vw");
        
        // Customizing the dialog overlay background if needed, but we'll focus on the content
        this.getElement().getThemeList().add("no-padding");
        
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.getStyle()
                .set("font-family", "Inter, Arial, sans-serif")
                .set("background-color", LIGHT_BG)
                .set("padding", "30px")
                .set("border-radius", "20px");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Profil von " + displayName);
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
        Div avatar = ImageComponents.avatar(safeProfile.profileImage(), 160, "#e0c4a4");
        
        // Info Section
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(false);

        Component rating = RatingComponents.compactRating(safeProfile.ratingSummary());
        rating.getElement().getStyle().set("margin-bottom", "10px");

        // Name
        H3 name = new H3(displayName);
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

        HorizontalLayout dogsLayout = new HorizontalLayout();
        dogsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dogsLayout.setSpacing(false);
        dogsLayout.getStyle().set("gap", "6px");
        Icon pawIcon = new Icon(VaadinIcon.HEART);
        pawIcon.setSize("16px");
        pawIcon.getStyle().set("color", DARK);
        Span dogs = new Span(valueOrDefault(safeProfile.petSummary(), "Keine Haustiere"));
        dogs.getStyle().set("font-style", "italic").set("color", DARK).set("font-weight", "600");
        dogsLayout.add(pawIcon, dogs);
        
        HorizontalLayout ageLayout = new HorizontalLayout();
        ageLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        ageLayout.setSpacing(false);
        ageLayout.getStyle().set("gap", "6px");
        Icon userIcon = new Icon(VaadinIcon.USER);
        userIcon.setSize("16px");
        userIcon.getStyle().set("color", DARK);
        Span age = new Span(formatAge(safeProfile.birthDate()));
        age.getStyle().set("font-style", "italic").set("color", DARK).set("font-weight", "600");
        ageLayout.add(userIcon, age);

        HorizontalLayout locationLayout = new HorizontalLayout();
        locationLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        locationLayout.setSpacing(false);
        locationLayout.getStyle().set("gap", "6px");
        Icon pinIcon = new Icon(VaadinIcon.MAP_MARKER);
        pinIcon.setSize("16px");
        pinIcon.getStyle().set("color", DARK);
        Span location = new Span(formatLocation(safeProfile.postalCode(), safeProfile.city()));
        location.getStyle().set("font-style", "italic").set("font-weight", "600").set("color", DARK);
        locationLayout.add(pinIcon, location);

        HorizontalLayout langLayout = new HorizontalLayout();
        langLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        langLayout.setSpacing(false);
        langLayout.getStyle().set("gap", "6px");
        Icon globeIcon = new Icon(VaadinIcon.GLOBE);
        globeIcon.setSize("16px");
        globeIcon.getStyle().set("color", DARK);
        Span lang = new Span(valueOrDefault(safeProfile.language(), "deutsch"));
        lang.getStyle().set("font-style", "italic").set("font-weight", "600").set("color", DARK);
        langLayout.add(globeIcon, lang);

        detailsLayout.add(dogsLayout, ageLayout, locationLayout, langLayout);

        // Description
        Paragraph desc1 = new Paragraph(valueOrDefault(safeProfile.bio(), "Noch keine Beschreibung hinterlegt."));
        desc1.getStyle().set("margin", "0 0 10px 0").set("color", "#6b5446").set("font-size", "15px");

        infoSection.add(rating, name, detailsLayout, desc1);

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
        profileCard.add(profileContent);
        if (safeProfile.accountStatus() == AccountStatus.VERIFIED) {
            profileCard.add(verifiedBadge);
        }

        // Card 2: Reviews
        Div reviewsCard = new Div();
        reviewsCard.getStyle()
                .set("background-color", CARD_BG)
                .set("border-radius", "20px")
                .set("padding", "30px")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("margin-top", "20px");

        H2 reviewsTitle = new H2("Bewertungen von " + displayName);
        reviewsTitle.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("font-weight", "800");

        VerticalLayout reviewsList = new VerticalLayout();
        reviewsList.setPadding(false);
        reviewsList.setSpacing(true);

        // Add real reviews from profile
        if (safeProfile.recentReviews() != null && !safeProfile.recentReviews().isEmpty()) {
            for (var review : safeProfile.recentReviews()) {
                reviewsList.add(createReviewItem(
                        review.rating(),
                        review.reviewerName(),
                        valueOrDefault(review.comment(), "Keine Bemerkung")
                ));
            }
        } else {
            Paragraph noReviews = new Paragraph("Keine Bewertungen vorhanden");
            noReviews.getStyle().set("color", "#999").set("font-style", "italic");
            reviewsList.add(noReviews);
        }

        reviewsCard.add(reviewsTitle, reviewsList);

        mainContainer.add(header, profileCard, reviewsCard);
        add(mainContainer);
    }

    private PublicUserProfileDto emptyProfile() {
        return new PublicUserProfileDto(null, null, null, null, null, null, null, null, null);
    }

    private String formatAge(LocalDate birthDate) {
        if (birthDate == null) {
            return "Alter nicht angegeben";
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 0) {
            return "Alter nicht angegeben";
        }
        return age + " Jahre alt";
    }

    private String formatLocation(String postalCode, String city) {
        String location = (valueOrEmpty(postalCode) + " " + valueOrEmpty(city)).trim();
        return valueOrDefault(location, "Ort nicht angegeben");
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private Div createReviewItem(int starsCount, String reviewerName, String reviewText) {
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
            star.getStyle().set("color", i < starsCount ? "#ffdf4a" : DARK);
            starsLayout.add(star);
        }

        Span title = new Span(valueOrDefault(reviewerName, "Anonym"));
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
