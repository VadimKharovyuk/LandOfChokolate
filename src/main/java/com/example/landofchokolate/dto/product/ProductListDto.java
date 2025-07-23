package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

    private String slug ;
    private Boolean isRecommendation;

    // Простые поля для отображения в списках
    private String categoryName;
    private String brandName;

    // Статус товара
    private boolean inStock;
    private boolean lowStock; // если остаток меньше 10
}