package com.example.landofchokolate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Минимальная конфигурация для системы избранного (Wishlist)
 */
@Component
@Getter
public class WishlistConfig {

    /**
     * Время жизни cookie wishlist в секундах (по умолчанию 1 год)
     */
    @Value("${app.wishlist.cookie.max-age:31536000}")
    private int cookieMaxAge;

    /**
     * Время жизни данных wishlist в днях (по умолчанию 365 дней)
     */
    @Value("${app.wishlist.expiration.days:365}")
    private int wishlistExpirationDays;

    /**
     * Название cookie для wishlist
     */
    @Value("${app.wishlist.cookie.name:WISHLIST_ID}")
    private String cookieName;




}