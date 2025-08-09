package com.example.landofchokolate.config.system;

import com.example.landofchokolate.model.MemoryLog;
import com.example.landofchokolate.service.MemoryLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.render.memory.enabled", havingValue = "true", matchIfMissing = true)
public class RenderMemoryLoggerService {

    private final RenderMemoryMonitoringService renderService;
    private final MemoryLogService memoryLogService;

    @Value("${monitoring.render.memory.database.enabled:true}")
    private boolean databaseLoggingEnabled;

    @Value("${monitoring.render.memory.cleanup.days:30}")
    private int logRetentionDays;

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    /**
     * Логирование памяти Render каждые 5 минут с сохранением в БД
     */
    @Scheduled(fixedRate = 300000) // 5 минут = 300000 мс
    public void logRenderMemoryStatistics() {
        try {
            log.info("🚀 === МОНИТОРИНГ ПАМЯТИ RENDER ===");

            // Выполняем анализ через существующий сервис
            renderService.analyzeMemoryForRender();

            // Сохраняем в базу данных, если включено
            if (databaseLoggingEnabled) {
                saveMemoryLogToDatabase();
            }

            log.info("🚀 === ЗАВЕРШЕН МОНИТОРИНГ ПАМЯТИ ===");

        } catch (Exception e) {
            log.error("❌ Ошибка при логировании памяти Render: {}", e.getMessage(), e);
        }
    }

    /**
     * Критический мониторинг каждую минуту при высоком использовании
     */
    @Scheduled(fixedRate = 60000) // каждую минуту
    public void criticalRenderMemoryCheck() {
        try {
            // Быстрая проверка без детального логирования
            long totalUsedMB = getCurrentMemoryUsage();
            long criticalThreshold = renderService.getCriticalThresholdMB();

            if (totalUsedMB > criticalThreshold) {
                log.error("🚨 КРИТИЧЕСКАЯ ПАМЯТЬ RENDER! {} MB / {} MB",
                        totalUsedMB, renderService.getMemoryLimitMB());

                // Детальный анализ при критическом уровне
                renderService.analyzeMemoryForRender();

                // Сохраняем критический лог в БД
                if (databaseLoggingEnabled) {
                    MemoryLog criticalLog = saveMemoryLogToDatabase();
                    log.error("💾 Критический лог сохранен в БД: ID={}", criticalLog.getId());
                }
            }
        } catch (Exception e) {
            log.debug("Ошибка критического мониторинга: {}", e.getMessage());
        }
    }

    /**
     * Еженедельная очистка старых логов (каждое воскресенье в 2:00)
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void cleanupOldMemoryLogs() {
        if (databaseLoggingEnabled) {
            try {
                log.info("🧹 Запуск очистки логов старше {} дней", logRetentionDays);
                memoryLogService.cleanupOldLogs(logRetentionDays);
            } catch (Exception e) {
                log.error("❌ Ошибка при очистке старых логов: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Еженедельный отчет по использованию памяти (каждый понедельник в 9:00)
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void weeklyMemoryReport() {
        if (databaseLoggingEnabled) {
            try {
                log.info("📊 === ЕЖЕНЕДЕЛЬНЫЙ ОТЧЕТ ПАМЯТИ ===");

                MemoryLogService.MemoryUsageStats weeklyStats = memoryLogService.getUsageStats(168); // 7 дней = 168 часов
                MemoryLogService.MemoryUsageStats dailyStats = memoryLogService.getUsageStats(24);   // 24 часа

                log.info("📈 Статистика за неделю:");
                log.info("   📊 Среднее использование: {}%",
                        String.format("%.1f", weeklyStats.getAverageUsagePercentage()));
                log.info("   🚨 Критических событий: {}", weeklyStats.getCriticalEventsCount());

                log.info("📈 Статистика за сутки:");
                log.info("   📊 Среднее использование: {}%",
                        String.format("%.1f", dailyStats.getAverageUsagePercentage()));
                log.info("   🚨 Критических событий: {}", dailyStats.getCriticalEventsCount());

                // Рекомендации на основе статистики
                if (weeklyStats.getCriticalEventsCount() > 10) {
                    log.warn("⚠️ РЕКОМЕНДАЦИЯ: Слишком много критических событий за неделю!");
                    log.warn("💡 Рассмотрите upgrade плана Render или оптимизацию приложения");
                }

                if (weeklyStats.getAverageUsagePercentage() > 85) {
                    log.warn("⚠️ РЕКОМЕНДАЦИЯ: Высокое среднее использование памяти!");
                    log.warn("💡 Требуется масштабирование или оптимизация");
                }

                log.info("📊 === ЗАВЕРШЕН ЕЖЕНЕДЕЛЬНЫЙ ОТЧЕТ ===");

            } catch (Exception e) {
                log.error("❌ Ошибка при генерации еженедельного отчета: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Сохранение лога памяти в базу данных
     */
    private MemoryLog  saveMemoryLogToDatabase() {
        try {
            return memoryLogService.createMemoryLog(
                    renderService.getRenderPlan(),
                    renderService.getMemoryLimitMB(),
                    renderService.getWarningThresholdMB(),
                    renderService.getCriticalThresholdMB()
            );
        } catch (Exception e) {
            log.error("❌ Ошибка при сохранении лога в БД: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Получить текущее использование памяти
     */
    private long getCurrentMemoryUsage() {
        try {
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);
            return heapUsed + nonHeapUsed;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Получить статистику использования памяти за период
     * Полезно для REST API или административного интерфейса
     */
    public MemoryLogService.MemoryUsageStats getMemoryStats(int hours) {
        if (databaseLoggingEnabled) {
            return memoryLogService.getUsageStats(hours);
        }
        return MemoryLogService.MemoryUsageStats.builder()
                .averageUsagePercentage(0.0)
                .criticalEventsCount(0L)
                .periodHours(hours)
                .build();
    }
}