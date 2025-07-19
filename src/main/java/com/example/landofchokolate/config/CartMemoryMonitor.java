// ===== CartMemoryMonitor.java =====
package com.example.landofchokolate.config;

import com.example.landofchokolate.repository.CartRepository;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartMemoryMonitor implements HttpSessionListener {

    private final CartRepository cartRepository;
    private final AtomicLong activeSessions = new AtomicLong(0);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        long currentSessions = activeSessions.incrementAndGet();
        log.info("Session created. Active sessions: {}", currentSessions);


        // Логируем статистику корзин при достижении определенных порогов
        if (currentSessions % 100 == 0) {
            logCartStatistics();
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        long currentSessions = activeSessions.decrementAndGet();
        log.info("Session destroyed. Active sessions: {}", currentSessions);

        // Очищаем атрибут корзины из уничтожаемой сессии
        try {
            event.getSession().removeAttribute("current_cart");
        } catch (Exception e) {
            log.debug("Error removing cart from destroyed session", e);
        }
    }

    /**
     * Получить статистику корзин
     */
    public void logCartStatistics() {
        try {
            Long activeCarts = cartRepository.countActiveCarts();
            Long cartsWithItems = cartRepository.countActiveCartsWithItems();
            Long totalItems = cartRepository.getTotalItemsInActiveCarts();
            Double avgItems = cartRepository.getAverageItemsPerCart();

            log.info("Cart Statistics - Active carts: {}, Carts with items: {}, Total items: {}, Avg items per cart: {}",
                    activeCarts, cartsWithItems, totalItems != null ? totalItems : 0,
                    avgItems != null ? String.format("%.2f", avgItems) : "0.00");

        } catch (Exception e) {
            log.error("Error collecting cart statistics", e);
        }
    }

    /**
     * Получить количество активных сессий
     */
    public long getActiveSessionCount() {
        return activeSessions.get();
    }

    /**
     * Проверить состояние памяти
     */
    public void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        log.info("Memory Usage - Used: {} MB, Free: {} MB, Total: {} MB, Max: {} MB, Usage: {:.2f}%",
                usedMemory / 1024 / 1024,
                freeMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                memoryUsagePercent);

        // Предупреждение при высоком использовании памяти
        if (memoryUsagePercent > 80) {
            log.warn("High memory usage detected: {:.2f}%. Consider garbage collection or cart cleanup.", memoryUsagePercent);
        }
    }
}


