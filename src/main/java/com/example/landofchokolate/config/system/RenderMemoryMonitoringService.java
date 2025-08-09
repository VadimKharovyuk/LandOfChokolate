package com.example.landofchokolate.config.system;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;


@Service
@Slf4j
public class RenderMemoryMonitoringService {

    private final MemoryMXBean memoryBean;

    // Настройки из application.properties (с fallback значениями)
    @Value("${monitoring.memory.limit-mb:#{null}}")
    private Long configuredMemoryLimitMB;

    @Value("${monitoring.memory.auto-detect:true}")
    private boolean autoDetectMemoryLimit;

    // Динамически определяемые значения для Render
    private long memoryLimitMB;
    private long criticalThresholdMB;
    private long warningThresholdMB;
    private String renderPlan;

    public RenderMemoryMonitoringService() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }

    @PostConstruct
    public void initializeRenderMemoryLimits() {
        detectRenderMemoryConfiguration();
        logRenderMemoryConfiguration();
    }

    /**
     * Автоматическое определение конфигурации памяти Render
     */
    private void detectRenderMemoryConfiguration() {
        // 1. Если задано в конфигурации - используем это
        if (configuredMemoryLimitMB != null && configuredMemoryLimitMB > 0) {
            memoryLimitMB = configuredMemoryLimitMB;
            renderPlan = "Настроено вручную";
            log.info("🔧 Используется настроенный лимит памяти: {} MB", memoryLimitMB);
        }
        // 2. Автоматическое определение для Render
        else if (autoDetectMemoryLimit) {
            detectRenderPlan();
        }
        // 3. Fallback к 512 MB (минимальный Render план)
        else {
            memoryLimitMB = 512;
            renderPlan = "Starter (fallback)";
        }

        // Устанавливаем пороги
        criticalThresholdMB = (long) (memoryLimitMB * 0.90); // 90%
        warningThresholdMB = (long) (memoryLimitMB * 0.80);  // 80%
    }

    /**
     * Определение плана Render по JVM памяти
     */
    private void detectRenderPlan() {
        long maxJvmMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        // Render типичные конфигурации JVM
        if (maxJvmMemoryMB <= 400) {
            memoryLimitMB = 512;
            renderPlan = "Starter";
        } else if (maxJvmMemoryMB <= 900) {
            memoryLimitMB = 1024;
            renderPlan = "Standard";
        } else if (maxJvmMemoryMB <= 1800) {
            memoryLimitMB = 2048;
            renderPlan = "Pro";
        } else {
            memoryLimitMB = 4096;
            renderPlan = "Pro+";
        }

        log.info("🎯 Автоматически определен план Render: {} ({} MB)", renderPlan, memoryLimitMB);
    }

    /**
     * Логирование конфигурации Render
     */
    private void logRenderMemoryConfiguration() {
        log.info("🚀 === КОНФИГУРАЦИЯ МОНИТОРИНГА RENDER ===");
        log.info("📦 План Render: {}", renderPlan);
        log.info("💾 Лимит памяти: {} MB", memoryLimitMB);
        log.info("💰 Стоимость: ${}/мес", getRenderPrice(memoryLimitMB));
        log.info("⚠️ Порог предупреждения: {} MB ({}%)", warningThresholdMB, 80);
        log.info("🚨 Критический порог: {} MB ({}%)", criticalThresholdMB, 90);
        log.info("🔧 Автоопределение: {}", autoDetectMemoryLimit ? "включено" : "отключено");

        if (configuredMemoryLimitMB != null) {
            log.info("⚙️ Ручная настройка: {} MB", configuredMemoryLimitMB);
        }

        // Показываем доступные планы
        log.info("📋 Доступные планы Render:");
        log.info("   🥉 Starter: 512 MB - $7/мес");
        log.info("   🥈 Standard: 1 GB - $25/мес");
        log.info("   🥇 Pro: 2 GB - $85/мес");
        log.info("   💎 Pro+: 4 GB - $170/мес");
    }

    /**
     * Адаптивный анализ памяти для Render
     */
    public void analyzeMemoryForRender() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        long heapUsed = heapMemory.getUsed() / (1024 * 1024);
        long nonHeapUsed = nonHeapMemory.getUsed() / (1024 * 1024);
        long totalUsed = heapUsed + nonHeapUsed;
        long remainingMB = memoryLimitMB - totalUsed;

        log.info("🎯 === АНАЛИЗ ДЛЯ RENDER {} ({} MB) ===", renderPlan.toUpperCase(), memoryLimitMB);
        log.info("📊 Использование: {} MB / {} MB ({}%)",
                totalUsed, memoryLimitMB,
                String.format("%.1f", (double) totalUsed / memoryLimitMB * 100));
        log.info("🆓 Осталось памяти: {} MB", remainingMB);

        // Анализ Non-Heap с адаптивными порогами для Render
        analyzeRenderNonHeapMemory(nonHeapUsed);

        // Анализ Heap с рекомендациями для Render
        analyzeRenderHeapMemory(heapMemory);

        // Рекомендации по JVM настройкам для текущего плана
        provideRenderJVMRecommendations();

        // Анализ критичности и рекомендации по upgrade
        analyzeRenderCriticalStatus(totalUsed, remainingMB);
    }

    /**
     * Анализ Non-Heap памяти для Render
     */
    private void analyzeRenderNonHeapMemory(long nonHeapUsed) {
        // Адаптивные пороги Non-Heap в зависимости от плана Render
        long nonHeapThreshold;
        long metaspaceLimit;
        long compressedClassLimit;

        switch (renderPlan.toLowerCase()) {
            case "starter":
                nonHeapThreshold = 100;
                metaspaceLimit = 64;
                compressedClassLimit = 32;
                break;
            case "standard":
                nonHeapThreshold = 150;
                metaspaceLimit = 128;
                compressedClassLimit = 64;
                break;
            case "pro":
            case "pro+":
                nonHeapThreshold = 200;
                metaspaceLimit = 256;
                compressedClassLimit = 128;
                break;
            default:
                nonHeapThreshold = Math.max(100, memoryLimitMB / 8);
                metaspaceLimit = Math.min(128, memoryLimitMB / 8);
                compressedClassLimit = Math.min(64, memoryLimitMB / 16);
        }

        if (nonHeapUsed > nonHeapThreshold) {
            log.warn("⚠️ Non-Heap память {} MB превышает рекомендуемый порог {} MB для плана {}!",
                    nonHeapUsed, nonHeapThreshold, renderPlan);
            log.warn("   💡 Добавьте в JVM флаги:");
            log.warn("   💡 -XX:MaxMetaspaceSize={}m", metaspaceLimit);
            log.warn("   💡 -XX:CompressedClassSpaceSize={}m", compressedClassLimit);
        } else {
            log.info("✅ Non-Heap память {} MB оптимальна для плана {}", nonHeapUsed, renderPlan);
        }
    }

    /**
     * Анализ Heap памяти для Render
     */
    private void analyzeRenderHeapMemory(MemoryUsage heapMemory) {
        long heapMax = heapMemory.getMax() / (1024 * 1024);
        long recommendedHeapMax = getRecommendedHeapMax();

        if (heapMax > recommendedHeapMax) {
            log.warn("⚠️ Heap лимит {} MB слишком велик для плана {}! Рекомендуется -Xmx{}m",
                    heapMax, renderPlan, recommendedHeapMax);
        } else if (heapMax < recommendedHeapMax * 0.7) {
            log.info("💡 Можно увеличить Heap до -Xmx{}m для лучшей производительности",
                    recommendedHeapMax);
        } else {
            log.info("✅ Heap лимит {} MB оптимален для плана {}", heapMax, renderPlan);
        }
    }

    /**
     * Рекомендуемый размер Heap для каждого плана Render
     */
    private long getRecommendedHeapMax() {
        switch (renderPlan.toLowerCase()) {
            case "starter": return 350;      // 512 MB * 0.68
            case "standard": return 768;     // 1024 MB * 0.75
            case "pro": return 1536;         // 2048 MB * 0.75
            case "pro+": return 3072;        // 4096 MB * 0.75
            default: return (long)(memoryLimitMB * 0.7);
        }
    }

    /**
     * JVM рекомендации для каждого плана Render
     */
    private void provideRenderJVMRecommendations() {
        log.info("💡 === ОПТИМАЛЬНЫЕ JVM НАСТРОЙКИ ДЛЯ RENDER {} ===", renderPlan.toUpperCase());

        switch (renderPlan.toLowerCase()) {
            case "starter":
                log.info("🎯 RENDER STARTER (512 MB) - $7/мес:");
                log.info("   -Xms200m -Xmx350m");
                log.info("   -XX:MaxMetaspaceSize=64m");
                log.info("   -XX:CompressedClassSpaceSize=32m");
                log.info("   -XX:+UseG1GC -XX:MaxGCPauseMillis=100");
                break;

            case "standard":
                log.info("🎯 RENDER STANDARD (1 GB) - $25/мес:");
                log.info("   -Xms400m -Xmx768m");
                log.info("   -XX:MaxMetaspaceSize=128m");
                log.info("   -XX:CompressedClassSpaceSize=64m");
                log.info("   -XX:+UseG1GC -XX:MaxGCPauseMillis=100");
                break;

            case "pro":
                log.info("🎯 RENDER PRO (2 GB) - $85/мес:");
                log.info("   -Xms512m -Xmx1536m");
                log.info("   -XX:MaxMetaspaceSize=256m");
                log.info("   -XX:CompressedClassSpaceSize=128m");
                log.info("   -XX:+UseG1GC");
                break;

            case "pro+":
                log.info("🎯 RENDER PRO+ (4 GB) - $170/мес:");
                log.info("   -Xms1024m -Xmx3072m");
                log.info("   -XX:MaxMetaspaceSize=256m");
                log.info("   -XX:+UseG1GC");
                break;

            default:
                log.info("🎯 RENDER CUSTOM ({} MB):", memoryLimitMB);
                log.info("   -Xms{}m -Xmx{}m", memoryLimitMB / 4, getRecommendedHeapMax());
                log.info("   -XX:MaxMetaspaceSize={}m", Math.min(256, memoryLimitMB / 8));
        }

        // Общие рекомендации для Render
        log.info("🔧 Общие флаги для всех планов Render:");
        log.info("   -XX:+UseCompressedOops");
        log.info("   -XX:+UseContainerSupport");
        log.info("   -XX:MaxRAMPercentage=75.0");
    }

    /**
     * Анализ критичности и рекомендации по апгрейду
     */
    private void analyzeRenderCriticalStatus(long totalUsed, long remainingMB) {
        double usagePercent = (double) totalUsed / memoryLimitMB * 100;

        if (totalUsed > criticalThresholdMB) {
            log.error("🚨 КРИТИЧНО: {}% памяти использовано на плане {}!",
                    String.format("%.1f", usagePercent), renderPlan);
            log.error("💡 СРОЧНЫЕ ДЕЙСТВИЯ:");
            log.error("   1. 🗑️ Принудительная сборка мусора");
            log.error("   2. 📈 Upgrade плана Render");
            log.error("   3. 🔧 Оптимизация JVM настроек");

            suggestRenderUpgrade();

        } else if (totalUsed > warningThresholdMB) {
            log.warn("⚠️ ВНИМАНИЕ: {}% памяти использовано на плане {}",
                    String.format("%.1f", usagePercent), renderPlan);
            log.warn("💡 Рекомендуется подготовка к масштабированию");

            if (remainingMB < 100) {
                log.warn("🔥 Осталось мало памяти! Рассмотрите upgrade");
                suggestRenderUpgrade();
            }

        } else {
            log.info("✅ Использование памяти оптимально: {}% на плане {}",
                    String.format("%.1f", usagePercent), renderPlan);

            if (usagePercent < 50 && !renderPlan.equals("Starter")) {
                log.info("💰 Возможно, можно сэкономить, используя план меньше");
            }
        }
    }

    /**
     * Предложения по upgrade плана Render
     */
    private void suggestRenderUpgrade() {
        String nextPlan = getNextRenderPlan();
        int nextPrice = getNextRenderPrice();
        long nextMemory = getNextRenderMemory();

        if (nextPlan != null) {
            log.error("💰 === РЕКОМЕНДАЦИЯ UPGRADE ===");
            log.error("📈 Текущий план: {} ({} MB) - ${}/мес", renderPlan, memoryLimitMB, getRenderPrice(memoryLimitMB));
            log.error("🚀 Рекомендуемый: {} ({} MB) - ${}/мес", nextPlan, nextMemory, nextPrice);
            log.error("💵 Доплата: +${}/мес", nextPrice - getRenderPrice(memoryLimitMB));
            log.error("🔧 После upgrade обновите JVM настройки автоматически!");
        } else {
            log.error("💎 Вы уже используете максимальный план Render!");
            log.error("🔧 Требуется оптимизация приложения или миграция на VPS");
        }
    }

    private String getNextRenderPlan() {
        switch (renderPlan.toLowerCase()) {
            case "starter": return "Standard";
            case "standard": return "Pro";
            case "pro": return "Pro+";
            default: return null;
        }
    }

    private long getNextRenderMemory() {
        switch (renderPlan.toLowerCase()) {
            case "starter": return 1024;
            case "standard": return 2048;
            case "pro": return 4096;
            default: return memoryLimitMB;
        }
    }

    private int getNextRenderPrice() {
        switch (renderPlan.toLowerCase()) {
            case "starter": return 25;
            case "standard": return 85;
            case "pro": return 170;
            default: return getRenderPrice(memoryLimitMB);
        }
    }

    private int getRenderPrice(long memoryMB) {
        if (memoryMB <= 512) return 7;
        if (memoryMB <= 1024) return 25;
        if (memoryMB <= 2048) return 85;
        return 170;
    }

    // Геттеры для использования в других классах
    public long getMemoryLimitMB() { return memoryLimitMB; }
    public long getCriticalThresholdMB() { return criticalThresholdMB; }
    public long getWarningThresholdMB() { return warningThresholdMB; }
    public String getRenderPlan() { return renderPlan; }
}