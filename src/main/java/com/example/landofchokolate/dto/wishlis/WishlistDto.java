//package com.example.landofchokolate.dto.wishlis;
//
//import lombok.Data;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//public class WishlistDto {
//    private Long id;
//    private String wishlistUuid;
//    private List<WishlistItemDto> items = new ArrayList<>();
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private boolean isEmpty;
//    private int totalItems;
//
//
//    // Геттеры и сеттеры для isEmpty
//    public boolean isEmpty() {
//        return items == null || items.isEmpty();
//    }
//
//    public void setEmpty(boolean empty) {
//        this.isEmpty = empty;
//    }
//
//    public void setIsEmpty(boolean empty) {
//        this.isEmpty = empty;
//    }
//
//    public boolean getIsEmpty() {
//        return isEmpty();
//    }
//
//    // Геттеры и сеттеры для totalItems
//    public int getTotalItems() {
//        return items != null ? items.size() : 0;
//    }
//
//    public void setTotalItems(int totalItems) {
//        this.totalItems = totalItems;
//    }
//
//    // Вспомогательные методы
//    public boolean hasItems() {
//        return !isEmpty();
//    }
//
//    public void addItem(WishlistItemDto item) {
//        if (items == null) {
//            items = new ArrayList<>();
//        }
//        items.add(item);
//    }
//
//    public void removeItem(WishlistItemDto item) {
//        if (items != null) {
//            items.remove(item);
//        }
//    }
//
//    public void clearItems() {
//        if (items != null) {
//            items.clear();
//        }
//    }
//
//    // Поиск товара по ID
//    public boolean containsProduct(Long productId) {
//        if (items == null || productId == null) {
//            return false;
//        }
//        return items.stream()
//                .anyMatch(item -> item.getProduct() != null &&
//                        productId.equals(item.getProduct().getId()));
//    }
//
//    // Найти элемент wishlist по ID товара
//    public WishlistItemDto findItemByProductId(Long productId) {
//        if (items == null || productId == null) {
//            return null;
//        }
//        return items.stream()
//                .filter(item -> item.getProduct() != null &&
//                        productId.equals(item.getProduct().getId()))
//                .findFirst()
//                .orElse(null);
//    }
//}

package com.example.landofchokolate.dto.wishlis;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class WishlistDto {
    private Long id;
    private String wishlistUuid;
    private List<WishlistItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEmpty;
    private int totalItems;
    private BigDecimal total; // Новый атрибут для общей стоимости
    private int availableItems; // Дополнительно: количество доступных товаров

    // Геттеры и сеттеры для isEmpty
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public void setEmpty(boolean empty) {
        this.isEmpty = empty;
    }

    public void setIsEmpty(boolean empty) {
        this.isEmpty = empty;
    }

    public boolean getIsEmpty() {
        return isEmpty();
    }

    // Геттеры и сеттеры для totalItems
    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    // Геттеры и сеттеры для total
    public BigDecimal getTotal() {
        if (total != null) {
            return total;
        }
        // Автоматический подсчет, если total не установлен
        return calculateTotal();
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // Геттеры и сеттеры для availableItems
    public int getAvailableItems() {
        if (availableItems > 0) {
            return availableItems;
        }
        // Автоматический подсчет, если не установлен
        return calculateAvailableItems();
    }

    public void setAvailableItems(int availableItems) {
        this.availableItems = availableItems;
    }

    // Вспомогательные методы
    public boolean hasItems() {
        return !isEmpty();
    }

    public void addItem(WishlistItemDto item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        // Сбрасываем кэшированные значения
        resetCalculatedValues();
    }

    public void removeItem(WishlistItemDto item) {
        if (items != null) {
            items.remove(item);
            // Сбрасываем кэшированные значения
            resetCalculatedValues();
        }
    }

    public void clearItems() {
        if (items != null) {
            items.clear();
        }
        // Сбрасываем кэшированные значения
        resetCalculatedValues();
    }

    // Поиск товара по ID
    public boolean containsProduct(Long productId) {
        if (items == null || productId == null) {
            return false;
        }
        return items.stream()
                .anyMatch(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()));
    }

    // Найти элемент wishlist по ID товара
    public WishlistItemDto findItemByProductId(Long productId) {
        if (items == null || productId == null) {
            return null;
        }
        return items.stream()
                .filter(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()))
                .findFirst()
                .orElse(null);
    }

    // Методы для расчета значений
    private BigDecimal calculateTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .filter(item -> item.getProduct() != null &&
                        item.getProduct().isAvailable()) // только доступные товары
                .map(item -> item.getProduct().getSafeCurrentPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateAvailableItems() {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        return (int) items.stream()
                .filter(item -> item.getProduct() != null &&
                        item.getProduct().isAvailable())
                .count();
    }

    // Расчет общей стоимости всех товаров (включая недоступные)
    public BigDecimal getTotalAllItems() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .filter(item -> item.getProduct() != null)
                .map(item -> item.getProduct().getSafeCurrentPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Получить список только доступных товаров
    public List<WishlistItemDto> getAvailableItemsList() {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        return items.stream()
                .filter(item -> item.getProduct() != null &&
                        item.getProduct().isAvailable())
                .toList();
    }

    // Сбросить кэшированные значения при изменении списка товаров
    private void resetCalculatedValues() {
        this.total = null;
        this.availableItems = 0;
    }

    // Метод для форматированного отображения суммы
    public String getFormattedTotal() {
        BigDecimal totalAmount = getTotal();
        return totalAmount.toString() + " грн";
    }

    // Проверка, есть ли доступные товары для покупки
    public boolean hasAvailableItems() {
        return getAvailableItems() > 0;
    }

    @Override
    public String toString() {
        return "WishlistDto{" +
                "id=" + id +
                ", wishlistUuid='" + wishlistUuid + '\'' +
                ", totalItems=" + getTotalItems() +
                ", availableItems=" + getAvailableItems() +
                ", total=" + getTotal() +
                ", isEmpty=" + isEmpty() +
                '}';
    }
}