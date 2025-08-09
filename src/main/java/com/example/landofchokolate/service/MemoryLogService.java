package com.example.landofchokolate.service;
import com.example.landofchokolate.model.MemoryLog;
import com.example.landofchokolate.repository.MemoryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "monitoring.render.memory.database.enabled", havingValue = "true", matchIfMissing = true)
public class MemoryLogService {

    private final MemoryLogRepository memoryLogRepository;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    /**
     * Создать и сохранить лог памяти
     */
    @Transactional
    public MemoryLog createMemoryLog(String renderPlan, long memoryLimitMb,
                                     long warningThresholdMb, long criticalThresholdMb) {
        try {
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

            long heapUsedMb = heapMemory.getUsed() / (1024 * 1024);
            long heapMaxMb = heapMemory.getMax() / (1024 * 1024);
            long nonHeapUsedMb = nonHeapMemory.getUsed() / (1024 * 1024);
            long totalUsedMb = heapUsedMb + nonHeapUsedMb;
            long remainingMb = memoryLimitMb - totalUsedMb;
            double usagePercentage = (double) totalUsedMb / memoryLimitMb * 100;

            // Определение статуса
            MemoryLog.MemoryStatus status = determineStatus(totalUsedMb, warningThresholdMb, criticalThresholdMb);
            boolean isCritical = totalUsedMb > criticalThresholdMb;
            boolean isWarning = totalUsedMb > warningThresholdMb;

            // Генерация рекомендаций
            String recommendations = generateRecommendations(status, usagePercentage, renderPlan);
            String jvmFlags = generateJvmFlags(renderPlan);

            MemoryLog memoryLog = MemoryLog.builder()
                    .timestamp(LocalDateTime.now())
                    .renderPlan(renderPlan)
                    .memoryLimitMb(memoryLimitMb)
                    .heapUsedMb(heapUsedMb)
                    .heapMaxMb(heapMaxMb)
                    .nonHeapUsedMb(nonHeapUsedMb)
                    .totalUsedMb(totalUsedMb)
                    .remainingMb(remainingMb)
                    .usagePercentage(Math.round(usagePercentage * 10.0) / 10.0) // округление до 1 знака
                    .status(status)
                    .isCritical(isCritical)
                    .isWarning(isWarning)
                    .warningThresholdMb(warningThresholdMb)
                    .criticalThresholdMb(criticalThresholdMb)
                    .renderPrice(getRenderPrice(memoryLimitMb))
                    .recommendations(recommendations)
                    .jvmFlags(jvmFlags)
                    .build();

            MemoryLog savedLog = memoryLogRepository.save(memoryLog);
            log.debug("💾 Сохранен лог памяти: ID={}, Status={}, Usage={}%",
                    savedLog.getId(), savedLog.getStatus(), savedLog.getUsagePercentage());

            return savedLog;

        } catch (Exception e) {
            log.error("❌ Ошибка при создании лога памяти: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать лог памяти", e);
        }
    }

    /**
     * Определить статус памяти
     */
    private MemoryLog.MemoryStatus determineStatus(long totalUsedMb, long warningThresholdMb, long criticalThresholdMb) {
        if (totalUsedMb > criticalThresholdMb) {
            return MemoryLog.MemoryStatus.CRITICAL;
        } else if (totalUsedMb > warningThresholdMb) {
            return MemoryLog.MemoryStatus.WARNING;
        } else {
            return MemoryLog.MemoryStatus.OPTIMAL;
        }
    }

    /**
     * Генерировать рекомендации
     */
    private String generateRecommendations(MemoryLog.MemoryStatus status, double usagePercentage, String renderPlan) {
        StringBuilder recommendations = new StringBuilder();

        switch (status) {
            case CRITICAL:
                recommendations.append("🚨 КРИТИЧНО! Немедленные действия:\n");
                recommendations.append("1. Принудительная сборка мусора\n");
                recommendations.append("2. Рассмотреть upgrade плана Render\n");
                recommendations.append("3. Оптимизировать JVM настройки\n");
                recommendations.append("4. Проверить утечки памяти\n");
                break;
            case WARNING:
                recommendations.append("⚠️ ВНИМАНИЕ! Рекомендуемые действия:\n");
                recommendations.append("1. Подготовиться к масштабированию\n");
                recommendations.append("2. Мониторить тенденции\n");
                recommendations.append("3. Оптимизировать приложение\n");
                break;
            case OPTIMAL:
                recommendations.append("✅ Память используется оптимально\n");
                if (usagePercentage < 50 && !renderPlan.equals("Starter")) {
                    recommendations.append("💰 Возможна экономия на плане меньшего размера\n");
                }
                break;
        }

        return recommendations.toString();
    }

    /**
     * Генерировать JVM флаги
     */
    private String generateJvmFlags(String renderPlan) {
        switch (renderPlan.toLowerCase()) {
            case "starter":
                return "-Xms200m -Xmx350m -XX:MaxMetaspaceSize=64m -XX:CompressedClassSpaceSize=32m -XX:+UseG1GC";
            case "standard":
                return "-Xms400m -Xmx768m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=64m -XX:+UseG1GC";
            case "pro":
                return "-Xms512m -Xmx1536m -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=128m -XX:+UseG1GC";
            case "pro+":
                return "-Xms1024m -Xmx3072m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC";
            default:
                return "-XX:+UseG1GC -XX:+UseCompressedOops -XX:+UseContainerSupport";
        }
    }

    /**
     * Получить цену плана Render
     */
    private int getRenderPrice(long memoryMb) {
        if (memoryMb <= 512) return 7;
        if (memoryMb <= 1024) return 25;
        if (memoryMb <= 2048) return 85;
        return 170;
    }

    /**
     * Получить логи за последние часы
     */
    public List<MemoryLog> getRecentLogs(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return memoryLogRepository.findByTimestampAfter(startTime);
    }

    /**
     * Получить критические логи за период
     */
    public List<MemoryLog> getCriticalLogs(LocalDateTime startTime, LocalDateTime endTime) {
        return memoryLogRepository.findCriticalLogsBetween(startTime, endTime);
    }

    /**
     * Получить статистику использования
     */
    public MemoryUsageStats getUsageStats(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();

        Double averageUsage = memoryLogRepository.getAverageUsagePercentage(startTime, endTime);
        Long criticalCount = memoryLogRepository.countCriticalEventsAfter(startTime);

        return MemoryUsageStats.builder()
                .averageUsagePercentage(averageUsage != null ? averageUsage : 0.0)
                .criticalEventsCount(criticalCount != null ? criticalCount : 0L)
                .periodHours(hours)
                .build();
    }

    /**
     * Очистка старых логов (автоматическая очистка)
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        try {
            memoryLogRepository.deleteByTimestampBefore(cutoffTime);
            log.info("🧹 Очищены логи старше {} дней", daysToKeep);
        } catch (Exception e) {
            log.error("❌ Ошибка при очистке старых логов: {}", e.getMessage(), e);
        }
    }

    /**
     * Статистика использования памяти
     */
    @lombok.Data
    @lombok.Builder
    public static class MemoryUsageStats {
        private Double averageUsagePercentage;
        private Long criticalEventsCount;
        private Integer periodHours;
    }
}