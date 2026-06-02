package com.softwareengineering.petsitter.offer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.image.service.ImageAssetService;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.user.domain.User;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OfferImagePresentationMapperTest {

    @Test
    void sitterOfferUsesCreatorProfileImage() {
        UUID userId = UUID.randomUUID();
        ImageRefDto image = new ImageRefDto(UUID.randomUUID());
        FakeImageAssetService images = new FakeImageAssetService(Map.of(userId, image), Map.of());
        User sitter = user(userId, "Ben");
        Offer offer = offer(OfferType.SITTER_OFFER, sitter);

        var tiles = new OfferImagePresentationMapper(images).coverTiles(offer);

        assertThat(tiles).hasSize(1);
        assertThat(tiles.get(0).image()).isEqualTo(image);
        assertThat(tiles.get(0).fallbackLabel()).isEqualTo("Ben");
    }

    @Test
    void ownerOfferKeepsPrimaryPetFirstThenSortsByName() {
        Pet primary = pet(UUID.randomUUID(), "Zora", PetSpecies.DOG);
        Pet alpha = pet(UUID.randomUUID(), "Alma", PetSpecies.CAT);
        Pet beta = pet(UUID.randomUUID(), "Balu", PetSpecies.DOG);
        Offer offer = offer(OfferType.OWNER_OFFER, user(UUID.randomUUID(), "Owner"));
        offer.setPets(java.util.List.of(primary, beta, alpha));

        var tiles = new OfferImagePresentationMapper(new FakeImageAssetService(Map.of(), Map.of()))
                .coverTiles(offer);

        assertThat(tiles).extracting(tile -> tile.fallbackLabel())
                .containsExactly("Zora", "Alma", "Balu");
        assertThat(tiles).extracting(tile -> tile.fallbackType())
                .containsExactly(OfferAnimalType.DOG, OfferAnimalType.CAT, OfferAnimalType.DOG);
    }

    private Offer offer(OfferType type, User creator) {
        Offer offer = new Offer();
        offer.setOfferId(UUID.randomUUID());
        offer.setOfferType(type);
        offer.setCreateUser(creator);
        offer.setAnimalType(OfferAnimalType.DOG);
        return offer;
    }

    private Pet pet(UUID id, String name, PetSpecies species) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setSpecies(species);
        return pet;
    }

    private User user(UUID id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        return user;
    }

    private static final class FakeImageAssetService extends ImageAssetService {
        private final Map<UUID, ImageRefDto> userImages;
        private final Map<UUID, ImageRefDto> petImages;

        private FakeImageAssetService(Map<UUID, ImageRefDto> userImages, Map<UUID, ImageRefDto> petImages) {
            super(null, null, null, null);
            this.userImages = userImages;
            this.petImages = petImages;
        }

        @Override
        public Optional<ImageRefDto> findUserImage(UUID userId) {
            return Optional.ofNullable(userImages.get(userId));
        }

        @Override
        public Optional<ImageRefDto> findPetImage(UUID petId) {
            return Optional.ofNullable(petImages.get(petId));
        }

        @Override
        public Map<UUID, ImageRefDto> findUserImages(Collection<UUID> userIds) {
            return select(userImages, userIds);
        }

        @Override
        public Map<UUID, ImageRefDto> findPetImages(Collection<UUID> petIds) {
            return select(petImages, petIds);
        }

        private Map<UUID, ImageRefDto> select(Map<UUID, ImageRefDto> source, Collection<UUID> ids) {
            Map<UUID, ImageRefDto> selected = new LinkedHashMap<>();
            ids.forEach(id -> {
                if (source.containsKey(id)) {
                    selected.put(id, source.get(id));
                }
            });
            return selected;
        }
    }
}
