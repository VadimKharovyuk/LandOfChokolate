package com.example.landofchokolate.dto.brend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBrandClientDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String slug;
    private Integer stockQuantity;
    private Boolean isActive;
    private String categoryName; // название категории для отображения

    // Статусы товара (аналогично ProductListDto но в рамках этого DTO)
    private boolean inStock;
    private boolean lowStock; // если остаток меньше 10

    // В будущем можно добавить специфичные для бренд-страницы поля:
    // private String shortDescription;
    // private LocalDateTime addedToBrandDate;
    // private Boolean isFeatured;
    // private Integer popularityRank;
}
