package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {



    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<ProductImageInfo> images;
    private Boolean isActive;

    // 🆕 SEO атрибуты
    private String metaTitle;
    private String metaDescription;
    private String description;

    // Связанные сущности
    private CategoryInfo category;
    private BrandInfo brand;


    // Информация о категории
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
        // Дополнительные поля категории, если нужны
        private String imageUrl;
        private String description;
    }

    // Информация о бренде
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandInfo {
        private Long id;
        private String name;
        // Дополнительные поля бренда, если нужны
        private String imageUrl;
        private String description;
    }

    // Утилитарные методы для удобства использования в шаблонах
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && stockQuantity > 0 && stockQuantity <= 5;
    }

    public String getStockStatus() {
        if (!isInStock()) {
            return "out-of-stock";
        } else if (isLowStock()) {
            return "low-stock";
        } else {
            return "in-stock";
        }
    }

    public String getStockMessage() {
        if (!isInStock()) {
            return "Немає в наявності";
        } else if (isLowStock()) {
            return "Залишилося мало";
        } else {
            return "В наявності: " + stockQuantity;
        }
    }
    public ProductImageInfo getMainImage() {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .findFirst()
                .orElse(images.get(0));
    }

}