package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.product.CreateProductDto;
import com.example.landofchokolate.dto.product.ProductListDto;
import com.example.landofchokolate.dto.product.ProductResponseDto;
import com.example.landofchokolate.dto.product.UpdateProductDto;
import com.example.landofchokolate.model.Product;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    /**
     * Создает новый продукт
     */
    ProductResponseDto createProduct(CreateProductDto createProductDto);

    /**
     * Обновляет существующий продукт
     */
    ProductResponseDto updateProduct(Long id, UpdateProductDto updateProductDto);

    /**
     * Удаляет продукт по ID
     */
    void deleteProduct(Long id);

    /**
     * Получает продукт по ID (полная информация)
     */
    ProductResponseDto getProductById(Long id);

    /**
     * Получает все продукты (упрощенная информация для списков)
     */
    List<ProductListDto> getAllProducts();

    /**
     * Получает все продукты (полная информация)
     */
    List<ProductResponseDto> getAllProductsDetailed();

    /**
     * Поиск продуктов по названию
     */
    List<ProductListDto> searchProductsByName(String name);

    /**
     * Получает продукты по категории
     */
    List<ProductListDto> getProductsByCategory(Long categoryId);

    /**
     * Получает продукты по бренду
     */
    List<ProductListDto> getProductsByBrand(Long brandId);

    /**
     * Получает продукты по диапазону цен
     */
    List<ProductListDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Получает товары в наличии
     */
    List<ProductListDto> getProductsInStock();

    /**
     * Получает товары с низким остатком (меньше 10 штук)
     */
    List<ProductListDto> getProductsWithLowStock();

    /**
     * Получает товары, которых нет в наличии
     */
    List<ProductListDto> getOutOfStockProducts();

    /**
     * Обновляет количество товара на складе
     */
    ProductResponseDto updateStock(Long productId, Integer newQuantity);

    /**
     * Увеличивает количество товара на складе
     */
    ProductResponseDto increaseStock(Long productId, Integer quantity);

    /**
     * Уменьшает количество товара на складе
     */
    ProductResponseDto decreaseStock(Long productId, Integer quantity);

    /**
     * Проверяет доступность товара в нужном количестве
     */
    boolean isProductAvailable(Long productId, Integer requiredQuantity);

    /**
     * Получает статистику по товарам
     */
    ProductStatistics getProductStatistics();


    Page<Product> getProductsByCategoryPage(Long id, int page, int size);

    /**
     * Внутренний класс для статистики
     */
    @Getter
    class ProductStatistics {
        private final long totalProducts;
        private final long inStockProducts;
        private final long outOfStockProducts;
        private final long lowStockProducts;
        private final BigDecimal totalInventoryValue;

        public ProductStatistics(long totalProducts, long inStockProducts,
                                 long outOfStockProducts, long lowStockProducts,
                                 BigDecimal totalInventoryValue) {
            this.totalProducts = totalProducts;
            this.inStockProducts = inStockProducts;
            this.outOfStockProducts = outOfStockProducts;
            this.lowStockProducts = lowStockProducts;
            this.totalInventoryValue = totalInventoryValue;
        }

    }
}