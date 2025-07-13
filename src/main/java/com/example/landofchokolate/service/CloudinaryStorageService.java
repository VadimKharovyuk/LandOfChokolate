package com.example.landofchokolate.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.landofchokolate.util.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public StorageResult uploadImage(MultipartFile file) throws IOException {
        try {
            // Параметры загрузки
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "land-of-chocolate/products",
                    "resource_type", "image",
                    "transformation", ObjectUtils.asMap(
                            "width", 800,
                            "height", 600,
                            "crop", "limit",
                            "quality", "auto",
                            "format", "auto"
                    )
            );

            // Загрузка файла
            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), uploadParams);

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            log.info("Image uploaded successfully: {}", publicId);

            return new StorageResult(imageUrl, publicId);

        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteImage(String imageId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            boolean deleted = "ok".equals(resultStatus);

            if (deleted) {
                log.info("Image deleted successfully: {}", imageId);
            } else {
                log.warn("Failed to delete image: {}, result: {}", imageId, resultStatus);
            }

            return deleted;

        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary: {}", imageId, e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getImageInfo(String imageId) {
        try {
            Map<String, Object> result = (Map<String, Object>) cloudinary.api().resource(imageId, ObjectUtils.emptyMap());

            return Map.of(
                    "public_id", result.get("public_id"),
                    "url", result.get("secure_url"),
                    "width", result.get("width"),
                    "height", result.get("height"),
                    "format", result.get("format"),
                    "bytes", result.get("bytes"),
                    "created_at", result.get("created_at")
            );

        } catch (Exception e) {
            log.error("Error getting image info from Cloudinary: {}", imageId, e);
            return Map.of("error", "Image not found or error occurred");
        }
    }
}