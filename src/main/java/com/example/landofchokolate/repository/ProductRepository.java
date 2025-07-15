package com.example.landofchokolate.repository;

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

    @Query("SELECT new com.example.landofchokolate.dto.product.ProductListDto(" +
            "p.id, p.name, p.price, p.stockQuantity, p.imageUrl, " +
            "c.name, b.name, " +
            "CASE WHEN p.stockQuantity > 0 THEN true ELSE false END, " +
            "CASE WHEN p.stockQuantity < 10 THEN true ELSE false END) " +
            "FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b")
    Page<ProductListDto> findAllProductListDto(Pageable pageable);
}