package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.mapper.CartMapper;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
//@Primary
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private static final String CART_SESSION_KEY = "cart";

    @Override
    public CartDto getCartDto(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = createNewCart();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
    /**
     * Создать новую пустую корзину
     */
    private CartDto createNewCart() {
        CartDto cart = new CartDto();
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setItems(new ArrayList<>());
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public void addProduct(HttpSession session, Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Товар не найден");
        }

        Product product = productOpt.get();

        // Проверка активности товара
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new RuntimeException("Товар недоступен");
        }

        // Проверка наличия
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Недостаточно товара на складе");
        }

        CartDto cart = getCartDto(session);

        // Проверяем, есть ли уже такой товар в корзине
        Optional<CartItemDto> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Увеличиваем количество существующего товара
            CartItemDto item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Проверяем, что общее количество не превышает остаток
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Недостаточно товара на складе");
            }

            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            // Добавляем новый товар в корзину
            CartItemDto newItem = createCartItem(product, quantity);
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Создать новый элемент корзины
     */
    private CartItemDto createCartItem(Product product, Integer quantity) {
        CartItemDto item = new CartItemDto();
        item.setQuantity(quantity);
        item.setPriceAtTime(product.getPrice());
        item.setAddedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        // Создаем информацию о продукте
        CartItemDto.ProductInfo productInfo = new CartItemDto.ProductInfo();
        productInfo.setId(product.getId());
        productInfo.setName(product.getName());
        productInfo.setSlug(product.getSlug());
        productInfo.setCurrentPrice(product.getPrice());
        productInfo.setImageUrl(product.getImageUrl());
        productInfo.setStockQuantity(product.getStockQuantity());
        productInfo.setIsActive(product.getIsActive());

        item.setProduct(productInfo);
        return item;
    }

    @Override
    public void updateQuantity(HttpSession session, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeProduct(session, productId);
            return;
        }

        CartDto cart = getCartDto(session);

        Optional<CartItemDto> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItemDto item = itemOpt.get();

            // Проверяем наличие на складе
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new RuntimeException("Недостаточно товара на складе");
            }

            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());

            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }

    @Override
    public void removeProduct(HttpSession session, Long productId) {
        CartDto cart = getCartDto(session);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cart.setUpdatedAt(LocalDateTime.now());

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @Override
    public void clearCart(HttpSession session) {
        CartDto cart = createNewCart();
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @Override
    public Integer getCartItemCount(HttpSession session) {
        CartDto cart = getCartDto(session);
        return cart.getTotalQuantity();
    }

    @Override
    public BigDecimal getCartTotal(HttpSession session) {
        CartDto cart = getCartDto(session);
        return cart.getTotalPrice();
    }

    @Override
    public boolean isCartEmpty(HttpSession session) {
        CartDto cart = getCartDto(session);
        return cart.isEmpty();
    }
}
