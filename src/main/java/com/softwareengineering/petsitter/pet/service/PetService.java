package com.softwareengineering.petsitter.pet.service;

import com.softwareengineering.petsitter.pet.domain.Pet;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PetService {

    public List<Pet> getPetsForOwner(UUID ownerId) {
        return Collections.emptyList();
    }

    public List<String> getPets() {
        return Collections.emptyList();
    }
}
