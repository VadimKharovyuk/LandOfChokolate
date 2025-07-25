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
public class ProductCacheConfig {

    @Bean("productCacheManager")
    public CacheManager productCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Кеш для продуктов по ID - средней продолжительности
        Cache<Object, Object> productByIdCache = Caffeine.newBuilder()
                .maximumSize(2000)  // Много продуктов
                .expireAfterWrite(Duration.ofMinutes(20))  // Продукты изменяются чаще
                .expireAfterAccess(Duration.ofMinutes(10)) // Популярные продукты остаются дольше
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("ProductById cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("productById", productByIdCache);

        // Кеш для продуктов по slug - такой же как по ID
        Cache<Object, Object> productBySlugCache = Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofMinutes(20))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("ProductBySlug cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("productBySlug", productBySlugCache);

        // Кеш для списков продуктов без фильтров - короткое время жизни
        Cache<Object, Object> allProductsCache = Caffeine.newBuilder()
                .maximumSize(50)   // Разные страницы и сортировки
                .expireAfterWrite(Duration.ofMinutes(10))  // Быстро устаревает
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("AllProducts cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("allProducts", allProductsCache);

        // Кеш для фильтрованных списков продуктов - очень короткое время
        Cache<Object, Object> filteredProductsCache = Caffeine.newBuilder()
                .maximumSize(300)  // Много комбинаций фильтров
                .expireAfterWrite(Duration.ofMinutes(5))   // Очень быстро устаревает
                .expireAfterAccess(Duration.ofMinutes(3))  // Еще быстрее если не используется
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("FilteredProducts cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("filteredProducts", filteredProductsCache);

        // Кеш для продуктов по категории - средней продолжительности
        Cache<Object, Object> productsByCategoryCache = Caffeine.newBuilder()
                .maximumSize(100)  // Разные категории и страницы
                .expireAfterWrite(Duration.ofMinutes(15))
                .expireAfterAccess(Duration.ofMinutes(8))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("ProductsByCategory cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("productsByCategory", productsByCategoryCache);

        // Кеш для продуктов по бренду - средней продолжительности
        Cache<Object, Object> productsByBrandCache = Caffeine.newBuilder()
                .maximumSize(100)  // Разные бренды и страницы
                .expireAfterWrite(Duration.ofMinutes(15))
                .expireAfterAccess(Duration.ofMinutes(8))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("ProductsByBrand cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("productsByBrand", productsByBrandCache);

        // Кеш для популярных/рекомендуемых продуктов
        Cache<Object, Object> popularProductsCache = Caffeine.newBuilder()
                .maximumSize(20)   // Разные лимиты
                .expireAfterWrite(Duration.ofMinutes(30))  // Дольше живет
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("PopularProducts cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("popularProducts", popularProductsCache);

        // Кеш для поиска продуктов - короткое время
        Cache<Object, Object> searchProductsCache = Caffeine.newBuilder()
                .maximumSize(200)  // Много поисковых запросов
                .expireAfterWrite(Duration.ofMinutes(8))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("SearchProducts cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("searchProducts", searchProductsCache);

        // Кеш для похожих продуктов
        Cache<Object, Object> relatedProductsCache = Caffeine.newBuilder()
                .maximumSize(500)  // Много продуктов со схожими
                .expireAfterWrite(Duration.ofMinutes(25))  // Средней продолжительности
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("RelatedProducts cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("relatedProducts", relatedProductsCache);

        // Кеш для статистики продуктов (количество в категории и т.д.)
        Cache<Object, Object> productStatsCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("ProductStats cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("productStats", productStatsCache);

        log.info("Product cache manager configured with {} caches",
                cacheManager.getCacheNames().size());

        return cacheManager;
    }
}