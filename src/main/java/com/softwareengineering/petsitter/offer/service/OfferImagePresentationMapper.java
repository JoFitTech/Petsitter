package com.softwareengineering.petsitter.offer.service;

import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.image.service.ImageAssetService;
import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.offer.domain.OfferAnimalType;
import com.softwareengineering.petsitter.offer.domain.OfferType;
import com.softwareengineering.petsitter.offer.dto.OfferCoverTileDto;
import com.softwareengineering.petsitter.offer.dto.OfferPetOptionDto;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.domain.PetSpecies;
import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.user.domain.User;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OfferImagePresentationMapper {

    private static final Comparator<Pet> PET_ORDER = Comparator
            .comparing(Pet::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(Pet::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final ImageAssetService imageAssetService;

    public OfferImagePresentationMapper(ImageAssetService imageAssetService) {
        this.imageAssetService = imageAssetService;
    }

    public Map<UUID, List<OfferCoverTileDto>> coverTilesByOffer(Collection<Offer> offers) {
        Map<UUID, List<OfferCoverTileDto>> tilesByOffer = new LinkedHashMap<>();
        if (offers == null || offers.isEmpty()) {
            return tilesByOffer;
        }
        PresentationImages images = loadImages(offers);
        offers.stream()
                .filter(Objects::nonNull)
                .forEach(offer -> tilesByOffer.put(offer.getOfferId(), coverTiles(offer, images)));
        return tilesByOffer;
    }

    public List<OfferCoverTileDto> coverTiles(Offer offer) {
        if (offer == null) {
            return List.of();
        }
        return coverTiles(offer, loadImages(List.of(offer)));
    }

    public List<PetDto> petDtos(Collection<Pet> pets) {
        if (pets == null || pets.isEmpty()) {
            return List.of();
        }
        List<Pet> sortedPets = pets.stream().filter(Objects::nonNull).sorted(PET_ORDER).toList();
        Map<UUID, ImageRefDto> images = imageAssetService.findPetImages(sortedPets.stream().map(Pet::getId).toList());
        return sortedPets.stream().map(pet -> PetDto.from(pet, images.get(pet.getId()))).toList();
    }

    public Map<UUID, List<PetDto>> petDtosByOffer(Collection<Offer> offers) {
        Map<UUID, List<PetDto>> petsByOffer = new LinkedHashMap<>();
        if (offers == null || offers.isEmpty()) {
            return petsByOffer;
        }
        List<Pet> pets = offers.stream()
                .filter(Objects::nonNull)
                .flatMap(offer -> sortedOfferPets(offer).stream())
                .distinct()
                .toList();
        Map<UUID, ImageRefDto> images = imageAssetService.findPetImages(pets.stream().map(Pet::getId).toList());
        offers.stream()
                .filter(Objects::nonNull)
                .forEach(offer -> petsByOffer.put(offer.getOfferId(), sortedOfferPets(offer).stream()
                        .map(pet -> PetDto.from(pet, images.get(pet.getId())))
                        .toList()));
        return petsByOffer;
    }

    public OfferPetOptionDto petOption(Pet pet) {
        ImageRefDto image = imageAssetService.findPetImage(pet.getId()).orElse(null);
        return new OfferPetOptionDto(pet.getId(), pet.getName(), pet.getSpecies(), image);
    }

    public ImageRefDto userImage(User user) {
        return user == null ? null : imageAssetService.findUserImage(user.getId()).orElse(null);
    }

    public Map<UUID, ImageRefDto> creatorImagesByOffer(Collection<Offer> offers) {
        Map<UUID, ImageRefDto> imagesByOffer = new LinkedHashMap<>();
        if (offers == null || offers.isEmpty()) {
            return imagesByOffer;
        }
        Map<UUID, ImageRefDto> images = imageAssetService.findUserImages(offers.stream()
                .filter(Objects::nonNull)
                .map(Offer::getCreateUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        offers.stream()
                .filter(Objects::nonNull)
                .filter(offer -> offer.getCreateUser() != null)
                .forEach(offer -> imagesByOffer.put(offer.getOfferId(), images.get(offer.getCreateUser().getId())));
        return imagesByOffer;
    }

    private List<OfferCoverTileDto> coverTiles(Offer offer, PresentationImages images) {
        if (offer.getOfferType() == OfferType.SITTER_OFFER) {
            User sitter = offer.getCreateUser();
            return List.of(new OfferCoverTileDto(
                    sitter == null ? null : images.userImages().get(sitter.getId()),
                    displayName(sitter),
                    offer.getAnimalType()));
        }
        return sortedOfferPets(offer).stream()
                .map(pet -> new OfferCoverTileDto(
                        images.petImages().get(pet.getId()),
                        pet.getName(),
                        animalType(pet.getSpecies())))
                .toList();
    }

    private PresentationImages loadImages(Collection<Offer> offers) {
        LinkedHashSet<UUID> userIds = new LinkedHashSet<>();
        LinkedHashSet<UUID> petIds = new LinkedHashSet<>();
        offers.stream()
                .filter(Objects::nonNull)
                .forEach(offer -> {
                    if (offer.getOfferType() == OfferType.SITTER_OFFER && offer.getCreateUser() != null) {
                        userIds.add(offer.getCreateUser().getId());
                    } else {
                        sortedOfferPets(offer).forEach(pet -> petIds.add(pet.getId()));
                    }
                });
        return new PresentationImages(
                imageAssetService.findUserImages(userIds),
                imageAssetService.findPetImages(petIds));
    }

    private List<Pet> sortedOfferPets(Offer offer) {
        UUID primaryPetId = offer.getPet() == null ? null : offer.getPet().getId();
        return offer.getPets().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((Pet pet) -> !Objects.equals(pet.getId(), primaryPetId))
                        .thenComparing(PET_ORDER))
                .toList();
    }

    private String displayName(User user) {
        if (user == null) {
            return null;
        }
        return user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getFirstName()
                : user.getDisplayName();
    }

    private OfferAnimalType animalType(PetSpecies species) {
        if (species == null) {
            return OfferAnimalType.OTHER;
        }
        return switch (species) {
            case DOG -> OfferAnimalType.DOG;
            case CAT -> OfferAnimalType.CAT;
            case BIRD -> OfferAnimalType.BIRD;
            case FISH -> OfferAnimalType.FISH;
            case REPTILE -> OfferAnimalType.REPTILE;
            case RABBIT -> OfferAnimalType.SMALL_ANIMAL;
            case OTHER -> OfferAnimalType.OTHER;
        };
    }

    private record PresentationImages(
            Map<UUID, ImageRefDto> userImages,
            Map<UUID, ImageRefDto> petImages
    ) {
    }
}
