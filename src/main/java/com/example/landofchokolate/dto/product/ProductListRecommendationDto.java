package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListRecommendationDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<ProductImageInfo> images;
    private String slug; // Оставляем для ссылок

    // Простые поля для отображения в списках
    private String categoryName;
    private String brandName;

    // Статус товара
    private boolean inStock;
    private boolean lowStock; // если остаток меньше 10

    // Конструктор для JPQL запросов (без images, они будут загружаться отдельно)
    public ProductListRecommendationDto(Long id, String name, BigDecimal price, Integer stockQuantity,
                                        String slug, String categoryName, String brandName,
                                        boolean inStock, boolean lowStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.slug = slug;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.inStock = inStock;
        this.lowStock = lowStock;
    }

    // Конструктор для JPQL запросов с imageUrl (для обратной совместимости)
    public ProductListRecommendationDto(Long id, String name, BigDecimal price, Integer stockQuantity,
                                        String imageUrl, String slug, String categoryName, String brandName,
                                        boolean inStock, boolean lowStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.slug = slug;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.inStock = inStock;
        this.lowStock = lowStock;

        // Создаем объект изображения из URL если он есть
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.images = List.of(ProductImageInfo.builder()
                    .imageUrl(imageUrl)
                    .isMain(true)
                    .sortOrder(0)
                    .build());
        }
    }

    // Метод для получения главного изображения
    public ProductImageInfo getMainImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .findFirst()
                .orElse(images.get(0));
    }

    // Метод для получения URL главного изображения
    public String getMainImageUrl() {
        ProductImageInfo mainImage = getMainImage();
        return mainImage != null ? mainImage.getImageUrl() : null;
    }
}