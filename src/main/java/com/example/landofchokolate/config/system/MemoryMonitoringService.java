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
    private final DecimalFormat df = new DecimalFormat("#.##");

    public MemoryMonitoringService() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Логирование статистики памяти каждые 5 минут
     */
    @Scheduled(fixedRate = 300000) // 5 минут = 300000 мс
    public void logMemoryStatistics() {
        logJVMMemoryUsage();
        logGarbageCollectionInfo();
    }



    /**
     * Подробное логирование памяти JVM
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

        log.info("📊 === JVM ПАМЯТЬ СТАТИСТИКА ===");
        log.info("🔷 HEAP ПАМЯТЬ: Использовано: {} MB / {} MB ({}%), Свободно: {} MB",
                heapUsed, heapMax, df.format(heapUsagePercent), heapFree);
        log.info("🔶 NON-HEAP ПАМЯТЬ: Использовано: {} MB / {} MB",
                nonHeapUsed, nonHeapMax > 0 ? nonHeapMax : "unlimited");

        // Системная память
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        log.info("💾 СИСТЕМНАЯ ПАМЯТЬ: Общая: {} MB, Свободная: {} MB, Максимальная: {} MB",
                totalMemory, freeMemory, maxMemory);
    }



    /**
     * Информация о сборщике мусора
     */
    private void logGarbageCollectionInfo() {
        try {
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
                log.info("🗑️ GC {}: Коллекций: {}, Время: {} мс",
                        gcBean.getName(),
                        gcBean.getCollectionCount(),
                        gcBean.getCollectionTime());
            });
        } catch (Exception e) {
            log.debug("Ошибка при получении информации о GC: {}", e.getMessage());
        }
    }

    /**
     * Получить текущую информацию о памяти
     */
    public MemoryInfo getMemoryInfo() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();

        long usedMB = heapMemory.getUsed() / (1024 * 1024);
        long maxMB = heapMemory.getMax() / (1024 * 1024);
        long freeMB = maxMB - usedMB;
        double usagePercent = ((double) usedMB / maxMB) * 100;

        return new MemoryInfo(usedMB, freeMB, maxMB, usagePercent);
    }



    /**
     * Класс для хранения информации о памяти
     */
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

        public String getUsagePercentage() { return new DecimalFormat("#.##").format(usagePercentageValue); }
    }
}