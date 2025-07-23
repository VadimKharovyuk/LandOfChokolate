package com.example.landofchokolate.repository;

import com.example.landofchokolate.dto.product.ProductDetailDto;
import com.example.landofchokolate.dto.product.ProductFilterDto;
import com.example.landofchokolate.dto.product.ProductListDto;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Поиск продуктов по названию (содержит подстроку, игнорируя регистр)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Получить продукты по категории
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Получить продукты по бренду
     */
    List<Product> findByBrandId(Long brandId);

    /**
     * Получить продукты по диапазону цен
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);



    /**
     * Получить продукты с количеством больше указанного
     */
    List<Product> findByStockQuantityGreaterThan(Integer quantity);


    /**
     * Получить продукты с количеством в диапазоне
     */
    List<Product> findByStockQuantityBetween(Integer minQuantity, Integer maxQuantity);

    /**
     * Получить продукты с точным количеством
     */
    List<Product> findByStockQuantity(Integer quantity);

    /**
     * Подсчет продуктов по категории
     */
    long countByCategoryId(Long categoryId);


    /**
     * Подсчет продуктов в наличии
     */
    long countByStockQuantityGreaterThan(Integer quantity);

    /**
     * Подсчет продуктов с низким остатком
     */
    long countByStockQuantityBetween(Integer minQuantity, Integer maxQuantity);

    /**
     * Подсчет продуктов с точным количеством
     */
    long countByStockQuantity(Integer quantity);



    /**
     * Рассчитать общую стоимость инвентаря
     */
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p")
    BigDecimal calculateTotalInventoryValue();




    // Исправленный запрос без фильтров
    @Query("SELECT new com.example.landofchokolate.dto.product.ProductListDto(" +
            "p.id, p.name, p.price, p.stockQuantity, p.imageUrl, p.slug, " +
            "p.isRecommendation, " + // ← Добавили недостающее поле
            "c.name, b.name, " +
            "CASE WHEN p.stockQuantity > 0 THEN true ELSE false END, " +
            "CASE WHEN p.stockQuantity > 0 AND p.stockQuantity <= 10 THEN true ELSE false END) " +
            "FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b")
    Page<ProductListDto> findAllProductListDto(Pageable pageable);

    // Исправленный запрос с правильным порядком полей
    @Query("SELECT new com.example.landofchokolate.dto.product.ProductListDto(" +
            "p.id, p.name, p.price, p.stockQuantity, p.imageUrl, " +
            "p.slug, p.isRecommendation, " +
            "c.name, b.name, " +
            "CASE WHEN p.stockQuantity > 0 THEN true ELSE false END, " +
            "CASE WHEN p.stockQuantity > 0 AND p.stockQuantity <= 10 THEN true ELSE false END) " +
            "FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b " +
            "WHERE (:#{#filters.searchName} IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#filters.searchName}, '%'))) " +
            "AND (:#{#filters.minPrice} IS NULL OR p.price >= :#{#filters.minPrice}) " +
            "AND (:#{#filters.maxPrice} IS NULL OR p.price <= :#{#filters.maxPrice}) " +
            "AND (:#{#filters.categoryId} IS NULL OR c.id = :#{#filters.categoryId}) " +
            "AND (:#{#filters.brandId} IS NULL OR b.id = :#{#filters.brandId}) " +
            "AND (:#{#filters.stockStatus} IS NULL OR " +
            "     (:#{#filters.stockStatus} = 'in-stock' AND p.stockQuantity > 10) OR " +
            "     (:#{#filters.stockStatus} = 'low-stock' AND p.stockQuantity > 0 AND p.stockQuantity <= 10) OR " +
            "     (:#{#filters.stockStatus} = 'out-of-stock' AND p.stockQuantity = 0))")
    Page<ProductListDto> findAllProductListDtoWithFilters(Pageable pageable, @Param("filters") ProductFilterDto filters);

    boolean existsBySlug(String slug);


    /**
     * Получает статистику цен для категории
     */
    @Query("""
        SELECT new map(
            MIN(p.price) as minPrice,
            MAX(p.price) as maxPrice,
            COUNT(p) as productCount
        ) 
        FROM Product p 
        WHERE p.category.id = :categoryId AND p.price > 0
        """)
    Map<String, Object> findPriceStatsByCategoryId(Long categoryId);



    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Optional<Product> findBySlug(String slug);


    /**
     * Находит все продукты без slug
     */
    List<Product> findBySlugIsNull();

    /**
     * Находит все активные товары категории, отсортированные по ID (новые первыми)
     */
    List<Product> findByCategoryAndIsActiveTrueOrderByIdDesc(Category category);



    Page<Product> findByBrandSlugAndIsActiveTrue(String slug, Pageable pageable);
}