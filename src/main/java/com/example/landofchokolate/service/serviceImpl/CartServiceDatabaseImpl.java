package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.enums.CartStatus;
import com.example.landofchokolate.mapper.CartMapper;
import com.example.landofchokolate.model.Cart;
import com.example.landofchokolate.model.CartItem;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.repository.CartRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Primary
@Service("cartServiceDatabase")
@RequiredArgsConstructor
@Slf4j
public class CartServiceDatabaseImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    private static final String CART_COOKIE_NAME = "cart_id";
    private static final String CART_SESSION_KEY = "current_cart";

    @Value("${app.cart.cookie.max-age:2592000}")
    private int cookieMaxAge;

    @Value("${app.cart.expiration.days:30}")
    private int cartExpirationDays;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Override
    @Transactional(readOnly = true)
    public CartDto getCartDto(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public void addProduct(HttpSession session, Long productId, Integer quantity) {
        // Загружаем продукт и корзину параллельно, если это возможно
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // Проверка активности товара
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new RuntimeException("Товар недоступен");
        }

        // Проверка наличия
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Недостаточно товара на складе");
        }

        Cart cart = getOrCreateCart(session);

        // Проверяем, есть ли уже такой товар в корзине
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Увеличиваем количество существующего товара
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Проверяем, что общее количество не превышает остаток
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Недостаточно товара на складе");
            }

            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now()); // Обновляем время изменения элемента
        } else {
            // Добавляем новый товар в корзину
            CartItem newItem = createCartItem(cart, product, quantity);
            cart.getItems().add(newItem);
        }

        updateCartActivity(cart);
        cart = cartRepository.save(cart);

        // Обновляем корзину в сессии
        updateCartInSession(session, cart);
    }

    /**
     * Обновить активность корзины
     */
    private void updateCartActivity(Cart cart) {
        cart.setLastActivityAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateQuantity(HttpSession session, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeProduct(session, productId);
            return;
        }

        Cart cart = getOrCreateCart(session);

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();

            // Проверяем наличие на складе
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new RuntimeException("Недостаточно товара на складе");
            }

            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
            updateCartActivity(cart);
            cart = cartRepository.save(cart);

            // Обновляем корзину в сессии
            updateCartInSession(session, cart);
        }
    }

    @Override
    @Transactional
    public void removeProduct(HttpSession session, Long productId) {
        Cart cart = getOrCreateCart(session);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        updateCartActivity(cart);
        cart = cartRepository.save(cart);

        // Обновляем корзину в сессии
        updateCartInSession(session, cart);
    }

    @Override
    @Transactional
    public void clearCart(HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.getItems().clear();
        updateCartActivity(cart);
        cart = cartRepository.save(cart);

        // Обновляем корзину в сессии
        updateCartInSession(session, cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemCount(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return calculateCartTotalQuantity(cart);
    }

    /**
     * Получить общее количество товаров в корзине
     */
    private Integer calculateCartTotalQuantity(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return calculateCartTotalPrice(cart);
    }

    /**
     * Получить общую стоимость корзины
     */
    private BigDecimal calculateCartTotalPrice(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCartEmpty(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return checkIfCartEmpty(cart);
    }

    /**
     * Проверить, пуста ли корзина
     */
    private boolean checkIfCartEmpty(Cart cart) {
        return cart.getItems().isEmpty();
    }

    /**
     * Получить или создать корзину с использованием FETCH JOIN
     */
    @Transactional
    protected Cart getOrCreateCart(HttpSession session) {
        // Сначала проверяем сессию
        Cart cachedCart = getCartFromSession(session);
        if (cachedCart != null && !isCartExpired(cachedCart)) {
            return cachedCart;
        }

        // Если в сессии нет или корзина истекла, загружаем из БД
        String cartUuid = getCartUuidFromCookie();

        if (cartUuid != null) {
            // Используем оптимизированный запрос с FETCH JOIN
            Optional<Cart> cartOpt = cartRepository.findByCartUuidAndStatusWithItems(cartUuid, CartStatus.ACTIVE);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                // Проверяем, не истекла ли корзина
                if (!isCartExpired(cart)) {
                    updateCartActivity(cart);
                    cart = cartRepository.save(cart);
                    // Сохраняем в сессии для последующих обращений
                    updateCartInSession(session, cart);
                    return cart;
                } else {
                    // Помечаем корзину как истёкшую
                    cart.setStatus(CartStatus.EXPIRED);
                    cartRepository.save(cart);
                    // Удаляем из сессии
                    clearCartFromSession(session);
                }
            }
        }

        // Создаём новую корзину
        Cart newCart = createNewCart();
        updateCartInSession(session, newCart);
        return newCart;
    }

    /**
     * Получить корзину из сессии
     */
    private Cart getCartFromSession(HttpSession session) {
        try {
            return (Cart) session.getAttribute(CART_SESSION_KEY);
        } catch (ClassCastException e) {
            log.warn("Invalid cart object in session, removing", e);
            session.removeAttribute(CART_SESSION_KEY);
            return null;
        }
    }

    /**
     * Обновить корзину в сессии
     */
    private void updateCartInSession(HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    /**
     * Очистить корзину из сессии
     */
    private void clearCartFromSession(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    /**
     * Создать новую корзину
     */
    @Transactional
    protected Cart createNewCart() {
        Cart cart = new Cart();
        cart.setCartUuid(UUID.randomUUID().toString());
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusDays(cartExpirationDays));

        // Сохраняем информацию о клиенте для анонимных корзин
        cart.setIpAddress(getClientIpAddress());

        cart = cartRepository.save(cart);

        // Устанавливаем cookie
        setCartCookie(cart.getCartUuid());

        return cart;
    }

    /**
     * Создать новый элемент корзины с установкой времени
     */
    private CartItem createCartItem(Cart cart, Product product, Integer quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtTime(product.getPrice());

        LocalDateTime now = LocalDateTime.now();
        item.setAddedAt(now);
        item.setUpdatedAt(now);

        return item;
    }

    /**
     * Получить UUID корзины из cookie
     */
    private String getCartUuidFromCookie() {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> CART_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Установить cookie с UUID корзины
     */
    private void setCartCookie(String cartUuid) {
        Cookie cookie = new Cookie(CART_COOKIE_NAME, cartUuid);
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Раскомментировать для HTTPS
        response.addCookie(cookie);
    }

    /**
     * Получить IP адрес клиента
     */
    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Получить корзину без создания новой (только для чтения) с кешированием
     * ИСПРАВЛЕНО: Используем FETCH JOIN для избежания LazyInitializationException
     */
    @Transactional(readOnly = true)
    protected Cart getCartReadOnly(HttpSession session) {
        // Сначала проверяем сессию
        Cart cachedCart = getCartFromSession(session);
        if (cachedCart != null && !isCartExpired(cachedCart)) {
            return cachedCart;
        }

        // Если в сессии нет или корзина истекла, загружаем из БД
        String cartUuid = getCartUuidFromCookie();

        if (cartUuid != null) {
            // ИСПРАВЛЕНО: Используем метод с FETCH JOIN для избежания LazyInitializationException
            Optional<Cart> cartOpt = cartRepository.findByCartUuidAndStatusWithItems(cartUuid, CartStatus.ACTIVE);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                if (!isCartExpired(cart)) {
                    // Сохраняем в сессии для последующих обращений (только для чтения)
                    updateCartInSession(session, cart);
                    return cart;
                }
            }
        }

        // Возвращаем пустую корзину для отображения
        Cart emptyCart = createEmptyCart();
        updateCartInSession(session, emptyCart);
        return emptyCart;
    }

    /**
     * Проверить истечение корзины
     */
    private boolean isCartExpired(Cart cart) {
        return cart.getExpiresAt() != null && LocalDateTime.now().isAfter(cart.getExpiresAt());
    }

    /**
     * Создать пустую корзину для отображения (без сохранения в БД)
     */
    private Cart createEmptyCart() {
        Cart cart = new Cart();
        cart.setCartUuid("");
        cart.setStatus(CartStatus.ACTIVE);
        // Инициализируем пустую коллекцию items для избежания NullPointerException
        cart.setItems(new ArrayList<>());
        return cart;
    }
}