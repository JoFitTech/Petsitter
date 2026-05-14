package com.softwareengineering.petsitter.location.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareengineering.petsitter.location.dto.PostalCodeLookup;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NominatimPostalCodeClient implements PostalCodeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NominatimPostalCodeClient.class);
    private static final Duration MIN_REQUEST_INTERVAL = Duration.ofMillis(1100);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Duration requestTimeout;
    private final String userAgent;
    private Instant lastRequestAt = Instant.EPOCH;

    public NominatimPostalCodeClient(
            @Value("${petsitter.location.nominatim-base-url:https://nominatim.openstreetmap.org}") String baseUrl,
            @Value("${petsitter.location.lookup-timeout-ms:3000}") long timeoutMs,
            @Value("${petsitter.location.nominatim-user-agent:Pawsitter/1.0 local-development}") String userAgent
    ) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.requestTimeout = Duration.ofMillis(timeoutMs);
        this.userAgent = userAgent;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public Optional<PostalCodeLookup> lookup(String countryCode, String postalCode) {
        String normalizedCountryCode = countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.ROOT);
        String normalizedPostalCode = postalCode == null ? "" : postalCode.trim();
        if (!PostalCodeService.GERMANY_COUNTRY_CODE.equals(normalizedCountryCode)) {
            return Optional.empty();
        }

        URI uri = URI.create(baseUrl
                + "/search?country=Germany"
                + "&postalcode=" + URLEncoder.encode(normalizedPostalCode, StandardCharsets.UTF_8)
                + "&format=jsonv2&addressdetails=1&limit=1");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", userAgent)
                .GET()
                .build();

        try {
            throttlePublicApiAccess();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PostalCodeLookupException("Nominatim antwortet mit Status " + response.statusCode() + ".");
            }
            List<NominatimSearchResult> results = objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {
                    });
            Optional<PostalCodeLookup> lookup = results.stream()
                    .filter(result -> result.lat() != null
                            && result.lon() != null
                            && result.address() != null
                            && normalizedPostalCode.equals(result.address().postcode()))
                    .findFirst()
                    .map(result -> toLookup(normalizedCountryCode, normalizedPostalCode, result));
            lookup.ifPresent(result -> LOGGER.info(
                    "Postal code lookup via Nominatim: postalCode={}, place={}, latitude={}, longitude={}",
                    result.postalCode(),
                    result.places().getFirst().name(),
                    result.places().getFirst().latitude(),
                    result.places().getFirst().longitude()));
            return lookup;
        } catch (IOException ex) {
            throw new PostalCodeLookupException("Nominatim konnte nicht gelesen werden.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PostalCodeLookupException("Nominatim wurde unterbrochen.", ex);
        } catch (NumberFormatException ex) {
            throw new PostalCodeLookupException("Nominatim hat ungueltige Koordinaten geliefert.", ex);
        }
    }

    private PostalCodeLookup toLookup(String countryCode, String postalCode, NominatimSearchResult result) {
        BigDecimal latitude = new BigDecimal(result.lat());
        BigDecimal longitude = new BigDecimal(result.lon());
        List<PostalCodeLookup.Place> places = placeNames(result).stream()
                .map(name -> new PostalCodeLookup.Place(name, latitude, longitude))
                .toList();
        return new PostalCodeLookup(countryCode, postalCode, places);
    }

    private List<String> placeNames(NominatimSearchResult result) {
        Set<String> names = new LinkedHashSet<>();
        NominatimAddress address = result.address();
        addIfPresent(names, address.city());
        addIfPresent(names, address.town());
        addIfPresent(names, address.village());
        addIfPresent(names, address.municipality());
        addIfPresent(names, address.county());
        addIfPresent(names, address.suburb());
        addIfPresent(names, result.displayName());
        if (names.isEmpty()) {
            names.add(result.name());
        }
        return names.stream()
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private void addIfPresent(Set<String> names, String name) {
        if (name != null && !name.isBlank()) {
            names.add(name.trim());
        }
    }

    private synchronized void throttlePublicApiAccess() throws InterruptedException {
        Instant now = Instant.now();
        long waitMs = MIN_REQUEST_INTERVAL.minus(Duration.between(lastRequestAt, now)).toMillis();
        if (waitMs > 0) {
            Thread.sleep(waitMs);
        }
        lastRequestAt = Instant.now();
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://nominatim.openstreetmap.org";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimSearchResult(
            String lat,
            String lon,
            String name,
            @com.fasterxml.jackson.annotation.JsonProperty("display_name") String displayName,
            NominatimAddress address
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimAddress(
            String postcode,
            String city,
            String town,
            String village,
            String municipality,
            String county,
            String suburb
    ) {
    }
}
