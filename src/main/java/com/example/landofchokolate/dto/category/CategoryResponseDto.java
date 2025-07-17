package com.example.landofchokolate.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String name;
    private String shortDescription;


    private String slug;
    private String imageUrl;
    private String imageId;
    private String metaTitle;
    private String metaDescription;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🆕 Добавить поля для топовых категорий
    private Boolean isFeatured;
}
