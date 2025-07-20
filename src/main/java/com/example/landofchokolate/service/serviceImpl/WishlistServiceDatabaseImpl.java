package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.wishlis.WishlistDto;
import com.example.landofchokolate.enums.WishlistStatus;
import com.example.landofchokolate.mapper.WishlistMapper;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.model.Wishlist;
import com.example.landofchokolate.model.WishlistItem;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.repository.WishlistRepository;
import com.example.landofchokolate.service.WishlistService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Service("wishlistServiceDatabase")
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceDatabaseImpl implements WishlistService {

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;

    private static final String WISHLIST_COOKIE_NAME = "wishlist_id";
    private static final String WISHLIST_SESSION_KEY = "current_wishlist";

    @Value("${app.wishlist.cookie.max-age:31536000}") // 1 год для wishlist
    private int cookieMaxAge;

    @Value("${app.wishlist.expiration.days:365}") // wishlist живет дольше корзины
    private int wishlistExpirationDays;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Override
    @Transactional(readOnly = true)
    public WishlistDto getWishlistDto(HttpSession session) {
        Wishlist wishlist = getWishlistReadOnly(session);
        return wishlistMapper.toDto(wishlist);
    }

    @Override
    @Transactional
    public void addProduct(HttpSession session, Long productId, String addedFromPage) {
        // Валидация входных параметров
        if (productId == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }

        // Загружаем продукт
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // Проверка активности товара
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new RuntimeException("Товар недоступен для добавления в избранное");
        }

        Wishlist wishlist = getOrCreateWishlist(session);

        // Проверяем, есть ли уже такой товар в избранном
        boolean productExists = wishlist.getItems().stream()
                .anyMatch(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()));

        if (productExists) {
            log.debug("Товар {} уже находится в избранном", productId);
            return; // Товар уже в избранном, ничего не делаем
        }

        // Добавляем новый товар в избранное
        WishlistItem newItem = createWishlistItem(wishlist, product, addedFromPage);
        wishlist.getItems().add(newItem);

        updateWishlistActivity(wishlist);
        wishlist = wishlistRepository.save(wishlist);

        // Принудительно инициализируем данные перед сохранением в сессию
        initializeWishlistData(wishlist);

        // Обновляем wishlist в сессии
        updateWishlistInSession(session, wishlist);

        log.debug("Товар {} добавлен в избранное с страницы {}", productId, addedFromPage);
    }

    @Override
    @Transactional
    public void removeProduct(HttpSession session, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }

        Wishlist wishlist = getOrCreateWishlist(session);

        boolean removed = wishlist.getItems().removeIf(item ->
                item.getProduct() != null && productId.equals(item.getProduct().getId()));

        if (removed) {
            updateWishlistActivity(wishlist);
            wishlist = wishlistRepository.save(wishlist);

            // Обновляем wishlist в сессии
            updateWishlistInSession(session, wishlist);

            log.debug("Товар {} удален из избранного", productId);
        } else {
            log.debug("Товар {} не найден в избранном для удаления", productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(HttpSession session, Long productId) {
        if (productId == null) {
            return false;
        }

        Wishlist wishlist = getWishlistReadOnly(session);
        return wishlist.getItems().stream()
                .anyMatch(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()));
    }

    @Override
    @Transactional
    public void clearWishlist(HttpSession session) {
        Wishlist wishlist = getOrCreateWishlist(session);
        wishlist.getItems().clear();
        updateWishlistActivity(wishlist);
        wishlist = wishlistRepository.save(wishlist);

        // Обновляем wishlist в сессии
        updateWishlistInSession(session, wishlist);

        log.debug("Избранное очищено");
    }

    @Override
    @Transactional(readOnly = true)
    public int getWishlistItemCount(HttpSession session) {
        Wishlist wishlist = getWishlistReadOnly(session);
        return calculateWishlistItemCount(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWishlistEmpty(HttpSession session) {
        Wishlist wishlist = getWishlistReadOnly(session);
        return checkIfWishlistEmpty(wishlist);
    }

    @Override
    @Transactional
    public void toggleProduct(HttpSession session, Long productId, String addedFromPage) {
        if (isProductInWishlist(session, productId)) {
            removeProduct(session, productId);
        } else {
            addProduct(session, productId, addedFromPage);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWishlistProductIds(HttpSession session) {
        Wishlist wishlist = getWishlistReadOnly(session);
        return wishlist.getItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());
    }


    // Альтернативный вариант с более безопасным подходом:
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> checkProductsInWishlist(HttpSession session, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new HashMap<>();
        }

        Set<Long> wishlistProductIds = new HashSet<>(getWishlistProductIds(session));
        Map<Long, Boolean> result = new HashMap<>();

        // Обрабатываем каждый ID, автоматически убирая дубликаты
        for (Long productId : productIds) {
            if (productId != null) {
                result.put(productId, wishlistProductIds.contains(productId));
            }
        }

        return result;
    }

    /**
     * Обновить активность wishlist
     */
    private void updateWishlistActivity(Wishlist wishlist) {
        wishlist.setLastActivityAt(LocalDateTime.now());
    }

    /**
     * Подсчитать количество товаров в избранном
     */
    private int calculateWishlistItemCount(Wishlist wishlist) {
        return wishlist.getItems().size();
    }

    /**
     * Проверить, пуст ли wishlist
     */
    private boolean checkIfWishlistEmpty(Wishlist wishlist) {
        return wishlist.getItems().isEmpty();
    }

    /**
     * Получить или создать wishlist с использованием FETCH JOIN
     */
    @Transactional
    protected Wishlist getOrCreateWishlist(HttpSession session) {
        // Сначала проверяем сессию
        Wishlist cachedWishlist = getWishlistFromSession(session);
        if (cachedWishlist != null && !isWishlistExpired(cachedWishlist)) {
            return cachedWishlist;
        }

        // Если в сессии нет или wishlist истек, загружаем из БД
        String wishlistUuid = getWishlistUuidFromCookie();

        if (wishlistUuid != null) {
            // Используем оптимизированный запрос с FETCH JOIN
            Optional<Wishlist> wishlistOpt = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                    wishlistUuid, WishlistStatus.ACTIVE);
            if (wishlistOpt.isPresent()) {
                Wishlist wishlist = wishlistOpt.get();
                // Проверяем, не истек ли wishlist
                if (!isWishlistExpired(wishlist)) {
                    updateWishlistActivity(wishlist);
                    wishlist = wishlistRepository.save(wishlist);
                    // Сохраняем в сессии для последующих обращений
                    updateWishlistInSession(session, wishlist);
                    return wishlist;
                } else {
                    // Помечаем wishlist как истёкший
                    wishlist.setStatus(WishlistStatus.EXPIRED);
                    wishlistRepository.save(wishlist);
                    // Удаляем из сессии
                    clearWishlistFromSession(session);
                }
            }
        }

        // Создаём новый wishlist
        Wishlist newWishlist = createNewWishlist();
        updateWishlistInSession(session, newWishlist);
        return newWishlist;
    }

    /**
     * Получить wishlist из сессии
     */
    private Wishlist getWishlistFromSession(HttpSession session) {
        try {
            return (Wishlist) session.getAttribute(WISHLIST_SESSION_KEY);
        } catch (ClassCastException e) {
            log.warn("Invalid wishlist object in session, removing", e);
            session.removeAttribute(WISHLIST_SESSION_KEY);
            return null;
        }
    }

    /**
     * Обновить wishlist в сессии
     */
    private void updateWishlistInSession(HttpSession session, Wishlist wishlist) {
        session.setAttribute(WISHLIST_SESSION_KEY, wishlist);
    }

    /**
     * Очистить wishlist из сессии
     */
    private void clearWishlistFromSession(HttpSession session) {
        session.removeAttribute(WISHLIST_SESSION_KEY);
    }

    /**
     * Создать новый wishlist
     */
    @Transactional
    protected Wishlist createNewWishlist() {
        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistUuid(UUID.randomUUID().toString());
        wishlist.setStatus(WishlistStatus.ACTIVE);
        wishlist.setExpiresAt(LocalDateTime.now().plusDays(wishlistExpirationDays));

        // Сохраняем информацию о клиенте для анонимных wishlist
        wishlist.setIpAddress(getClientIpAddress());
        wishlist.setUserAgent(getUserAgent());

        wishlist = wishlistRepository.save(wishlist);

        // Устанавливаем cookie
        setWishlistCookie(wishlist.getWishlistUuid());

        log.debug("Создан новый wishlist с UUID: {}", wishlist.getWishlistUuid());
        return wishlist;
    }

    /**
     * Создать новый элемент wishlist
     */
    private WishlistItem createWishlistItem(Wishlist wishlist, Product product, String addedFromPage) {
        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProduct(product);
        item.setAddedFromPage(addedFromPage);

        LocalDateTime now = LocalDateTime.now();
        item.setAddedAt(now);

        return item;
    }

    /**
     * Получить UUID wishlist из cookie
     */
    private String getWishlistUuidFromCookie() {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> WISHLIST_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Установить cookie с UUID wishlist
     */
    private void setWishlistCookie(String wishlistUuid) {
        Cookie cookie = new Cookie(WISHLIST_COOKIE_NAME, wishlistUuid);
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Для HTTPS
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
     * Получить User Agent
     */
    private String getUserAgent() {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && userAgent.length() > 500 ?
                userAgent.substring(0, 500) : userAgent;
    }

    /**
     * Метод getWishlistReadOnly - гарантирует загрузку всех данных
     */
    @Transactional(readOnly = true)
    protected Wishlist getWishlistReadOnly(HttpSession session) {
        // Сначала проверяем сессию
        Wishlist cachedWishlist = getWishlistFromSession(session);
        if (cachedWishlist != null && !isWishlistExpired(cachedWishlist)) {
            // Проверяем, что все данные загружены (не proxy)
            if (isWishlistFullyLoaded(cachedWishlist)) {
                return cachedWishlist;
            }
        }

        // Если в сессии нет или wishlist истек, загружаем из БД
        String wishlistUuid = getWishlistUuidFromCookie();

        if (wishlistUuid != null) {
            // Используем метод с FETCH JOIN для избежания LazyInitializationException
            Optional<Wishlist> wishlistOpt = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                    wishlistUuid, WishlistStatus.ACTIVE);
            if (wishlistOpt.isPresent()) {
                Wishlist wishlist = wishlistOpt.get();
                if (!isWishlistExpired(wishlist)) {
                    // Принудительно инициализируем все proxy-объекты
                    initializeWishlistData(wishlist);

                    // Сохраняем в сессии для последующих обращений
                    updateWishlistInSession(session, wishlist);
                    return wishlist;
                }
            }
        }

        // Возвращаем пустой wishlist для отображения
        Wishlist emptyWishlist = createEmptyWishlist();
        updateWishlistInSession(session, emptyWishlist);
        return emptyWishlist;
    }

    /**
     * Принудительная инициализация всех данных wishlist
     */
    private void initializeWishlistData(Wishlist wishlist) {
        if (wishlist.getItems() != null) {
            for (WishlistItem item : wishlist.getItems()) {
                if (item.getProduct() != null) {
                    // Принудительно инициализируем proxy Product
                    Product product = item.getProduct();
                    // Обращаемся к полям, чтобы загрузить данные
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

    /**
     * Проверить, что wishlist полностью загружен (нет proxy-объектов)
     */
    private boolean isWishlistFullyLoaded(Wishlist wishlist) {
        if (wishlist.getItems() == null) {
            return true;
        }

        try {
            for (WishlistItem item : wishlist.getItems()) {
                if (item.getProduct() != null) {
                    // Пытаемся обратиться к данным продукта
                    item.getProduct().getName();
                    item.getProduct().getPrice();
                }
            }
            return true;
        } catch (LazyInitializationException e) {
            return false;
        }
    }

    /**
     * Проверить истечение wishlist
     */
    private boolean isWishlistExpired(Wishlist wishlist) {
        return wishlist.getExpiresAt() != null && LocalDateTime.now().isAfter(wishlist.getExpiresAt());
    }

    /**
     * Создать пустой wishlist для отображения (без сохранения в БД)
     */
    private Wishlist createEmptyWishlist() {
        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistUuid("");
        wishlist.setStatus(WishlistStatus.ACTIVE);
        // Инициализируем пустую коллекцию items для избежания NullPointerException
        wishlist.setItems(new ArrayList<>());
        return wishlist;
    }
}