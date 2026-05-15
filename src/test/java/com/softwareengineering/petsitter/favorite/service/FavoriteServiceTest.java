package com.softwareengineering.petsitter.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwareengineering.petsitter.favorite.domain.Favorite;
import com.softwareengineering.petsitter.favorite.repository.FavoriteRepository;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferStatus;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.OfferCardDto;
import com.softwareengineering.petsitter.offer.repository.OfferRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.AccountRole;
import com.softwareengineering.petsitter.user.domain.User;
import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FavoriteServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T10:00:00Z"),
            ZoneOffset.UTC);

    @Test
    void toggleCreatesFavoriteForAvailableForeignOffer() {
        User currentUser = user(UUID.randomUUID());
        Offer offer = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        OfferRepositoryState offerState = new OfferRepositoryState(List.of(offer));
        FavoriteService favoriteService = service(favoriteState, offerState, Optional.of(currentUser));

        boolean result = favoriteService.toggleCurrentUserFavorite(offer.getOfferId());

        assertThat(result).isTrue();
        assertThat(favoriteState.savedFavorites).hasSize(1);
        assertThat(favoriteState.savedFavorites.getFirst().getUser()).isSameAs(currentUser);
        assertThat(favoriteState.savedFavorites.getFirst().getOffer()).isSameAs(offer);
    }

    @Test
    void toggleDeletesExistingFavorite() {
        User currentUser = user(UUID.randomUUID());
        Offer offer = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        Favorite favorite = new Favorite(currentUser, offer);
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        favoriteState.existingFavorites.put(offer.getOfferId(), favorite);
        OfferRepositoryState offerState = new OfferRepositoryState(List.of(offer));
        FavoriteService favoriteService = service(favoriteState, offerState, Optional.of(currentUser));

        boolean result = favoriteService.toggleCurrentUserFavorite(offer.getOfferId());

        assertThat(result).isFalse();
        assertThat(favoriteState.deletedFavorites).containsExactly(favorite);
        assertThat(offerState.findByIdCalls).isZero();
    }

    @Test
    void toggleRejectsUnknownOffer() {
        User currentUser = user(UUID.randomUUID());
        UUID offerId = UUID.randomUUID();
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        OfferRepositoryState offerState = new OfferRepositoryState(List.of());
        FavoriteService favoriteService = service(favoriteState, offerState, Optional.of(currentUser));

        assertThatThrownBy(() -> favoriteService.toggleCurrentUserFavorite(offerId))
                .isInstanceOf(NotFoundException.class);

        assertThat(favoriteState.savedFavorites).isEmpty();
    }

    @Test
    void toggleRejectsOwnUnavailableOrExpiredOffers() {
        User currentUser = user(UUID.randomUUID());
        Offer ownOffer = offer(UUID.randomUUID(), currentUser, OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        Offer bookedOffer = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.BOOKED,
                LocalDate.of(2026, 5, 10));
        Offer expiredOffer = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.OPEN,
                LocalDate.of(2026, 5, 9));
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        OfferRepositoryState offerState = new OfferRepositoryState(List.of(ownOffer, bookedOffer, expiredOffer));
        FavoriteService favoriteService = service(favoriteState, offerState, Optional.of(currentUser));

        assertThatThrownBy(() -> favoriteService.toggleCurrentUserFavorite(ownOffer.getOfferId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> favoriteService.toggleCurrentUserFavorite(bookedOffer.getOfferId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> favoriteService.toggleCurrentUserFavorite(expiredOffer.getOfferId()))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(favoriteState.savedFavorites).isEmpty();
    }

    @Test
    void getCurrentUserFavoriteOffersFiltersToAvailableForeignOpenOffers() {
        User currentUser = user(UUID.randomUUID());
        User otherUser = user(UUID.randomUUID());
        Offer visible = offer(UUID.randomUUID(), otherUser, OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        visible.setTitle("Katzenbetreuung");
        Offer booked = offer(UUID.randomUUID(), otherUser, OfferStatus.BOOKED, LocalDate.of(2026, 5, 10));
        Offer cancelled = offer(UUID.randomUUID(), otherUser, OfferStatus.CANCELLED, LocalDate.of(2026, 5, 10));
        Offer expired = offer(UUID.randomUUID(), otherUser, OfferStatus.OPEN, LocalDate.of(2026, 5, 9));
        Offer own = offer(UUID.randomUUID(), currentUser, OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        favoriteState.favoritesByUser = List.of(
                new Favorite(currentUser, visible),
                new Favorite(currentUser, booked),
                new Favorite(currentUser, cancelled),
                new Favorite(currentUser, expired),
                new Favorite(currentUser, own));
        FavoriteService favoriteService = service(favoriteState, new OfferRepositoryState(List.of()), Optional.of(currentUser));

        List<OfferCardDto> result = favoriteService.getCurrentUserFavoriteOffers();

        assertThat(result).extracting(OfferCardDto::id).containsExactly(visible.getOfferId());
        assertThat(result.getFirst().title()).isEqualTo("Katzenbetreuung");
        assertThat(result.getFirst().favorited()).isTrue();
    }

    @Test
    void favoriteOfferIdsForCurrentUserReturnsStoredIds() {
        User currentUser = user(UUID.randomUUID());
        Offer first = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        Offer second = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        Offer third = offer(UUID.randomUUID(), user(UUID.randomUUID()), OfferStatus.OPEN, LocalDate.of(2026, 5, 10));
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        favoriteState.favoritesByIds = List.of(new Favorite(currentUser, third), new Favorite(currentUser, first));
        FavoriteService favoriteService = service(favoriteState, new OfferRepositoryState(List.of()), Optional.of(currentUser));
        LinkedHashSet<UUID> requestedIds = new LinkedHashSet<>(
                List.of(first.getOfferId(), second.getOfferId(), third.getOfferId()));

        Set<UUID> result = favoriteService.favoriteOfferIdsForCurrentUser(requestedIds);

        assertThat(result).containsExactly(third.getOfferId(), first.getOfferId());
        assertThat(favoriteState.requestedIdsUserId).isEqualTo(currentUser.getId());
        assertThat(favoriteState.requestedOfferIds).isEqualTo(requestedIds);
    }

    @Test
    void favoriteOfferIdsForCurrentUserReturnsEmptyWithoutAuthenticatedUser() {
        FavoriteRepositoryState favoriteState = new FavoriteRepositoryState();
        OfferRepositoryState offerState = new OfferRepositoryState(List.of());
        FavoriteService favoriteService = service(favoriteState, offerState, Optional.empty());

        Set<UUID> result = favoriteService.favoriteOfferIdsForCurrentUser(List.of(UUID.randomUUID()));

        assertThat(result).isEmpty();
        assertThat(favoriteState.requestedIdsUserId).isNull();
        assertThat(offerState.findByIdCalls).isZero();
    }

    private FavoriteService service(
            FavoriteRepositoryState favoriteState,
            OfferRepositoryState offerState,
            Optional<User> currentUser) {
        return new FavoriteService(
                favoriteRepository(favoriteState),
                offerRepository(offerState),
                authenticatedUser(currentUser),
                FIXED_CLOCK);
    }

    private FavoriteRepository favoriteRepository(FavoriteRepositoryState state) {
        return (FavoriteRepository) Proxy.newProxyInstance(
                FavoriteRepository.class.getClassLoader(),
                new Class<?>[] {FavoriteRepository.class},
                (proxy, method, args) -> {
                    if ("findByUserIdAndOfferOfferId".equals(method.getName())) {
                        state.requestedFindUserId = (UUID) args[0];
                        state.requestedFindOfferId = (UUID) args[1];
                        return Optional.ofNullable(state.existingFavorites.get((UUID) args[1]));
                    }
                    if ("save".equals(method.getName())) {
                        Favorite favorite = (Favorite) args[0];
                        state.savedFavorites.add(favorite);
                        return favorite;
                    }
                    if ("delete".equals(method.getName())) {
                        state.deletedFavorites.add((Favorite) args[0]);
                        return null;
                    }
                    if ("deleteByUserIdAndOfferOfferId".equals(method.getName())) {
                        state.deletedByUserId = (UUID) args[0];
                        state.deletedByOfferId = (UUID) args[1];
                        return null;
                    }
                    if ("findAllByUserIdOrderByCreatedAtDesc".equals(method.getName())) {
                        state.requestedFavoritesUserId = (UUID) args[0];
                        return state.favoritesByUser;
                    }
                    if ("findAllByUserIdAndOfferOfferIdIn".equals(method.getName())) {
                        state.requestedIdsUserId = (UUID) args[0];
                        state.requestedOfferIds = new LinkedHashSet<>((Collection<UUID>) args[1]);
                        return state.favoritesByIds;
                    }
                    if ("toString".equals(method.getName())) {
                        return "FavoriteRepositoryTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                });
    }

    private OfferRepository offerRepository(OfferRepositoryState state) {
        return (OfferRepository) Proxy.newProxyInstance(
                OfferRepository.class.getClassLoader(),
                new Class<?>[] {OfferRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        state.findByIdCalls++;
                        return Optional.ofNullable(state.offersById.get((UUID) args[0]));
                    }
                    if ("toString".equals(method.getName())) {
                        return "OfferRepositoryFavoriteTestDouble";
                    }
                    throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                });
    }

    private AuthenticatedUser authenticatedUser(Optional<User> user) {
        return new AuthenticatedUser(null) {
            @Override
            public Optional<User> get() {
                return user;
            }
        };
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(userId + "@petsitter.local");
        user.setFirstName("Anna");
        user.setLastName("Mueller");
        user.setDisplayName("Anna Mueller");
        user.setStreet("Rosenweg");
        user.setHouseNumber("14");
        user.setPostalCode("50667");
        user.setCity("Koeln");
        user.setAccountRole(AccountRole.SIGNED_IN_USER);
        return user;
    }

    private Offer offer(UUID offerId, User creator, OfferStatus status, LocalDate startDate) {
        Offer offer = new Offer();
        offer.setOfferId(offerId);
        offer.setCreateUser(creator);
        offer.setOfferType(OfferType.SITTER_OFFER);
        offer.setStatus(status);
        offer.setStartDate(startDate);
        offer.setEndDate(startDate.plusDays(1));
        offer.setPrice(BigDecimal.TEN);
        offer.setTitle("Angebot");
        return offer;
    }

    private static final class FavoriteRepositoryState {
        private final Map<UUID, Favorite> existingFavorites = new LinkedHashMap<>();
        private final List<Favorite> savedFavorites = new ArrayList<>();
        private final List<Favorite> deletedFavorites = new ArrayList<>();
        private UUID requestedFindUserId;
        private UUID requestedFindOfferId;
        private UUID deletedByUserId;
        private UUID deletedByOfferId;
        private UUID requestedFavoritesUserId;
        private UUID requestedIdsUserId;
        private Set<UUID> requestedOfferIds;
        private List<Favorite> favoritesByUser = List.of();
        private List<Favorite> favoritesByIds = List.of();
    }

    private static final class OfferRepositoryState {
        private final Map<UUID, Offer> offersById;
        private int findByIdCalls;

        private OfferRepositoryState(List<Offer> offers) {
            this.offersById = offers.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Offer::getOfferId,
                            offer -> offer,
                            (first, second) -> first,
                            LinkedHashMap::new));
        }
    }
}
