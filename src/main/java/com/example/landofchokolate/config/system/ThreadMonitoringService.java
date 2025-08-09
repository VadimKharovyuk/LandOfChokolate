package com.example.landofchokolate.config.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@Service
@Slf4j
public class ThreadMonitoringService {

    private final ThreadMXBean threadBean;

    public ThreadMonitoringService() {
        this.threadBean = ManagementFactory.getThreadMXBean();
    }

    @Scheduled(fixedRate = 300000)
    public void logThreadStatistics() {
        int activeThreads = threadBean.getThreadCount();
        int peakThreads = threadBean.getPeakThreadCount();
        int daemonThreads = threadBean.getDaemonThreadCount();
        long totalStarted = threadBean.getTotalStartedThreadCount();

        log.info("🧵 ПОТОКИ: Активные: {}, Пиковое: {}, Демоны: {}, Всего запущено: {}",
                activeThreads, peakThreads, daemonThreads, totalStarted);
    }
}
