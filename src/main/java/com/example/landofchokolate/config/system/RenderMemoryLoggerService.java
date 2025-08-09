package com.example.landofchokolate.config.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "monitoring.render.memory.enabled", havingValue = "true", matchIfMissing = true)
public class RenderMemoryLoggerService {

    @Autowired
    private RenderMemoryMonitoringService renderService;

    /**
     * Логирование памяти Render каждые 5 минут
     */
    @Scheduled(fixedRate = 300000) // 5 минут = 300000 мс
    public void logRenderMemoryStatistics() {
        try {
            log.info("🚀 === МОНИТОРИНГ ПАМЯТИ RENDER ===");
            renderService.analyzeMemoryForRender();
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
            }
        } catch (Exception e) {
            log.debug("Ошибка критического мониторинга: {}", e.getMessage());
        }
    }

    /**
     * Получить текущее использование памяти
     */
    private long getCurrentMemoryUsage() {
        try {
            java.lang.management.MemoryMXBean memoryBean =
                    java.lang.management.ManagementFactory.getMemoryMXBean();

            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);

            return heapUsed + nonHeapUsed;
        } catch (Exception e) {
            return 0;
        }
    }
}