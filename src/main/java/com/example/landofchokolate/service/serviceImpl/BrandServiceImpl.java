package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.brend.BrandFilterDto;
import com.example.landofchokolate.dto.brend.BrandProjection;
import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.brend.CreateBrandDto;
import com.example.landofchokolate.mapper.BrandMapper;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.repository.BrandRepository;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.util.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final StorageService storageService;

    @Override
    public BrandResponseDto createBrand(CreateBrandDto createBrandDto) {
        log.info("Creating brand with name: {}", createBrandDto.getName());

        Brand brand = brandMapper.toEntity(createBrandDto);

        // Обработка загрузки изображения
        handleImageUpload(createBrandDto.getImage(), brand);

        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully with id: {}", savedBrand.getId());
        return brandMapper.toResponseDto(savedBrand);
    }

    @Override
    public BrandResponseDto updateBrand(Long id, CreateBrandDto updateBrandDto) {
        log.info("Updating brand with id: {}", id);

        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        // Сохраняем старые данные изображения для возможного удаления
        String oldImageId = existingBrand.getImageId();

        // Обновляем основные поля
        brandMapper.updateEntityFromDto(updateBrandDto, existingBrand);

        // Обработка изображения (если загружен новый файл)
        if (updateBrandDto.getImage() != null && !updateBrandDto.getImage().isEmpty()) {
            // Удаляем старое изображение, если оно было
            if (oldImageId != null && !oldImageId.isEmpty()) {
                deleteImageSafely(oldImageId);
            }

            // Загружаем новое изображение
            handleImageUpload(updateBrandDto.getImage(), existingBrand);
        }

        Brand savedBrand = brandRepository.save(existingBrand);

        log.info("Brand updated successfully with id: {}", savedBrand.getId());
        return brandMapper.toResponseDto(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandProjection> getBrandsForFilters() {
        log.info("Getting brands for filter");
        return brandRepository.findAllByOrderByNameAsc();
    }

    @Override
    public void deleteBrand(Long id) {
        log.info("Deleting brand with id: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        // Удаляем изображение из хранилища, если оно есть
        if (brand.getImageId() != null && !brand.getImageId().isEmpty()) {
            deleteImageSafely(brand.getImageId());
        }

        brandRepository.deleteById(id);
        log.info("Brand deleted successfully with id: {}", id);
    }

    @Override
    public BrandResponseDto getBrandById(Long id) {
        log.info("Getting brand with id: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        return brandMapper.toResponseDto(brand);
    }

    @Override
    public List<BrandResponseDto> getAllBrands() {
        log.info("Getting all brands");

        List<Brand> brands = brandRepository.findAll();
        log.info("Found {} brands", brands.size());

        return brandMapper.toResponseDtoList(brands);
    }

    /**
     * Приватный метод для обработки загрузки изображения
     */
    private void handleImageUpload(MultipartFile imageFile, Brand brand) {
        if (imageFile == null || imageFile.isEmpty()) {
            log.debug("No image file provided for brand");
            return;
        }

        try {
            log.info("Uploading brand image to storage: {}", imageFile.getOriginalFilename());

            // Валидация файла
            validateImageFile(imageFile);

            // Загружаем изображение через StorageService
            StorageService.StorageResult uploadResult = storageService.uploadImage(imageFile);

            // Устанавливаем URL и ID изображения в entity
            brand.setImageUrl(uploadResult.getUrl());
            brand.setImageId(uploadResult.getImageId());

            log.info("Image uploaded successfully. URL: {}, ID: {}",
                    uploadResult.getUrl(), uploadResult.getImageId());

        } catch (IOException e) {
            log.error("Failed to upload image for brand: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при загрузке изображения: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during image upload: {}", e.getMessage(), e);
            throw new RuntimeException("Неожиданная ошибка при загрузке изображения: " + e.getMessage(), e);
        }
    }

    /**
     * Приватный метод для безопасного удаления изображения
     */
    private void deleteImageSafely(String imageId) {
        if (imageId == null || imageId.isEmpty()) {
            return;
        }

        try {
            log.info("Deleting image with ID: {}", imageId);
            boolean deleted = storageService.deleteImage(imageId);

            if (deleted) {
                log.info("Image deleted successfully: {}", imageId);
            } else {
                log.warn("Failed to delete image: {}", imageId);
            }
        } catch (Exception e) {
            log.error("Error deleting image with ID: {}", imageId, e);

        }
    }

    /**
     * Приватный метод для валидации файла изображения
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        // Проверка размера файла (например, максимум 5MB)
        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSizeInBytes) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

        // Проверка расширения файла
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        String lowerFilename = originalFilename.toLowerCase();
        if (!lowerFilename.endsWith(".jpg") &&
                !lowerFilename.endsWith(".jpeg") &&
                !lowerFilename.endsWith(".png") &&
                !lowerFilename.endsWith(".gif")) {
            throw new IllegalArgumentException("Поддерживаются только файлы JPG, PNG и GIF");
        }

        log.debug("Image file validation passed: {}", originalFilename);
    }
}