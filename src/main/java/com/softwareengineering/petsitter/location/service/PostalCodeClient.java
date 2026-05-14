package com.softwareengineering.petsitter.location.service;

import com.softwareengineering.petsitter.location.dto.PostalCodeLookup;
import java.util.Optional;

public interface PostalCodeClient {
    Optional<PostalCodeLookup> lookup(String countryCode, String postalCode);
}
