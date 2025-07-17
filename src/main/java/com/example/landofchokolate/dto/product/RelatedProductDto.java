package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatedProductDto {

    // Минимальная информация для карточки похожего товара
    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

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
            return "В наличии";
        }
    }
}