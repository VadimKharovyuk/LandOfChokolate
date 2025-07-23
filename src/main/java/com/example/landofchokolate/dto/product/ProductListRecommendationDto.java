package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class ProductListRecommendationDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String slug; // Оставляем для ссылок

    // Простые поля для отображения в списках
    private String categoryName;
    private String brandName;

    // Статус товара
    private boolean inStock;
    private boolean lowStock; // если остаток меньше 10

    // Конструктор для JPQL запросов
    public ProductListRecommendationDto(Long id, String name, BigDecimal price, Integer stockQuantity,
                                        String imageUrl, String slug, String categoryName, String brandName,
                                        boolean inStock, boolean lowStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.slug = slug;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.inStock = inStock;
        this.lowStock = lowStock;
    }
}