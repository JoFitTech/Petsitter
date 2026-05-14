package com.softwareengineering.petsitter.ui.offer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.softwareengineering.petsitter.offer.service.OfferService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PetsitterFilterViewTest {

    @Test
    void constructorInitializesDefaultCriteriaAfterOfferServiceIsAssigned() {
        assertDoesNotThrow(() -> new PetsitterFilterView(new TestOfferService()));
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
    }
}
