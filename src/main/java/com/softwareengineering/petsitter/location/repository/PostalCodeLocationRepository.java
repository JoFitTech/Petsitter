package com.softwareengineering.petsitter.location.repository;

import com.softwareengineering.petsitter.location.domain.PostalCodeLocation;
import com.softwareengineering.petsitter.location.domain.PostalCodeLocationId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostalCodeLocationRepository extends JpaRepository<PostalCodeLocation, PostalCodeLocationId> {
    Optional<PostalCodeLocation> findByCountryCodeAndPostalCode(String countryCode, String postalCode);

    List<PostalCodeLocation> findAllByCountryCodeAndPostalCodeIn(String countryCode, Collection<String> postalCodes);
}
