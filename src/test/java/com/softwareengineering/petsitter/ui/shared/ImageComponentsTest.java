package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.dto.OfferCoverTileDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ImageComponentsTest {

    @Test
    void rendersFallbackForEmptyCover() {
        var cover = ImageComponents.offerCover(List.of(), "120px", "#fff");

        assertThat(findByClass(cover, "offer-cover-tile")).hasSize(1);
    }

    @Test
    void rendersUpToFourTilesAndOverflowCount() {
        List<OfferCoverTileDto> tiles = IntStream.range(0, 6)
                .mapToObj(index -> new OfferCoverTileDto(null, "Tier " + index, OfferAnimalType.DOG))
                .toList();

        var cover = ImageComponents.offerCover(tiles, "120px", "#fff");

        assertThat(findByClass(cover, "offer-cover-tile")).hasSize(4);
        assertThat(containsText(cover, "+3")).isTrue();
    }

    @Test
    void avatarUsesImmutableMediaUrlWhenImageExists() {
        UUID assetId = UUID.randomUUID();

        var avatar = ImageComponents.avatar(new ImageRefDto(assetId), 64, "#fff");

        assertThat(avatar.getChildren().findFirst().orElseThrow().getElement().getAttribute("src"))
                .isEqualTo("/media/images/" + assetId + "/avatar");
    }

    private List<Component> findByClass(Component root, String className) {
        return java.util.stream.Stream.concat(
                root.getClassNames().contains(className) ? java.util.stream.Stream.of(root) : java.util.stream.Stream.empty(),
                root.getChildren().flatMap(child -> findByClass(child, className).stream()))
                .toList();
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && hasText.getText().contains(text)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }
}
