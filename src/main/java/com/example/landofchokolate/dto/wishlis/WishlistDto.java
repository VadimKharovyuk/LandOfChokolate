package com.example.landofchokolate.dto.wishlis;

import lombok.Data;
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

    // Вспомогательные методы
    public boolean hasItems() {
        return !isEmpty();
    }

    public void addItem(WishlistItemDto item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public void removeItem(WishlistItemDto item) {
        if (items != null) {
            items.remove(item);
        }
    }

    public void clearItems() {
        if (items != null) {
            items.clear();
        }
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
}