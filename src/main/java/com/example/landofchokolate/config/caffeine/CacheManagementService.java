package com.example.landofchokolate.config.caffeine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CacheManagementService {

    @Autowired
    private CacheManager cacheManager;

    /**
     * МЕТОД 1: Очистить ВСЕ кэши разом
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cache '{}' cleared", cacheName);
            }
        });
        log.info("All caches cleared successfully");
    }

    /**
     * МЕТОД 2: Очистить конкретный кэш по имени
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache '{}' cleared", cacheName);
        } else {
            log.warn("Cache '{}' not found", cacheName);
        }
    }

    /**
     * МЕТОД 3: Очистить конкретный ключ в кэше
     */
    public void evictCacheEntry(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Cache entry '{}' evicted from cache '{}'", key, cacheName);
        }
    }

    /**
     * МЕТОД 4: Получить информацию о всех кэшах
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // Для Caffeine кэша можем получить статистику
                Object nativeCache = cache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats =
                            ((com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache).stats();

                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("hitCount", caffeineStats.hitCount());
                    cacheInfo.put("missCount", caffeineStats.missCount());
                    cacheInfo.put("hitRate", String.format("%.2f%%", caffeineStats.hitRate() * 100));
                    cacheInfo.put("evictionCount", caffeineStats.evictionCount());

                    stats.put(cacheName, cacheInfo);
                } else {
                    stats.put(cacheName, "Active");
                }
            }
        });

        return stats;
    }
}