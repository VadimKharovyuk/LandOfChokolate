package com.example.landofchokolate.service.serviceImpl;
import com.example.landofchokolate.dto.brend.*;
import com.example.landofchokolate.mapper.BrandMapper;
import com.example.landofchokolate.mapper.ProductMapper;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.repository.BrandRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.SlugService;
import com.example.landofchokolate.util.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(
        cacheManager = "brandCacheManager",  // Указываем конкретный CacheManager
        cacheNames = {"brandById", "brandBySlug", "allBrands", "brandFilters", "brandProducts", "brandLimit"}
)
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final StorageService storageService;
    private final SlugService slugService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Caching(
            put = {
                    @CachePut(value = "brandById", key = "#result.id"),
                    @CachePut(value = "brandBySlug", key = "#result.slug")
            },
            evict = {
                    @CacheEvict(value = "allBrands", allEntries = true),
                    @CacheEvict(value = "brandFilters", allEntries = true),
                    @CacheEvict(value = "brandLimit", allEntries = true)
            }
    )
    public BrandResponseDto createBrand(CreateBrandDto createBrandDto) {
        log.info("Creating new brand: {}", createBrandDto.getName());

        Brand brand = brandMapper.toEntity(createBrandDto);

        // Обработка загрузки изображения
        handleImageUpload(createBrandDto.getImage(), brand);

        String slug = slugService.generateUniqueSlugForBrand(createBrandDto.getName());
        brand.setSlug(slug);

        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully with ID: {} and slug: {}", savedBrand.getId(), savedBrand.getSlug());
        return brandMapper.toResponseDto(savedBrand);
    }

    @Override
    @Caching(
            put = {
                    @CachePut(value = "brandById", key = "#id"),
                    @CachePut(value = "brandBySlug", key = "#result.slug")
            },
            evict = {
                    @CacheEvict(value = "allBrands", allEntries = true),
                    @CacheEvict(value = "brandFilters", allEntries = true),
                    @CacheEvict(value = "brandLimit", allEntries = true),
                    @CacheEvict(value = "brandProducts", allEntries = true) // Продукты тоже могут измениться
            }
    )
    public BrandResponseDto updateBrand(Long id, CreateBrandDto updateBrandDto) {
        log.info("Updating brand with ID: {}", id);

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
        log.info("Brand updated successfully: {}", savedBrand.getId());
        return brandMapper.toResponseDto(savedBrand);
    }

    @Override
    @Cacheable(value = "brandFilters", key = "'all'")
    @Transactional(readOnly = true)
    public List<BrandProjection> getBrandsForFilters() {
        log.info("Fetching brands for filters from database");
        return brandRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "brandById", key = "#id"),
            @CacheEvict(value = "brandBySlug", allEntries = true), // Не знаем slug заранее
            @CacheEvict(value = "allBrands", allEntries = true),
            @CacheEvict(value = "brandFilters", allEntries = true),
            @CacheEvict(value = "brandLimit", allEntries = true),
            @CacheEvict(value = "brandProducts", allEntries = true)
    })
    public void deleteBrand(Long id) {
        log.info("Deleting brand with ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        // Удаляем изображение из хранилища, если оно есть
        if (brand.getImageId() != null && !brand.getImageId().isEmpty()) {
            deleteImageSafely(brand.getImageId());
        }

        brandRepository.deleteById(id);
        log.info("Brand deleted successfully: {}", id);
    }

    @Override
    @Cacheable(value = "allBrands", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public BrandPageResponseDto getBrandsForClient(Pageable pageable) {
        log.info("Fetching brands for client: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Brand> brandPage = brandRepository.findAll(pageable);

        List<BrandClientDto> brandClientDtos = brandMapper.toClientDtoList(brandPage.getContent());

        return new BrandPageResponseDto(
                brandClientDtos,
                brandPage.getNumber(),
                brandPage.getTotalPages(),
                brandPage.getTotalElements(),
                brandPage.getSize(),
                brandPage.hasNext(),
                brandPage.hasPrevious()
        );
    }

    @Override
    @Cacheable(value = "brandBySlug", key = "#slug")
    public BrandClientDto getBrandBySlug(String slug) {
        log.info("Fetching brand by slug: {}", slug);

        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Brand not found with slug: " + slug));

        return brandMapper.toClientDto(brand);
    }

    @Override
    @Cacheable(value = "brandLimit", key = "'limit_' + #limit")
    public List<BrandClientDto> getBrandByLimit(int limit) {
        log.info("Fetching brands with limit: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        Page<Brand> brandPage = brandRepository.findAll(pageable);

        return brandMapper.toClientDtoList(brandPage.getContent());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"brandBySlug", "allBrands", "brandFilters", "brandLimit"}, allEntries = true)
    public void generateMissingSlugForBrands() {
        log.info("Generating missing slugs for brands");

        List<Brand> brandsWithoutSlug = brandRepository.findBySlugIsNull();
        log.info("Found {} brands without slug", brandsWithoutSlug.size());

        for (Brand brand : brandsWithoutSlug) {
            String slug = slugService.generateUniqueSlugForBrand(brand.getName());
            brand.setSlug(slug);
            brandRepository.save(brand);
            log.debug("Generated slug '{}' for brand '{}'", slug, brand.getName());
        }

        log.info("Slug generation completed for {} brands", brandsWithoutSlug.size());
    }

    @Override
    @Cacheable(value = "brandProducts",
            key = "#slug + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public BrandProductsPageResponseDto getBrandDetailBySlug(String slug, Pageable pageable) {
        log.info("Fetching brand products by slug: {}, page: {}", slug, pageable.getPageNumber());

        Page<Product> productPage = productRepository.findByBrandSlugAndIsActiveTrue(slug, pageable);

        List<ProductBrandClientDto> productDtos = productMapper.toProductBrandClientDtoList(productPage.getContent());

        return new BrandProductsPageResponseDto(
                productDtos,
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getSize(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    @Override
    @Cacheable(value = "brandById", key = "#id")
    public BrandResponseDto getBrandById(Long id) {
        log.info("Fetching brand by ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        return brandMapper.toResponseDto(brand);
    }

    @Override
    @Cacheable(value = "allBrands", key = "'all_brands'")
    public List<BrandResponseDto> getAllBrands() {
        log.info("Fetching all brands from database");

        List<Brand> brands = brandRepository.findAll();
        return brandMapper.toResponseDtoList(brands);
    }


    private void handleImageUpload(MultipartFile imageFile, Brand brand) {
        if (imageFile == null || imageFile.isEmpty()) {
            log.debug("No image file provided for brand");
            return;
        }

        try {
            log.info("Uploading brand image to storage: {}", imageFile.getOriginalFilename());

            validateImageFile(imageFile);
            StorageService.StorageResult uploadResult = storageService.uploadImage(imageFile);

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

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSizeInBytes) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

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