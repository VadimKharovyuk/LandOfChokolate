package com.example.landofchokolate.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long id;
    private Long cartId;
    private ProductInfo product;
    private Integer quantity;
    private BigDecimal priceAtTime;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    // Информация о продукте
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
        private BigDecimal currentPrice; // Текущая цена для сравнения
        private String imageUrl;
        private Integer stockQuantity;
        private Boolean isActive;
    }

    // Утилитарные методы
    public BigDecimal getTotalPrice() {
        return priceAtTime.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isPriceChanged() {
        return product != null && !priceAtTime.equals(product.getCurrentPrice());
    }

    public boolean isInStock() {
        return product != null && product.getStockQuantity() != null && product.getStockQuantity() >= quantity;
    }

    public boolean isProductActive() {
        return product != null && Boolean.TRUE.equals(product.getIsActive());
    }
}