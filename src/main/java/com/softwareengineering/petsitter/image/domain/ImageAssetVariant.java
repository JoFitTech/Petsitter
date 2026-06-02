package com.softwareengineering.petsitter.image.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "image_asset_variant")
@IdClass(ImageAssetVariant.Key.class)
public class ImageAssetVariant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private ImageAsset asset;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ImageVariant variant;

    @Column(name = "mime_type", nullable = false, length = 64)
    private String mimeType;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] content;

    public ImageAsset getAsset() {
        return asset;
    }

    public void setAsset(ImageAsset asset) {
        this.asset = asset;
    }

    public ImageVariant getVariant() {
        return variant;
    }

    public void setVariant(ImageVariant variant) {
        this.variant = variant;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static class Key implements Serializable {
        private UUID asset;
        private ImageVariant variant;

        public Key() {
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(asset, key.asset) && variant == key.variant;
        }

        @Override
        public int hashCode() {
            return Objects.hash(asset, variant);
        }
    }
}
