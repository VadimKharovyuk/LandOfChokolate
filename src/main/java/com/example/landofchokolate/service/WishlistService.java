package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.wishlis.WishlistDto;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

public interface WishlistService {

    /**
     * Получить DTO wishlist
     */
    WishlistDto getWishlistDto(HttpSession session);

    /**
     * Добавить товар в избранное
     */
    void addProduct(HttpSession session, Long productId);

    /**
     * Удалить товар из избранного
     */
    void removeProduct(HttpSession session, Long productId);

    /**
     * Проверить, находится ли товар в избранном
     */
    boolean isProductInWishlist(HttpSession session, Long productId);

    /**
     * Очистить избранное
     */
    void clearWishlist(HttpSession session);

    /**
     * Получить количество товаров в избранном
     */
    int getWishlistItemCount(HttpSession session);

    /**
     * Проверить, пусто ли избранное
     */
    boolean isWishlistEmpty(HttpSession session);

    /**
     * Переключить товар в избранном (добавить, если нет; удалить, если есть)
     */
    void toggleProduct(HttpSession session, Long productId, String addedFromPage);

    /**
     * Получить список ID товаров в избранном
     */
    List<Long> getWishlistProductIds(HttpSession session);

    /**
     * Проверить наличие нескольких товаров в избранном
     * @param productIds список ID товаров для проверки
     * @return Map где ключ - ID товара, значение - находится ли в избранном
     */
    Map<Long, Boolean> checkProductsInWishlist(HttpSession session, List<Long> productIds);

    Map<String, Object> getDebugInfo(HttpSession session);

    /**
     * Получить расширенную отладочную информацию
     */
    Map<String, Object> getEnhancedDebugInfo(HttpSession session);

    /**
     * Принудительно пересоздать wishlist
     */
    Map<String, Object> forceRecreateWishlist(HttpSession session);

    /**
     * Найти потерянные wishlist
     */
    List<Map<String, Object>> findOrphanedWishlists();

    /**
     * Обновить cookie для текущего wishlist
     */
    void updateWishlistCookie(HttpSession session);

    /**
     * Синхронизировать cookie с сессией
     */
    Map<String, Object> syncCookieWithSession(HttpSession session);


}