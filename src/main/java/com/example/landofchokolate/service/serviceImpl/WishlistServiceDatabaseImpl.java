package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.wishlis.WishlistDto;
import com.example.landofchokolate.enums.WishlistStatus;
import com.example.landofchokolate.mapper.WishlistMapper;
import com.example.landofchokolate.model.Cart;
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
        WishlistDto wishlistDto =wishlistMapper.toDto(wishlist);
        return wishlistDto;

    }


    /**
     * Отладочный метод для проверки состояния wishlist
     */
    private void debugWishlistState(String methodName, HttpSession session) {
        log.debug("=== {} ===", methodName);

        // Проверяем сессию
        Wishlist sessionWishlist = getWishlistFromSession(session);
        log.debug("Session wishlist: {}", sessionWishlist != null ? sessionWishlist.getWishlistUuid() : "null");

        // Проверяем cookie
        String cookieUuid = getWishlistUuidFromCookie();
        log.debug("Cookie UUID: {}", cookieUuid);

        // Проверяем базу данных
        if (cookieUuid != null && !cookieUuid.isEmpty()) {
            Optional<Wishlist> dbWishlist = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                    cookieUuid, WishlistStatus.ACTIVE);
            log.debug("DB wishlist found: {}, items count: {}",
                    dbWishlist.isPresent(),
                    dbWishlist.map(w -> w.getItems().size()).orElse(0));
        }

        log.debug("===============");
    }

    /**
     * ИСПРАВЛЕННЫЙ метод добавления товара с обновлением cookie
     */
    @Override
    @Transactional
    public void addProduct(HttpSession session, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new RuntimeException("Товар недоступен для добавления в избранное");
        }

        Wishlist wishlist = getOrCreateWishlist(session);

        // Проверяем, есть ли уже такой товар в избранном
        if (containsProduct(wishlist, productId)) {
            log.debug("Товар {} уже находится в избранном", productId);
            return;
        }

        // Создаем и добавляем новый элемент
        WishlistItem newItem = createWishlistItem(wishlist, product);
        addItemToWishlist(wishlist, newItem);

        // Обновляем активность
        updateWishlistActivity(wishlist);

        wishlist = wishlistRepository.save(wishlist);

        // ВАЖНО: Убеждаемся, что cookie соответствует текущему wishlist
        String cookieUuid = getWishlistUuidFromCookie();
        if (!wishlist.getWishlistUuid().equals(cookieUuid)) {
            log.warn("Cookie UUID {} не соответствует wishlist UUID {}. Обновляем cookie.",
                    cookieUuid, wishlist.getWishlistUuid());
            setWishlistCookie(wishlist.getWishlistUuid());
        }

        // Принудительно инициализируем данные перед сохранением в сессию
        initializeWishlistData(wishlist);

        // Обновляем wishlist в сессии
        updateWishlistInSession(session, wishlist);

        log.debug("Товар {} добавлен в избранное, wishlist UUID: {}", productId, wishlist.getWishlistUuid());
    }

    /**
     * Метод для синхронизации cookie с текущим wishlist
     */
    @Override
    @Transactional
    public Map<String, Object> syncCookieWithSession(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Wishlist sessionWishlist = getWishlistFromSession(session);
            String cookieUuid = getWishlistUuidFromCookie();

            result.put("sessionUuid", sessionWishlist != null ? sessionWishlist.getWishlistUuid() : null);
            result.put("cookieUuid", cookieUuid);

            if (sessionWishlist != null && !sessionWishlist.getWishlistUuid().equals(cookieUuid)) {
                // Обновляем cookie
                setWishlistCookie(sessionWishlist.getWishlistUuid());
                result.put("updated", true);
                result.put("newCookieUuid", sessionWishlist.getWishlistUuid());
                log.info("Cookie синхронизирован с сессией: {}", sessionWishlist.getWishlistUuid());
            } else {
                result.put("updated", false);
                result.put("message", "Cookie уже синхронизирован");
            }

            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("Ошибка при синхронизации cookie", e);
        }

        return result;
    }

    @Override
    @Transactional
    public void removeProduct(HttpSession session, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("ID товара не может быть null");
        }

        Wishlist wishlist = getOrCreateWishlist(session);

        boolean removed = removeProductFromWishlist(wishlist, productId);

        if (removed) {
            updateWishlistActivity(wishlist);
            wishlist = wishlistRepository.save(wishlist);
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
        return containsProduct(wishlist, productId);
    }

    @Override
    @Transactional
    public void clearWishlist(HttpSession session) {
        Wishlist wishlist = getOrCreateWishlist(session);
        clearWishlistItems(wishlist);
        updateWishlistActivity(wishlist);
        wishlist = wishlistRepository.save(wishlist);
        updateWishlistInSession(session, wishlist);
        log.debug("Избранное очищено");
    }

    @Override
    @Transactional(readOnly = true)
    public int getWishlistItemCount(HttpSession session) {
        Wishlist wishlist = getWishlistReadOnly(session);
        return getItemCount(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWishlistEmpty(HttpSession session) {
        Wishlist wishlist = getWishlistReadOnly(session);
        return isWishlistEmpty(wishlist);
    }

    @Override
    @Transactional
    public void toggleProduct(HttpSession session, Long productId, String addedFromPage) {
        if (isProductInWishlist(session, productId)) {
            removeProduct(session, productId);
        } else {
            addProduct(session, productId);
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

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> checkProductsInWishlist(HttpSession session, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new HashMap<>();
        }

        Set<Long> wishlistProductIds = new HashSet<>(getWishlistProductIds(session));
        Map<Long, Boolean> result = new HashMap<>();

        for (Long productId : productIds) {
            if (productId != null) {
                result.put(productId, wishlistProductIds.contains(productId));
            }
        }
        return result;
    }

    // ========== БИЗНЕС-МЕТОДЫ ДЛЯ РАБОТЫ С WISHLIST ==========

    private boolean containsProduct(Wishlist wishlist, Long productId) {
        if (wishlist.getItems() == null || productId == null) {
            return false;
        }
        return wishlist.getItems().stream()
                .anyMatch(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()));
    }

    private void addItemToWishlist(Wishlist wishlist, WishlistItem item) {
        if (wishlist.getItems() == null) {
            wishlist.setItems(new ArrayList<>());
        }
        wishlist.getItems().add(item);
        item.setWishlist(wishlist);
    }

    private boolean removeProductFromWishlist(Wishlist wishlist, Long productId) {
        if (wishlist.getItems() == null) {
            return false;
        }
        return wishlist.getItems().removeIf(item ->
                item.getProduct() != null && productId.equals(item.getProduct().getId()));
    }

    private void clearWishlistItems(Wishlist wishlist) {
        if (wishlist.getItems() != null) {
            wishlist.getItems().clear();
        }
    }

    private int getItemCount(Wishlist wishlist) {
        return wishlist.getItems() != null ? wishlist.getItems().size() : 0;
    }

    private boolean isWishlistEmpty(Wishlist wishlist) {
        return wishlist.getItems() == null || wishlist.getItems().isEmpty();
    }

    private boolean isWishlistExpired(Wishlist wishlist) {
        return wishlist.getExpiresAt() != null && LocalDateTime.now().isAfter(wishlist.getExpiresAt());
    }

    private void updateWishlistActivity(Wishlist wishlist) {
        LocalDateTime now = LocalDateTime.now();
        wishlist.setLastActivityAt(now);
        wishlist.setUpdatedAt(now);
    }

    // ========== УПРОЩЕННЫЕ МЕТОДЫ СОЗДАНИЯ WISHLIST ==========

    /**
     * Создать новый wishlist - теперь UUID генерируется в @PrePersist
     */
    private Wishlist createNewWishlist() {
        Wishlist wishlist = new Wishlist();
        // wishlistUuid будет автоматически сгенерирован в @PrePersist
        wishlist.setStatus(WishlistStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        wishlist.setExpiresAt(now.plusDays(wishlistExpirationDays));
        wishlist.setItems(new ArrayList<>());

        return wishlist;
    }

    private Wishlist createTemporaryWishlist() {
        Wishlist wishlist = new Wishlist();
        // Для временного wishlist установим специальный UUID
        wishlist.setWishlistUuid("temp-" + System.currentTimeMillis());
        wishlist.setStatus(WishlistStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        wishlist.setCreatedAt(now);
        wishlist.setUpdatedAt(now);
        wishlist.setLastActivityAt(now);
        wishlist.setExpiresAt(now.plusDays(1));
        wishlist.setItems(new ArrayList<>());

        return wishlist;
    }

    private Wishlist createEmptyWishlist() {
        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistUuid("");
        wishlist.setStatus(WishlistStatus.ACTIVE);
        wishlist.setItems(new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();
        wishlist.setCreatedAt(now);
        wishlist.setUpdatedAt(now);
        wishlist.setLastActivityAt(now);

        return wishlist;
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ СЕРВИСА ==========

    /**
     * ИСПРАВЛЕННЫЙ метод getOrCreateWishlist с улучшенной отладкой
     */
    @Transactional
    protected Wishlist getOrCreateWishlist(HttpSession session) {
        debugWishlistState("getOrCreateWishlist - START", session);

        // 1. Проверяем сессию
        Wishlist cachedWishlist = getWishlistFromSession(session);
        if (cachedWishlist != null && !isWishlistExpired(cachedWishlist)) {
            log.debug("Используем wishlist из сессии: {}", cachedWishlist.getWishlistUuid());
            return cachedWishlist;
        }

        // 2. Проверяем cookie и загружаем из БД
        String wishlistUuid = getWishlistUuidFromCookie();
        log.debug("UUID из cookie: {}", wishlistUuid);

        if (wishlistUuid != null && !wishlistUuid.isEmpty() && !wishlistUuid.startsWith("temp-")) {
            try {
                log.debug("Пытаемся загрузить wishlist из БД с UUID: {}", wishlistUuid);

                Optional<Wishlist> wishlistOpt = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                        wishlistUuid, WishlistStatus.ACTIVE);

                if (wishlistOpt.isPresent()) {
                    Wishlist wishlist = wishlistOpt.get();
                    log.debug("Найден wishlist в БД, количество товаров: {}", wishlist.getItems().size());

                    if (!isWishlistExpired(wishlist)) {
                        updateWishlistActivity(wishlist);
                        wishlist = wishlistRepository.save(wishlist);

                        // ВАЖНО: Принудительно инициализируем данные
                        initializeWishlistData(wishlist);

                        // Восстанавливаем в сессии
                        updateWishlistInSession(session, wishlist);

                        log.debug("Wishlist успешно восстановлен из БД");
                        return wishlist;
                    } else {
                        log.debug("Wishlist истек, помечаем как EXPIRED");
                        wishlist.setStatus(WishlistStatus.EXPIRED);
                        wishlistRepository.save(wishlist);
                        clearWishlistFromSession(session);
                    }
                } else {
                    log.debug("Wishlist с UUID {} не найден в БД", wishlistUuid);
                }
            } catch (Exception e) {
                log.error("Ошибка при загрузке wishlist с UUID: {}", wishlistUuid, e);
                clearWishlistFromSession(session);
                clearWishlistCookie();
            }
        }

        // 3. Создаём новый wishlist
        try {
            Wishlist newWishlist = createNewWishlistAndSave();
            updateWishlistInSession(session, newWishlist);
            log.debug("Создан новый wishlist: {}", newWishlist.getWishlistUuid());
            return newWishlist;
        } catch (Exception e) {
            log.error("Критическая ошибка при создании нового wishlist", e);
            return createTemporaryWishlistForSession(session);
        }
    }

    /**
     * ИСПРАВЛЕННЫЙ метод создания и сохранения wishlist
     */
    @Transactional
    protected Wishlist createNewWishlistAndSave() {
        Wishlist wishlist = createNewWishlist();

        // Добавляем информацию о клиенте
        wishlist.setIpAddress(getClientIpAddress());
        wishlist.setUserAgent(getUserAgent());

        try {
            // Сохраняем в БД
            wishlist = wishlistRepository.save(wishlist);

            log.info("Wishlist сохранен в БД с UUID: {}", wishlist.getWishlistUuid());

            // Проверяем, что действительно сохранился
            Optional<Wishlist> verification = wishlistRepository.findByWishlistUuid(wishlist.getWishlistUuid());
            if (!verification.isPresent()) {
                throw new RuntimeException("Wishlist не найден в БД после сохранения!");
            }

            // ВАЖНО: Устанавливаем cookie ПОСЛЕ сохранения
            setWishlistCookie(wishlist.getWishlistUuid());

            log.info("Cookie установлен для нового wishlist: {}", wishlist.getWishlistUuid());
            return wishlist;

        } catch (Exception e) {
            log.error("Ошибка при сохранении нового wishlist", e);
            throw new RuntimeException("Не удалось создать новый wishlist", e);
        }
    }

    private Wishlist createTemporaryWishlistForSession(HttpSession session) {
        Wishlist tempWishlist = createTemporaryWishlist();
        updateWishlistInSession(session, tempWishlist);
        log.warn("Создан временный wishlist: {}", tempWishlist.getWishlistUuid());
        return tempWishlist;
    }

    private WishlistItem createWishlistItem(Wishlist wishlist, Product product) {
        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProduct(product);
        item.setAddedAt(LocalDateTime.now());

        String currentPage = getCurrentPageFromRequest();
        item.setAddedFromPage(currentPage);

        return item;
    }

    private String getCurrentPageFromRequest() {
        try {
            String requestURI = request.getRequestURI();
            String queryString = request.getQueryString();

            if (queryString != null) {
                return requestURI + "?" + queryString;
            }
            return requestURI;
        } catch (Exception e) {
            log.debug("Не удалось получить текущую страницу: {}", e.getMessage());
            return "unknown";
        }
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С СЕССИЕЙ И COOKIE ==========

    private Wishlist getWishlistFromSession(HttpSession session) {
        try {
            return (Wishlist) session.getAttribute(WISHLIST_SESSION_KEY);
        } catch (ClassCastException e) {
            log.warn("Invalid wishlist object in session, removing", e);
            session.removeAttribute(WISHLIST_SESSION_KEY);
            return null;
        }
    }

    private void updateWishlistInSession(HttpSession session, Wishlist wishlist) {
        session.setAttribute(WISHLIST_SESSION_KEY, wishlist);
    }

    private void clearWishlistFromSession(HttpSession session) {
        session.removeAttribute(WISHLIST_SESSION_KEY);
    }

    /**
     * Улучшенная обработка cookie
     */
    private String getWishlistUuidFromCookie() {
        if (request.getCookies() != null) {
            String uuid = Arrays.stream(request.getCookies())
                    .filter(cookie -> WISHLIST_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            log.debug("Cookie UUID: {}", uuid);
            return uuid;
        }
        log.debug("Нет cookies в запросе");
        return null;
    }

    private void setWishlistCookie(String wishlistUuid) {
        log.debug("Устанавливаем cookie для UUID: {}", wishlistUuid);

        try {
            Cookie cookie = new Cookie(WISHLIST_COOKIE_NAME, wishlistUuid);
            cookie.setMaxAge(cookieMaxAge);
            cookie.setPath("/");
            cookie.setHttpOnly(true);

            // Для локальной разработки отключаем secure
            cookie.setSecure(false); // Измените на true для production с HTTPS

            // Добавляем SameSite политику
            cookie.setAttribute("SameSite", "Lax");

            response.addCookie(cookie);

            log.debug("Cookie успешно добавлен в response: {} = {}", WISHLIST_COOKIE_NAME, wishlistUuid);

            // Принудительно коммитим response для установки cookie
            try {
                response.flushBuffer();
            } catch (Exception e) {
                log.debug("Не удалось принудительно flush response: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Ошибка при установке cookie", e);
        }
    }

    private void clearWishlistCookie() {
        Cookie cookie = new Cookie(WISHLIST_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

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

    private String getUserAgent() {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && userAgent.length() > 500 ?
                userAgent.substring(0, 500) : userAgent;
    }
    /**
     * ИСПРАВЛЕННЫЙ метод getWishlistReadOnly с принудительной загрузкой из БД
     */
    @Transactional(readOnly = true)
    protected Wishlist getWishlistReadOnly(HttpSession session) {
        debugWishlistState("getWishlistReadOnly - START", session);

        // 1. Проверяем сессию
        Wishlist cachedWishlist = getWishlistFromSession(session);
        if (cachedWishlist != null && !isWishlistExpired(cachedWishlist)) {
            if (isWishlistFullyLoaded(cachedWishlist)) {
                log.debug("Используем полностью загруженный wishlist из сессии");
                return cachedWishlist;
            }
        }

        // 2. Принудительно загружаем из БД по cookie
        String wishlistUuid = getWishlistUuidFromCookie();
        log.debug("Загружаем из БД по cookie UUID: {}", wishlistUuid);

        if (wishlistUuid != null && !wishlistUuid.isEmpty() && !wishlistUuid.startsWith("temp-")) {
            Optional<Wishlist> wishlistOpt = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                    wishlistUuid, WishlistStatus.ACTIVE);

            if (wishlistOpt.isPresent()) {
                Wishlist wishlist = wishlistOpt.get();
                log.debug("Найден wishlist в БД для чтения, товаров: {}", wishlist.getItems().size());

                if (!isWishlistExpired(wishlist)) {
                    // Принудительно инициализируем все proxy-объекты
                    initializeWishlistData(wishlist);

                    // Сохраняем в сессии для последующих обращений
                    updateWishlistInSession(session, wishlist);
                    return wishlist;
                }
            }
        }

        // 3. Возвращаем пустой wishlist
        log.debug("Возвращаем пустой wishlist");
        Wishlist emptyWishlist = createEmptyWishlist();
        updateWishlistInSession(session, emptyWishlist);
        return emptyWishlist;
    }

    /**
     * Улучшенная инициализация данных wishlist
     */
    private void initializeWishlistData(Wishlist wishlist) {
        log.debug("Инициализируем данные wishlist с {} элементами",
                wishlist.getItems() != null ? wishlist.getItems().size() : 0);

        if (wishlist.getItems() != null) {
            for (WishlistItem item : wishlist.getItems()) {
                if (item.getProduct() != null) {
                    try {
                        // Принудительно инициализируем proxy Product
                        Product product = item.getProduct();
                        product.getId();
                        product.getName();
                        product.getPrice();
                        product.getImageUrl();
                        product.getStockQuantity();
                        product.getIsActive();
                        product.getSlug();

                        log.debug("Инициализирован товар: {} - {}", product.getId(), product.getName());
                    } catch (Exception e) {
                        log.error("Ошибка при инициализации товара в wishlist", e);
                    }
                }
            }
        }
    }

    /**
     * Улучшенная проверка полной загрузки wishlist
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
            log.debug("Wishlist полностью загружен");
            return true;
        } catch (LazyInitializationException e) {
            log.debug("Wishlist не полностью загружен: {}", e.getMessage());
            return false;
        }
    }


    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getDebugInfo(HttpSession session) {
        Map<String, Object> debug = new HashMap<>();

        // Информация о сессии
        Wishlist sessionWishlist = getWishlistFromSession(session);
        debug.put("sessionWishlistUuid", sessionWishlist != null ? sessionWishlist.getWishlistUuid() : null);
        debug.put("sessionItemCount", sessionWishlist != null ? getItemCount(sessionWishlist) : 0);

        // Информация о cookie
        String cookieUuid = getWishlistUuidFromCookie();
        debug.put("cookieUuid", cookieUuid);

        // Информация о БД
        if (cookieUuid != null && !cookieUuid.isEmpty()) {
            Optional<Wishlist> dbWishlist = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                    cookieUuid, WishlistStatus.ACTIVE);
            debug.put("dbWishlistExists", dbWishlist.isPresent());
            debug.put("dbItemCount", dbWishlist.map(w -> w.getItems().size()).orElse(0));
            debug.put("dbWishlistExpired", dbWishlist.map(this::isWishlistExpired).orElse(false));
        }

        return debug;
    }


    @Transactional
    @Override
    public void updateWishlistCookie(HttpSession session) {
        Wishlist wishlist = getWishlistFromSession(session);
        if (wishlist != null && wishlist.getWishlistUuid() != null && !wishlist.getWishlistUuid().isEmpty()) {
            // Очищаем старый cookie
            clearWishlistCookie();

            // Устанавливаем новый
            setWishlistCookie(wishlist.getWishlistUuid());

            log.info("Cookie обновлен для wishlist: {}", wishlist.getWishlistUuid());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEnhancedDebugInfo(HttpSession session) {
        Map<String, Object> debug = new HashMap<>();

        // Информация о сессии
        Wishlist sessionWishlist = getWishlistFromSession(session);
        debug.put("sessionWishlistUuid", sessionWishlist != null ? sessionWishlist.getWishlistUuid() : null);
        debug.put("sessionItemCount", sessionWishlist != null ? getItemCount(sessionWishlist) : 0);

        // Информация о cookie
        String cookieUuid = getWishlistUuidFromCookie();
        debug.put("cookieUuid", cookieUuid);

        // ДЕТАЛЬНАЯ проверка БД
        if (cookieUuid != null && !cookieUuid.isEmpty()) {
            // Проверяем любой wishlist с этим UUID (включая неактивные)
            Optional<Wishlist> anyWishlist = wishlistRepository.findByWishlistUuid(cookieUuid);
            debug.put("anyWishlistInDB", anyWishlist.isPresent());

            if (anyWishlist.isPresent()) {
                Wishlist w = anyWishlist.get();
                debug.put("wishlistStatus", w.getStatus().toString());
                debug.put("wishlistCreatedAt", w.getCreatedAt());
                debug.put("wishlistExpiresAt", w.getExpiresAt());
                debug.put("wishlistExpired", isWishlistExpired(w));
                debug.put("actualItemCount", w.getItems() != null ? w.getItems().size() : 0);
            }

            // Проверяем только активные
            Optional<Wishlist> activeWishlist = wishlistRepository.findByWishlistUuidAndStatusWithItems(
                    cookieUuid, WishlistStatus.ACTIVE);
            debug.put("activeWishlistExists", activeWishlist.isPresent());
            debug.put("activeItemCount", activeWishlist.map(w -> w.getItems().size()).orElse(0));
        }

        // Статистика по всем wishlist в БД
        long totalWishlists = wishlistRepository.count();
        debug.put("totalWishlistsInDB", totalWishlists);

        // Проверяем последние созданные wishlist
        try {
            List<Wishlist> recentWishlists = wishlistRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .collect(Collectors.toList());

            List<Map<String, Object>> recentInfo = recentWishlists.stream()
                    .map(w -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("uuid", w.getWishlistUuid());
                        info.put("status", w.getStatus().toString());
                        info.put("createdAt", w.getCreatedAt());
                        info.put("itemCount", w.getItems() != null ? w.getItems().size() : 0);
                        return info;
                    })
                    .collect(Collectors.toList());

            debug.put("recentWishlists", recentInfo);
        } catch (Exception e) {
            debug.put("recentWishlistsError", e.getMessage());
        }

        return debug;
    }


    @Transactional
    public Map<String, Object> forceRecreateWishlist(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Очищаем сессию
            clearWishlistFromSession(session);

            // Очищаем cookie
            clearWishlistCookie();

            // Создаем новый wishlist
            Wishlist newWishlist = createNewWishlistAndSave();
            updateWishlistInSession(session, newWishlist);

            result.put("success", true);
            result.put("newUuid", newWishlist.getWishlistUuid());
            result.put("message", "Wishlist успешно пересоздан");

            log.info("Принудительно пересоздан wishlist: {}", newWishlist.getWishlistUuid());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("Ошибка при пересоздании wishlist", e);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findOrphanedWishlists() {
        try {
            // Ищем все wishlist за последние 24 часа
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

            return wishlistRepository.findAll().stream()
                    .filter(w -> w.getCreatedAt().isAfter(yesterday))
                    .map(w -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("uuid", w.getWishlistUuid());
                        info.put("status", w.getStatus().toString());
                        info.put("createdAt", w.getCreatedAt());
                        info.put("itemCount", w.getItems() != null ? w.getItems().size() : 0);
                        info.put("expired", isWishlistExpired(w));
                        info.put("ipAddress", w.getIpAddress());
                        return info;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка при поиске потерянных wishlist", e);
            return new ArrayList<>();
        }
    }
}