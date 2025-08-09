//package com.example.landofchokolate.config.system;
//
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import javax.management.MBeanServer;
//import java.lang.management.ManagementFactory;
//import java.lang.management.MemoryMXBean;
//import java.lang.management.MemoryUsage;
//import java.text.DecimalFormat;
//
//@Service
//@Slf4j
//public class MemoryMonitoringService {
//
//    private final MemoryMXBean memoryBean;
//    private final MBeanServer mBeanServer;
//    private final DecimalFormat df = new DecimalFormat("#.##");
//
//    public MemoryMonitoringService() {
//        this.memoryBean = ManagementFactory.getMemoryMXBean();
//        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
//    }
//
//    /**
//     * Логирование статистики памяти каждые 5 минут
//     */
//    @Scheduled(fixedRate = 300000) // 5 минут = 300000 мс
//    public void logMemoryStatistics() {
//        logJVMMemoryUsage();
//        logGarbageCollectionInfo();
//    }
//
//
//
//    /**
//     * Подробное логирование памяти JVM
//     */
//    public void logJVMMemoryUsage() {
//        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
//        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
//
//        // Heap память (основная память для объектов)
//        long heapUsed = heapMemory.getUsed() / (1024 * 1024);
//        long heapMax = heapMemory.getMax() / (1024 * 1024);
//        long heapFree = heapMax - heapUsed;
//        double heapUsagePercent = ((double) heapUsed / heapMax) * 100;
//
//        // Non-Heap память (метаклассы, код)
//        long nonHeapUsed = nonHeapMemory.getUsed() / (1024 * 1024);
//        long nonHeapMax = nonHeapMemory.getMax() / (1024 * 1024);
//
//        log.info("📊 === JVM ПАМЯТЬ СТАТИСТИКА ===");
//        log.info("🔷 HEAP ПАМЯТЬ: Использовано: {} MB / {} MB ({}%), Свободно: {} MB",
//                heapUsed, heapMax, df.format(heapUsagePercent), heapFree);
//        log.info("🔶 NON-HEAP ПАМЯТЬ: Использовано: {} MB / {} MB",
//                nonHeapUsed, nonHeapMax > 0 ? nonHeapMax : "unlimited");
//
//        // Системная память
//        Runtime runtime = Runtime.getRuntime();
//        long totalMemory = runtime.totalMemory() / (1024 * 1024);
//        long freeMemory = runtime.freeMemory() / (1024 * 1024);
//        long maxMemory = runtime.maxMemory() / (1024 * 1024);
//
//        log.info("💾 СИСТЕМНАЯ ПАМЯТЬ: Общая: {} MB, Свободная: {} MB, Максимальная: {} MB",
//                totalMemory, freeMemory, maxMemory);
//    }
//
//
//
//    /**
//     * Информация о сборщике мусора
//     */
//    private void logGarbageCollectionInfo() {
//        try {
//            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
//                log.info("🗑️ GC {}: Коллекций: {}, Время: {} мс",
//                        gcBean.getName(),
//                        gcBean.getCollectionCount(),
//                        gcBean.getCollectionTime());
//            });
//        } catch (Exception e) {
//            log.debug("Ошибка при получении информации о GC: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * Получить текущую информацию о памяти
//     */
//    public MemoryInfo getMemoryInfo() {
//        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
//
//        long usedMB = heapMemory.getUsed() / (1024 * 1024);
//        long maxMB = heapMemory.getMax() / (1024 * 1024);
//        long freeMB = maxMB - usedMB;
//        double usagePercent = ((double) usedMB / maxMB) * 100;
//
//        return new MemoryInfo(usedMB, freeMB, maxMB, usagePercent);
//    }
//
//    /**
//     * Принудительный вызов сборки мусора (использовать осторожно!)
//     */
//    public void forceGarbageCollection() {
//        log.info("🗑️ Принудительный вызов сборки мусора...");
//        long beforeMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
//
//        System.gc();
//
//        // Ждем немного для завершения GC
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        long afterMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
//        long freed = beforeMemory - afterMemory;
//
//        log.info("🗑️ Сборка мусора завершена. Освобождено: {} MB", freed);
//    }
//
//    /**
//     * Класс для хранения информации о памяти
//     */
//    @Getter
//    public static class MemoryInfo {
//        private final long usedMemoryMB;
//        private final long freeMemoryMB;
//        private final long maxMemoryMB;
//        private final double usagePercentageValue;
//
//        public MemoryInfo(long usedMemoryMB, long freeMemoryMB, long maxMemoryMB, double usagePercentageValue) {
//            this.usedMemoryMB = usedMemoryMB;
//            this.freeMemoryMB = freeMemoryMB;
//            this.maxMemoryMB = maxMemoryMB;
//            this.usagePercentageValue = usagePercentageValue;
//        }
//
//        public String getUsagePercentage() { return new DecimalFormat("#.##").format(usagePercentageValue); }
//    }
//}


package com.example.landofchokolate.config.system;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

@Service
@Slf4j
public class MemoryMonitoringService {

    private final MemoryMXBean memoryBean;
    private final MBeanServer mBeanServer;
    private final DecimalFormat df;

    // Константы для 512 MB лимита Render
    private static final long RENDER_MEMORY_LIMIT_MB = 512;
    private static final long CRITICAL_THRESHOLD_MB = 460; // 90% от лимита
    private static final long WARNING_THRESHOLD_MB = 410;  // 80% от лимита

    public MemoryMonitoringService() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();

        // Создаем DecimalFormat с точкой как разделителем
        this.df = new DecimalFormat("#.##");
        this.df.setDecimalFormatSymbols(java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
    }

    /**
     * Логирование статистики памяти каждые 5 минут
     */
    @Scheduled(fixedRate = 300000)
    public void logMemoryStatistics() {
        logJVMMemoryUsage();
        logGarbageCollectionInfo();
        analyzeMemoryForRender();
    }

    /**
     * Критический мониторинг каждую минуту при высоком использовании
     */
    @Scheduled(fixedRate = 60000) // каждую минуту
    public void criticalMemoryCheck() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        long totalUsedMB = (heapMemory.getUsed() + nonHeapMemory.getUsed()) / (1024 * 1024);

        if (totalUsedMB > CRITICAL_THRESHOLD_MB) {
            log.error("🚨 КРИТИЧНО! Память: {} MB / {} MB - приложение может крашнуться!",
                    totalUsedMB, RENDER_MEMORY_LIMIT_MB);
            log.error("💡 СРОЧНО: Нужно увеличить память на Render или оптимизировать код!");

            // Принудительная сборка мусора при критическом уровне
            forceGarbageCollection();
        } else if (totalUsedMB > WARNING_THRESHOLD_MB) {
            log.warn("⚠️ ВНИМАНИЕ: Память {} MB / {} MB - близко к лимиту!",
                    totalUsedMB, RENDER_MEMORY_LIMIT_MB);
        }
    }

    /**
     * Подробное логирование памяти JVM с анализом для Render
     */
    public void logJVMMemoryUsage() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        // Heap память (основная память для объектов)
        long heapUsed = heapMemory.getUsed() / (1024 * 1024);
        long heapMax = heapMemory.getMax() / (1024 * 1024);
        long heapFree = heapMax - heapUsed;
        double heapUsagePercent = ((double) heapUsed / heapMax) * 100;

        // Non-Heap память (метаклассы, код)
        long nonHeapUsed = nonHeapMemory.getUsed() / (1024 * 1024);
        long nonHeapMax = nonHeapMemory.getMax() / (1024 * 1024);

        // Системная память
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        // Общее использование памяти
        long totalUsedMemory = heapUsed + nonHeapUsed;
        double renderUsagePercent = ((double) totalUsedMemory / RENDER_MEMORY_LIMIT_MB) * 100;

        log.info("📊 === JVM ПАМЯТЬ СТАТИСТИКА ===");
        log.info("🔷 HEAP ПАМЯТЬ: Использовано: {} MB / {} MB ({}%), Свободно: {} MB",
                heapUsed, heapMax, df.format(heapUsagePercent), heapFree);
        log.info("🔶 NON-HEAP ПАМЯТЬ: Использовано: {} MB / {} MB",
                nonHeapUsed, nonHeapMax > 0 ? nonHeapMax : "unlimited");
        log.info("💾 СИСТЕМНАЯ ПАМЯТЬ: Общая: {} MB, Свободная: {} MB, Максимальная: {} MB",
                totalMemory, freeMemory, maxMemory);

        // КРИТИЧНО! Анализ относительно лимита Render
        log.info("🚀 === АНАЛИЗ ДЛЯ RENDER (512 MB ЛИМИТ) ===");
        log.info("📈 ОБЩЕЕ ИСПОЛЬЗОВАНИЕ: {} MB / {} MB ({}%)",
                totalUsedMemory, RENDER_MEMORY_LIMIT_MB, df.format(renderUsagePercent));

        long remainingMemory = RENDER_MEMORY_LIMIT_MB - totalUsedMemory;
        log.info("🆓 ОСТАЛОСЬ ПАМЯТИ: {} MB", remainingMemory);

        // Статус и рекомендации
        if (renderUsagePercent > 90) {
            log.error("🔥 КРИТИЧНО: Осталось только {} MB! Приложение может крашнуться!", remainingMemory);
        } else if (renderUsagePercent > 80) {
            log.warn("⚠️ ВЫСОКОЕ использование: Осталось {} MB. Нужна оптимизация!", remainingMemory);
        } else if (renderUsagePercent > 60) {
            log.info("🟡 УМЕРЕННОЕ использование: {} MB свободно", remainingMemory);
        } else {
            log.info("🟢 ХОРОШЕЕ использование: {} MB запаса", remainingMemory);
        }
    }

    /**
     * Анализ памяти специально для Render
     */
    private void analyzeMemoryForRender() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        long heapUsed = heapMemory.getUsed() / (1024 * 1024);
        long nonHeapUsed = nonHeapMemory.getUsed() / (1024 * 1024);
        long totalUsed = heapUsed + nonHeapUsed;

        log.info("🎯 === РЕКОМЕНДАЦИИ ДЛЯ RENDER ===");

        // Анализ Non-Heap (часто проблема в Java)
        if (nonHeapUsed > 100) {
            log.warn("⚠️ Non-Heap память {} MB слишком велика! Рекомендации:", nonHeapUsed);
            log.warn("   💡 Добавьте в JVM: -XX:MaxMetaspaceSize=64m");
            log.warn("   💡 Добавьте в JVM: -XX:CompressedClassSpaceSize=32m");
        }

        // Рекомендации по JVM настройкам
        long heapMax = heapMemory.getMax() / (1024 * 1024);
        if (heapMax > 300) {
            log.warn("⚠️ Heap лимит {} MB слишком велик для 512 MB! Установите -Xmx350m", heapMax);
        }

        // Прогноз нехватки памяти
        if (totalUsed > WARNING_THRESHOLD_MB) {
            long timeToLimit = estimateTimeToMemoryLimit();
            if (timeToLimit > 0) {
                log.warn("⏰ При текущем росте память закончится через {} минут", timeToLimit);
            }
        }

        // Рекомендации по тарифам Render
        if (totalUsed > CRITICAL_THRESHOLD_MB) {
            log.error("💰 РЕКОМЕНДАЦИЯ: Обновите Render до 1 GB ($25/мес) или оптимизируйте приложение");
        }
    }

    /**
     * Простая оценка времени до исчерпания памяти
     */
    private long estimateTimeToMemoryLimit() {
        // Здесь можно добавить логику отслеживания роста памяти во времени
        // Пока возвращаем примерную оценку
        return -1; // Не реализовано
    }

    /**
     * Информация о сборщике мусора с анализом
     */
    private void logGarbageCollectionInfo() {
        try {
            log.info("🗑️ === СТАТИСТИКА СБОРКИ МУСОРА ===");
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
                long collections = gcBean.getCollectionCount();
                long time = gcBean.getCollectionTime();

                log.info("🗑️ GC {}: Коллекций: {}, Время: {} мс",
                        gcBean.getName(), collections, time);

                // Анализ частоты GC
                if (collections > 100) {
                    log.warn("⚠️ Слишком частый GC! Возможно нехватка памяти или утечки");
                }

                if (time > 5000) {
                    log.warn("⚠️ GC тратит много времени: {} мс. Приложение может тормозить", time);
                }
            });
        } catch (Exception e) {
            log.debug("Ошибка при получении информации о GC: {}", e.getMessage());
        }
    }

    /**
     * Получить детальную информацию о памяти для API/мониторинга
     */
    public DetailedMemoryInfo getDetailedMemoryInfo() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        Runtime runtime = Runtime.getRuntime();

        // Проверяем что данные получены
        if (heapMemory == null || nonHeapMemory == null) {
            throw new RuntimeException("Не удалось получить информацию о памяти JVM");
        }

        // Heap память
        long heapUsedMB = heapMemory.getUsed() / (1024 * 1024);
        long heapMaxMB = heapMemory.getMax() / (1024 * 1024);
        long heapFreeMB = heapMaxMB - heapUsedMB;
        double heapUsagePercent = ((double) heapUsedMB / heapMaxMB) * 100;

        // Non-Heap память
        long nonHeapUsedMB = nonHeapMemory.getUsed() / (1024 * 1024);
        long nonHeapMaxMB = nonHeapMemory.getMax() / (1024 * 1024);

        // Системная память
        long systemTotalMB = runtime.totalMemory() / (1024 * 1024);
        long systemFreeMB = runtime.freeMemory() / (1024 * 1024);
        long systemMaxMB = runtime.maxMemory() / (1024 * 1024);

        // Общая статистика для Render
        long totalUsedMB = heapUsedMB + nonHeapUsedMB;
        double renderUsagePercent = ((double) totalUsedMB / RENDER_MEMORY_LIMIT_MB) * 100;
        long remainingMB = RENDER_MEMORY_LIMIT_MB - totalUsedMB;

        // Статус и рекомендации
        String status = getMemoryStatus(renderUsagePercent);
        String statusIcon = getMemoryStatusIcon(renderUsagePercent);
        String statusText = getMemoryStatusText(renderUsagePercent);
        String usageStatus = getUsageStatus(renderUsagePercent);
        String recommendation = getMemoryRecommendation(renderUsagePercent, nonHeapUsedMB);

        return new DetailedMemoryInfo(
                heapUsedMB, heapFreeMB, heapMaxMB, heapUsagePercent, statusIcon, statusText, usageStatus,
                nonHeapUsedMB, nonHeapMaxMB,
                systemTotalMB, systemFreeMB, systemMaxMB,
                totalUsedMB, RENDER_MEMORY_LIMIT_MB, renderUsagePercent, remainingMB,
                status, recommendation
        );
    }

    private String getMemoryStatusIcon(double renderUsagePercent) {
        if (renderUsagePercent > 90) return "🔴";
        if (renderUsagePercent > 80) return "🟡";
        if (renderUsagePercent > 60) return "🟠";
        return "🟢";
    }

    private String getMemoryStatusText(double renderUsagePercent) {
        if (renderUsagePercent > 90) return "Критичний рівень";
        if (renderUsagePercent > 80) return "Високий рівень";
        if (renderUsagePercent > 60) return "Помірний рівень";
        return "Нормальний рівень";
    }

    private String getUsageStatus(double renderUsagePercent) {
        if (renderUsagePercent > 90) return "critical";
        if (renderUsagePercent > 80) return "warning";
        if (renderUsagePercent > 60) return "moderate";
        return "good";
    }


    public EnhancedMemoryInfo getEnhancedMemoryInfo() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        long heapUsedMB = heapMemory.getUsed() / (1024 * 1024);
        long heapMaxMB = heapMemory.getMax() / (1024 * 1024);
        long heapFreeMB = heapMaxMB - heapUsedMB;
        double heapUsagePercent = ((double) heapUsedMB / heapMaxMB) * 100;

        long nonHeapUsedMB = nonHeapMemory.getUsed() / (1024 * 1024);
        long nonHeapMaxMB = nonHeapMemory.getMax() / (1024 * 1024);

        long totalUsedMB = heapUsedMB + nonHeapUsedMB;
        double renderUsagePercent = ((double) totalUsedMB / RENDER_MEMORY_LIMIT_MB) * 100;
        long remainingMB = RENDER_MEMORY_LIMIT_MB - totalUsedMB;

        String status = getMemoryStatus(renderUsagePercent);
        String recommendation = getMemoryRecommendation(renderUsagePercent, nonHeapUsedMB);

        return new EnhancedMemoryInfo(heapUsedMB, heapFreeMB, heapMaxMB, heapUsagePercent,
                nonHeapUsedMB, nonHeapMaxMB, totalUsedMB, RENDER_MEMORY_LIMIT_MB,
                renderUsagePercent, remainingMB, status, recommendation);
    }

    private String getMemoryStatus(double renderUsagePercent) {
        if (renderUsagePercent > 90) return "CRITICAL";
        if (renderUsagePercent > 80) return "WARNING";
        if (renderUsagePercent > 60) return "MODERATE";
        return "GOOD";
    }

    private String getMemoryRecommendation(double renderUsagePercent, long nonHeapUsedMB) {
        if (renderUsagePercent > 90) {
            return "Срочно увеличьте память на Render до 1GB или оптимизируйте приложение!";
        }
        if (renderUsagePercent > 80) {
            return "Добавьте JVM флаги: -XX:MaxMetaspaceSize=64m -Xmx350m";
        }
        if (nonHeapUsedMB > 100) {
            return "Оптимизируйте Non-Heap память: -XX:MaxMetaspaceSize=64m";
        }
        return "Память использется эффективно";
    }

    /**
     * Принудительный вызов сборки мусора с детальным логированием
     */
    public void forceGarbageCollection() {
        log.info("🗑️ Принудительный вызов сборки мусора...");

        MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();
        MemoryUsage beforeNonHeap = memoryBean.getNonHeapMemoryUsage();
        long beforeTotal = (beforeHeap.getUsed() + beforeNonHeap.getUsed()) / (1024 * 1024);

        System.gc();

        try {
            Thread.sleep(2000); // Ждем дольше для завершения
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
        MemoryUsage afterNonHeap = memoryBean.getNonHeapMemoryUsage();
        long afterTotal = (afterHeap.getUsed() + afterNonHeap.getUsed()) / (1024 * 1024);

        long freedTotal = beforeTotal - afterTotal;
        long freedHeap = (beforeHeap.getUsed() - afterHeap.getUsed()) / (1024 * 1024);

        log.info("🗑️ Сборка мусора завершена:");
        log.info("   📉 Освобождено общей памяти: {} MB", freedTotal);
        log.info("   📉 Освобождено Heap памяти: {} MB", freedHeap);
        log.info("   📊 Общая память после GC: {} MB / {} MB", afterTotal, RENDER_MEMORY_LIMIT_MB);
    }

    /**
     * Детальная информация о памяти (совместимо с вашим JSON форматом)
     */
    @Getter
    public static class DetailedMemoryInfo {
        // Heap память
        private final long heapUsedMB;
        private final long heapFreeMB;
        private final long heapMaxMB;
        private final double heapUsagePercent;
        private final String heapStatusIcon;
        private final String heapStatusText;
        private final String heapUsageStatus;

        // Non-Heap память
        private final long nonHeapUsedMB;
        private final long nonHeapMaxMB;

        // Системная память
        private final long systemTotalMB;
        private final long systemFreeMB;
        private final long systemMaxMB;

        // Статистика для Render
        private final long totalUsedMB;
        private final long renderLimitMB;
        private final double renderUsagePercent;
        private final long remainingMB;

        // Статус и рекомендации
        private final String status;
        private final String recommendation;

        public DetailedMemoryInfo(long heapUsedMB, long heapFreeMB, long heapMaxMB, double heapUsagePercent,
                                  String heapStatusIcon, String heapStatusText, String heapUsageStatus,
                                  long nonHeapUsedMB, long nonHeapMaxMB,
                                  long systemTotalMB, long systemFreeMB, long systemMaxMB,
                                  long totalUsedMB, long renderLimitMB, double renderUsagePercent, long remainingMB,
                                  String status, String recommendation) {
            this.heapUsedMB = heapUsedMB;
            this.heapFreeMB = heapFreeMB;
            this.heapMaxMB = heapMaxMB;
            this.heapUsagePercent = heapUsagePercent;
            this.heapStatusIcon = heapStatusIcon;
            this.heapStatusText = heapStatusText;
            this.heapUsageStatus = heapUsageStatus;
            this.nonHeapUsedMB = nonHeapUsedMB;
            this.nonHeapMaxMB = nonHeapMaxMB;
            this.systemTotalMB = systemTotalMB;
            this.systemFreeMB = systemFreeMB;
            this.systemMaxMB = systemMaxMB;
            this.totalUsedMB = totalUsedMB;
            this.renderLimitMB = renderLimitMB;
            this.renderUsagePercent = renderUsagePercent;
            this.remainingMB = remainingMB;
            this.status = status;
            this.recommendation = recommendation;
        }

        // Форматированные значения (с точкой для JSON)
        public String getHeapUsageFormatted() {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
            return df.format(heapUsagePercent);
        }

        public String getRenderUsageFormatted() {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
            return df.format(renderUsagePercent);
        }

        // Совместимость с вашим JSON форматом
        public Object toCompatibleFormat() {
            return new Object() {
                public final String title = "Пам'ять JVM";
                public final String icon = "💾";
                public final String description = "Статистика використання пам'яті віртуальної машини Java";
                public final String timestamp = java.time.LocalDateTime.now().toString();
                public final String status = DetailedMemoryInfo.this.status.toLowerCase();

                public final Object heap = new Object() {
                    public final long used_mb = heapUsedMB;
                    public final long free_mb = heapFreeMB;
                    public final long max_mb = heapMaxMB;
                    public final double usage_percent = heapUsagePercent; // Используем double напрямую
                    public final String icon = "🔷";
                    public final String name = "Heap пам'ять";
                    public final String description = "Основна пам'ять для об'єктів програми";
                    public final String status_icon = heapStatusIcon;
                    public final String status_text = heapStatusText;
                    public final String usage_status = heapUsageStatus;
                };

                public final Object non_heap = new Object() {
                    public final long used_mb = nonHeapUsedMB;
                    public final long max_mb = nonHeapMaxMB > 0 ? nonHeapMaxMB : -1;
                    public final String icon = "🔶";
                    public final String name = "Non-Heap пам'ять";
                    public final String description = "Пам'ять для метакласів та коду JVM";
                };

                public final Object system = new Object() {
                    public final long total_mb = systemTotalMB;
                    public final long free_mb = systemFreeMB;
                    public final long max_mb = systemMaxMB;
                    public final String icon = "🖥️";
                    public final String name = "Системна пам'ять";
                    public final String description = "Загальна пам'ять процесу JVM";
                };

                public final Object render_analysis = new Object() {
                    public final long total_used_mb = totalUsedMB;
                    public final long limit_mb = renderLimitMB;
                    public final double usage_percent = renderUsagePercent; // Используем double напрямую
                    public final long remaining_mb = remainingMB;
                    public final String recommendation = DetailedMemoryInfo.this.recommendation;
                    public final String icon = "🚀";
                    public final String name = "Аналіз для Render";
                    public final String description = "Використання пам'яті відносно ліміту хостингу";
                };
            };
        }
    }
    @Getter
    public static class EnhancedMemoryInfo {
        private final long heapUsedMB;
        private final long heapFreeMB;
        private final long heapMaxMB;
        private final double heapUsagePercent;

        private final long nonHeapUsedMB;
        private final long nonHeapMaxMB;

        private final long totalUsedMB;
        private final long renderLimitMB;
        private final double renderUsagePercent;
        private final long remainingMB;

        private final String status;
        private final String recommendation;

        public EnhancedMemoryInfo(long heapUsedMB, long heapFreeMB, long heapMaxMB, double heapUsagePercent,
                                  long nonHeapUsedMB, long nonHeapMaxMB, long totalUsedMB, long renderLimitMB,
                                  double renderUsagePercent, long remainingMB, String status, String recommendation) {
            this.heapUsedMB = heapUsedMB;
            this.heapFreeMB = heapFreeMB;
            this.heapMaxMB = heapMaxMB;
            this.heapUsagePercent = heapUsagePercent;
            this.nonHeapUsedMB = nonHeapUsedMB;
            this.nonHeapMaxMB = nonHeapMaxMB;
            this.totalUsedMB = totalUsedMB;
            this.renderLimitMB = renderLimitMB;
            this.renderUsagePercent = renderUsagePercent;
            this.remainingMB = remainingMB;
            this.status = status;
            this.recommendation = recommendation;
        }

        public String getHeapUsageFormatted() { return new DecimalFormat("#.##").format(heapUsagePercent); }
        public String getRenderUsageFormatted() { return new DecimalFormat("#.##").format(renderUsagePercent); }
    }

    // Оставляем старый класс для совместимости
    @Getter
    public static class MemoryInfo {
        private final long usedMemoryMB;
        private final long freeMemoryMB;
        private final long maxMemoryMB;
        private final double usagePercentageValue;

        public MemoryInfo(long usedMemoryMB, long freeMemoryMB, long maxMemoryMB, double usagePercentageValue) {
            this.usedMemoryMB = usedMemoryMB;
            this.freeMemoryMB = freeMemoryMB;
            this.maxMemoryMB = maxMemoryMB;
            this.usagePercentageValue = usagePercentageValue;
        }

        public String getUsagePercentage() {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
            return df.format(usagePercentageValue);
        }
    }
}