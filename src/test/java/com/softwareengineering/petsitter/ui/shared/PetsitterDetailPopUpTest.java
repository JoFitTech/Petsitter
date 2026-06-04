package com.softwareengineering.petsitter.ui.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.softwareengineering.petsitter.booking.service.BookingService;
import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.offer.domain.OfferCareType;
import com.softwareengineering.petsitter.offer.domain.OfferFrequency;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.softwareengineering.petsitter.offerrequest.service.RequestService;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.domain.PetTag;
import com.softwareengineering.petsitter.pet.domain.PetVaccinationStatus;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.ui.user.LoginView;
import com.softwareengineering.petsitter.user.domain.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PetsitterDetailPopUpTest {

    @AfterEach
    void clearCurrentUi() {
        UI.setCurrent(null);
    }

    @Test
    void rendersSeparateDetailBlockForEachOwnerOfferPet() {
        PetDto cat = new PetDto(
                UUID.randomUUID(),
                "Mila",
                PetSpecies.CAT,
                null,
                "Europaeisch Kurzhaar",
                LocalDate.now().minusYears(4),
                "Braucht morgens ihr Spezialfutter.",
                PetVaccinationStatus.GEIMPFT,
                Set.of(PetTag.STUBENREIN, PetTag.VERSPIELT)
        );
        PetDto dog = new PetDto(
                UUID.randomUUID(),
                "Balu",
                PetSpecies.DOG,
                null,
                null,
                null,
                null,
                null,
                Set.of(PetTag.BRAUCHT_MEDIKAMENTE)
        );
        PetsitterDetailPopUp popup = new PetsitterDetailPopUp(
                ownerOfferCard(List.of(cat, dog)),
                "ca. 2 km",
                4,
                new OwnOfferService(),
                null,
                null,
                null,
                null,
                null
        );

        List<Component> cards = findByClassName(popup, "offer-pet-detail-card");

        assertThat(cards).hasSize(2);
        assertThat(containsText(cards.get(0), "Mila")).isTrue();
        assertThat(containsText(cards.get(0), "Katze")).isTrue();
        assertThat(containsText(cards.get(0), "Europaeisch Kurzhaar")).isTrue();
        assertThat(containsText(cards.get(0), "4 Jahre alt")).isTrue();
        assertThat(containsText(cards.get(0), "Geimpft")).isTrue();
        assertThat(containsText(cards.get(0), "Stubenrein")).isTrue();
        assertThat(containsText(cards.get(0), "Verspielt")).isTrue();
        assertThat(containsText(cards.get(0), "Braucht morgens ihr Spezialfutter.")).isTrue();
        assertThat(containsText(cards.get(0), "Braucht Medikamente")).isFalse();
        assertThat(containsText(cards.get(1), "Balu")).isTrue();
        assertThat(containsText(cards.get(1), "Hund")).isTrue();
        assertThat(containsText(cards.get(1), "Impfstatus unbekannt")).isTrue();
        assertThat(containsText(cards.get(1), "Braucht Medikamente")).isTrue();
        assertThat(containsText(cards.get(1), "Keine Tierbeschreibung hinterlegt.")).isTrue();
        assertThat(containsText(cards.get(1), "Verspielt")).isFalse();
        assertThat(containsText(popup, "Mila, Balu")).isFalse();
        assertThat(findByClassName(popup, "offer-description-field")).hasSize(1);
    }

    @Test
    void rendersCreatorAsBorderlessProfileButtonWithAvatarAndName() {
        PetsitterDetailPopUp popup = new PetsitterDetailPopUp(
                ownerOfferCard(List.of()),
                "ca. 2 km",
                4,
                new OwnOfferService(),
                null,
                null,
                null,
                null,
                null
        );

        List<Component> creatorLinks = findByClassName(popup, "offer-creator-link");

        assertThat(creatorLinks).hasSize(1);
        Button creatorLink = (Button) creatorLinks.get(0);
        assertThat(creatorLink.getStyle().get("background")).isEqualTo("transparent");
        assertThat(creatorLink.getStyle().get("border")).isEqualTo("none");
        assertThat(findByClassName(creatorLink, "offer-creator-avatar")).hasSize(1);
        assertThat(findByClassName(creatorLink, "offer-creator-name")).hasSize(1);
        assertThat(creatorLink.getChildren().findFirst().orElseThrow().getStyle().get("flex-direction"))
                .isEqualTo("row");
        assertThat(containsText(creatorLink, "Anna")).isTrue();
    }

    @Test
    void hidesOfferDescriptionSectionWhenDescriptionIsBlank() {
        PetsitterDetailPopUp popup = new PetsitterDetailPopUp(
                ownerOfferCard(List.of(), "   "),
                "ca. 2 km",
                4,
                new OwnOfferService(),
                null,
                null,
                null,
                null,
                null
        );

        assertThat(findByClassName(popup, "offer-description-field")).isEmpty();
        assertThat(containsText(popup, "Beschreibung")).isFalse();
    }

    @Test
    void requestActionRedirectsAnonymousUserToLoginWithoutCallingRequestServices() {
        RecordingUI ui = new RecordingUI();
        UI.setCurrent(ui);
        RequestService requestService = mock(RequestService.class);
        ChatService chatService = mock(ChatService.class);
        BookingService bookingService = mock(BookingService.class);
        PetsitterDetailPopUp popup = new PetsitterDetailPopUp(
                ownerOfferCard(List.of()),
                "ca. 2 km",
                4,
                new OtherUserOfferService(),
                requestService,
                chatService,
                new AnonymousUser(),
                null,
                bookingService
        );

        findButtonByText(popup, "Auftrag anfragen").click();

        assertThat(ui.navigationTarget).isEqualTo(LoginView.class);
        verifyNoInteractions(requestService, chatService, bookingService);
    }

    private OfferCardDto ownerOfferCard(List<PetDto> pets) {
        return ownerOfferCard(pets, "Offer-Beschreibung");
    }

    private OfferCardDto ownerOfferCard(List<PetDto> pets, String description) {
        return new OfferCardDto(
                UUID.randomUUID(),
                "Betreuung gesucht",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                BigDecimal.valueOf(80),
                null,
                false,
                description,
                OfferFrequency.ONE_TIME,
                OfferCareType.PET_SITTING,
                "Mila, Balu",
                "Katze, Hund",
                "Europaeisch Kurzhaar",
                "Geimpft, Impfstatus unbekannt, Stubenrein, Verspielt, Braucht Medikamente",
                pets,
                null,
                null,
                null,
                false,
                OfferType.OWNER_OFFER,
                UUID.randomUUID(),
                "Anna"
        );
    }

    private List<Component> findByClassName(Component root, String className) {
        List<Component> matches = new ArrayList<>();
        if (root.getClassNames().contains(className)) {
            matches.add(root);
        }
        root.getChildren().forEach(child -> matches.addAll(findByClassName(child, className)));
        return matches;
    }

    private boolean containsText(Component root, String text) {
        if (root instanceof HasText hasText && hasText.getText() != null && hasText.getText().contains(text)) {
            return true;
        }
        return root.getChildren().anyMatch(child -> containsText(child, text));
    }

    private Button findButtonByText(Component root, String text) {
        return findButtonsByText(root, text).stream()
                .findFirst()
                .orElseThrow();
    }

    private List<Button> findButtonsByText(Component root, String text) {
        List<Button> matches = new ArrayList<>();
        if (root instanceof Button button && text.equals(button.getText())) {
            matches.add(button);
        }
        root.getChildren().forEach(child -> matches.addAll(findButtonsByText(child, text)));
        return matches;
    }

    private static final class OwnOfferService extends OfferService {

        private OwnOfferService() {
            super(null, null, null, null);
        }

        @Override
        public boolean isCurrentUserOffer(UUID offerId) {
            return true;
        }

        @Override
        public boolean canCurrentUserEditOffer(UUID offerId) {
            return false;
        }
    }

    private static final class OtherUserOfferService extends OfferService {

        private OtherUserOfferService() {
            super(null, null, null, null);
        }

        @Override
        public boolean isCurrentUserOffer(UUID offerId) {
            return false;
        }

        @Override
        public boolean canCurrentUserEditOffer(UUID offerId) {
            return false;
        }
    }

    private static final class AnonymousUser extends AuthenticatedUser {

        private AnonymousUser() {
            super(null);
        }

        @Override
        public Optional<User> get() {
            return Optional.empty();
        }
    }

    private static final class RecordingUI extends UI {

        private Class<?> navigationTarget;

        @Override
        public <T extends Component> Optional<T> navigate(Class<T> navigationTarget) {
            this.navigationTarget = navigationTarget;
            return Optional.empty();
        }
    }
}
