package com.example.landofchokolate.dto.card;

import com.example.landofchokolate.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private Long id;

    // Новые поля
    private String cartUuid;
    private CartStatus status;
    private LocalDateTime lastActivityAt;
    private String userAgent;
    private String ipAddress;
    private LocalDateTime expiresAt;

    // Существующие поля
    private List<CartItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Утилитарные методы
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemsCount() {
        return items.size();
    }

    // Новые утилитарные методы
    public boolean isActive() {
        return CartStatus.ACTIVE.equals(this.status);
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}