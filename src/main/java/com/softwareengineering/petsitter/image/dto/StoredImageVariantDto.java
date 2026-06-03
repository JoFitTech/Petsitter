package com.softwareengineering.petsitter.image.dto;

public record StoredImageVariantDto(
        String mimeType,
        byte[] content
) {
}
