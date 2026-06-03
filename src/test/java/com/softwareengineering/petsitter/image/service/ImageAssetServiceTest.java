package com.softwareengineering.petsitter.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.image.domain.ImageAsset;
import com.softwareengineering.petsitter.image.domain.ImageVariant;
import com.softwareengineering.petsitter.image.repository.ImageAssetRepository;
import com.softwareengineering.petsitter.image.repository.ImageAssetVariantRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.user.domain.User;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ImageAssetServiceTest {

    private ImageAssetRepository assetRepository;
    private PetRepository petRepository;
    private AuthenticatedUser authenticatedUser;
    private ImageAssetService service;
    private User currentUser;

    @BeforeEach
    void setUp() {
        assetRepository = mock(ImageAssetRepository.class);
        petRepository = mock(PetRepository.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        currentUser = user(UUID.randomUUID());
        when(authenticatedUser.get()).thenReturn(Optional.of(currentUser));
        when(assetRepository.save(any(ImageAsset.class))).thenAnswer(invocation -> {
            ImageAsset asset = invocation.getArgument(0);
            asset.setId(UUID.randomUUID());
            return asset;
        });
        service = new ImageAssetService(assetRepository, mock(ImageAssetVariantRepository.class),
                petRepository, authenticatedUser);
    }

    @Test
    void storesOptimizedJpegVariantsForPngUpload() throws Exception {
        service.replaceCurrentUserProfileImage(image("png", 600, 400, true));

        ArgumentCaptor<ImageAsset> captor = ArgumentCaptor.forClass(ImageAsset.class);
        verify(assetRepository).save(captor.capture());
        ImageAsset asset = captor.getValue();

        assertThat(asset.getUser()).isEqualTo(currentUser);
        assertThat(asset.getVariants()).extracting(variant -> variant.getVariant())
                .containsExactly(ImageVariant.AVATAR, ImageVariant.DISPLAY);
        assertThat(asset.getVariants()).allSatisfy(variant -> {
            assertThat(variant.getMimeType()).isEqualTo("image/jpeg");
            assertThat(variant.getFileSize()).isEqualTo(variant.getContent().length);
        });
        assertThat(asset.getVariants().get(0).getWidth()).isEqualTo(256);
        assertThat(asset.getVariants().get(1).getWidth()).isEqualTo(768);
        assertThat(ImageIO.read(new ByteArrayInputStream(asset.getVariants().get(0).getContent())).getWidth())
                .isEqualTo(256);
    }

    @Test
    void rejectsUnsupportedMimeTypeOversizedAndInvalidContent() throws Exception {
        byte[] valid = image("jpg", 40, 40, false);

        assertThatThrownBy(() -> service.validateUpload(valid, "image/gif"))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> service.validateUpload(valid, "image/png"))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> service.validateUpload(new byte[ImageAssetService.MAX_UPLOAD_BYTES + 1], "image/png"))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> service.validateUpload(new byte[] {1, 2, 3}, "image/png"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void replacingProfileImageDeletesOldAssetBeforeSavingNewOne() throws Exception {
        ImageAsset previous = new ImageAsset();
        when(assetRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(previous));

        service.replaceCurrentUserProfileImage(image("jpg", 100, 100, false));

        verify(assetRepository).delete(previous);
        verify(assetRepository).flush();
    }

    @Test
    void refusesImageUpdateForPetOwnedByAnotherUser() throws Exception {
        Pet pet = new Pet();
        pet.setId(UUID.randomUUID());
        pet.setOwner(user(UUID.randomUUID()));
        when(petRepository.findById(pet.getId())).thenReturn(Optional.of(pet));

        assertThatThrownBy(() -> service.replaceCurrentUserPetImage(pet.getId(), image("jpg", 100, 100, false)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private User user(UUID id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private byte[] image(String format, int width, int height, boolean alpha) throws Exception {
        BufferedImage image = new BufferedImage(width, height,
                alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(new Color(120, 80, 40, alpha ? 120 : 255));
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }
}
