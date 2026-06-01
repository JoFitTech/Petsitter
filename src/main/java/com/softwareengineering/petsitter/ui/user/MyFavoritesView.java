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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
        new PetsitterDetailPopUp(dto, OfferCardComponent.formatDistance(dto.distanceKm()), 4,
                offerService, requestService, chatService, authenticatedUser, userService, bookingService).open();
    }

    private boolean onFavoriteClicked(OfferCardDto dto) {
        openRemoveFavoriteDialog(dto);
        return true;
    }

    private void openRemoveFavoriteDialog(OfferCardDto dto) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("padding", "28px")
                .set("gap", "18px")
                .set("font-family", "Inter, Arial, sans-serif")
                .set("color", DARK)
                .set("width", "360px")
                .set("max-width", "calc(100vw - 48px)");

        H3 title = new H3("Favorit entfernen?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "20px")
                .set("font-weight", "800");

        Paragraph text = new Paragraph("Dieses Offer wird aus deiner Favoritenliste entfernt.");
        text.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "#7b7069");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.getStyle().set("gap", "10px");

        Button cancel = new Button("Abbrechen");
        cancel.getStyle()
                .set("background", "#f3eadf")
                .set("color", DARK)
                .set("border-radius", "22px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
        cancel.addClickListener(event -> dialog.close());

        Button confirm = new Button("Entfernen");
        confirm.getStyle()
                .set("background", DARK)
                .set("color", "white")
                .set("border-radius", "22px")
                .set("box-shadow", "none")
                .set("cursor", "pointer");
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

        actions.add(cancel, confirm);
        content.add(title, text, actions);
        dialog.add(content);
        dialog.open();
    }
}
