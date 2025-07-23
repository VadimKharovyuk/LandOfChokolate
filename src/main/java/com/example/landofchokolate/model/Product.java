package com.example.landofchokolate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    private String imageUrl;
    private String imageId;

    @Column(unique = true)
    private String slug;


    private Boolean isActive = true;


    // Новый атрибут для отображения на главной странице
    @Column(name = "is_recommendation")
    private Boolean isRecommendation = false;

    // Новый атрибут для подсчета кликов
    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
