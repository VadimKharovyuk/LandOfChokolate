package com.example.landofchokolate.config;
import com.example.landofchokolate.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.cart.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class CartCleanupScheduler {

    private final CartRepository cartRepository;

    @Value("${app.cart.cleanup.delete-threshold-days:30}")
    private int deleteThresholdDays;

    /**
     * 🕘 КАЖДЫЕ 24 ЧАСА - Удаление корзин через месяц неиспользования
     *
     * Выполняемые действия:
     * • ВСЕ корзины старше указанного порога (по умолчанию 30 дней) → УДАЛЯЮТСЯ
     */
    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void cleanupOldCarts() {
        try {
            log.info("Starting cart cleanup for carts older than {} days", deleteThresholdDays);

            LocalDateTime deleteThreshold = LocalDateTime.now().minusDays(deleteThresholdDays);

            // Сначала удаляем cart_items
            cartRepository.deleteCartItemsForOldCarts(deleteThreshold);

            // Потом удаляем корзины
            int deletedCount = cartRepository.deleteAllOldCarts(deleteThreshold);

            if (deletedCount > 0) {
                log.info("Cart cleanup completed: deleted {} carts older than {} days",
                        deletedCount, deleteThresholdDays);
            } else {
                log.debug("Cart cleanup completed: no old carts found for deletion");
            }

        } catch (Exception e) {
            log.error("Error during cart cleanup", e);
        }
    }



}