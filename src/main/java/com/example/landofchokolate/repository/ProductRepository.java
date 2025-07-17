package com.example.landofchokolate.repository;

import com.example.landofchokolate.dto.product.ProductFilterDto;
import com.example.landofchokolate.dto.product.ProductListDto;
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
     * Поиск продуктов по точному названию
     */
    Optional<Product> findByName(String name);

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
     * Получить продукты дороже указанной цены
     */
    List<Product> findByPriceGreaterThan(BigDecimal price);

    /**
     * Получить продукты дешевле указанной цены
     */
    List<Product> findByPriceLessThan(BigDecimal price);

    /**
     * Получить продукты с количеством больше указанного
     */
    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    /**
     * Получить продукты с количеством меньше указанного
     */
    List<Product> findByStockQuantityLessThan(Integer quantity);

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
     * Подсчет продуктов по бренду
     */
    long countByBrandId(Long brandId);

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
     * Проверка существования продукта с таким названием
     */
    boolean existsByName(String name);

    /**
     * Проверка существования продукта с таким названием (исключая текущий продукт)
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Получить самые дорогие продукты
     */
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    List<Product> findTopByOrderByPriceDesc();

    /**
     * Получить самые дешевые продукты
     */
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    List<Product> findTopByOrderByPriceAsc();

    /**
     * Получить продукты с наибольшим остатком
     */
    @Query("SELECT p FROM Product p ORDER BY p.stockQuantity DESC")
    List<Product> findTopByOrderByStockQuantityDesc();

    /**
     * Поиск продуктов по названию категории
     */
    @Query("SELECT p FROM Product p WHERE p.category.name LIKE %:categoryName%")
    List<Product> findByCategoryNameContaining(@Param("categoryName") String categoryName);

    /**
     * Поиск продуктов по названию бренда
     */
    @Query("SELECT p FROM Product p WHERE p.brand.name LIKE %:brandName%")
    List<Product> findByBrandNameContaining(@Param("brandName") String brandName);

    /**
     * Комплексный поиск по названию продукта, категории или бренда
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> findBySearchTerm(@Param("searchTerm") String searchTerm);

    /**
     * Получить продукты по фильтрам
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false AND p.stockQuantity = 0))")
    List<Product> findProductsByFilters(@Param("categoryId") Long categoryId,
                                        @Param("brandId") Long brandId,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("inStock") Boolean inStock);

    /**
     * Рассчитать общую стоимость инвентаря
     */
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p")
    BigDecimal calculateTotalInventoryValue();

    /**
     * Рассчитать общую стоимость инвентаря по категории
     */
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p WHERE p.category.id = :categoryId")
    BigDecimal calculateInventoryValueByCategory(@Param("categoryId") Long categoryId);

    /**
     * Рассчитать общую стоимость инвентаря по бренду
     */
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p WHERE p.brand.id = :brandId")
    BigDecimal calculateInventoryValueByBrand(@Param("brandId") Long brandId);

    /**
     * Получить продукты с изображениями
     */
    @Query("SELECT p FROM Product p WHERE p.imageUrl IS NOT NULL")
    List<Product> findProductsWithImages();

    /**
     * Получить продукты без изображений
     */
    @Query("SELECT p FROM Product p WHERE p.imageUrl IS NULL")
    List<Product> findProductsWithoutImages();

    /**
     * Получить популярные продукты (можно расширить логику в будущем)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 ORDER BY p.stockQuantity ASC")
    List<Product> findPopularProducts();

    /**
     * Получить рекомендуемые продукты по категории
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.stockQuantity > 0 AND p.id != :excludeProductId ORDER BY p.price ASC")
    List<Product> findRecommendedProductsByCategory(@Param("categoryId") Long categoryId,
                                                    @Param("excludeProductId") Long excludeProductId);

    @Query("SELECT new com.example.landofchokolate.dto.product.ProductListDto(" +
            "p.id, p.name, p.price, p.stockQuantity, p.imageUrl, " +
            "c.name, b.name, " +
            "CASE WHEN p.stockQuantity > 0 THEN true ELSE false END, " +
            "CASE WHEN p.stockQuantity < 10 THEN true ELSE false END) " +
            "FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b")
    Page<ProductListDto> findAllProductListDto(Pageable pageable);

    @Query("SELECT new com.example.landofchokolate.dto.product.ProductListDto(" +
            "p.id, p.name, p.price, p.stockQuantity, p.imageUrl, " +
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


    boolean existsByCategoryId(Long id);


    /**
     * Находит минимальную цену товаров в категории
     */
    @Query("SELECT MIN(p.price) FROM Product p WHERE p.category.id = :categoryId AND p.price > 0")
    BigDecimal findMinPriceByCategoryId(Long categoryId);

    /**
     * Находит максимальную цену товаров в категории
     */
    @Query("SELECT MAX(p.price) FROM Product p WHERE p.category.id = :categoryId")
    BigDecimal findMaxPriceByCategoryId(Long categoryId);

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
}