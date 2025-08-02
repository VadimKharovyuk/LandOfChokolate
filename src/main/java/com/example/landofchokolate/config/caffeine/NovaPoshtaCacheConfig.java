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
public class NovaPoshtaCacheConfig {


    @Bean("novaPoshtaCacheManager")
    public CacheManager novaPoshtaCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Кеш для трекинга посылок
        Cache<Object, Object> trackingCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(60))
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .evictionListener((key, value, cause) ->
                        log.info("Видалення з кешу Nova Poshta трекінгу: ключ={}, причина={}", key, cause))
                .build();
        cacheManager.registerCustomCache("novaPoshtaTracking", trackingCache);

        return cacheManager;
    }
}
