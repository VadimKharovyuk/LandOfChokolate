package com.example.landofchokolate.config;

import com.example.landofchokolate.service.MemoryMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для мониторинга памяти JVM и сессий
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MemoryMonitoringController {

    private final MemoryMonitoringService memoryMonitoringService;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
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
     * Получить информацию о сессиях
     * GET /api/monitoring/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessionInfo() {
        try {
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("timestamp", LocalDateTime.now());
            sessionInfo.put("status", "success");
            sessionInfo.put("icon", "🔐");
            sessionInfo.put("title", "HTTP Сесії");
            sessionInfo.put("description", "Статистика активних користувацьких сесій");

            try {
                // Попытка получить статистику через JMX
                ObjectName sessionManager = new ObjectName("Catalina:type=Manager,host=localhost,context=/");

                if (mBeanServer.isRegistered(sessionManager)) {
                    Integer activeSessions = (Integer) mBeanServer.getAttribute(sessionManager, "activeSessions");
                    Integer maxActiveSessions = (Integer) mBeanServer.getAttribute(sessionManager, "maxActiveSessions");
                    Long sessionCounter = (Long) mBeanServer.getAttribute(sessionManager, "sessionCounter");
                    Integer maxInactiveInterval = (Integer) mBeanServer.getAttribute(sessionManager, "maxInactiveInterval");

                    sessionInfo.put("active_sessions", activeSessions);
                    sessionInfo.put("active_sessions_icon", "👥");
                    sessionInfo.put("max_active_sessions", maxActiveSessions);
                    sessionInfo.put("total_created_sessions", sessionCounter);
                    sessionInfo.put("timeout_seconds", maxInactiveInterval);
                    sessionInfo.put("timeout_minutes", maxInactiveInterval / 60);
                    sessionInfo.put("timeout_icon", "⏰");

                    // Примерный расчет памяти сессий
                    Map<String, Object> memoryEstimate = calculateSessionMemory(activeSessions);
                    sessionInfo.put("memory_estimate", memoryEstimate);

                    sessionInfo.put("server_type", "external_tomcat");
                    sessionInfo.put("server_icon", "🖥️");
                } else {
                    // Для embedded сервера
                    sessionInfo.put("server_type", "embedded");
                    sessionInfo.put("server_icon", "📦");
                    sessionInfo.put("message", "Детальна статистика недоступна для вбудованого сервера");
                    sessionInfo.put("active_sessions", "невідомо");
                    sessionInfo.put("recommendation", "Використовуйте Spring Boot Actuator для детального моніторингу");
                }
            } catch (Exception jmxError) {
                log.warn("⚠️ JMX недоступний: {}", jmxError.getMessage());
                sessionInfo.put("server_type", "embedded_or_restricted");
                sessionInfo.put("server_icon", "🔒");
                sessionInfo.put("message", "JMX статистика недоступна");
                sessionInfo.put("error", jmxError.getMessage());
            }

            log.info("🔐 API: Запитано інформацію про сесії");
            return ResponseEntity.ok(sessionInfo);

        } catch (Exception e) {
            log.error("❌ Помилка при отриманні інформації про сесії: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "icon", "❌",
                            "description", "Сталася помилка при отриманні статистики сесій"
                    ));
        }
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
            ResponseEntity<Map<String, Object>> sessionResponse = getSessionInfo();
            ResponseEntity<Map<String, Object>> gcResponse = getGCInfo();

            if (memoryResponse.getStatusCode().is2xxSuccessful()) {
                fullStats.put("memory", memoryResponse.getBody());
            }
            if (sessionResponse.getStatusCode().is2xxSuccessful()) {
                fullStats.put("sessions", sessionResponse.getBody());
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

    /**
     * Получить рекомендации по оптимизации
     * GET /api/monitoring/recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations() {
        try {
            MemoryMonitoringService.MemoryInfo memoryInfo = memoryMonitoringService.getMemoryInfo();

            Map<String, Object> recommendations = new HashMap<>();
            recommendations.put("timestamp", LocalDateTime.now());
            recommendations.put("status", "success");
            recommendations.put("icon", "💡");
            recommendations.put("title", "Рекомендації з оптимізації");
            recommendations.put("description", "Поради щодо покращення продуктивності системи");

            Map<String, String> advice = new HashMap<>();

            // Анализ использования памяти
            double usagePercent = memoryInfo.getUsagePercentageValue();
            if (usagePercent > 90) {
                advice.put("memory_critical", "🔴 Критичне використання пам'яті! Збільште heap розмір або оптимізуйте програму");
            } else if (usagePercent > 80) {
                advice.put("memory_warning", "🟡 Високе використання пам'яті. Рекомендується моніторинг та оптимізація");
            } else if (usagePercent < 30) {
                advice.put("memory_info", "🟢 Низьке використання пам'яті. Можна зменшити heap розмір для економії ресурсів");
            } else {
                advice.put("memory_ok", "🟢 Використання пам'яті в нормі");
            }

            // JVM параметры
            advice.put("jvm_tuning", "⚙️ Рекомендовані JVM параметри: -Xms512m -Xmx1024m -XX:+UseG1GC");
            advice.put("monitoring", "📊 Увімкніть Spring Boot Actuator для розширеного моніторингу");

            recommendations.put("current_usage_percent", usagePercent);
            recommendations.put("recommendations", advice);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            log.error("❌ Помилка при генерації рекомендацій: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "icon", "❌",
                            "description", "Сталася помилка при генерації рекомендацій"
                    ));
        }
    }

    // Вспомогательные методы

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