package com.example.landofchokolate.config.caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
@EnableCaching  // Оставляем только в одном месте
@Slf4j
public class CategoryCacheConfig {

    @Bean("categoryCacheManager")
    @Primary
    public CacheManager categoryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Кеш для категорий по ID
        Cache<Object, Object> categoryByIdCache = Caffeine.newBuilder()
                .maximumSize(300)
                .expireAfterWrite(Duration.ofHours(4))
                .expireAfterAccess(Duration.ofHours(1))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("CategoryById cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("categoryById", categoryByIdCache);

        // Кеш для категорий по slug
        Cache<Object, Object> categoryBySlugCache = Caffeine.newBuilder()
                .maximumSize(300)
                .expireAfterWrite(Duration.ofHours(4))
                .expireAfterAccess(Duration.ofHours(1))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("CategoryBySlug cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("categoryBySlug", categoryBySlugCache);

        // Кеш для всех категорий
        Cache<Object, Object> allCategoriesCache = Caffeine.newBuilder()
                .maximumSize(20)
                .expireAfterWrite(Duration.ofMinutes(45))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("AllCategories cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("allCategories", allCategoriesCache);

        // Кеш для публичных категорий
        Cache<Object, Object> publicCategoriesCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("PublicCategories cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("publicCategories", publicCategoriesCache);

        // Кеш для топ категорий
        Cache<Object, Object> topCategoriesCache = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMinutes(20))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("TopCategories cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("topCategories", topCategoriesCache);

        // Кеш для поиска категорий по имени
        Cache<Object, Object> categoriesByNameCache = Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofMinutes(25))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("CategoriesByName cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("categoriesByName", categoriesByNameCache);

        // Кеш для данных редактирования категории
        Cache<Object, Object> categoryEditDataCache = Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("CategoryEditData cache eviction: key={}, cause={}", key, cause))
                .build();
        cacheManager.registerCustomCache("categoryEditData", categoryEditDataCache);

        log.info("Category cache manager configured with {} caches",
                cacheManager.getCacheNames().size());

        return cacheManager;
    }
}