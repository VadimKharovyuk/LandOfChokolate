package com.example.landofchokolate.model;

import com.example.landofchokolate.enums.PriceUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

//    private String imageUrl;
//    private String imageId;

    @Column(unique = true)
    private String slug;


    // 🆕 SEO поля
    @Column(name = "meta_title", length = 60)
    private String metaTitle;

    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private String seoKeywords;


    private Boolean isActive = true;

    @Column(name = "is_recommendation")
    private Boolean isRecommendation = false;


    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;


    @Enumerated(EnumType.STRING)
    @Column(name = "price_unit", nullable = false)
    private PriceUnit priceUnit = PriceUnit.PER_PIECE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;


    // 🆕 Множественные изображения
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, createdAt ASC")
    private List<ProductImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ProductImage getMainImage() {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .findFirst()
                .orElse(images.get(0));
    }


//    // 🆕 Количество отзывов
//    @Column(name = "review_count")
//    private Integer reviewCount = 0;


//
//    // 🆕 Заголовок H1 (может отличаться от названия товара)
//    @Column(name = "h1_title", length = 80)
//    private String h1Title;


//    / 🆕 Enum для лейблов товаров
//    public enum ProductLabel {
//        NEW("Новинка"),
//        SALE("Акція"),
//        HOT("Хіт продажів"),
//        RECOMMENDED("Рекомендуємо"),
//        LIMITED("Обмежена кількість"),
//        BESTSELLER("Бестселер");
//
//        private final String displayName;
//
//        ProductLabel(String displayName) {
//            this.displayName = displayName;
//        }
//
//        public String getDisplayName() {
//            return displayName;
//        }

//    <span th:text="${product.priceUnit.label}"></span>
//<!-- Выведет: "за штуку" или "за 100 г" -->
}
