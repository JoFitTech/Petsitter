package com.softwareengineering.petsitter.ui.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.review.dto.UserRatingSummary;
import com.softwareengineering.petsitter.review.dto.UserReviewDto;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.dto.UserProfileDto;
import com.softwareengineering.petsitter.user.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class UserViewTest {

    @Test
    void buildReviewsRendersRecentProfileReviews() throws Exception {
        UserReviewDto firstReview = new UserReviewDto(
                UUID.randomUUID(),
                "Ben Betreuung",
                5,
                "Sehr zuverlässig und freundlich.",
                LocalDateTime.of(2026, 2, 1, 12, 0)
        );
        UserReviewDto secondReview = new UserReviewDto(
                UUID.randomUUID(),
                "Clara Katze",
                3,
                "Flexible Übergabe.",
                LocalDateTime.of(2026, 1, 20, 18, 30)
        );
        UserView view = view(profile(new UserRatingSummary(4.2d, 2L), List.of(firstReview, secondReview)));

        Component reviews = invokeBuildReviews(view);

        assertThat(containsText(reviews, "Meine Bewertungen")).isTrue();
        assertThat(containsText(reviews, "★★★★★")).isTrue();
        assertThat(containsText(reviews, "★★★☆☆")).isTrue();
        assertThat(containsText(reviews, "Bewertung von Ben Betreuung")).isTrue();
        assertThat(containsText(reviews, "Sehr zuverlässig und freundlich.")).isTrue();
        assertThat(containsText(reviews, "Bewertung von Clara Katze")).isTrue();
        assertThat(containsText(reviews, "Flexible Übergabe.")).isTrue();
        assertThat(containsText(reviews, "Bruno war bestens betreut")).isFalse();
    }

    @Test
    void buildReviewsRendersEmptyStateWithoutRecentReviews() throws Exception {
        UserView view = view(profile(new UserRatingSummary(0.0d, 0L), List.of()));

        Component reviews = invokeBuildReviews(view);

        assertThat(containsText(reviews, "Meine Bewertungen")).isTrue();
        assertThat(containsText(reviews, "Noch keine Bewertungen vorhanden")).isTrue();
        assertThat(containsText(reviews, "Sehr zuverlässig")).isFalse();
    }

    @Test
    void buildAvatarCardRendersDynamicRatingSummaryAndNeutralState() throws Exception {
        UserView ratedView = view(profile(new UserRatingSummary(4.6d, 7L), List.of()));
        UserView emptyView = view(profile(new UserRatingSummary(0.0d, 0L), List.of()));

        Component ratedAvatar = invokeBuildAvatarCard(ratedView);
        Component emptyAvatar = invokeBuildAvatarCard(emptyView);

        assertThat(containsText(ratedAvatar, "★★★★★ 4,6 (7)")).isTrue();
        assertThat(containsText(emptyAvatar, "☆☆☆☆☆ Noch keine Bewertungen")).isTrue();
    }

    private UserView view(UserProfileDto profile) {
        UserService userService = mock(UserService.class);
        when(userService.getCurrentUserProfile()).thenReturn(Optional.of(profile));
        return new UserView(userService, null, null, null, null, null, null, null, null, null, null);
    }

    private Component invokeBuildReviews(UserView view) throws Exception {
        Method method = UserView.class.getDeclaredMethod("buildReviews");
        method.setAccessible(true);
        return (Component) method.invoke(view);
    }

    private Component invokeBuildAvatarCard(UserView view) throws Exception {
        Method method = UserView.class.getDeclaredMethod("buildAvatarCard", boolean.class, AtomicReference.class);
        method.setAccessible(true);
        return (Component) method.invoke(view, false, null);
    }

    private UserProfileDto profile(UserRatingSummary ratingSummary, List<UserReviewDto> recentReviews) {
        return new UserProfileDto(
                UUID.randomUUID(),
                "anna.mueller@petsitter.local",
                "Anna",
                "Mueller",
                "Anna",
                null,
                LocalDate.of(1995, 5, 2),
                "deutsch",
                "deutsch",
                "Ich betreue gerne Hunde und Katzen.",
                "Rosenweg",
                "14",
                "50667",
                "Koeln",
                null,
                "Deutschland",
                null,
                null,
                "1 Hund",
                AccountRole.SIGNED_IN_USER,
                AccountStatus.VERIFIED,
                null,
                ratingSummary,
                recentReviews
        );
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && hasText.getText() != null && hasText.getText().contains(text)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }
}
