package com.softwareengineering.petsitter.ui.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.user.domain.AccountStatus;
import com.softwareengineering.petsitter.user.dto.PublicUserProfileDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProfilePopUpTest {

    @Test
    void rendersPublicProfileData() {
        PublicUserProfileDto profile = new PublicUserProfileDto(
                UUID.randomUUID(),
                "Ben Betreuung",
                LocalDate.of(1990, 1, 1),
                "deutsch",
                "Ich betreue Hunde und Katzen.",
                "50667",
                "Koeln",
                "2 Hunde",
                AccountStatus.VERIFIED
        );

        ProfilePopUp popUp = new ProfilePopUp(profile);

        assertThat(containsText(popUp, "Profil von Ben Betreuung")).isTrue();
        assertThat(containsText(popUp, "Ben Betreuung")).isTrue();
        assertThat(containsText(popUp, "2 Hunde")).isTrue();
        assertThat(containsText(popUp, "50667 Koeln")).isTrue();
        assertThat(containsText(popUp, "deutsch")).isTrue();
        assertThat(containsText(popUp, "Ich betreue Hunde und Katzen.")).isTrue();
        assertThat(containsText(popUp, "Verifiziert")).isTrue();
        assertThat(containsText(popUp, "Bewertungen von Ben Betreuung")).isTrue();
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && hasText.getText() != null && hasText.getText().contains(text)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }
}
