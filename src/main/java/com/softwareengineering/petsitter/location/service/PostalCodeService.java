package com.softwareengineering.petsitter.location.service;

import com.softwareengineering.petsitter.location.domain.PostalCodeLocation;
import com.softwareengineering.petsitter.location.dto.PostalCodeLookup;
import com.softwareengineering.petsitter.location.dto.PostalCodeValidationResult;
import com.softwareengineering.petsitter.location.repository.PostalCodeLocationRepository;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostalCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostalCodeService.class);

    public static final String GERMANY_COUNTRY_CODE = "DE";
    private static final Pattern GERMAN_POSTAL_CODE = Pattern.compile("\\d{5}");
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final PostalCodeLocationRepository repository;
    private final PostalCodeClient client;
    private final Clock clock;

    @Autowired
    public PostalCodeService(PostalCodeLocationRepository repository, PostalCodeClient client) {
        this(repository, client, Clock.systemUTC());
    }

    PostalCodeService(PostalCodeLocationRepository repository, PostalCodeClient client, Clock clock) {
        this.repository = repository;
        this.client = client;
        this.clock = clock;
    }

    @Transactional
    public PostalCodeValidationResult validateGermanPostalCode(String postalCode, String city) {
        String normalizedPostalCode = normalizePostalCode(postalCode);
        if (!isValidGermanPostalCodeFormat(normalizedPostalCode)) {
            return PostalCodeValidationResult.invalid("Bitte eine gültige deutsche Postleitzahl eingeben.");
        }

        Optional<PostalCodeLocation> location;
        try {
            location = findGermanLocation(normalizedPostalCode);
        } catch (PostalCodeLookupException ex) {
            return PostalCodeValidationResult.invalid(
                    "Die Postleitzahl konnte gerade nicht überprüft werden. Bitte später erneut versuchen.");
        }
        if (location.isEmpty()) {
            return PostalCodeValidationResult.invalid("Bitte eine gültige deutsche Postleitzahl eingeben.");
        }
        if (!isBlank(city) && !matchesCity(location.get(), city)) {
            return PostalCodeValidationResult.invalid("Die Postleitzahl passt nicht zum angegebenen Ort.");
        }
        return PostalCodeValidationResult.success();
    }

    @Transactional
    public Optional<PostalCodeLocation> findGermanLocation(String postalCode) {
        String normalizedPostalCode = normalizePostalCode(postalCode);
        if (!isValidGermanPostalCodeFormat(normalizedPostalCode)) {
            return Optional.empty();
        }
        return findLocation(GERMANY_COUNTRY_CODE, normalizedPostalCode);
    }

    @Transactional(readOnly = true)
    public Optional<PostalCodeLocation> findCachedGermanLocation(String postalCode) {
        String normalizedPostalCode = normalizePostalCode(postalCode);
        if (!isValidGermanPostalCodeFormat(normalizedPostalCode)) {
            return Optional.empty();
        }
        return findCachedLocation(GERMANY_COUNTRY_CODE, normalizedPostalCode);
    }

    @Transactional(readOnly = true)
    public Optional<PostalCodeLocation> findCachedLocation(String countryCode, String postalCode) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        String normalizedPostalCode = normalizePostalCode(postalCode);
        return repository.findByCountryCodeAndPostalCode(normalizedCountryCode, normalizedPostalCode)
                .filter(location -> hasPlausibleCoordinates(
                        normalizedCountryCode,
                        location.getLatitude(),
                        location.getLongitude()));
    }

    @Transactional(readOnly = true)
    public Map<String, PostalCodeLocation> findCachedGermanLocations(Set<String> postalCodes) {
        Set<String> normalizedPostalCodes = postalCodes == null ? Set.of() : postalCodes.stream()
                .map(this::normalizePostalCode)
                .filter(this::isValidGermanPostalCodeFormat)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalizedPostalCodes.isEmpty()) {
            return Map.of();
        }

        return repository.findAllByCountryCodeAndPostalCodeIn(GERMANY_COUNTRY_CODE, normalizedPostalCodes)
                .stream()
                .filter(location -> hasPlausibleCoordinates(
                        GERMANY_COUNTRY_CODE,
                        location.getLatitude(),
                        location.getLongitude()))
                .collect(Collectors.toMap(
                        PostalCodeLocation::getPostalCode,
                        Function.identity(),
                        (existing, duplicate) -> existing));
    }

    @Transactional
    public Optional<PostalCodeLocation> findLocation(String countryCode, String postalCode) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        String normalizedPostalCode = normalizePostalCode(postalCode);
        Optional<PostalCodeLocation> cachedLocation = repository.findByCountryCodeAndPostalCode(
                normalizedCountryCode,
                normalizedPostalCode);
        if (cachedLocation.isPresent()
                && hasPlausibleCoordinates(
                        normalizedCountryCode,
                        cachedLocation.get().getLatitude(),
                        cachedLocation.get().getLongitude())) {
            return cachedLocation;
        }
        cachedLocation.ifPresent(location -> LOGGER.info(
                "Postal code cache entry has implausible coordinates and will be refreshed: "
                        + "postalCode={}, latitude={}, longitude={}",
                location.getPostalCode(),
                location.getLatitude(),
                location.getLongitude()));
        return fetchAndCache(normalizedCountryCode, normalizedPostalCode);
    }

    private boolean hasPlausibleCoordinates(String countryCode, BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        double lat = latitude.doubleValue();
        double lon = longitude.doubleValue();
        boolean globallyValid = lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
        if (!globallyValid) {
            return false;
        }
        if (GERMANY_COUNTRY_CODE.equals(normalizeCountryCode(countryCode))) {
            return lat >= 47 && lat <= 56 && lon >= 5 && lon <= 16;
        }
        return true;
    }

    public double distanceKm(PostalCodeLocation from, PostalCodeLocation to) {
        double fromLat = Math.toRadians(from.getLatitude().doubleValue());
        double fromLon = Math.toRadians(from.getLongitude().doubleValue());
        double toLat = Math.toRadians(to.getLatitude().doubleValue());
        double toLon = Math.toRadians(to.getLongitude().doubleValue());
        double latDistance = toLat - fromLat;
        double lonDistance = toLon - fromLon;

        double a = Math.pow(Math.sin(latDistance / 2), 2)
                + Math.cos(fromLat) * Math.cos(toLat) * Math.pow(Math.sin(lonDistance / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public int roundedDistanceKm(double distanceKm) {
        return Math.max(0, (int) Math.round(distanceKm));
    }

    public String normalizePostalCode(String postalCode) {
        return postalCode == null ? "" : postalCode.trim().replace(" ", "");
    }

    public boolean isValidGermanPostalCodeFormat(String postalCode) {
        return postalCode != null && GERMAN_POSTAL_CODE.matcher(postalCode).matches();
    }

    public boolean matchesCity(PostalCodeLocation location, String city) {
        Set<String> inputKeys = cityKeys(city);
        if (inputKeys.isEmpty()) {
            return true;
        }
        return location.getPlaceNamesList().stream()
                .map(this::cityKeys)
                .anyMatch(placeKeys -> placeKeys.stream().anyMatch(inputKeys::contains));
    }

    private Optional<PostalCodeLocation> fetchAndCache(String countryCode, String postalCode) {
        Optional<PostalCodeLookup> lookup = client.lookup(countryCode, postalCode);
        if (lookup.isEmpty() || lookup.get().places().isEmpty()) {
            return Optional.empty();
        }

        PostalCodeLocation location = toLocation(lookup.get());
        return Optional.of(repository.save(location));
    }

    private PostalCodeLocation toLocation(PostalCodeLookup lookup) {
        PostalCodeLookup.Place primaryPlace = lookup.places().getFirst();
        String placeNames = lookup.places().stream()
                .map(PostalCodeLookup.Place::name)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.joining("\n"));
        return new PostalCodeLocation(
                normalizeCountryCode(lookup.countryCode()),
                normalizePostalCode(lookup.postalCode()),
                primaryPlace.name(),
                placeNames,
                primaryPlace.latitude(),
                primaryPlace.longitude(),
                LocalDateTime.now(clock)
        );
    }

    private String normalizeCountryCode(String countryCode) {
        return countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private Set<String> cityKeys(String city) {
        String normalized = normalizeWhitespace(city);
        if (normalized.isBlank()) {
            return Set.of();
        }

        Set<String> keys = new LinkedHashSet<>();
        keys.add(normalized);
        keys.add(germanTransliteration(normalized));
        keys.add(stripDiacritics(normalized));
        keys.remove("");
        return keys;
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.GERMANY)
                .replaceAll("\\s+", " ");
    }

    private String germanTransliteration(String value) {
        return value
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
    }

    private String stripDiacritics(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("ß", "ss");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
