package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListClickDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String slug;
    private Integer clickCount;
}
