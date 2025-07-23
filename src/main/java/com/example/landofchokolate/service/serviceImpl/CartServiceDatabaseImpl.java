package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.card.CartDto;
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
import org.hibernate.LazyInitializationException;
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
        log.info("Добавляем продукт {} (количество: {}) в корзину", productId, quantity);

        // Загружаем продукт
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // Проверки продукта
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new RuntimeException("Товар недоступен");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Недостаточно товара на складе");
        }

        // Получаем или создаем корзину
        Cart cart = getOrCreateCart(session);
        log.info("Используем корзину: ID={}, UUID='{}'", cart.getId(), cart.getCartUuid());

        // Проверяем, есть ли уже такой товар в корзине
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Увеличиваем количество существующего товара
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Недостаточно товара на складе");
            }

            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now());
            item.setProduct(product);

            log.info("Обновлено количество товара {} до {}", productId, newQuantity);
        } else {
            // Добавляем новый товар в корзину
            CartItem newItem = createCartItem(cart, product, quantity);
            cart.getItems().add(newItem);

            log.info("Добавлен новый товар {} в корзину", productId);
        }

        // Обновляем и сохраняем корзину
        updateCartActivity(cart);
        cart = cartRepository.save(cart);

        // Принудительно инициализируем данные перед сохранением в сессию
        initializeCartData(cart);

        // Обновляем корзину в сессии
        updateCartInSession(session, cart);

        log.info("Товар {} успешно добавлен в корзину {}", productId, cart.getCartUuid());
    }

    @Override
    @Transactional
    public void updateQuantity(HttpSession session, Long productId, Integer quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }

        if (quantity == null) {
            throw new IllegalArgumentException("Количество не может быть null");
        }

        if (quantity <= 0) {
            removeProduct(session, productId);
            return;
        }

        Cart cart = getOrCreateCart(session);

        // Загружаем продукт
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new RuntimeException("Товар больше не доступен");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Недостаточно товара на складе. Доступно: " + product.getStockQuantity());
        }

        // Ищем товар в корзине
        CartItem targetItem = cart.getItems().stream()
                .filter(item -> item.getProduct() != null && productId.equals(item.getProduct().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Товар не найден в корзине"));

        // Обновляем количество
        targetItem.setQuantity(quantity);
        targetItem.setUpdatedAt(LocalDateTime.now());
        targetItem.setProduct(product);

        // Сохраняем изменения
        updateCartActivity(cart);
        cart = cartRepository.save(cart);
        updateCartInSession(session, cart);

        log.debug("Количество товара {} в корзине обновлено до {}", productId, quantity);
    }

    @Override
    @Transactional
    public void removeProduct(HttpSession session, Long productId) {
        Cart cart = getOrCreateCart(session);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        updateCartActivity(cart);
        cart = cartRepository.save(cart);

        updateCartInSession(session, cart);
    }

    @Override
    @Transactional
    public void clearCart(HttpSession session) {
        Cart cart = getOrCreateCart(session);
        cart.getItems().clear();
        updateCartActivity(cart);
        cart = cartRepository.save(cart);

        updateCartInSession(session, cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemCount(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return calculateCartTotalQuantity(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return calculateCartTotalPrice(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCartEmpty(HttpSession session) {
        Cart cart = getCartReadOnly(session);
        return cart.getItems().isEmpty();
    }

    // =============================================================================
    // ПРИВАТНЫЕ МЕТОДЫ
    // =============================================================================

    /**
     * ИСПРАВЛЕННЫЙ метод создания новой корзины
     */
    @Transactional
    protected Cart createNewCart() {
        log.info("=== СОЗДАНИЕ НОВОЙ КОРЗИНЫ - НАЧАЛО ===");

        String newCartUuid = UUID.randomUUID().toString();
        log.info("Step 1: Сгенерирован UUID: '{}'", newCartUuid);

        if (newCartUuid == null || newCartUuid.trim().isEmpty()) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА: UUID.randomUUID() вернул пустое значение!");
            throw new RuntimeException("Failed to generate cart UUID");
        }

        // СОЗДАЕМ КОРЗИНУ ЧЕРЕЗ new Cart() - БЕЗ ПАРАМЕТРОВ
        Cart cart = new Cart();
        log.info("Step 2: Создан объект Cart с конструктором по умолчанию");

        // ЯВНО УСТАНАВЛИВАЕМ ВСЕ ПОЛЯ
        cart.setCartUuid(newCartUuid);
        log.info("Step 3: Установлен cartUuid: '{}'", cart.getCartUuid());

        cart.setStatus(CartStatus.ACTIVE);
        log.info("Step 4: Установлен status: {}", cart.getStatus());

        cart.setIpAddress(getClientIpAddress());
        log.info("Step 5: Установлен ipAddress: {}", cart.getIpAddress());

        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 500) {
            userAgent = userAgent.substring(0, 500); // Обрезаем до максимальной длины
        }
        cart.setUserAgent(userAgent);

        cart.setExpiresAt(LocalDateTime.now().plusDays(cartExpirationDays));


        // Инициализируем временные поля
        LocalDateTime now = LocalDateTime.now();
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);
        cart.setLastActivityAt(now);


        // Инициализируем список items (хотя он должен быть инициализирован по умолчанию)
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        log.info("Step 9: Инициализирован список items (size: {})", cart.getItems().size());


        if (cart.getCartUuid() == null || cart.getCartUuid().trim().isEmpty()) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА: cartUuid пустой перед сохранением!");
            throw new RuntimeException("Cart UUID is null or empty before saving");
        }

        try {
            // Проверяем, не существует ли уже такой UUID
            if (cartRepository.existsByCartUuidAndStatus(cart.getCartUuid(), CartStatus.ACTIVE)) {
                log.warn("UUID {} уже существует! Генерируем новый.", cart.getCartUuid());
                String newUuid = UUID.randomUUID().toString();
                cart.setCartUuid(newUuid);
                log.info("Новый UUID установлен: '{}'", cart.getCartUuid());
            }

            log.info("Step 10: Сохраняем корзину в БД...");
            Cart savedCart = cartRepository.save(cart);
            log.info("Step 11: Корзина сохранена с ID: {}", savedCart.getId());


            if (savedCart.getCartUuid() == null || savedCart.getCartUuid().trim().isEmpty()) {
                log.error("КРИТИЧЕСКАЯ ОШИБКА: cartUuid пустой ПОСЛЕ сохранения!");
                throw new RuntimeException("Cart UUID became null or empty after saving");
            }

            // Устанавливаем cookie
            setCartCookie(savedCart.getCartUuid());


            return savedCart;

        } catch (Exception e) {
            log.error("Ошибка при создании корзины: {}", e.getMessage(), e);

            if (e.getCause() != null) {
                log.error("Причина ошибки: {}", e.getCause().getMessage());
            }

            throw new RuntimeException("Failed to create new cart: " + e.getMessage(), e);
        }
    }

    @Transactional
    protected Cart getOrCreateCart(HttpSession session) {
        log.debug("=== ПОЛУЧЕНИЕ ИЛИ СОЗДАНИЕ КОРЗИНЫ ===");

        // Сначала проверяем сессию
        Cart cachedCart = getCartFromSession(session);
        if (cachedCart != null && !isCartExpired(cachedCart)) {
            log.debug("Найдена корзина в сессии: UUID='{}'", cachedCart.getCartUuid());

            if (cachedCart.getCartUuid() == null || cachedCart.getCartUuid().trim().isEmpty()) {
                log.warn("Корзина в сессии имеет пустой UUID, удаляем из сессии");
                clearCartFromSession(session);
            } else {
                return cachedCart;
            }
        }

        // Если в сессии нет или корзина истекла, загружаем из БД
        String cartUuid = getCartUuidFromCookie();

        if (cartUuid != null && !cartUuid.trim().isEmpty()) {
            Optional<Cart> cartOpt = cartRepository.findByCartUuidAndStatusWithItems(cartUuid, CartStatus.ACTIVE);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                log.debug("Найдена корзина в БД: ID={}, UUID='{}'", cart.getId(), cart.getCartUuid());

                if (!isCartExpired(cart)) {
                    updateCartActivity(cart);
                    cart = cartRepository.save(cart);
                    updateCartInSession(session, cart);
                    return cart;
                } else {

                    cart.setStatus(CartStatus.EXPIRED);
                    cartRepository.save(cart);
                    clearCartFromSession(session);
                }
            } else {
                log.warn("Корзина с UUID '{}' не найдена в БД", cartUuid);
            }
        } else {
            log.debug("UUID корзины отсутствует в куки");
        }

        // Создаём новую корзину
        log.info("Создаём новую корзину");
        Cart newCart = createNewCart();

        if (newCart.getCartUuid() == null || newCart.getCartUuid().trim().isEmpty()) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА: новая корзина имеет пустой UUID!");
            throw new RuntimeException("New cart has empty UUID");
        }

        updateCartInSession(session, newCart);
        return newCart;
    }

    @Transactional(readOnly = true)
    protected Cart getCartReadOnly(HttpSession session) {
        Cart cachedCart = getCartFromSession(session);
        if (cachedCart != null && !isCartExpired(cachedCart)) {
            if (isCartFullyLoaded(cachedCart)) {
                return cachedCart;
            }
        }

        String cartUuid = getCartUuidFromCookie();
        if (cartUuid != null) {
            Optional<Cart> cartOpt = cartRepository.findByCartUuidAndStatusWithItems(cartUuid, CartStatus.ACTIVE);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                if (!isCartExpired(cart)) {
                    initializeCartData(cart);
                    updateCartInSession(session, cart);
                    return cart;
                }
            }
        }

        Cart emptyCart = createEmptyCart();
        updateCartInSession(session, emptyCart);
        return emptyCart;
    }

    // Вспомогательные методы
    private void updateCartActivity(Cart cart) {
        cart.setLastActivityAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
    }

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

    private Integer calculateCartTotalQuantity(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private BigDecimal calculateCartTotalPrice(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Cart getCartFromSession(HttpSession session) {
        try {
            return (Cart) session.getAttribute(CART_SESSION_KEY);
        } catch (ClassCastException e) {
            log.warn("Invalid cart object in session, removing", e);
            session.removeAttribute(CART_SESSION_KEY);
            return null;
        }
    }

    private void updateCartInSession(HttpSession session, Cart cart) {
        log.debug("updateCartInSession: входящий cart UUID='{}'", cart.getCartUuid());

        if (cart.getCartUuid() == null || cart.getCartUuid().trim().isEmpty()) {
            log.error("ПОПЫТКА СОХРАНИТЬ КОРЗИНУ С ПУСТЫМ UUID В СЕССИЮ!");
            return;
        }

        session.setAttribute(CART_SESSION_KEY, cart);
        log.debug("Корзина сохранена в сессии с UUID='{}'", cart.getCartUuid());
    }

    private void clearCartFromSession(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

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

    private void setCartCookie(String cartUuid) {


        if (cartUuid == null || cartUuid.trim().isEmpty()) {
            log.error("Попытка установить пустую куку {}!", CART_COOKIE_NAME);
            return;
        }

        try {
            Cookie cookie = new Cookie(CART_COOKIE_NAME, cartUuid);
            cookie.setMaxAge(cookieMaxAge);
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            cookie.setSecure(false);   // для localhost

            response.addCookie(cookie);

            log.info("Кука {} успешно установлена: '{}'", CART_COOKIE_NAME, cartUuid);

        } catch (Exception e) {
            log.error("Ошибка при установке куки {}: {}", CART_COOKIE_NAME, e.getMessage(), e);
        }
    }

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

    private void initializeCartData(Cart cart) {
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.getId();
                    product.getName();
                    product.getPrice();
                    product.getImageUrl();
                    product.getStockQuantity();
                    product.getIsActive();
                    product.getSlug();
                }
            }
        }
    }

    private boolean isCartFullyLoaded(Cart cart) {
        if (cart.getItems() == null) {
            return true;
        }

        try {
            for (CartItem item : cart.getItems()) {
                if (item.getProduct() != null) {
                    item.getProduct().getName();
                    item.getProduct().getPrice();
                }
            }
            return true;
        } catch (LazyInitializationException e) {
            return false;
        }
    }

    private boolean isCartExpired(Cart cart) {
        return cart.getExpiresAt() != null && LocalDateTime.now().isAfter(cart.getExpiresAt());
    }

    private Cart createEmptyCart() {
        Cart cart = new Cart();
        cart.setCartUuid("");
        cart.setStatus(CartStatus.ACTIVE);
        cart.setItems(new ArrayList<>());
        return cart;
    }
}