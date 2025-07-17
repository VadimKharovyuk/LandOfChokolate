package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {

    // Основная информация о товаре (соответствует текущей модели)
    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String imageId;
    private Boolean isActive;

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
            return "Нет в наличии";
        } else if (isLowStock()) {
            return "Осталось мало";
        } else {
            return "В наличии: " + stockQuantity;
        }
    }

    public String getStockBadgeClass() {
        if (!isInStock()) {
            return "badge-out-of-stock";
        } else if (isLowStock()) {
            return "badge-low-stock";
        } else {
            return "badge-in-stock";
        }
    }
}