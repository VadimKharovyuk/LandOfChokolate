package com.example.landofchokolate.config.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
@Slf4j
public class BrandCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Кеш для брендов по ID - долгоживущий
        Cache<Object, Object> brandByIdCache = Caffeine.newBuilder()
                .maximumSize(500)  // Достаточно для всех брендов
                .expireAfterWrite(Duration.ofHours(6))  // Обновляются редко
                .expireAfterAccess(Duration.ofHours(2)) // Если не используется
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("Brand cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("brandById", brandByIdCache);

        // Кеш для брендов по slug - тоже долгоживущий
        Cache<Object, Object> brandBySlugCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(6))
                .expireAfterAccess(Duration.ofHours(2))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("BrandBySlug cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("brandBySlug", brandBySlugCache);

        // Кеш для списка всех брендов - среднее время жизни
        Cache<Object, Object> allBrandsCache = Caffeine.newBuilder()
                .maximumSize(50)   // Мало записей, но тяжелые
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("AllBrands cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("allBrands", allBrandsCache);

        // Кеш для фильтров брендов - быстро обновляемый
        Cache<Object, Object> brandFiltersCache = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("BrandFilters cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("brandFilters", brandFiltersCache);

        // Кеш для продуктов бренда - страничная навигация
        Cache<Object, Object> brandProductsCache = Caffeine.newBuilder()
                .maximumSize(200)  // Много страниц разных брендов
                .expireAfterWrite(Duration.ofMinutes(20))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("BrandProducts cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("brandProducts", brandProductsCache);

        // Кеш для лимитированных списков брендов (например, топ-10)
        Cache<Object, Object> brandLimitCache = Caffeine.newBuilder()
                .maximumSize(20)   // Разные лимиты
                .expireAfterWrite(Duration.ofMinutes(45))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("BrandLimit cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("brandLimit", brandLimitCache);

        return cacheManager;
    }
}