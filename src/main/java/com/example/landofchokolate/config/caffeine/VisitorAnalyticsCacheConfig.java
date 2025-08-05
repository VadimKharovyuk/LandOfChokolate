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
public class VisitorAnalyticsCacheConfig {

    @Bean("visitorAnalyticsCacheManager")
    public CacheManager visitorAnalyticsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 📊 Кеш для базовой статистики (главный дашборд)
        Cache<Object, Object> basicStatsCache = Caffeine.newBuilder()
                .maximumSize(10) // Небольшой кеш, т.к. базовая статистика одна
                .expireAfterWrite(Duration.ofMinutes(5)) // Обновляем каждые 5 минут
                .expireAfterAccess(Duration.ofMinutes(3))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("🗑️ Видалення базової статистики з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorBasicStats", basicStatsCache);

        // 🌍 Кеш для статистики по странам
        Cache<Object, Object> countryStatsCache = Caffeine.newBuilder()
                .maximumSize(100) // Разные периоды (1, 7, 30, 90 дней)
                .expireAfterWrite(Duration.ofMinutes(15)) // Обновляем каждые 15 минут
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("🌍 Видалення статистики країн з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorCountryStats", countryStatsCache);

        // 👥 Кеш для топ IP адресов
        Cache<Object, Object> topIpsCache = Caffeine.newBuilder()
                .maximumSize(50) // Разные периоды
                .expireAfterWrite(Duration.ofMinutes(10)) // Обновляем каждые 10 минут
                .expireAfterAccess(Duration.ofMinutes(8))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("👥 Видалення топ IP з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorTopIPs", topIpsCache);

        // 🔍 Кеш для поиска по IP (детальная статистика)
        Cache<Object, Object> ipStatsCache = Caffeine.newBuilder()
                .maximumSize(1000) // Много IP адресов
                .expireAfterWrite(Duration.ofMinutes(30)) // Статистика по IP меняется редко
                .expireAfterAccess(Duration.ofMinutes(20))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("🔍 Видалення статистики IP з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorIpStats", ipStatsCache);

        // 📄 Кеш для пагинированных списков посетителей
        Cache<Object, Object> visitorsListCache = Caffeine.newBuilder()
                .maximumSize(200) // Разные страницы и размеры
                .expireAfterWrite(Duration.ofMinutes(3)) // Часто обновляется
                .expireAfterAccess(Duration.ofMinutes(2))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("📄 Видалення списку відвідувачів з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorsList", visitorsListCache);

        // 🕐 Кеш для хронологии IP (timeline)
        Cache<Object, Object> ipTimelineCache = Caffeine.newBuilder()
                .maximumSize(500) // Много IP с разными лимитами
                .expireAfterWrite(Duration.ofMinutes(20))
                .expireAfterAccess(Duration.ofMinutes(15))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("🕐 Видалення хронології IP з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorIpTimeline", ipTimelineCache);

        // 📈 Кеш для почасовой статистики
        Cache<Object, Object> hourlyStatsCache = Caffeine.newBuilder()
                .maximumSize(30) // Разные периоды (1, 7, 30 дней)
                .expireAfterWrite(Duration.ofMinutes(60)) // Обновляем каждый час
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("📈 Видалення погодинної статистики з кешу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("visitorHourlyStats", hourlyStatsCache);

        log.info("✅ Кеш для аналітики відвідувачів сконфігуровано успішно");
        return cacheManager;
    }
}