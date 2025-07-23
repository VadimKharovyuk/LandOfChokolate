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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final StorageService storageService;
    private final SlugService slugService;

    @Override
    public ProductResponseDto createProduct(CreateProductDto createProductDto) {
        log.info("Creating product with name: {}", createProductDto.getName());

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

        log.info("Product created successfully with id: {} and slug: {}", savedProduct.getId(), savedProduct.getSlug());
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDto updateProduct(Long id, UpdateProductDto updateProductDto) {
        log.info("Updating product with id: {}", id);

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

        // Обновляем slug если название изменилось
        if (!oldName.equals(updateProductDto.getName())) {
            String newSlug = slugService.generateUniqueSlugForProduct(updateProductDto.getName());
            existingProduct.setSlug(newSlug);
            log.info("Updated product slug from '{}' to '{}' due to name change", existingProduct.getSlug(), newSlug);
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

        log.info("Product updated successfully with id: {} and slug: {}", savedProduct.getId(), savedProduct.getSlug());
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Удаляем изображение из хранилища
        if (product.getImageId() != null && !product.getImageId().isEmpty()) {
            deleteImageSafely(product.getImageId());
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        log.info("Getting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getAllProducts() {
        log.info("Getting all products (list view)");

        List<Product> products = productRepository.findAll();
        log.info("Found {} products", products.size());

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> searchProductsByName(String name) {
        log.info("Searching products by name: {}", name);

        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        log.info("Found {} products matching name: {}", products.size(), name);

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getProductsByCategory(Long categoryId) {
        log.info("Getting products by category id: {}", categoryId);

        List<Product> products = productRepository.findByCategoryId(categoryId);
        log.info("Found {} products in category {}", products.size(), categoryId);

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getProductsByBrand(Long brandId) {
        log.info("Getting products by brand id: {}", brandId);

        List<Product> products = productRepository.findByBrandId(brandId);
        log.info("Found {} products for brand {}", products.size(), brandId);

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Getting products by price range: {} - {}", minPrice, maxPrice);

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        log.info("Found {} products in price range", products.size());

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getProductsInStock() {
        log.info("Getting products in stock");

        List<Product> products = productRepository.findByStockQuantityGreaterThan(0);
        log.info("Found {} products in stock", products.size());

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getProductsWithLowStock() {
        log.info("Getting products with low stock");

        List<Product> products = productRepository.findByStockQuantityBetween(1, 9);
        log.info("Found {} products with low stock", products.size());

        return productMapper.toListDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListDto> getOutOfStockProducts() {
        log.info("Getting out of stock products");

        List<Product> products = productRepository.findByStockQuantity(0);
        log.info("Found {} out of stock products", products.size());

        return productMapper.toListDtoList(products);
    }

    @Override
    public ProductResponseDto updateStock(Long productId, Integer newQuantity) {
        log.info("Updating stock for product {} to {}", productId, newQuantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);

        log.info("Stock updated successfully for product {}", productId);
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDto increaseStock(Long productId, Integer quantity) {
        log.info("Increasing stock for product {} by {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Increase quantity must be positive");
        }

        product.setStockQuantity(product.getStockQuantity() + quantity);
        Product savedProduct = productRepository.save(product);

        log.info("Stock increased successfully for product {}", productId);
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDto decreaseStock(Long productId, Integer quantity) {
        log.info("Decreasing stock for product {} by {}", productId, quantity);

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

        log.info("Stock decreased successfully for product {}", productId);
        return productMapper.toResponseDto(savedProduct);
    }


    @Override
    @Transactional(readOnly = true)
    public ProductStatistics getProductStatistics() {
        log.info("Calculating product statistics");

        long totalProducts = productRepository.count();
        long inStockProducts = productRepository.countByStockQuantityGreaterThan(0);
        long outOfStockProducts = productRepository.countByStockQuantity(0);
        long lowStockProducts = productRepository.countByStockQuantityBetween(1, 9);

        BigDecimal totalInventoryValue = productRepository.calculateTotalInventoryValue();
        if (totalInventoryValue == null) {
            totalInventoryValue = BigDecimal.ZERO;
        }

        log.info("Statistics calculated - Total: {}, In Stock: {}, Out of Stock: {}, Low Stock: {}",
                totalProducts, inStockProducts, outOfStockProducts, lowStockProducts);

        return new ProductStatistics(totalProducts, inStockProducts,
                outOfStockProducts, lowStockProducts, totalInventoryValue);
    }

    @Override
    public Page<Product> getProductsByCategoryPage(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findByCategoryIdAndIsActiveTrue(id, pageable);
    }

    @Override
    public ProductDetailDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with slug: " + slug));

        return productMapper.toDetailDto(product);
    }


    /**
     * Генерирует slug для всех продуктов где он null
     */
    @Override
    @Transactional
    public void generateMissingSlugForAllProducts() {
        List<Product> productsWithoutSlug = productRepository.findBySlugIsNull();

        for (Product product : productsWithoutSlug) {
            String slug = slugService.generateUniqueSlugForProduct(product.getName());
            product.setSlug(slug);
            productRepository.save(product);
            log.info("Generated slug '{}' for product id={} name='{}'",
                    slug, product.getId(), product.getName());
        }

        log.info("Generated slugs for {} products", productsWithoutSlug.size());
    }

    @Override
    public List<RelatedProductDto> getRelatedProducts(String slug, int limit) {
        log.info("Getting related products for product slug: {} with limit: {}", slug, limit);

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
            // Используем простой подход через findAll с фильтрацией
            List<Product> allCategoryProducts = productRepository.findByCategoryAndIsActiveTrueOrderByIdDesc(
                    currentProduct.getCategory()
            );

            List<Product> relatedProducts = allCategoryProducts.stream()
                    .filter(p -> !p.getId().equals(currentProduct.getId())) // Исключаем текущий товар
                    .filter(Product::getIsActive) // Только активные товары
                    .limit(limit) // Ограничиваем количество
                    .collect(Collectors.toList());

            log.info("Found {} related products for product: {}", relatedProducts.size(), currentProduct.getName());

            return productMapper.toRelatedDtoList(relatedProducts);

        } catch (Exception e) {
            log.error("Error getting related products for slug: {}", slug, e);
            return List.of(); // Возвращаем пустой список в случае ошибки
        }
    }

    @Override
    public List<ProductListRecommendationDto> getProductListRecommendations( int limit) {
        return productRepository.findAllRecommendationProducts()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Приватный метод для обработки загрузки изображения
     */
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

            log.info("Image uploaded successfully. URL: {}, ID: {}",
                    uploadResult.getUrl(), uploadResult.getImageId());

        } catch (IOException e) {
            log.error("Failed to upload image for product: {}", e.getMessage(), e);
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
            // Не выбрасываем исключение, чтобы не прерывать основную операцию
        }
    }

    /**
     * Приватный метод для валидации файла изображения
     */
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