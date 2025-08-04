package com.example.landofchokolate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String  shortDescription;


    private String imageUrl;
    private String imageId;

    // Статус активности
    private Boolean isActive = true;

    // Ручная пометка топовой категории
    private Boolean isFeatured = false;

    // Счетчик просмотров (для аналитики)
    private Long viewCount = 0L;

    // URL-friendly название для SEO
    @Column(unique = true)
    private String slug;

    // Для SEO
    private String metaTitle;
    private String metaDescription;
    private String seoKeywords;

    // Временные метки
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
