package com.softwareengineering.petsitter.pet.dto;

import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import java.util.UUID;

public record PetDto(UUID id, String name, PetSpecies species, String breed, Integer age, String notes) {}
