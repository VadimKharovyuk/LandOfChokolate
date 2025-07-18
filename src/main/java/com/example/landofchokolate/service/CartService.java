package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.model.Product;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;

public interface CartService {
    CartDto getCartDto(HttpSession session);

    void addProduct(HttpSession session, Long productId, Integer quantity);

    void updateQuantity(HttpSession session, Long productId, Integer quantity);

    void removeProduct(HttpSession session, Long productId);
    void clearCart(HttpSession session);
    Integer getCartItemCount(HttpSession session);
    BigDecimal getCartTotal(HttpSession session);
    boolean isCartEmpty(HttpSession session);
}
