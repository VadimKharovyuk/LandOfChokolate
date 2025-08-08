package com.example.landofchokolate.dto.product;

import com.example.landofchokolate.enums.PriceUnit;
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

    private PriceUnit priceUnit = PriceUnit.PER_PIECE;

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