package com.softwareengineering.petsitter.ui.offer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.softwareengineering.petsitter.favorite.service.FavoriteService;
import com.softwareengineering.petsitter.location.dto.PostalCodeMapLocation;
import com.softwareengineering.petsitter.offer.service.OfferService;
import java.util.Collection;
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
