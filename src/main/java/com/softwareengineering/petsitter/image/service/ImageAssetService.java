package com.softwareengineering.petsitter.image.service;

import com.softwareengineering.petsitter.image.domain.ImageAsset;
import com.softwareengineering.petsitter.image.domain.ImageAssetVariant;
import com.softwareengineering.petsitter.image.domain.ImageVariant;
import com.softwareengineering.petsitter.image.dto.ImageRefDto;
import com.softwareengineering.petsitter.image.dto.StoredImageVariantDto;
import com.softwareengineering.petsitter.image.repository.ImageAssetRepository;
import com.softwareengineering.petsitter.image.repository.ImageAssetVariantRepository;
import com.softwareengineering.petsitter.pet.domain.Pet;
import com.softwareengineering.petsitter.pet.repository.PetRepository;
import com.softwareengineering.petsitter.security.AuthenticatedUser;
import com.softwareengineering.petsitter.shared.exception.BusinessRuleViolationException;
import com.softwareengineering.petsitter.shared.exception.ForbiddenOperationException;
import com.softwareengineering.petsitter.shared.exception.NotFoundException;
import com.softwareengineering.petsitter.user.domain.User;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageAssetService {

    public static final int MAX_UPLOAD_BYTES = 5 * 1024 * 1024;
    public static final long MAX_PIXELS = 20_000_000L;
    private static final String JPEG_MIME_TYPE = "image/jpeg";

    private final ImageAssetRepository assetRepository;
    private final ImageAssetVariantRepository variantRepository;
    private final PetRepository petRepository;
    private final AuthenticatedUser authenticatedUser;

    public ImageAssetService(
            ImageAssetRepository assetRepository,
            ImageAssetVariantRepository variantRepository,
            PetRepository petRepository,
            AuthenticatedUser authenticatedUser) {
        this.assetRepository = assetRepository;
        this.variantRepository = variantRepository;
        this.petRepository = petRepository;
        this.authenticatedUser = authenticatedUser;
    }

    public void validateUpload(byte[] content, String declaredMimeType) {
        readSupportedImage(content, declaredMimeType);
    }

    @Transactional
    public ImageRefDto replaceCurrentUserProfileImage(byte[] croppedImage) {
        User user = currentUserOrThrow();
        assetRepository.findByUserId(user.getId()).ifPresent(this::deleteAndFlush);
        return saveForUser(user, croppedImage);
    }

    @Transactional
    public void removeCurrentUserProfileImage() {
        User user = currentUserOrThrow();
        assetRepository.findByUserId(user.getId()).ifPresent(assetRepository::delete);
    }

    @Transactional
    public ImageRefDto replaceCurrentUserPetImage(UUID petId, byte[] croppedImage) {
        Pet pet = currentUserPetOrThrow(petId);
        assetRepository.findByPetId(petId).ifPresent(this::deleteAndFlush);
        return saveForPet(pet, croppedImage);
    }

    @Transactional
    public void removeCurrentUserPetImage(UUID petId) {
        currentUserPetOrThrow(petId);
        assetRepository.findByPetId(petId).ifPresent(assetRepository::delete);
    }

    @Transactional(readOnly = true)
    public Optional<ImageRefDto> findUserImage(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return assetRepository.findByUserId(userId).map(this::toRef);
    }

    @Transactional(readOnly = true)
    public Optional<ImageRefDto> findPetImage(UUID petId) {
        if (petId == null) {
            return Optional.empty();
        }
        return assetRepository.findByPetId(petId).map(this::toRef);
    }

    @Transactional(readOnly = true)
    public Map<UUID, ImageRefDto> findUserImages(Collection<UUID> userIds) {
        Map<UUID, ImageRefDto> images = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return images;
        }
        assetRepository.findAllByUserIdIn(userIds).forEach(asset -> images.put(asset.getUser().getId(), toRef(asset)));
        return images;
    }

    @Transactional(readOnly = true)
    public Map<UUID, ImageRefDto> findPetImages(Collection<UUID> petIds) {
        Map<UUID, ImageRefDto> images = new LinkedHashMap<>();
        if (petIds == null || petIds.isEmpty()) {
            return images;
        }
        assetRepository.findAllByPetIdIn(petIds).forEach(asset -> images.put(asset.getPet().getId(), toRef(asset)));
        return images;
    }

    @Transactional(readOnly = true)
    public Optional<StoredImageVariantDto> findVariant(UUID assetId, ImageVariant variant) {
        if (assetId == null || variant == null) {
            return Optional.empty();
        }
        return variantRepository.findByAssetIdAndVariant(assetId, variant)
                .map(stored -> new StoredImageVariantDto(stored.getMimeType(), stored.getContent()));
    }

    private ImageRefDto saveForUser(User user, byte[] croppedImage) {
        ImageAsset asset = new ImageAsset();
        asset.setUser(user);
        addVariants(asset, croppedImage);
        return toRef(assetRepository.save(asset));
    }

    private ImageRefDto saveForPet(Pet pet, byte[] croppedImage) {
        ImageAsset asset = new ImageAsset();
        asset.setPet(pet);
        addVariants(asset, croppedImage);
        return toRef(assetRepository.save(asset));
    }

    private void addVariants(ImageAsset asset, byte[] croppedImage) {
        BufferedImage source = readSupportedImage(croppedImage, null);
        asset.addVariant(createVariant(ImageVariant.AVATAR, source, 256));
        asset.addVariant(createVariant(ImageVariant.DISPLAY, source, 768));
    }

    private ImageAssetVariant createVariant(ImageVariant type, BufferedImage source, int size) {
        byte[] encoded = encodeJpeg(resizeSquare(source, size), 0.9f);
        ImageAssetVariant variant = new ImageAssetVariant();
        variant.setVariant(type);
        variant.setMimeType(JPEG_MIME_TYPE);
        variant.setWidth(size);
        variant.setHeight(size);
        variant.setFileSize(encoded.length);
        variant.setContent(encoded);
        return variant;
    }

    private BufferedImage readSupportedImage(byte[] content, String declaredMimeType) {
        if (content == null || content.length == 0) {
            throw new BusinessRuleViolationException("Bitte waehle eine Bilddatei aus.");
        }
        if (content.length > MAX_UPLOAD_BYTES) {
            throw new BusinessRuleViolationException("Das Bild darf maximal 5 MB gross sein.");
        }
        if (declaredMimeType != null && !isSupportedMimeType(declaredMimeType)) {
            throw new BusinessRuleViolationException("Nur JPEG- und PNG-Bilder werden unterstuetzt.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) {
                throw invalidImage();
            }
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!"jpeg".equals(format) && !"jpg".equals(format) && !"png".equals(format)) {
                    throw new BusinessRuleViolationException("Nur JPEG- und PNG-Bilder werden unterstuetzt.");
                }
                if (declaredMimeType != null && !matchesDeclaredMimeType(format, declaredMimeType)) {
                    throw new BusinessRuleViolationException("Dateityp und Bildinhalt stimmen nicht ueberein.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) {
                    throw new BusinessRuleViolationException("Das Bild darf maximal 20 Megapixel haben.");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw invalidImage();
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalidImage();
        }
    }

    private BufferedImage resizeSquare(BufferedImage source, int size) {
        int edge = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - edge) / 2;
        int y = (source.getHeight() - edge) / 2;
        BufferedImage target = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, size, size);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, size, size, x, y, x + edge, y + edge, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), params);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new BusinessRuleViolationException("Das Bild konnte nicht verarbeitet werden.");
        } finally {
            writer.dispose();
        }
    }

    private void deleteAndFlush(ImageAsset asset) {
        assetRepository.delete(asset);
        assetRepository.flush();
    }

    private User currentUserOrThrow() {
        return authenticatedUser.get()
                .orElseThrow(() -> new ForbiddenOperationException("Nicht angemeldet."));
    }

    private Pet currentUserPetOrThrow(UUID petId) {
        User currentUser = currentUserOrThrow();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Haustier nicht gefunden."));
        if (!currentUser.getId().equals(pet.getOwner().getId())) {
            throw new ForbiddenOperationException("Dieses Haustier gehoert dir nicht.");
        }
        return pet;
    }

    private ImageRefDto toRef(ImageAsset asset) {
        return new ImageRefDto(asset.getId());
    }

    private boolean isSupportedMimeType(String mimeType) {
        return "image/jpeg".equalsIgnoreCase(mimeType) || "image/png".equalsIgnoreCase(mimeType);
    }

    private boolean matchesDeclaredMimeType(String format, String mimeType) {
        return "png".equals(format)
                ? "image/png".equalsIgnoreCase(mimeType)
                : "image/jpeg".equalsIgnoreCase(mimeType);
    }

    private BusinessRuleViolationException invalidImage() {
        return new BusinessRuleViolationException("Die Datei ist kein gueltiges JPEG- oder PNG-Bild.");
    }
}
