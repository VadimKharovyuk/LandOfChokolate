package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilterDto {
    private String searchName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long categoryId;
    private Long brandId;
    private String stockStatus;

    // Вспомогательные методы для проверки
    public boolean hasSearchName() {
        return searchName != null && !searchName.trim().isEmpty();
    }

    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }

    public boolean hasCategory() {
        return categoryId != null;
    }

    public boolean hasBrand() {
        return brandId != null;
    }

    public boolean hasStockStatus() {
        return stockStatus != null && !stockStatus.trim().isEmpty();
    }

    public boolean hasAnyFilter() {
        return hasSearchName() || hasPriceRange() || hasCategory() || hasBrand() || hasStockStatus();
    }
}