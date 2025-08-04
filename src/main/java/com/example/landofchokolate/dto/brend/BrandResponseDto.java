package com.example.landofchokolate.dto.brend;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDto {
    private Long id;
    private String name;
    private String description;

    private String shortDescription;

    private String imageUrl;
    private String slug ;

    // 🆕 SEO поля для брендов
    private String metaTitle;
    private String metaDescription;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
