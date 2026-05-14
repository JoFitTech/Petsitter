package com.softwareengineering.petsitter.location.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.location.domain.PostalCodeLocation;
import com.softwareengineering.petsitter.location.dto.PostalCodeLookup;
import com.softwareengineering.petsitter.location.dto.PostalCodeValidationResult;
import com.softwareengineering.petsitter.location.repository.PostalCodeLocationRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostalCodeServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-14T10:15:30Z"), ZoneOffset.UTC);
    private PostalCodeLocationRepositoryFake repository;
    private PostalCodeClientFake client;
    private PostalCodeService service;

    @BeforeEach
    void setUp() {
        repository = new PostalCodeLocationRepositoryFake();
        client = new PostalCodeClientFake();
        service = new PostalCodeService(repository.repository(), client, clock);
    }

    @Test
    void validateGermanPostalCodeRejectsInvalidFormatWithoutApiCall() {
        PostalCodeValidationResult result = service.validateGermanPostalCode("1234", "Berlin");

        assertThat(result.valid()).isFalse();
        assertThat(repository.findCount).isZero();
        assertThat(client.lookupCount).isZero();
    }

    @Test
    void validateGermanPostalCodeAcceptsFetchedPostalCodeAndCachesLocation() {
        client.lookupResult = Optional.of(lookup("DE", "10115", "Berlin", "52.5321", "13.3849"));

        PostalCodeValidationResult result = service.validateGermanPostalCode(" 10115 ", "Berlin");

        assertThat(result.valid()).isTrue();
        assertThat(repository.savedLocation.getCountryCode()).isEqualTo("DE");
        assertThat(repository.savedLocation.getPostalCode()).isEqualTo("10115");
        assertThat(repository.savedLocation.getPrimaryPlaceName()).isEqualTo("Berlin");
        assertThat(repository.savedLocation.getFetchedAt()).isEqualTo(java.time.LocalDateTime.parse("2026-05-14T10:15:30"));
    }

    @Test
    void validateGermanPostalCodeRejectsUnknownPostalCode() {
        client.lookupResult = Optional.empty();

        PostalCodeValidationResult result = service.validateGermanPostalCode("99999", "Berlin");

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("Bitte eine gültige deutsche Postleitzahl eingeben.");
    }

    @Test
    void validateGermanPostalCodeMatchesGermanUmlautTransliteration() {
        repository.seed(location("DE", "50667", "Koeln", "Koeln", "50.9384", "6.9597"));

        PostalCodeValidationResult result = service.validateGermanPostalCode("50667", "Köln");

        assertThat(result.valid()).isTrue();
        assertThat(client.lookupCount).isZero();
    }

    @Test
    void validateGermanPostalCodeRejectsMismatchingCity() {
        repository.seed(location("DE", "10115", "Berlin", "Berlin", "52.5321", "13.3849"));

        PostalCodeValidationResult result = service.validateGermanPostalCode("10115", "Hamburg");

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).isEqualTo("Die Postleitzahl passt nicht zum angegebenen Ort.");
    }

    @Test
    void validateGermanPostalCodeUsesCacheWhenAvailable() {
        PostalCodeLocation cached = location("DE", "10115", "Berlin", "Berlin", "52.5321", "13.3849");
        repository.seed(cached);

        Optional<PostalCodeLocation> result = service.findGermanLocation("10115");

        assertThat(result).contains(cached);
        assertThat(client.lookupCount).isZero();
    }

    @Test
    void findGermanLocationRefreshesCachedLocationWithImplausibleCoordinates() {
        PostalCodeLocation cached = location("DE", "49080", "Osnabrück", "Osnabrück", "3404", "52.2491");
        repository.seed(cached);
        client.lookupResult = Optional.of(lookup("DE", "49080", "Osnabrück", "52.2588709", "8.0327320"));

        Optional<PostalCodeLocation> result = service.findGermanLocation("49080");

        assertThat(result).isPresent();
        assertThat(result.get().getLatitude()).isEqualByComparingTo("52.2588709");
        assertThat(result.get().getLongitude()).isEqualByComparingTo("8.0327320");
        assertThat(client.lookupCount).isOne();
    }

    @Test
    void validateGermanPostalCodeFailsWhenApiIsUnavailableAndNoCacheExists() {
        client.lookupException = new PostalCodeLookupException("down");

        PostalCodeValidationResult result = service.validateGermanPostalCode("10115", "Berlin");

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("konnte gerade nicht überprüft werden");
    }

    private PostalCodeLookup lookup(String countryCode, String postalCode, String city, String latitude, String longitude) {
        return new PostalCodeLookup(countryCode, postalCode, List.of(
                new PostalCodeLookup.Place(city, new BigDecimal(latitude), new BigDecimal(longitude))));
    }

    private PostalCodeLocation location(String countryCode, String postalCode, String primaryPlaceName,
            String placeNames, String latitude, String longitude) {
        return new PostalCodeLocation(
                countryCode,
                postalCode,
                primaryPlaceName,
                placeNames,
                new BigDecimal(latitude),
                new BigDecimal(longitude),
                java.time.LocalDateTime.now(clock));
    }

    private static class PostalCodeClientFake implements PostalCodeClient {
        private Optional<PostalCodeLookup> lookupResult = Optional.empty();
        private PostalCodeLookupException lookupException;
        private int lookupCount;

        @Override
        public Optional<PostalCodeLookup> lookup(String countryCode, String postalCode) {
            lookupCount++;
            if (lookupException != null) {
                throw lookupException;
            }
            return lookupResult;
        }
    }

    private static class PostalCodeLocationRepositoryFake {
        private final Map<String, PostalCodeLocation> locations = new HashMap<>();
        private PostalCodeLocation savedLocation;
        private int findCount;

        void seed(PostalCodeLocation location) {
            locations.put(key(location.getCountryCode(), location.getPostalCode()), location);
        }

        PostalCodeLocationRepository repository() {
            return (PostalCodeLocationRepository) Proxy.newProxyInstance(
                    PostalCodeLocationRepository.class.getClassLoader(),
                    new Class<?>[] {PostalCodeLocationRepository.class},
                    (proxy, method, args) -> {
                        if ("findByCountryCodeAndPostalCode".equals(method.getName())) {
                            findCount++;
                            return Optional.ofNullable(locations.get(key((String) args[0], (String) args[1])));
                        }
                        if ("save".equals(method.getName())) {
                            savedLocation = (PostalCodeLocation) args[0];
                            seed(savedLocation);
                            return savedLocation;
                        }
                        if ("toString".equals(method.getName())) {
                            return "PostalCodeLocationRepositoryFake";
                        }
                        throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                    }
            );
        }

        private static String key(String countryCode, String postalCode) {
            return countryCode + ":" + postalCode;
        }
    }
}
