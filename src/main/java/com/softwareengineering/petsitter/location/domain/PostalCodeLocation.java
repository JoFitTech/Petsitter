package com.softwareengineering.petsitter.location.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "postal_code_location")
@IdClass(PostalCodeLocationId.class)
public class PostalCodeLocation {

    @Id
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Id
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "primary_place_name", nullable = false, length = 180)
    private String primaryPlaceName;

    @Column(name = "place_names", nullable = false, length = 1000)
    private String placeNames;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 6)
    private BigDecimal longitude;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public PostalCodeLocation() {
    }

    public PostalCodeLocation(String countryCode, String postalCode, String primaryPlaceName,
            String placeNames, BigDecimal latitude, BigDecimal longitude, LocalDateTime fetchedAt) {
        this.countryCode = countryCode;
        this.postalCode = postalCode;
        this.primaryPlaceName = primaryPlaceName;
        this.placeNames = placeNames;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fetchedAt = fetchedAt;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPrimaryPlaceName() {
        return primaryPlaceName;
    }

    public void setPrimaryPlaceName(String primaryPlaceName) {
        this.primaryPlaceName = primaryPlaceName;
    }

    public String getPlaceNames() {
        return placeNames;
    }

    public void setPlaceNames(String placeNames) {
        this.placeNames = placeNames;
    }

    public List<String> getPlaceNamesList() {
        if (placeNames == null || placeNames.isBlank()) {
            return List.of();
        }
        return Arrays.stream(placeNames.split("\\R"))
                .filter(value -> !value.isBlank())
                .toList();
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
