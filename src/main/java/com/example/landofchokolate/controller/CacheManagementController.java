package com.example.landofchokolate.controller;

import com.example.landofchokolate.config.caffeine.CacheManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cache")
@Slf4j
public class CacheManagementController {

    @Autowired
    private CacheManagementService cacheManagementService;

    /**
     * 🔥 ОЧИСТИТЬ ВСЕ КЭШИ РАЗОМ
     */
    @PostMapping("/clear-all")
    public ResponseEntity<String> clearAllCaches() {
        try {
            cacheManagementService.clearAllCaches();
            return ResponseEntity.ok("✅ All caches cleared successfully!");
        } catch (Exception e) {
            log.error("Error clearing all caches", e);
            return ResponseEntity.status(500).body("❌ Error clearing caches: " + e.getMessage());
        }
    }

    /**
     * 🎯 ОЧИСТИТЬ КОНКРЕТНЫЙ КЭШ
     */
    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearSpecificCache(@PathVariable String cacheName) {
        try {
            cacheManagementService.clearCache(cacheName);
            return ResponseEntity.ok("✅ Cache '" + cacheName + "' cleared!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * 📊 ПОСМОТРЕТЬ СТАТИСТИКУ КЭШЕЙ
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = cacheManagementService.getCacheStats();
        return ResponseEntity.ok(stats);
    }


    // ============ БЫСТРЫЕ КНОПКИ ============

    /**
     * 🛍️ ОЧИСТИТЬ КЭШИ ПРОДУКТОВ
     */
    @PostMapping("/clear-products")
    public ResponseEntity<String> clearProductCaches() {
        cacheManagementService.clearCache("productById");
        cacheManagementService.clearCache("popularProducts");
        return ResponseEntity.ok("✅ Product caches cleared!");
    }

    /**
     * 🗺️ ОЧИСТИТЬ КЭШИ НАВИГАЦИИ
     */
    @PostMapping("/clear-navigation")
    public ResponseEntity<String> clearNavigationCaches() {
        cacheManagementService.clearCache("categories");
        cacheManagementService.clearCache("sitemap");
        return ResponseEntity.ok("✅ Navigation caches cleared!");
    }


}