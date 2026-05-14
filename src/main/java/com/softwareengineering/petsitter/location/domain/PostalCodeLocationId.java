package com.softwareengineering.petsitter.location.domain;

import java.io.Serializable;
import java.util.Objects;

public class PostalCodeLocationId implements Serializable {

    private String countryCode;
    private String postalCode;

    public PostalCodeLocationId() {
    }

    public PostalCodeLocationId(String countryCode, String postalCode) {
        this.countryCode = countryCode;
        this.postalCode = postalCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostalCodeLocationId that)) {
            return false;
        }
        return Objects.equals(countryCode, that.countryCode)
                && Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, postalCode);
    }
}
