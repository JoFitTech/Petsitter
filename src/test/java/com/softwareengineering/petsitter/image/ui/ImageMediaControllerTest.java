package com.softwareengineering.petsitter.image.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.softwareengineering.petsitter.image.domain.ImageVariant;
import com.softwareengineering.petsitter.image.dto.StoredImageVariantDto;
import com.softwareengineering.petsitter.image.service.ImageAssetService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImageMediaControllerTest {

    @Test
    void returnsPublicImmutableImageResponseWithNosniffHeader() {
        UUID assetId = UUID.randomUUID();
        ImageAssetService service = mock(ImageAssetService.class);
        when(service.findVariant(assetId, ImageVariant.AVATAR))
                .thenReturn(Optional.of(new StoredImageVariantDto("image/jpeg", new byte[] {1, 2, 3})));

        var response = new ImageMediaController(service).loadImage(assetId, "avatar");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/jpeg");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeaders().getCacheControl()).contains("public").contains("immutable");
    }

    @Test
    void returnsNotFoundForUnknownAssetOrVariant() {
        UUID assetId = UUID.randomUUID();
        ImageAssetService service = mock(ImageAssetService.class);
        when(service.findVariant(assetId, ImageVariant.DISPLAY)).thenReturn(Optional.empty());
        ImageMediaController controller = new ImageMediaController(service);

        assertThat(controller.loadImage(assetId, "display").getStatusCode().value()).isEqualTo(404);
        assertThat(controller.loadImage(assetId, "thumbnail").getStatusCode().value()).isEqualTo(404);
    }
}
