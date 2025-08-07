package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
//    private String imageUrl;

    // 🆕 Добавляем список изображений
    private List<ProductImageInfo> images;
    private String slug ;

    private String metaTitle;
    private String metaDescription;
    private String description;


    // Вложенные объекты для связанных сущностей
    private CategoryInfo category;
    private BrandInfo brand;
     private Boolean isRecommendation = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandInfo {
        private Long id;
        private String name;
        private String imageUrl;
    }


    /**
     * Получить главное изображение
     */
    public ProductImageInfo getMainImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .findFirst()
                .orElse(images.get(0));
    }
    /**
     * Получить дополнительные изображения (кроме главного)
     */
    public List<ProductImageInfo> getAdditionalImages() {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .filter(img -> !Boolean.TRUE.equals(img.getIsMain()))
                .toList();
    }
    /**
     * Проверить, есть ли изображения
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * Получить количество изображений
     */
    public int getImageCount() {
        return images != null ? images.size() : 0;
    }
}