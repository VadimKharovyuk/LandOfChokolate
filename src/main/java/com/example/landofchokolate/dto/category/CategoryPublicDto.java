package com.example.landofchokolate.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для публичного отображения категории (для пользователей сайта)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPublicDto {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String imageUrl;

    // Для SEO
    private String metaTitle;
    private String metaDescription;

    // Дополнительная информация
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Статистика (опционально)
    private Integer productsCount;


    // 🆕 Ценовая информация
    private BigDecimal minPrice;        // минимальная цена товара в категории
    private BigDecimal maxPrice;        // максимальная цена товара в категории (опционально)
    private String priceRange;

    // ✅ Добавить этот метод для автоматического формирования ценового диапазона
    public String getPriceRange() {
        if (minPrice == null) {
            return null;
        }

        if (maxPrice == null || minPrice.equals(maxPrice)) {
            return "від " + minPrice + " ₴";
        }

        return "від " + minPrice + " до " + maxPrice + " ₴";
    }
}