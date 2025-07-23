package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String slug ;

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
}