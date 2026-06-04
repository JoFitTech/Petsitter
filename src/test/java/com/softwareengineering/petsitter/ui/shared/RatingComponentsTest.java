package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.icon.Icon;
import java.util.List;
import org.junit.jupiter.api.Test;

class RatingComponentsTest {

    @Test
    void rendersEmptyStateForZeroRatings() {
        Component rating = RatingComponents.compactRating(new UserRatingSummary(0.0d, 0L));

        assertThat(RatingComponents.roundedStarCount(new UserRatingSummary(0.0d, 0L))).isZero();
        assertThat(containsText(rating, RatingComponents.EMPTY_RATING_TEXT)).isTrue();
        assertThat(starIcons(rating))
                .hasSize(5)
                .allSatisfy(icon -> assertThat(icon.getElement().getAttribute("icon")).isEqualTo("vaadin:star-o"));
    }

    @Test
    void roundsAverageRatingWithSharedMathRoundLogic() {
        assertThat(RatingComponents.roundedStarCount(new UserRatingSummary(3.4d, 7L))).isEqualTo(3);
        assertThat(RatingComponents.roundedStarCount(new UserRatingSummary(3.5d, 7L))).isEqualTo(4);
        assertThat(RatingComponents.roundedStarCount(new UserRatingSummary(4.6d, 7L))).isEqualTo(5);
    }

    @Test
    void rendersFilledAndEmptyStarsForRoundedAverage() {
        Component stars = RatingComponents.starsForSummary(new UserRatingSummary(2.6d, 3L), 20);

        List<Icon> icons = starIcons(stars);
        assertThat(icons).hasSize(5);
        assertThat(icons.subList(0, 3))
                .allSatisfy(icon -> assertThat(icon.getElement().getAttribute("icon")).isEqualTo("vaadin:star"));
        assertThat(icons.subList(3, 5))
                .allSatisfy(icon -> assertThat(icon.getElement().getAttribute("icon")).isEqualTo("vaadin:star-o"));
    }

    private List<Icon> starIcons(Component root) {
        return java.util.stream.Stream.concat(
                root instanceof Icon icon ? java.util.stream.Stream.of(icon) : java.util.stream.Stream.empty(),
                root.getChildren().flatMap(child -> starIcons(child).stream()))
                .toList();
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && hasText.getText() != null && hasText.getText().contains(text)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }
}
