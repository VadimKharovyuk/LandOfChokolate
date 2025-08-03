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

    //    // 🆕 Рейтинг товара
//    @Column(name = "rating")
//    private BigDecimal rating = BigDecimal.ZERO;


//    // 🆕 Количество отзывов
//    @Column(name = "review_count")
//    private Integer reviewCount = 0;

    // 🆕 SEO поля
    @Column(name = "meta_title", length = 60)
    private String metaTitle;

    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;


    private Boolean isActive = true;

    @Column(name = "is_recommendation")
    private Boolean isRecommendation = false;


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



//    // 🆕 Новинка
//    @Column(name = "is_new")
//    private Boolean isNew = false;

//    // 🆕 Количество продаж
//    @Column(name = "sales_count", nullable = false)
//    private Integer salesCount = 0;




//    // В модель Product добавить:
//    @Column(name = "meta_keywords", length = 255)
//    private String metaKeywords;

//    <head>
//    <!-- Если заполнено - используем, если нет - базовая генерация -->
//    <meta name="keywords"
//    th:content="${product.metaKeywords} ?:
//    ${product.name + ', ' + product.brand.name + ', шоколад, купити'}">
//</head>
//


//    // 🆕 Alt текст для изображений (очень важно для Google Images)
//    @Column(name = "image_alt_text", length = 125)
//    private String imageAltText;
//
//    // 🆕 Заголовок H1 (может отличаться от названия товара)
//    @Column(name = "h1_title", length = 80)
//    private String h1Title;


//    // Заголовок для Facebook/Instagram/Telegram (например: "🍫 Lindt Dark - ЗНИЖКА 20%!")
//    @Column(name = "og_title", length = 70)
//    private String ogTitle;
//
//    // Описание для соцсетей (например: "Гіркий шоколад для справжніх гурманів! 🔥 Акція тільки сьогодні")
//    @Column(name = "og_description", length = 200)
//    private String ogDescription;
//
//    // Специальная картинка для соцсетей (может отличаться от основного фото товара)
//    @Column(name = "og_image_url")
//    private String ogImageUrl;

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
}
