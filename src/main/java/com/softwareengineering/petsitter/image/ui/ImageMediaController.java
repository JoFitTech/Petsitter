package com.softwareengineering.petsitter.image.ui;

import com.softwareengineering.petsitter.image.domain.ImageVariant;
import com.softwareengineering.petsitter.image.service.ImageAssetService;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media/images")
public class ImageMediaController {

    private final ImageAssetService imageAssetService;

    public ImageMediaController(ImageAssetService imageAssetService) {
        this.imageAssetService = imageAssetService;
    }

    @GetMapping("/{assetId}/{variant}")
    public ResponseEntity<byte[]> loadImage(
            @PathVariable UUID assetId,
            @PathVariable String variant) {
        ImageVariant requestedVariant;
        try {
            requestedVariant = ImageVariant.valueOf(variant.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }

        return imageAssetService.findVariant(assetId, requestedVariant)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.mimeType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(image.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
