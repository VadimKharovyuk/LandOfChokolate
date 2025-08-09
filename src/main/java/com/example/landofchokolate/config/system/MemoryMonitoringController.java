package com.example.landofchokolate.config.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MemoryMonitoringController {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final DecimalFormat df = new DecimalFormat("#.##");


    /**
     * Получить общую информацию о памяти JVM
     * GET /api/monitoring/memory
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryInfo() {
        try {
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
            Runtime runtime = Runtime.getRuntime();

            Map<String, Object> memoryInfo = new HashMap<>();

            // Основная информация
            memoryInfo.put("timestamp", LocalDateTime.now());
            memoryInfo.put("status", "success");
            memoryInfo.put("icon", "💾");
            memoryInfo.put("title", "Пам'ять JVM");
            memoryInfo.put("description", "Статистика використання пам'яті віртуальної машини Java");

            // Heap память
            Map<String, Object> heapInfo = new HashMap<>();
            long heapUsedMB = heapMemory.getUsed() / (1024 * 1024);
            long heapMaxMB = heapMemory.getMax() / (1024 * 1024);
            long heapFreeMB = heapMaxMB - heapUsedMB;
            double heapUsagePercent = ((double) heapUsedMB / heapMaxMB) * 100;

            heapInfo.put("icon", "🔷");
            heapInfo.put("name", "Heap пам'ять");
            heapInfo.put("description", "Основна пам'ять для об'єктів програми");
            heapInfo.put("used_mb", heapUsedMB);
            heapInfo.put("max_mb", heapMaxMB);
            heapInfo.put("free_mb", heapFreeMB);
            heapInfo.put("usage_percent", Math.round(heapUsagePercent * 100.0) / 100.0);
            heapInfo.put("usage_status", getUsageStatus(heapUsagePercent));
            heapInfo.put("status_icon", getUsageIcon(heapUsagePercent));
            heapInfo.put("status_text", getUsageTextUkrainian(heapUsagePercent));

            // Non-Heap память
            Map<String, Object> nonHeapInfo = new HashMap<>();
            nonHeapInfo.put("icon", "🔶");
            nonHeapInfo.put("name", "Non-Heap пам'ять");
            nonHeapInfo.put("description", "Пам'ять для метакласів та коду JVM");
            nonHeapInfo.put("used_mb", nonHeapMemory.getUsed() / (1024 * 1024));
            nonHeapInfo.put("max_mb", nonHeapMemory.getMax() > 0 ? nonHeapMemory.getMax() / (1024 * 1024) : -1);

            // Системная память
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("icon", "🖥️");
            systemInfo.put("name", "Системна пам'ять");
            systemInfo.put("description", "Загальна пам'ять процесу JVM");
            systemInfo.put("total_mb", runtime.totalMemory() / (1024 * 1024));
            systemInfo.put("free_mb", runtime.freeMemory() / (1024 * 1024));
            systemInfo.put("max_mb", runtime.maxMemory() / (1024 * 1024));

            memoryInfo.put("heap", heapInfo);
            memoryInfo.put("non_heap", nonHeapInfo);
            memoryInfo.put("system", systemInfo);

            log.info("🔍 API: Запитано інформацію про пам'ять JVM");
            return ResponseEntity.ok(memoryInfo);

        } catch (Exception e) {
            log.error("❌ Помилка при отриманні інформації про пам'ять: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "icon", "❌",
                            "description", "Сталася помилка при отриманні статистики пам'яті"
                    ));
        }
    }


    private String getUsageIcon(double usagePercent) {
        if (usagePercent > 90) return "🔴";
        if (usagePercent > 80) return "🟡";
        if (usagePercent > 60) return "🟠";
        return "🟢";
    }

    private String getUsageTextUkrainian(double usagePercent) {
        if (usagePercent > 90) return "Критичний рівень";
        if (usagePercent > 80) return "Високий рівень";
        if (usagePercent > 60) return "Помірний рівень";
        return "Нормальний рівень";
    }



    /**
     * Получить информацию о сборщике мусора
     * GET /api/monitoring/gc
     */
    @GetMapping("/gc")
    public ResponseEntity<Map<String, Object>> getGCInfo() {
        try {
            Map<String, Object> gcInfo = new HashMap<>();
            gcInfo.put("timestamp", LocalDateTime.now());
            gcInfo.put("status", "success");
            gcInfo.put("icon", "🗑️");
            gcInfo.put("title", "Збирач сміття");
            gcInfo.put("description", "Статистика роботи Garbage Collector");

            Map<String, Map<String, Object>> collectors = new HashMap<>();

            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
                Map<String, Object> collectorInfo = new HashMap<>();
                collectorInfo.put("collection_count", gcBean.getCollectionCount());
                collectorInfo.put("collection_time_ms", gcBean.getCollectionTime());
                collectorInfo.put("memory_pool_names", gcBean.getMemoryPoolNames());

                collectors.put(gcBean.getName(), collectorInfo);
            });

            gcInfo.put("garbage_collectors", collectors);

            log.info("🗑️ API: Запитано інформацію про збирач сміття");
            return ResponseEntity.ok(gcInfo);

        } catch (Exception e) {
            log.error("❌ Помилка при отриманні інформації про GC: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "icon", "❌",
                            "description", "Сталася помилка при отриманні статистики збирача сміття"
                    ));
        }
    }

    /**
     * Получить полную статистику (все в одном запросе)
     * GET /api/monitoring/full
     */
    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> getFullStatistics() {
        try {
            Map<String, Object> fullStats = new HashMap<>();
            fullStats.put("timestamp", LocalDateTime.now());
            fullStats.put("status", "success");
            fullStats.put("icon", "📊");
            fullStats.put("title", "Повна статистика системи");
            fullStats.put("description", "Комплексний моніторинг пам'яті, сесій та збирача сміття");

            // Получаем все данные
            ResponseEntity<Map<String, Object>> memoryResponse = getMemoryInfo();

            ResponseEntity<Map<String, Object>> gcResponse = getGCInfo();

            if (memoryResponse.getStatusCode().is2xxSuccessful()) {
                fullStats.put("memory", memoryResponse.getBody());
            }
            if (gcResponse.getStatusCode().is2xxSuccessful()) {
                fullStats.put("garbage_collection", gcResponse.getBody());
            }

            log.info("📊 API: Запитано повну статистику системи");
            return ResponseEntity.ok(fullStats);

        } catch (Exception e) {
            log.error("❌ Помилка при отриманні повної статистики: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "icon", "❌",
                            "description", "Сталася помилка при отриманні повної статистики"
                    ));
        }
    }

    /**
     * Принудительный вызов сборки мусора
     * POST /api/monitoring/gc/force
     */
    @PostMapping("/gc/force")
    public ResponseEntity<Map<String, Object>> forceGarbageCollection() {
        try {
            log.warn("🗑️ API: Примусовий виклик збирача сміття через API");

            long beforeMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long startTime = System.currentTimeMillis();

            // Вызов GC
            System.gc();

            // Небольшая пауза для завершения GC
            Thread.sleep(1000);

            long afterMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long endTime = System.currentTimeMillis();
            long freed = beforeMemory - afterMemory;
            long duration = endTime - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("status", "completed");
            result.put("icon", "🗑️");
            result.put("title", "Збирач сміття запущено");
            result.put("description", "Примусове очищення пам'яті завершено");
            result.put("memory_before_mb", beforeMemory);
            result.put("memory_after_mb", afterMemory);
            result.put("memory_freed_mb", freed);
            result.put("freed_icon", freed > 0 ? "✅" : "ℹ️");
            result.put("duration_ms", duration);
            result.put("warning", "Примусовий GC може впливати на продуктивність");
            result.put("warning_icon", "⚠️");

            log.info("🗑️ Примусовий збирач сміття завершено. Звільнено: {} MB за {} мс", freed, duration);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Помилка при примусовому збиранні сміття: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "icon", "❌",
                            "description", "Сталася помилка при запуску збирача сміття"
                    ));
        }
    }


    private String getUsageStatus(double usagePercent) {
        if (usagePercent > 90) return "critical";
        if (usagePercent > 80) return "warning";
        if (usagePercent > 60) return "moderate";
        return "good";
    }

    private Map<String, Object> calculateSessionMemory(Integer activeSessions) {
        Map<String, Object> memoryCalc = new HashMap<>();

        if (activeSessions == null || activeSessions == 0) {
            memoryCalc.put("total_mb", 0.0);
            memoryCalc.put("per_session_kb", 0);
            return memoryCalc;
        }

        // Примерная оценка памяти на сессию
        int estimatedBytesPerSession = 3 * 1024; // 3KB в среднем
        long totalSessionMemory = (long) activeSessions * estimatedBytesPerSession;
        double sessionMemoryMB = totalSessionMemory / (1024.0 * 1024.0);

        memoryCalc.put("total_mb", Math.round(sessionMemoryMB * 100.0) / 100.0);
        memoryCalc.put("per_session_kb", estimatedBytesPerSession / 1024);
        memoryCalc.put("estimation_note", "Приблизна оцінка на основі середніх значень");
        memoryCalc.put("icon", "💾");

        return memoryCalc;
    }
}