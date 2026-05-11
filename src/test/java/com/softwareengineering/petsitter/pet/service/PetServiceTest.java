package com.softwareengineering.petsitter.pet.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PetServiceTest {

    @Test
    void getPetsForOwnerLoadsPetsFromRepository() {
        UUID ownerId = UUID.randomUUID();
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(pet(PetSpecies.DOG)));

        List<Pet> result = new PetService(petRepository.repository()).getPetsForOwner(ownerId);

        assertThat(result).hasSize(1);
        assertThat(petRepository.requestedOwnerId).hasValue(ownerId);
    }

    @Test
    void getPetSummaryForOwnerFormatsSpeciesCounts() {
        UUID ownerId = UUID.randomUUID();
        PetRepositoryFake petRepository = new PetRepositoryFake(List.of(
                pet(PetSpecies.DOG),
                pet(PetSpecies.DOG),
                pet(PetSpecies.CAT),
                pet(PetSpecies.RABBIT)
        ));

        String summary = new PetService(petRepository.repository()).getPetSummaryForOwner(ownerId);

        assertThat(summary).isEqualTo("2 Hunde, 1 Katze, 1 Kaninchen");
    }

    @Test
    void getPetSummaryForOwnerHandlesEmptyPets() {
        String summary = new PetService(new PetRepositoryFake(List.of()).repository())
                .getPetSummaryForOwner(UUID.randomUUID());

        assertThat(summary).isEqualTo("Keine Haustiere");
    }

    private Pet pet(PetSpecies species) {
        Pet pet = new Pet();
        pet.setSpecies(species);
        return pet;
    }

    private static class PetRepositoryFake {
        private final List<Pet> pets;
        private final AtomicReference<UUID> requestedOwnerId = new AtomicReference<>();

        PetRepositoryFake(List<Pet> pets) {
            this.pets = pets;
        }

        PetRepository repository() {
            return (PetRepository) Proxy.newProxyInstance(
                    PetRepository.class.getClassLoader(),
                    new Class<?>[] {PetRepository.class},
                    (proxy, method, args) -> {
                        if ("findAllByOwnerId".equals(method.getName())) {
                            requestedOwnerId.set((UUID) args[0]);
                            return pets;
                        }
                        if ("toString".equals(method.getName())) {
                            return "PetRepositoryFake";
                        }
                        throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
                    }
            );
        }
    }
}
