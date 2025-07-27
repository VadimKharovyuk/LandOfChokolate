//package com.example.landofchokolate.service.serviceImpl;
//
//import com.example.landofchokolate.dto.product.*;
//import com.example.landofchokolate.exception.ProductNotFoundException;
//import com.example.landofchokolate.mapper.ProductMapper;
//import com.example.landofchokolate.model.Brand;
//import com.example.landofchokolate.model.Category;
//import com.example.landofchokolate.model.Product;
//import com.example.landofchokolate.repository.BrandRepository;
//import com.example.landofchokolate.repository.CategoryRepository;
//import com.example.landofchokolate.repository.ProductRepository;
//import com.example.landofchokolate.service.ProductService;
//import com.example.landofchokolate.service.SlugService;
//import com.example.landofchokolate.util.StorageService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class ProductServiceImpl implements ProductService {
//
//    private final ProductRepository productRepository;
//    private final CategoryRepository categoryRepository;
//    private final BrandRepository brandRepository;
//    private final ProductMapper productMapper;
//    private final StorageService storageService;
//    private final SlugService slugService;
//
//    @Override
//    public ProductResponseDto createProduct(CreateProductDto createProductDto) {
//        // Получаем категорию и бренд
//        Category category = categoryRepository.findById(createProductDto.getCategoryId())
//                .orElseThrow(() -> new RuntimeException("Category not found with id: " + createProductDto.getCategoryId()));
//
//        Brand brand = brandRepository.findById(createProductDto.getBrandId())
//                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + createProductDto.getBrandId()));
//
//        // Преобразуем DTO в Entity
//        Product product = productMapper.toEntity(createProductDto);
//
//        // Устанавливаем связи
//        productMapper.setRelations(product, category, brand);
//
//        // Генерируем и устанавливаем уникальный slug
//        String slug = slugService.generateUniqueSlugForProduct(product.getName());
//        product.setSlug(slug);
//
//        // Обработка изображения
//        handleImageUpload(createProductDto.getImage(), product);
//
//        // Сохраняем продукт
//        Product savedProduct = productRepository.save(product);
//
//        return productMapper.toResponseDto(savedProduct);
//    }
//
//    @Override
//    public ProductResponseDto updateProduct(Long id, UpdateProductDto updateProductDto) {
//
//        Product existingProduct = productRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
//
//        // Получаем новые категорию и бренд
//        Category category = categoryRepository.findById(updateProductDto.getCategoryId())
//                .orElseThrow(() -> new RuntimeException("Category not found with id: " + updateProductDto.getCategoryId()));
//
//        Brand brand = brandRepository.findById(updateProductDto.getBrandId())
//                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + updateProductDto.getBrandId()));
//
//        // Сохраняем старые данные изображения
//        String oldImageId = existingProduct.getImageId();
//
//        // Обновляем основные поля
//        String oldName = existingProduct.getName();
//        existingProduct.setName(updateProductDto.getName());
//        existingProduct.setPrice(updateProductDto.getPrice());
//        existingProduct.setStockQuantity(updateProductDto.getStockQuantity());
//        existingProduct.setIsRecommendation(updateProductDto.getIsRecommendation());
//
//        // Обновляем slug если название изменилось
//        if (!oldName.equals(updateProductDto.getName())) {
//            String newSlug = slugService.generateUniqueSlugForProduct(updateProductDto.getName());
//            existingProduct.setSlug(newSlug);
//        }
//
//        // Устанавливаем новые связи
//        productMapper.setRelations(existingProduct, category, brand);
//
//        // Обработка изображения
//        if (updateProductDto.getRemoveCurrentImage() != null && updateProductDto.getRemoveCurrentImage()) {
//            // Удаляем текущее изображение
//            if (oldImageId != null && !oldImageId.isEmpty()) {
//                deleteImageSafely(oldImageId);
//            }
//            existingProduct.setImageUrl(null);
//            existingProduct.setImageId(null);
//        } else if (updateProductDto.getImage() != null && !updateProductDto.getImage().isEmpty()) {
//            // Загружаем новое изображение
//            if (oldImageId != null && !oldImageId.isEmpty()) {
//                deleteImageSafely(oldImageId);
//            }
//            handleImageUpload(updateProductDto.getImage(), existingProduct);
//        }
//
//        Product savedProduct = productRepository.save(existingProduct);
//
//        return productMapper.toResponseDto(savedProduct);
//    }
//
//    @Override
//    public void deleteProduct(Long id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
//
//        // Удаляем изображение из хранилища
//        if (product.getImageId() != null && !product.getImageId().isEmpty()) {
//            deleteImageSafely(product.getImageId());
//        }
//
//        productRepository.deleteById(id);
//
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public ProductResponseDto getProductById(Long id) {
//
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
//
//        return productMapper.toResponseDto(product);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getAllProducts() {
//
//        List<Product> products = productRepository.findAll();
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> searchProductsByName(String name) {
//
//        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getProductsByCategory(Long categoryId) {
//        List<Product> products = productRepository.findByCategoryId(categoryId);
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getProductsByBrand(Long brandId) {
//
//        List<Product> products = productRepository.findByBrandId(brandId);
//        return productMapper.toListDtoList(products);
//    }
//
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
//
//        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
//
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getProductsInStock() {
//        List<Product> products = productRepository.findByStockQuantityGreaterThan(0);
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getProductsWithLowStock() {
//        List<Product> products = productRepository.findByStockQuantityBetween(1, 9);
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListDto> getOutOfStockProducts() {
//        List<Product> products = productRepository.findByStockQuantity(0);
//        return productMapper.toListDtoList(products);
//    }
//
//    @Override
//    public ProductResponseDto updateStock(Long productId, Integer newQuantity) {
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
//
//        if (newQuantity < 0) {
//            throw new IllegalArgumentException("Stock quantity cannot be negative");
//        }
//
//        product.setStockQuantity(newQuantity);
//        Product savedProduct = productRepository.save(product);
//
//        return productMapper.toResponseDto(savedProduct);
//    }
//
//    @Override
//    public ProductResponseDto increaseStock(Long productId, Integer quantity) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
//
//        if (quantity <= 0) {
//            throw new IllegalArgumentException("Increase quantity must be positive");
//        }
//
//        product.setStockQuantity(product.getStockQuantity() + quantity);
//        Product savedProduct = productRepository.save(product);
//
//        return productMapper.toResponseDto(savedProduct);
//    }
//
//    @Override
//    public ProductResponseDto decreaseStock(Long productId, Integer quantity) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
//
//        if (quantity <= 0) {
//            throw new IllegalArgumentException("Decrease quantity must be positive");
//        }
//
//        int newQuantity = product.getStockQuantity() - quantity;
//        if (newQuantity < 0) {
//            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQuantity());
//        }
//
//        product.setStockQuantity(newQuantity);
//        Product savedProduct = productRepository.save(product);
//
//        return productMapper.toResponseDto(savedProduct);
//    }
//
//
//    @Override
//    @Transactional(readOnly = true)
//    public ProductStatistics getProductStatistics() {
//
//        long totalProducts = productRepository.count();
//        long inStockProducts = productRepository.countByStockQuantityGreaterThan(0);
//        long outOfStockProducts = productRepository.countByStockQuantity(0);
//        long lowStockProducts = productRepository.countByStockQuantityBetween(1, 9);
//
//        BigDecimal totalInventoryValue = productRepository.calculateTotalInventoryValue();
//        if (totalInventoryValue == null) {
//            totalInventoryValue = BigDecimal.ZERO;
//        }
//
//        return new ProductStatistics(totalProducts, inStockProducts,
//                outOfStockProducts, lowStockProducts, totalInventoryValue);
//    }
//
//    @Override
//    public Page<Product> getProductsByCategoryPage(Long id, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
//        return productRepository.findByCategoryIdAndIsActiveTrue(id, pageable);
//    }
//
//    @Override
//    public ProductDetailDto getProductBySlug(String slug) {
//        Product product = productRepository.findBySlug(slug)
//                .orElseThrow(() -> new ProductNotFoundException("Product not found with slug: " + slug));
//        // Увеличиваем счетчик кликов при просмотре продукта
//        incrementClickCount(product.getId());
//        return productMapper.toDetailDto(product);
//    }
//
//    /**
//     * Приватный метод для увеличения счетчика кликов продукта
//     */
//    private void incrementClickCount(Long productId) {
//        try {
//            productRepository.incrementClickCount(productId);
//            log.debug("Incremented click count for product with id: {}", productId);
//        } catch (Exception e) {
//            log.error("Failed to increment click count for product id: {}", productId, e);
//
//        }
//    }
//
//
//    /**
//     * Генерирует slug для всех продуктов где он null
//     */
//    @Override
//    @Transactional
//    public void generateMissingSlugForAllProducts() {
//        List<Product> productsWithoutSlug = productRepository.findBySlugIsNull();
//
//        for (Product product : productsWithoutSlug) {
//            String slug = slugService.generateUniqueSlugForProduct(product.getName());
//            product.setSlug(slug);
//            productRepository.save(product);
//
//        }
//    }
//
//    @Override
//    public List<RelatedProductDto> getRelatedProducts(String slug, int limit) {
//
//        try {
//            // Находим текущий товар
//            Product currentProduct = productRepository.findBySlug(slug)
//                    .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
//
//            // Проверяем есть ли категория у товара
//            if (currentProduct.getCategory() == null) {
//                log.warn("Product {} has no category, returning empty related products list", slug);
//                return List.of();
//            }
//
//            // Находим похожие товары из той же категории (исключая текущий товар)
//            // Используем простой подход через findAll с фильтрацией
//            List<Product> allCategoryProducts = productRepository.findByCategoryAndIsActiveTrueOrderByIdDesc(
//                    currentProduct.getCategory()
//            );
//
//            List<Product> relatedProducts = allCategoryProducts.stream()
//                    .filter(p -> !p.getId().equals(currentProduct.getId())) // Исключаем текущий товар
//                    .filter(Product::getIsActive) // Только активные товары
//                    .limit(limit) // Ограничиваем количество
//                    .collect(Collectors.toList());
//
//            return productMapper.toRelatedDtoList(relatedProducts);
//
//        } catch (Exception e) {
//            log.error("Error getting related products for slug: {}", slug, e);
//            return List.of(); // Возвращаем пустой список в случае ошибки
//        }
//    }
//
//    @Override
//    public List<ProductListRecommendationDto> getProductListRecommendations( int limit) {
//        return productRepository.findAllRecommendationProducts()
//                .stream()
//                .limit(limit)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public PagedResponse<ProductListClickDto> getProductsClick(Pageable pageable) {
//        // Сортировка по количеству кликов (по убыванию)
//        Pageable sortedPageable = PageRequest.of(
//                pageable.getPageNumber(),
//                pageable.getPageSize(),
//                Sort.by(Sort.Direction.DESC, "clickCount")
//        );
//
//        Page<Product> productPage = productRepository.findAll(sortedPageable);
//
//        List<ProductListClickDto> productDtos = productPage.getContent()
//                .stream()
//                .map(productMapper::toListDtoClick)
//                .collect(Collectors.toList());
//
//        return new PagedResponse<>(productDtos, productPage);
//    }
//
//
//    /**
//     * Приватный метод для обработки загрузки изображения
//     */
//    private void handleImageUpload(MultipartFile imageFile, Product product) {
//        if (imageFile == null || imageFile.isEmpty()) {
//            log.debug("No image file provided for product");
//            return;
//        }
//
//        try {
//            log.info("Uploading product image to storage: {}", imageFile.getOriginalFilename());
//
//            // Валидация файла
//            validateImageFile(imageFile);
//
//            // Загружаем изображение через StorageService
//            StorageService.StorageResult uploadResult = storageService.uploadImage(imageFile);
//
//            // Устанавливаем URL и ID изображения в entity
//            product.setImageUrl(uploadResult.getUrl());
//            product.setImageId(uploadResult.getImageId());
//
//        } catch (IOException e) {
//            log.error("Failed to upload image for product: {}", e.getMessage(), e);
//            throw new RuntimeException("Ошибка при загрузке изображения: " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("Unexpected error during image upload: {}", e.getMessage(), e);
//            throw new RuntimeException("Неожиданная ошибка при загрузке изображения: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Приватный метод для безопасного удаления изображения
//     */
//    private void deleteImageSafely(String imageId) {
//        if (imageId == null || imageId.isEmpty()) {
//            return;
//        }
//
//        try {
//            boolean deleted = storageService.deleteImage(imageId);
//
//            if (deleted) {
//                log.info("Image deleted successfully: {}", imageId);
//            } else {
//                log.warn("Failed to delete image: {}", imageId);
//            }
//        } catch (Exception e) {
//            log.error("Error deleting image with ID: {}", imageId, e);
//            // Не выбрасываем исключение, чтобы не прерывать основную операцию
//        }
//    }
//
//    /**
//     * Приватный метод для валидации файла изображения
//     */
//    private void validateImageFile(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("Файл изображения не может быть пустым");
//        }
//
//        // Проверка размера файла (максимум 5MB)
//        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB
//        if (file.getSize() > maxSizeInBytes) {
//            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
//        }
//
//        // Проверка типа файла
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new IllegalArgumentException("Файл должен быть изображением");
//        }
//
//        // Проверка расширения файла
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename == null || originalFilename.isEmpty()) {
//            throw new IllegalArgumentException("Имя файла не может быть пустым");
//        }
//
//        String lowerFilename = originalFilename.toLowerCase();
//        if (!lowerFilename.endsWith(".jpg") &&
//                !lowerFilename.endsWith(".jpeg") &&
//                !lowerFilename.endsWith(".png") &&
//                !lowerFilename.endsWith(".gif")) {
//            throw new IllegalArgumentException("Поддерживаются только файлы JPG, PNG и GIF");
//        }
//
//        log.debug("Image file validation passed: {}", originalFilename);
//    }
//}

package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.product.*;
import com.example.landofchokolate.exception.ProductNotFoundException;
import com.example.landofchokolate.mapper.ProductMapper;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.repository.BrandRepository;
import com.example.landofchokolate.repository.CategoryRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.ProductService;
import com.example.landofchokolate.service.SlugService;
import com.example.landofchokolate.util.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@CacheConfig(
        cacheManager = "productCacheManager",
        cacheNames = {"productById", "productBySlug", "allProducts", "filteredProducts",
                "productsByCategory", "productsByBrand", "popularProducts",
                "searchProducts", "relatedProducts", "productStats"}
)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final StorageService storageService;
    private final SlugService slugService;


    @Override
    @Caching(
            put = {
                    @CachePut(value = "productById", key = "#result.id"),
                    @CachePut(value = "productBySlug", key = "#result.slug")
            },
            evict = {
                    @CacheEvict(value = "allProducts", allEntries = true),
                    @CacheEvict(value = "filteredProducts", allEntries = true),
                    @CacheEvict(value = "productsByCategory", allEntries = true),
                    @CacheEvict(value = "productsByBrand", allEntries = true),
                    @CacheEvict(value = "searchProducts", allEntries = true),
                    @CacheEvict(value = "popularProducts", allEntries = true),
                    @CacheEvict(value = "productStats", allEntries = true)
            }
    )
    public ProductResponseDto createProduct(CreateProductDto createProductDto) {
        log.info("Creating new product: {}", createProductDto.getName());

        // Получаем категорию и бренд
        Category category = categoryRepository.findById(createProductDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + createProductDto.getCategoryId()));

        Brand brand = brandRepository.findById(createProductDto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + createProductDto.getBrandId()));

        // Преобразуем DTO в Entity
        Product product = productMapper.toEntity(createProductDto);

        // Устанавливаем связи
        productMapper.setRelations(product, category, brand);

        // Генерируем и устанавливаем уникальный slug
        String slug = slugService.generateUniqueSlugForProduct(product.getName());
        product.setSlug(slug);

        // Обработка изображения
        handleImageUpload(createProductDto.getImage(), product);

        // Сохраняем продукт
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {} and slug: {}", savedProduct.getId(), savedProduct.getSlug());
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Caching(
            put = {
                    @CachePut(value = "productById", key = "#id"),
                    @CachePut(value = "productBySlug", key = "#result.slug")
            },
            evict = {
                    @CacheEvict(value = "allProducts", allEntries = true),
                    @CacheEvict(value = "filteredProducts", allEntries = true),
                    @CacheEvict(value = "productsByCategory", allEntries = true),
                    @CacheEvict(value = "productsByBrand", allEntries = true),
                    @CacheEvict(value = "searchProducts", allEntries = true),
                    @CacheEvict(value = "relatedProducts", allEntries = true),
                    @CacheEvict(value = "productStats", allEntries = true)
            }
    )
    public ProductResponseDto updateProduct(Long id, UpdateProductDto updateProductDto) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Получаем новые категорию и бренд
        Category category = categoryRepository.findById(updateProductDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + updateProductDto.getCategoryId()));

        Brand brand = brandRepository.findById(updateProductDto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + updateProductDto.getBrandId()));

        // Сохраняем старые данные изображения
        String oldImageId = existingProduct.getImageId();

        // Обновляем основные поля
        String oldName = existingProduct.getName();
        existingProduct.setName(updateProductDto.getName());
        existingProduct.setPrice(updateProductDto.getPrice());
        existingProduct.setStockQuantity(updateProductDto.getStockQuantity());
        existingProduct.setIsRecommendation(updateProductDto.getIsRecommendation());

        // Обновляем slug если название изменилось
        if (!oldName.equals(updateProductDto.getName())) {
            String newSlug = slugService.generateUniqueSlugForProduct(updateProductDto.getName());
            existingProduct.setSlug(newSlug);
        }

        // Устанавливаем новые связи
        productMapper.setRelations(existingProduct, category, brand);

        // Обработка изображения
        if (updateProductDto.getRemoveCurrentImage() != null && updateProductDto.getRemoveCurrentImage()) {
            // Удаляем текущее изображение
            if (oldImageId != null && !oldImageId.isEmpty()) {
                deleteImageSafely(oldImageId);
            }
            existingProduct.setImageUrl(null);
            existingProduct.setImageId(null);
        } else if (updateProductDto.getImage() != null && !updateProductDto.getImage().isEmpty()) {
            // Загружаем новое изображение
            if (oldImageId != null && !oldImageId.isEmpty()) {
                deleteImageSafely(oldImageId);
            }
            handleImageUpload(updateProductDto.getImage(), existingProduct);
        }

        Product savedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully: {}", savedProduct.getId());
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productById", key = "#id"),
            @CacheEvict(value = "productBySlug", allEntries = true), // Не знаем slug заранее
            @CacheEvict(value = "allProducts", allEntries = true),
            @CacheEvict(value = "filteredProducts", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productsByBrand", allEntries = true),
            @CacheEvict(value = "searchProducts", allEntries = true),
            @CacheEvict(value = "relatedProducts", allEntries = true),
            @CacheEvict(value = "productStats", allEntries = true)
    })
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Удаляем изображение из хранилища
        if (product.getImageId() != null && !product.getImageId().isEmpty()) {
            deleteImageSafely(product.getImageId());
        }

        productRepository.deleteById(id);

        log.info("Product deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productById", key = "#id")
    public ProductResponseDto getProductById(Long id) {
        log.info("Fetching product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allProducts", key = "'admin_all_products'")
    public List<ProductListDto> getAllProducts() {
        log.info("Fetching all products for admin");

        List<Product> products = productRepository.findAll();
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "searchProducts", key = "'admin_search_' + #name")
    public List<ProductListDto> searchProductsByName(String name) {
        log.info("Searching products by name for admin: {}", name);

        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsByCategory", key = "'admin_category_' + #categoryId")
    public List<ProductListDto> getProductsByCategory(Long categoryId) {
        log.info("Fetching products by category for admin: {}", categoryId);

        List<Product> products = productRepository.findByCategoryId(categoryId);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsByBrand", key = "'admin_brand_' + #brandId")
    public List<ProductListDto> getProductsByBrand(Long brandId) {
        log.info("Fetching products by brand for admin: {}", brandId);

        List<Product> products = productRepository.findByBrandId(brandId);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productStats", key = "'price_range_' + #minPrice?.toString() + '_' + #maxPrice?.toString()")
    public List<ProductListDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching products by price range: {} - {}", minPrice, maxPrice);

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productStats", key = "'in_stock'")
    public List<ProductListDto> getProductsInStock() {
        log.info("Fetching products in stock");

        List<Product> products = productRepository.findByStockQuantityGreaterThan(0);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productStats", key = "'low_stock'")
    public List<ProductListDto> getProductsWithLowStock() {
        log.info("Fetching products with low stock");

        List<Product> products = productRepository.findByStockQuantityBetween(1, 9);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productStats", key = "'out_of_stock'")
    public List<ProductListDto> getOutOfStockProducts() {
        List<Product> products = productRepository.findByStockQuantity(0);
        return productMapper.toListDtoList(products);
    }

    @Override
    @Caching(
            put = @CachePut(value = "productById", key = "#productId"),
            evict = {
                    @CacheEvict(value = "allProducts", allEntries = true),
                    @CacheEvict(value = "productStats", allEntries = true)
            }
    )
    public ProductResponseDto updateStock(Long productId, Integer newQuantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Caching(
            put = @CachePut(value = "productById", key = "#productId"),
            evict = {
                    @CacheEvict(value = "allProducts", allEntries = true),
                    @CacheEvict(value = "productStats", allEntries = true)
            }
    )
    public ProductResponseDto increaseStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Increase quantity must be positive");
        }

        product.setStockQuantity(product.getStockQuantity() + quantity);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Caching(
            put = @CachePut(value = "productById", key = "#productId"),
            evict = {
                    @CacheEvict(value = "allProducts", allEntries = true),
                    @CacheEvict(value = "productStats", allEntries = true)
            }
    )
    public ProductResponseDto decreaseStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Decrease quantity must be positive");
        }

        int newQuantity = product.getStockQuantity() - quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        product.setStockQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productStats", key = "'statistics'")
    public ProductStatistics getProductStatistics() {

        long totalProducts = productRepository.count();
        long inStockProducts = productRepository.countByStockQuantityGreaterThan(0);
        long outOfStockProducts = productRepository.countByStockQuantity(0);
        long lowStockProducts = productRepository.countByStockQuantityBetween(1, 9);

        BigDecimal totalInventoryValue = productRepository.calculateTotalInventoryValue();
        if (totalInventoryValue == null) {
            totalInventoryValue = BigDecimal.ZERO;
        }

        return new ProductStatistics(totalProducts, inStockProducts,
                outOfStockProducts, lowStockProducts, totalInventoryValue);
    }

    @Override
    @Cacheable(value = "productsByCategory",
            key = "#id + '_page_' + #page + '_' + #size")
    public Page<Product> getProductsByCategoryPage(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findByCategoryIdAndIsActiveTrue(id, pageable);
    }

    @Override
    @Cacheable(value = "productBySlug", key = "'v2_' + #slug")
    public ProductDetailDto getProductBySlug(String slug) {

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with slug: " + slug));

        // Увеличиваем счетчик кликов при просмотре продукта
        incrementClickCount(product.getId());

        return productMapper.toDetailDto(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productBySlug", "allProducts", "searchProducts"}, allEntries = true)
    public void generateMissingSlugForAllProducts() {

        List<Product> productsWithoutSlug = productRepository.findBySlugIsNull();

        for (Product product : productsWithoutSlug) {
            String slug = slugService.generateUniqueSlugForProduct(product.getName());
            product.setSlug(slug);
            productRepository.save(product);

        }

    }

    @Override
    @Cacheable(value = "relatedProducts", key = "'v2_' + #slug + '_' + #limit")
    public List<RelatedProductDto> getRelatedProducts(String slug, int limit) {

        try {
            // Находим текущий товар
            Product currentProduct = productRepository.findBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));

            // Проверяем есть ли категория у товара
            if (currentProduct.getCategory() == null) {
                log.warn("Product {} has no category, returning empty related products list", slug);
                return List.of();
            }

            // Находим похожие товары из той же категории (исключая текущий товар)
            List<Product> allCategoryProducts = productRepository.findByCategoryAndIsActiveTrueOrderByIdDesc(
                    currentProduct.getCategory()
            );

            List<Product> relatedProducts = allCategoryProducts.stream()
                    .filter(p -> !p.getId().equals(currentProduct.getId())) // Исключаем текущий товар
                    .filter(Product::getIsActive) // Только активные товары
                    .limit(limit) // Ограничиваем количество
                    .collect(Collectors.toList());

            return productMapper.toRelatedDtoList(relatedProducts);

        } catch (Exception e) {
            log.error("Error getting related products for slug: {}", slug, e);
            return List.of(); // Возвращаем пустой список в случае ошибки
        }
    }

    @Override
    @Cacheable(value = "popularProducts", key = "'recommendations_' + #limit")
    public List<ProductListRecommendationDto> getProductListRecommendations(int limit) {

        return productRepository.findAllRecommendationProducts()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "productStats",
            key = "'clicks_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PagedResponse<ProductListClickDto> getProductsClick(Pageable pageable) {

        // Сортировка по количеству кликов (по убыванию)
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "clickCount")
        );

        Page<Product> productPage = productRepository.findAll(sortedPageable);

        List<ProductListClickDto> productDtos = productPage.getContent()
                .stream()
                .map(productMapper::toListDtoClick)
                .collect(Collectors.toList());

        return new PagedResponse<>(productDtos, productPage);
    }

    /**
     * Приватный метод для увеличения счетчика кликов продукта
     */
    @CacheEvict(value = "productStats", allEntries = true)
    public void incrementClickCount(Long productId) {
        try {
            productRepository.incrementClickCount(productId);
        } catch (Exception e) {
            log.error("Failed to increment click count for product id: {}", productId, e);
        }
    }

    // Приватные методы без изменений

    private void handleImageUpload(MultipartFile imageFile, Product product) {
        if (imageFile == null || imageFile.isEmpty()) {
            log.debug("No image file provided for product");
            return;
        }

        try {
            log.info("Uploading product image to storage: {}", imageFile.getOriginalFilename());

            // Валидация файла
            validateImageFile(imageFile);

            // Загружаем изображение через StorageService
            StorageService.StorageResult uploadResult = storageService.uploadImage(imageFile);

            // Устанавливаем URL и ID изображения в entity
            product.setImageUrl(uploadResult.getUrl());
            product.setImageId(uploadResult.getImageId());

        } catch (IOException e) {
            log.error("Failed to upload image for product: {}", e.getMessage(), e);
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
            boolean deleted = storageService.deleteImage(imageId);

            if (deleted) {
                log.info("Image deleted successfully: {}", imageId);
            } else {
                log.warn("Failed to delete image: {}", imageId);
            }
        } catch (Exception e) {
            log.error("Error deleting image with ID: {}", imageId, e);
            // Не выбрасываем исключение, чтобы не прерывать основную операцию
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        // Проверка размера файла (максимум 5MB)
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