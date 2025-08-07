package com.example.landofchokolate.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String slug;

    private String mainImageUrl;
    private String altText;

    private Integer stockQuantity;
}