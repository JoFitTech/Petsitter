package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.review.dto.UserReviewDto;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.dto.PublicUserProfileDto;
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
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class ProfilePopUp extends Dialog {

    private static final String DARK = "#4a3428";
    private static final String LIGHT_BG = "#f3eada";
    private static final String CARD_BG = "#fbf7f0";
    private static final String REVIEW_BG = "#f5eee4";

    public ProfilePopUp() {
        this(new PublicUserProfileDto(
                null, "Max Mustermann",
                LocalDate.now().minusYears(21), "deutsch",
                "Hallo, ich bin Max und betreue seit mehreren Jahren Hunde und Katzen.",
                "76689", "Neuthard", "2 Hunde", AccountStatus.VERIFIED),
                null, List.of());
    }

    public ProfilePopUp(PublicUserProfileDto profile) {
        this(profile, null, List.of());
    }

    public ProfilePopUp(PublicUserProfileDto profile, UserRatingSummary ratingSummary,
                        List<UserReviewDto> recentReviews) {
        PublicUserProfileDto safeProfile = profile != null ? profile : emptyProfile();
        String displayName = valueOrDefault(safeProfile.displayName(), "Unbekannter Nutzer");

        this.setWidth("800px");
        this.setMaxWidth("90vw");
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
        title.getStyle().set("margin", "0").set("color", DARK)
                .set("font-size", "24px").set("font-weight", "800");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.getStyle().set("color", DARK);
        closeButton.addClickListener(e -> this.close());
        header.add(title, closeButton);

        // Profil-Card
        Div profileCard = new Div();
        profileCard.getStyle()
                .set("background-color", CARD_BG).set("border-radius", "20px")
                .set("padding", "30px").set("width", "100%")
                .set("box-sizing", "border-box").set("position", "relative");

        HorizontalLayout profileContent = new HorizontalLayout();
        profileContent.setSpacing(true);
        profileContent.setAlignItems(FlexComponent.Alignment.START);
        profileContent.getStyle().set("gap", "40px");

        Div avatar = createAvatar(160);

        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(false);

        // Sterne: echt oder leer
        HorizontalLayout starsLayout = new HorizontalLayout();
        starsLayout.setSpacing(false);
        starsLayout.getStyle().set("gap", "4px").set("margin-bottom", "10px");

        int filledStars = (ratingSummary != null && ratingSummary.ratingCount() > 0)
                ? (int) Math.round(ratingSummary.averageRating()) : 0;
        for (int i = 0; i < 5; i++) {
            Icon star = new Icon(i < filledStars ? VaadinIcon.STAR : VaadinIcon.STAR_O);
            star.setSize("20px");
            star.getStyle().set("color", i < filledStars ? "#ffdf4a" : "#c0b090");
            starsLayout.add(star);
        }
        if (ratingSummary != null && ratingSummary.ratingCount() > 0) {
            Span ratingText = new Span("  " + ratingSummary.averageRating() + " (" + ratingSummary.ratingCount() + ")");
            ratingText.getStyle().set("font-size", "14px").set("color", "#6b5446").set("margin-left", "6px");
            starsLayout.add(ratingText);
        }

        H3 name = new H3(displayName);
        name.getStyle().set("margin", "0 0 15px 0").set("color", DARK)
                .set("font-size", "22px").set("font-weight", "700");

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);
        detailsLayout.getStyle().set("margin-bottom", "20px").set("gap", "4px");
        detailsLayout.add(
                buildInfoLine(VaadinIcon.HEART, valueOrDefault(safeProfile.petSummary(), "Keine Haustiere")),
                buildInfoLine(VaadinIcon.USER, formatAge(safeProfile.birthDate())),
                buildInfoLine(VaadinIcon.MAP_MARKER, formatLocation(safeProfile.postalCode(), safeProfile.city())),
                buildInfoLine(VaadinIcon.GLOBE, valueOrDefault(safeProfile.language(), "deutsch"))
        );

        Paragraph desc = new Paragraph(valueOrDefault(safeProfile.bio(), "Noch keine Beschreibung hinterlegt."));
        desc.getStyle().set("margin", "0 0 10px 0").set("color", "#6b5446").set("font-size", "15px");

        infoSection.add(starsLayout, name, detailsLayout, desc);

        Div verifiedBadge = new Div();
        verifiedBadge.getStyle()
                .set("position", "absolute").set("top", "30px").set("right", "30px")
                .set("background-color", "#f0faf5").set("color", "#2e7d32")
                .set("padding", "6px 12px").set("border-radius", "20px")
                .set("font-size", "13px").set("font-weight", "600")
                .set("display", "flex").set("align-items", "center").set("gap", "4px");
        Icon checkIcon = new Icon(VaadinIcon.CHECK);
        checkIcon.setSize("14px");
        checkIcon.getStyle().set("color", "#2e7d32");
        verifiedBadge.add(checkIcon, new Span("Verifiziert"));

        profileContent.add(avatar, infoSection);
        profileCard.add(profileContent);
        if (safeProfile.accountStatus() == AccountStatus.VERIFIED) {
            profileCard.add(verifiedBadge);
        }

        // Bewertungs-Card
        Div reviewsCard = new Div();
        reviewsCard.getStyle()
                .set("background-color", CARD_BG).set("border-radius", "20px")
                .set("padding", "30px").set("width", "100%")
                .set("box-sizing", "border-box").set("margin-top", "20px");

        H2 reviewsTitle = new H2("Bewertungen von " + displayName);
        reviewsTitle.getStyle().set("margin", "0 0 20px 0").set("color", DARK)
                .set("font-size", "22px").set("font-weight", "800");

        VerticalLayout reviewsList = new VerticalLayout();
        reviewsList.setPadding(false);
        reviewsList.setSpacing(true);

        if (recentReviews != null && !recentReviews.isEmpty()) {
            for (UserReviewDto r : recentReviews) {
                reviewsList.add(createReviewItem(r.rating(), r.reviewerName(),
                        r.comment() != null ? "\u201E" + r.comment() + "\u201C" : ""));
            }
        } else {
            Span noReviews = new Span("Noch keine Bewertungen vorhanden.");
            noReviews.getStyle().set("font-size", "14px").set("color", "#a08060");
            reviewsList.add(noReviews);
        }

        reviewsCard.add(reviewsTitle, reviewsList);
        mainContainer.add(header, profileCard, reviewsCard);
        add(mainContainer);
    }

    private HorizontalLayout buildInfoLine(VaadinIcon iconType, String text) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "6px");
        Icon icon = new Icon(iconType);
        icon.setSize("16px");
        icon.getStyle().set("color", DARK);
        Span s = new Span(text);
        s.getStyle().set("font-style", "italic").set("color", DARK).set("font-weight", "600");
        layout.add(icon, s);
        return layout;
    }

    private PublicUserProfileDto emptyProfile() {
        return new PublicUserProfileDto(null, null, null, null, null, null, null, null, null);
    }

    private String formatAge(LocalDate birthDate) {
        if (birthDate == null) return "Alter nicht angegeben";
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age < 0 ? "Alter nicht angegeben" : age + " Jahre alt";
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

    private Div createAvatar(int size) {
        Div avatar = new Div();
        avatar.getStyle()
                .set("width", size + "px").set("height", size + "px")
                .set("min-width", size + "px").set("border-radius", "50%")
                .set("background-color", "#e0c4a4")
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("overflow", "hidden");
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
                .set("background-color", REVIEW_BG).set("border-radius", "12px")
                .set("padding", "20px").set("width", "100%")
                .set("box-sizing", "border-box").set("border", "1px solid #efe4d3");

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

        Span title = new Span(titleText);
        title.getStyle().set("color", DARK).set("font-weight", "700").set("font-size", "15px");
        header.add(starsLayout, title);

        reviewItem.add(header);
        if (!reviewText.isBlank()) {
            Paragraph text = new Paragraph(reviewText);
            text.getStyle().set("margin", "0").set("color", "#6b5446").set("font-size", "14px");
            reviewItem.add(text);
        }
        return reviewItem;
    }
}
