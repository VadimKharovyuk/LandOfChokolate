package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<ProductImageInfo> images;

    private String slug ;


    // Простые поля для отображения в списках
    private String categoryName;
    private String brandName;

    // Статус товара
    private boolean inStock;
    private boolean lowStock; // если остаток меньше 10

//    / 🆕 СПЕЦИАЛЬНЫЙ КОНСТРУКТОР ДЛЯ JPQL ЗАПРОСОВ
    public ProductListDto(Long id, String name, BigDecimal price, Integer stockQuantity,
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

        // 🔄 Создаем временное изображение из imageUrl для совместимости
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ProductImageInfo mainImage = new ProductImageInfo();
            mainImage.setImageUrl(imageUrl);
            mainImage.setIsMain(true);
            mainImage.setSortOrder(0);
            mainImage.setAltText(name); // Используем название как alt
            this.images = List.of(mainImage);
        } else {
            this.images = new ArrayList<>();
        }
    }

    // Helper методы для обратной совместимости
    public String getImageUrl() {
        return getMainImageUrl();
    }

    public String getMainImageUrl() {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .findFirst()
                .map(ProductImageInfo::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }


}