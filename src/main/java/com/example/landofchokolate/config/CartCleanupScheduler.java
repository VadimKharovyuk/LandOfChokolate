//package com.example.landofchokolate.config;
//
//import com.example.landofchokolate.enums.CartStatus;
//import com.example.landofchokolate.model.Cart;
//import com.example.landofchokolate.repository.CartRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * Планировщик для автоматической очистки корзин
// *
// * Настройки в application.properties:
// * # ===== Настройки очистки корзин =====
// * # Включить/выключить автоматическую очистку корзин
// * app.cart.cleanup.enabled=true
// * # Порог неактивности в часах (корзины старше этого времени обрабатываются)
// * app.cart.cleanup.inactive-threshold-hours=24
// * # Порог удаления в днях (корзины с определенными статусами старше этого времени удаляются)
// * app.cart.cleanup.delete-threshold-days=7
// * # Размер пакета для обработки корзин за один раз
// * app.cart.cleanup.batch-size=100
// * # Немедленное удаление пустых и очень старых корзин
// * app.cart.cleanup.immediate-delete=true
// *
// * # ===== Расписание очистки =====
// * # Каждый час - обработка неактивных корзин
// * # Каждые 6 часов - удаление помеченных корзин
// * # Каждые 24 часа - экстренная очистка старых корзин (30+ дней)
// * # Каждые 30 минут - логирование статистики
// *
// * # ===== Настройки корзин =====
// * # Время жизни cookie корзины (30 дней в секундах)
// * app.cart.cookie.max-age=2592000
// * # Время истечения корзины в днях
// * app.cart.expiration.days=30
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//@ConditionalOnProperty(value = "app.cart.cleanup.enabled", havingValue = "true", matchIfMissing = true)
//public class CartCleanupScheduler {
//
//    private final CartRepository cartRepository;
//    private final CartMemoryMonitor cartMemoryMonitor;
//
//    @Value("${app.cart.cleanup.inactive-threshold-hours:24}")
//    private int inactiveThresholdHours;
//
//    @Value("${app.cart.cleanup.delete-threshold-days:7}")
//    private int deleteThresholdDays;
//
//    @Value("${app.cart.cleanup.batch-size:100}")
//    private int batchSize;
//
//    @Value("${app.cart.cleanup.immediate-delete:true}")
//    private boolean immediateDelete;
//
//    /**
//     * 🕐 1. КАЖДЫЙ ЧАС - Основная очистка корзин
//     *
//     * Выполняемые действия:
//     * • Пустые корзины → УДАЛЯЮТСЯ НЕМЕДЛЕННО
//     * • Корзины старше 7 дней → УДАЛЯЮТСЯ НЕМЕДЛЕННО
//     * • Остальные неактивные → помечаются как ABANDONED
//     */
//    @Scheduled(fixedRate = 3600000) // каждый час
//    @Transactional
//    public void cleanupInactiveCarts() {
//        try {
//            LocalDateTime inactiveThreshold = LocalDateTime.now().minusHours(inactiveThresholdHours);
//            List<Cart> inactiveCarts = cartRepository.findCartsForCleanup(inactiveThreshold);
//
//            if (!inactiveCarts.isEmpty()) {
//                log.info("Found {} inactive carts to process", inactiveCarts.size());
//
//                int processedCount = 0;
//                int deletedCount = 0;
//
//                for (Cart cart : inactiveCarts) {
//                    if (processedCount >= batchSize) {
//                        break;
//                    }
//
//                    if (immediateDelete && shouldDeleteImmediately(cart)) {
//                        // Немедленно удаляем пустые корзины или очень старые
//                        cartRepository.delete(cart);
//                        deletedCount++;
//                        log.debug("Deleted cart {} immediately", cart.getCartUuid());
//                    } else {
//                        // Помечаем как заброшенную для последующего удаления
//                        cart.setStatus(CartStatus.ABANDONED);
//                        cartRepository.save(cart);
//                        log.debug("Marked cart {} as abandoned", cart.getCartUuid());
//                    }
//
//                    processedCount++;
//                }
//
//                log.info("Processed {} carts: {} deleted immediately, {} marked as abandoned",
//                        processedCount, deletedCount, processedCount - deletedCount);
//            }
//
//        } catch (Exception e) {
//            log.error("Error during inactive cart cleanup", e);
//        }
//    }
//
//    /**
//     * 🕕 2. КАЖДЫЕ 6 ЧАСОВ - Удаление помеченных корзин
//     *
//     * Выполняемые действия:
//     * • ABANDONED корзины старше 1 дня → УДАЛЯЮТСЯ
//     * • Корзины со статусами EXPIRED, CONVERTED, ABANDONED старше 7 дней → УДАЛЯЮТСЯ
//     */
//    @Scheduled(fixedRate = 21600000) // каждые 6 часов
//    @Transactional
//    public void deleteAbandonedCarts() {
//        try {
//            LocalDateTime deleteThreshold = LocalDateTime.now().minusDays(deleteThresholdDays);
//
//            // Удаляем корзины со статусами EXPIRED, CONVERTED, ABANDONED старше порога
//            int deletedCount = cartRepository.deleteOldCarts(deleteThreshold);
//
//            if (deletedCount > 0) {
//                log.info("Deleted {} old carts older than {} days", deletedCount, deleteThresholdDays);
//            }
//
//            // Дополнительно удаляем все ABANDONED корзины старше 1 дня
//            LocalDateTime abandonedThreshold = LocalDateTime.now().minusDays(1);
//            int abandonedDeleted = cartRepository.deleteAbandonedCarts(abandonedThreshold);
//
//            if (abandonedDeleted > 0) {
//                log.info("Deleted {} abandoned carts older than 1 day", abandonedDeleted);
//            }
//
//        } catch (Exception e) {
//            log.error("Error during old cart deletion", e);
//        }
//    }
//
//    /**
//     * 🕘 3. КАЖДЫЕ 24 ЧАСА - Экстренная очистка всех старых корзин
//     *
//     * Выполняемые действия:
//     * • ВСЕ корзины старше 30 дней → УДАЛЯЮТСЯ ПРИНУДИТЕЛЬНО
//     */
//    @Scheduled(fixedRate = 86400000) // каждые 24 часа
//    @Transactional
//    public void emergencyCleanup() {
//        try {
//            // Удаляем все корзины старше 30 дней независимо от статуса
//            LocalDateTime emergencyThreshold = LocalDateTime.now().minusDays(30);
//            int deletedCount = cartRepository.deleteAllOldCarts(emergencyThreshold);
//
//            if (deletedCount > 0) {
//                log.info("Emergency cleanup: deleted {} carts older than 30 days", deletedCount);
//            }
//
//        } catch (Exception e) {
//            log.error("Error during emergency cleanup", e);
//        }
//    }
//
//    /**
//     * 📊 КАЖДЫЕ 30 МИНУТ - Логирование статистики
//     */
//    @Scheduled(fixedRate = 1800000) // каждые 30 минут
//    public void logStatistics() {
//        try {
//            cartMemoryMonitor.logCartStatistics();
//            cartMemoryMonitor.checkMemoryUsage();
//
//        } catch (Exception e) {
//            log.error("Error during statistics logging", e);
//        }
//    }
//
//    /**
//     * Проверить, нужно ли удалить корзину немедленно
//     */
//    private boolean shouldDeleteImmediately(Cart cart) {
//        // Удаляем немедленно если:
//        // 1. Корзина пустая (нет товаров)
//        // 2. Корзина очень старая (>7 дней без активности)
//        // 3. Корзина уже помечена как истекшая
//
//        boolean isEmpty = cart.getItems() == null || cart.getItems().isEmpty();
//        boolean isVeryOld = cart.getLastActivityAt() != null &&
//                cart.getLastActivityAt().isBefore(LocalDateTime.now().minusDays(7));
//        boolean isExpired = cart.getStatus() == CartStatus.EXPIRED;
//
//        return isEmpty || isVeryOld || isExpired;
//    }
//
//    /**
//     * 🔧 Ручная очистка для администраторов
//     */
//    public void manualCleanup(int daysThreshold) {
//        log.info("Starting manual cleanup for carts older than {} days", daysThreshold);
//
//        try {
//            LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
//            int deletedCount = cartRepository.deleteAllOldCarts(threshold);
//            log.info("Manual cleanup completed: deleted {} carts", deletedCount);
//
//            // Логируем статистику после очистки
//            cartMemoryMonitor.logCartStatistics();
//
//        } catch (Exception e) {
//            log.error("Error during manual cleanup", e);
//        }
//    }
//}
//
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

/**
 * Планировщик для автоматической очистки корзин
 *
 * Настройки в application.properties:
 * # ===== Настройки очистки корзин =====
 * # Включить/выключить автоматическую очистку корзин
 * app.cart.cleanup.enabled=true
 * # Порог удаления корзин в днях (по умолчанию 30 дней)
 * app.cart.cleanup.delete-threshold-days=30
 *
 * # ===== Расписание очистки =====
 * # Каждые 24 часа - удаление старых корзин (30+ дней неиспользования)
 *
 * # ===== Настройки корзин =====
 * # Время жизни cookie корзины (30 дней в секундах)
 * app.cart.cookie.max-age=2592000
 * # Время истечения корзины в днях
 * app.cart.expiration.days=30
 */
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
    @Scheduled(fixedRate = 86400000) // каждые 24 часа (в миллисекундах)
    @Transactional
    public void cleanupOldCarts() {
        try {
            log.info("Starting cart cleanup for carts older than {} days", deleteThresholdDays);

            // Удаляем все корзины старше указанного порога
            LocalDateTime deleteThreshold = LocalDateTime.now().minusDays(deleteThresholdDays);
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

    /**
     * 🔧 Ручная очистка для администраторов
     *
     * @param daysThreshold количество дней для порога удаления
     */
    public void manualCleanup(int daysThreshold) {
        log.info("Starting manual cart cleanup for carts older than {} days", daysThreshold);

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
            int deletedCount = cartRepository.deleteAllOldCarts(threshold);

            log.info("Manual cart cleanup completed: deleted {} carts", deletedCount);

        } catch (Exception e) {
            log.error("Error during manual cart cleanup", e);
        }
    }

}