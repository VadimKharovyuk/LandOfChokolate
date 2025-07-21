package com.example.landofchokolate.dto.wishlis;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class WishlistItemDto {
    private Long id;
    private ProductInfo product;
    private LocalDateTime addedAt;


    // Конструкторы
    public WishlistItemDto() {
        this.addedAt = LocalDateTime.now();
    }



    // Вспомогательные методы
    public boolean hasValidProduct() {
        return product != null && product.getId() != null;
    }

    public String getProductName() {
        return product != null ? product.getName() : "Неизвестный товар";
    }

    public BigDecimal getProductPrice() {
        return product != null ? product.getCurrentPrice() : BigDecimal.ZERO;
    }

    public boolean isProductActive() {
        return product != null && product.isActive();
    }

    public boolean isProductInStock() {
        return product != null && product.isInStock();
    }

    @Data
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
        private String imageUrl;
        private BigDecimal currentPrice;
        private boolean isActive;
        private Integer stockQuantity;

        // Конструкторы
        public ProductInfo() {
            this.currentPrice = BigDecimal.ZERO;
            this.isActive = false;
            this.stockQuantity = 0;
        }

        public ProductInfo(Long id, String name, BigDecimal currentPrice) {
            this();
            this.id = id;
            this.name = name;
            this.currentPrice = currentPrice;
        }

        // Геттеры и сеттеры для isActive
        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }

        public void setIsActive(Boolean active) {
            this.isActive = active != null ? active : false;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        // Проверка наличия на складе
        public boolean isInStock() {
            return stockQuantity != null && stockQuantity > 0;
        }

        public boolean isOutOfStock() {
            return !isInStock();
        }

        // Проверка доступности товара
        public boolean isAvailable() {
            return isActive() && isInStock();
        }

        // Вспомогательные методы
        public String getDisplayName() {
            return name != null ? name : "Неизвестный товар";
        }

        public String getDisplayPrice() {
            if (currentPrice == null) {
                return "Цена не указана";
            }
            return currentPrice.toString() + " ₽";
        }

        public String getStockStatus() {
            if (stockQuantity == null || stockQuantity <= 0) {
                return "Нет в наличии";
            } else if (stockQuantity < 5) {
                return "Мало в наличии";
            } else {
                return "В наличии";
            }
        }

        public String getAvailabilityStatus() {
            if (!isActive()) {
                return "Товар снят с продажи";
            } else if (!isInStock()) {
                return "Нет в наличии";
            } else {
                return "Доступен";
            }
        }

        // Безопасные геттеры
        public String getSafeSlug() {
            return slug != null ? slug : "";
        }

        public String getSafeImageUrl() {
            return imageUrl != null ? imageUrl : "/images/no-image.png";
        }

        public BigDecimal getSafeCurrentPrice() {
            return currentPrice != null ? currentPrice : BigDecimal.ZERO;
        }

        public Integer getSafeStockQuantity() {
            return stockQuantity != null ? stockQuantity : 0;
        }

        // Методы для сравнения
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductInfo that = (ProductInfo) o;
            return id != null && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "ProductInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", currentPrice=" + currentPrice +
                    ", isActive=" + isActive +
                    ", stockQuantity=" + stockQuantity +
                    '}';
        }
    }

    // Методы для сравнения WishlistItemDto
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistItemDto that = (WishlistItemDto) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "WishlistItemDto{" +
                "id=" + id +
                ", product=" + (product != null ? product.getDisplayName() : "null") +
                ", addedAt=" + addedAt +
                '}';
    }
}
