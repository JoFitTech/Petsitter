package com.softwareengineering.petsitter.ui.shared;

import com.softwareengineering.petsitter.image.domain.ImageVariant;
import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.offer.dto.OfferCoverTileDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import java.util.List;

public final class ImageComponents {

    private ImageComponents() {
    }

    public static Div avatar(ImageRefDto image, int size, String backgroundColor) {
        Div avatar = new Div();
        avatar.addClassName("profile-image-avatar");
        avatar.getStyle()
                .set("width", size + "px")
                .set("height", size + "px")
                .set("min-width", size + "px")
                .set("border-radius", "50%")
                .set("background-color", backgroundColor)
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden");

        if (image != null) {
            Image rendered = new Image(url(image, ImageVariant.AVATAR), "Profilbild");
            rendered.getStyle()
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("object-fit", "cover");
            avatar.add(rendered);
            return avatar;
        }

        Div svgWrap = new Div();
        svgWrap.getElement().setProperty("innerHTML",
                "<svg width='" + (size * 0.58) + "' height='" + (size * 0.58)
                        + "' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>"
                        + "<circle cx='12' cy='8' r='4' fill='white'/>"
                        + "<path d='M4 20c0-4 3.6-7 8-7s8 3 8 7' fill='white'/></svg>");
        avatar.add(svgWrap);
        return avatar;
    }

    public static Div offerCover(List<OfferCoverTileDto> tiles, String height, String fallbackColor) {
        Div cover = new Div();
        cover.addClassName("offer-cover-collage");
        cover.getStyle()
                .set("height", height)
                .set("width", "100%")
                .set("display", "grid")
                .set("gap", "2px")
                .set("background", fallbackColor)
                .set("position", "relative")
                .set("overflow", "hidden");

        List<OfferCoverTileDto> safeTiles = tiles == null ? List.of() : tiles;
        List<OfferCoverTileDto> visibleTiles = safeTiles.stream().limit(4).toList();
        configureGrid(cover, visibleTiles.size());

        if (visibleTiles.isEmpty()) {
            cover.add(tile(null, fallbackColor));
            return cover;
        }

        for (int index = 0; index < visibleTiles.size(); index++) {
            Div tile = tile(visibleTiles.get(index), fallbackColor);
            if (visibleTiles.size() == 3 && index == 0) {
                tile.getStyle().set("grid-row", "span 2");
            }
            if (safeTiles.size() > 4 && index == 3) {
                Span overlay = new Span("+" + (safeTiles.size() - 3));
                overlay.addClassName("offer-cover-overflow");
                overlay.getStyle()
                        .set("position", "absolute")
                        .set("inset", "0")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("background", "rgba(74, 52, 40, 0.58)")
                        .set("color", "white")
                        .set("font-size", "26px")
                        .set("font-weight", "800");
                tile.add(overlay);
            }
            cover.add(tile);
        }
        return cover;
    }

    public static String url(ImageRefDto image, ImageVariant variant) {
        return "/media/images/" + image.assetId() + "/" + variant.name().toLowerCase();
    }

    private static Div tile(OfferCoverTileDto tile, String defaultColor) {
        Div container = new Div();
        container.addClassName("offer-cover-tile");
        container.getStyle()
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("min-width", "0")
                .set("min-height", "0")
                .set("background", tile == null ? defaultColor : OfferCardComponent.colorFor(tile.fallbackType()));
        if (tile != null && tile.image() != null) {
            Image image = new Image(url(tile.image(), ImageVariant.DISPLAY), fallbackLabel(tile));
            image.getStyle()
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("object-fit", "cover");
            container.add(image);
        } else if (tile != null && tile.fallbackLabel() != null && !tile.fallbackLabel().isBlank()) {
            Span label = new Span(tile.fallbackLabel());
            label.getStyle()
                    .set("position", "absolute")
                    .set("left", "12px")
                    .set("bottom", "10px")
                    .set("color", "white")
                    .set("font-size", "13px")
                    .set("font-weight", "800")
                    .set("text-shadow", "0 1px 4px rgba(74, 52, 40, 0.55)");
            container.add(label);
        }
        return container;
    }

    private static void configureGrid(Div cover, int tileCount) {
        if (tileCount <= 1) {
            cover.getStyle().set("grid-template-columns", "1fr").set("grid-template-rows", "1fr");
        } else if (tileCount == 2) {
            cover.getStyle().set("grid-template-columns", "repeat(2, 1fr)").set("grid-template-rows", "1fr");
        } else {
            cover.getStyle().set("grid-template-columns", "repeat(2, 1fr)").set("grid-template-rows", "repeat(2, 1fr)");
        }
    }

    private static String fallbackLabel(OfferCoverTileDto tile) {
        return tile.fallbackLabel() == null || tile.fallbackLabel().isBlank() ? "Offer-Bild" : tile.fallbackLabel();
    }
}
