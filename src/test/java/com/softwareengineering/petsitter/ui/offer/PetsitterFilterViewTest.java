package com.softwareengineering.petsitter.ui.offer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.softwareengineering.petsitter.favorite.service.FavoriteService;
import com.softwareengineering.petsitter.location.dto.PostalCodeMapLocation;
import com.softwareengineering.petsitter.offer.dto.OfferSearchCriteria;
import com.softwareengineering.petsitter.offer.service.OfferService;
import com.vaadin.flow.router.QueryParameters;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PetsitterFilterViewTest {

    @Test
    void constructorInitializesDefaultCriteriaAfterOfferServiceIsAssigned() {
        assertDoesNotThrow(() -> new PetsitterFilterView(new TestOfferService(), new TestFavoriteService(),
                null, null, null, null, null));
    }

    @Test
    void unlimitedDistanceSurvivesQueryParameterRoundTrip() throws Exception {
        PetsitterFilterView view = new PetsitterFilterView(new TestOfferService(), new TestFavoriteService(),
                null, null, null, null, null);

        OfferSearchCriteria criteria = (OfferSearchCriteria) invokePrivate(
                view,
                "parseCriteria",
                new Class<?>[]{Map.class},
                Map.of(
                        "distanceKm", List.of(String.valueOf(OfferSearchCriteria.ANY_DISTANCE_KM)),
                        "originPostalCode", List.of("10115")));
        QueryParameters parameters = (QueryParameters) invokePrivate(
                view,
                "queryParametersFor",
                new Class<?>[]{OfferSearchCriteria.class},
                criteria);

        assertThat(criteria.hasUnlimitedDistance()).isTrue();
        assertThat(parameters.getParameters())
                .containsEntry("distanceKm", List.of(String.valueOf(OfferSearchCriteria.ANY_DISTANCE_KM)))
                .containsEntry("originPostalCode", List.of("10115"));
    }

    private Object invokePrivate(Object target, String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static final class TestOfferService extends OfferService {

        private TestOfferService() {
            super(null, null, null, null);
        }

        @Override
        public Optional<String> getCurrentUserPostalCode() {
            return Optional.of("10115");
        }

        @Override
        public Optional<String> validateOriginPostalCode(String postalCode) {
            return Optional.empty();
        }

        @Override
        public Optional<PostalCodeMapLocation> resolveSearchOriginLocation(String postalCode) {
            return Optional.empty();
        }
    }

    private static final class TestFavoriteService extends FavoriteService {

        private TestFavoriteService() {
            super(null, null, null);
        }

        @Override
        public Set<UUID> favoriteOfferIdsForCurrentUser(Collection<UUID> offerIds) {
            return Set.of();
        }
    }
}
