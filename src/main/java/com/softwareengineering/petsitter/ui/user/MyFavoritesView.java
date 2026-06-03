package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.favorite.service.FavoriteService;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.ui.shared.OfferCardComponent;
import com.softwareengineering.petsitter.ui.shared.PetsitterDetailPopUp;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;

public class MyFavoritesView extends Div {

    private static final String DARK = "#4a3428";
    private static final String CARD_BG = "#ffffff";

    private final FavoriteService favoriteService;
    private final OfferService offerService;
    private final RequestService requestService;
    private final ChatService chatService;
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final BookingService bookingService;
    private final Div favoritesContainer = new Div();

    public MyFavoritesView(FavoriteService favoriteService, OfferService offerService,
                           RequestService requestService, ChatService chatService,
                           AuthenticatedUser authenticatedUser, UserService userService,
                           BookingService bookingService) {
        this.favoriteService = favoriteService;
        this.offerService = offerService;
        this.requestService = requestService;
        this.chatService = chatService;
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.bookingService = bookingService;

        setWidthFull();
        getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("padding", "36px")
            .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
            .set("box-sizing", "border-box");

        configureFavoritesContainer();
        add(buildHeader(), favoritesContainer);
        renderFavorites();
    }

    private Component buildHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("margin-bottom", "36px");

        H2 title = new H2("Meine Favoriten");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "800")
                .set("color", DARK);

        row.add(title);
        return row;
    }

    private void configureFavoritesContainer() {
        favoritesContainer.setWidthFull();
        favoritesContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "32px");
    }

    private void renderFavorites() {
        favoritesContainer.removeAll();
        List<OfferCardDto> favorites = favoriteService.getCurrentUserFavoriteOffers();
        if (favorites.isEmpty()) {
            favoritesContainer.add(buildEmptyState());
            return;
        }
        List<OfferCardDto> sitterOffers = favorites.stream()
                .filter(dto -> dto.offerType() == OfferType.SITTER_OFFER)
                .toList();
        List<OfferCardDto> ownerOffers = favorites.stream()
                .filter(dto -> dto.offerType() == OfferType.OWNER_OFFER)
                .toList();

        if (!sitterOffers.isEmpty()) {
            favoritesContainer.add(buildOfferSection("Sitter-Angebote", sitterOffers));
        }
        if (!ownerOffers.isEmpty()) {
            favoritesContainer.add(buildOfferSection("Tierhalter-Aufträge", ownerOffers));
        }
    }

    private Component buildOfferSection(String title, List<OfferCardDto> offers) {
        Div section = new Div();
        section.setWidthFull();
        section.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");

        H3 heading = new H3(title);
        heading.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", DARK);

        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
                .set("gap", "24px");

        offers.forEach(dto -> grid.add(new OfferCardComponent(
                dto,
                this::openOfferDialog,
                this::onFavoriteClicked)));

        section.add(heading, grid);
        return section;
    }

    private Component buildEmptyState() {
        Div empty = new Div();
        empty.getStyle()
                .set("background", "#fbf8f1")
                .set("border", "1px solid #ead5ae")
                .set("border-radius", "18px")
                .set("padding", "28px")
                .set("box-sizing", "border-box");

        H3 title = new H3("Noch keine Favoriten");
        title.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("color", DARK);

        Paragraph text = new Paragraph("Gelikte Offers erscheinen hier, solange sie verfügbar sind.");
        text.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#7b7069");

        empty.add(title, text);
        return empty;
    }

    private void openOfferDialog(OfferCardDto dto) {
        new PetsitterDetailPopUp(dto, OfferCardComponent.formatDistance(dto.distanceKm()),
                OfferCardComponent.starsForAverage(dto.creatorAverageRating()),
                offerService, requestService, chatService, authenticatedUser, userService, bookingService).open();
    }

    private boolean onFavoriteClicked(OfferCardDto dto) {
        openRemoveFavoriteDialog(dto);
        return true;
    }

    private void openRemoveFavoriteDialog(OfferCardDto dto) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setMaxWidth("95vw");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.getElement().getThemeList().add("no-padding");
        dialog.getElement().getStyle()
                .set("border-radius", "20px")
                .set("font-family", "Inter, Arial, sans-serif");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("padding", "32px 48px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("background-color", "#f3eada")
                .set("border-radius", "20px")
                .set("box-sizing", "border-box");

        Button closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle()
                .set("position", "absolute")
                .set("top", "24px")
                .set("right", "24px")
                .set("color", DARK)
                .set("font-size", "22px")
                .set("cursor", "pointer")
                .set("background", "transparent")
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("z-index", "10");
        closeBtn.addClickListener(e -> dialog.close());

        H3 title = new H3("Favorit entfernen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "21px")
                .set("font-weight", "800")
                .set("line-height", "1.2")
                .set("padding-right", "32px")
                .set("color", DARK);

        Paragraph text = new Paragraph("Dieses Offer wird aus deiner Favoritenliste entfernt.");
        text.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("line-height", "1.5")
                .set("color", "#9a8070");

        Button confirm = new Button("Entfernen");
        confirm.setWidthFull();
        confirm.getStyle()
                .set("height", "48px")
                .set("border-radius", "24px")
                .set("background", "#5c3d1e")
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "15px")
                .set("box-shadow", "0 2px 8px rgba(74,52,40,0.1)")
                .set("cursor", "pointer")
                .set("border", "none")
                .set("font-family", "Inter, Arial, sans-serif");
        confirm.addClickListener(event -> {
            try {
                favoriteService.removeCurrentUserFavorite(dto.id());
                dialog.close();
                renderFavorites();
                Notification.show("Favorit entfernt.", 2500, Notification.Position.TOP_CENTER);
            } catch (RuntimeException exception) {
                Notification.show(exception.getMessage(), 3500, Notification.Position.TOP_CENTER);
            }
        });

        wrapper.add(closeBtn, title, text, confirm);
        dialog.add(wrapper);
        dialog.open();
    }
}
