package com.example.landofchokolate.config.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class StoreReviewCacheConfig {

    @Bean("storeReviewCacheManager")
    public CacheManager storeReviewCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Кеш для пагинированных списков отзывов
        Cache<Object, Object> reviewsListCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("Видалення з кешу списку відгуків: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("storeReviewsList", reviewsListCache);

        // Кеш для главной страницы (последние отзывы с лимитом)
        Cache<Object, Object> reviewsMainPageCache = Caffeine.newBuilder()
                .maximumSize(50) // Мало записей, т.к. обычно только разные лимиты (3, 5, 10)
                .expireAfterWrite(Duration.ofMinutes(20)) // Главная страница должна быть свежей
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("Видалення з кешу головної сторінки: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("storeReviewsMainPage", reviewsMainPageCache);

        return cacheManager;
    }
}
