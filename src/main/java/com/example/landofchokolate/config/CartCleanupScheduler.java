package com.example.landofchokolate.config;

import com.example.landofchokolate.enums.CartStatus;
import com.example.landofchokolate.model.Cart;
import com.example.landofchokolate.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Планировщик для автоматической очистки корзин
 *
 * Настройки в application.properties:
 * # ===== Настройки очистки корзин =====
 * # Включить/выключить автоматическую очистку корзин
 * app.cart.cleanup.enabled=true
 * # Порог неактивности в часах (корзины старше этого времени обрабатываются)
 * app.cart.cleanup.inactive-threshold-hours=24
 * # Порог удаления в днях (корзины с определенными статусами старше этого времени удаляются)
 * app.cart.cleanup.delete-threshold-days=7
 * # Размер пакета для обработки корзин за один раз
 * app.cart.cleanup.batch-size=100
 * # Немедленное удаление пустых и очень старых корзин
 * app.cart.cleanup.immediate-delete=true
 *
 * # ===== Расписание очистки =====
 * # Каждый час - обработка неактивных корзин
 * # Каждые 6 часов - удаление помеченных корзин
 * # Каждые 24 часа - экстренная очистка старых корзин (30+ дней)
 * # Каждые 30 минут - логирование статистики
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
    private final CartMemoryMonitor cartMemoryMonitor;

    @Value("${app.cart.cleanup.inactive-threshold-hours:24}")
    private int inactiveThresholdHours;

    @Value("${app.cart.cleanup.delete-threshold-days:7}")
    private int deleteThresholdDays;

    @Value("${app.cart.cleanup.batch-size:100}")
    private int batchSize;

    @Value("${app.cart.cleanup.immediate-delete:true}")
    private boolean immediateDelete;

    /**
     * 🕐 1. КАЖДЫЙ ЧАС - Основная очистка корзин
     *
     * Выполняемые действия:
     * • Пустые корзины → УДАЛЯЮТСЯ НЕМЕДЛЕННО
     * • Корзины старше 7 дней → УДАЛЯЮТСЯ НЕМЕДЛЕННО
     * • Остальные неактивные → помечаются как ABANDONED
     */
    @Scheduled(fixedRate = 3600000) // каждый час
    @Transactional
    public void cleanupInactiveCarts() {
        try {
            LocalDateTime inactiveThreshold = LocalDateTime.now().minusHours(inactiveThresholdHours);
            List<Cart> inactiveCarts = cartRepository.findCartsForCleanup(inactiveThreshold);

            if (!inactiveCarts.isEmpty()) {
                log.info("Found {} inactive carts to process", inactiveCarts.size());

                int processedCount = 0;
                int deletedCount = 0;

                for (Cart cart : inactiveCarts) {
                    if (processedCount >= batchSize) {
                        break;
                    }

                    if (immediateDelete && shouldDeleteImmediately(cart)) {
                        // Немедленно удаляем пустые корзины или очень старые
                        cartRepository.delete(cart);
                        deletedCount++;
                        log.debug("Deleted cart {} immediately", cart.getCartUuid());
                    } else {
                        // Помечаем как заброшенную для последующего удаления
                        cart.setStatus(CartStatus.ABANDONED);
                        cartRepository.save(cart);
                        log.debug("Marked cart {} as abandoned", cart.getCartUuid());
                    }

                    processedCount++;
                }

                log.info("Processed {} carts: {} deleted immediately, {} marked as abandoned",
                        processedCount, deletedCount, processedCount - deletedCount);
            }

        } catch (Exception e) {
            log.error("Error during inactive cart cleanup", e);
        }
    }

    /**
     * 🕕 2. КАЖДЫЕ 6 ЧАСОВ - Удаление помеченных корзин
     *
     * Выполняемые действия:
     * • ABANDONED корзины старше 1 дня → УДАЛЯЮТСЯ
     * • Корзины со статусами EXPIRED, CONVERTED, ABANDONED старше 7 дней → УДАЛЯЮТСЯ
     */
    @Scheduled(fixedRate = 21600000) // каждые 6 часов
    @Transactional
    public void deleteAbandonedCarts() {
        try {
            LocalDateTime deleteThreshold = LocalDateTime.now().minusDays(deleteThresholdDays);

            // Удаляем корзины со статусами EXPIRED, CONVERTED, ABANDONED старше порога
            int deletedCount = cartRepository.deleteOldCarts(deleteThreshold);

            if (deletedCount > 0) {
                log.info("Deleted {} old carts older than {} days", deletedCount, deleteThresholdDays);
            }

            // Дополнительно удаляем все ABANDONED корзины старше 1 дня
            LocalDateTime abandonedThreshold = LocalDateTime.now().minusDays(1);
            int abandonedDeleted = cartRepository.deleteAbandonedCarts(abandonedThreshold);

            if (abandonedDeleted > 0) {
                log.info("Deleted {} abandoned carts older than 1 day", abandonedDeleted);
            }

        } catch (Exception e) {
            log.error("Error during old cart deletion", e);
        }
    }

    /**
     * 🕘 3. КАЖДЫЕ 24 ЧАСА - Экстренная очистка всех старых корзин
     *
     * Выполняемые действия:
     * • ВСЕ корзины старше 30 дней → УДАЛЯЮТСЯ ПРИНУДИТЕЛЬНО
     */
    @Scheduled(fixedRate = 86400000) // каждые 24 часа
    @Transactional
    public void emergencyCleanup() {
        try {
            // Удаляем все корзины старше 30 дней независимо от статуса
            LocalDateTime emergencyThreshold = LocalDateTime.now().minusDays(30);
            int deletedCount = cartRepository.deleteAllOldCarts(emergencyThreshold);

            if (deletedCount > 0) {
                log.info("Emergency cleanup: deleted {} carts older than 30 days", deletedCount);
            }

        } catch (Exception e) {
            log.error("Error during emergency cleanup", e);
        }
    }

    /**
     * 📊 КАЖДЫЕ 30 МИНУТ - Логирование статистики
     */
    @Scheduled(fixedRate = 1800000) // каждые 30 минут
    public void logStatistics() {
        try {
            cartMemoryMonitor.logCartStatistics();
            cartMemoryMonitor.checkMemoryUsage();

        } catch (Exception e) {
            log.error("Error during statistics logging", e);
        }
    }

    /**
     * Проверить, нужно ли удалить корзину немедленно
     */
    private boolean shouldDeleteImmediately(Cart cart) {
        // Удаляем немедленно если:
        // 1. Корзина пустая (нет товаров)
        // 2. Корзина очень старая (>7 дней без активности)
        // 3. Корзина уже помечена как истекшая

        boolean isEmpty = cart.getItems() == null || cart.getItems().isEmpty();
        boolean isVeryOld = cart.getLastActivityAt() != null &&
                cart.getLastActivityAt().isBefore(LocalDateTime.now().minusDays(7));
        boolean isExpired = cart.getStatus() == CartStatus.EXPIRED;

        return isEmpty || isVeryOld || isExpired;
    }

    /**
     * 🔧 Ручная очистка для администраторов
     */
    public void manualCleanup(int daysThreshold) {
        log.info("Starting manual cleanup for carts older than {} days", daysThreshold);

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
            int deletedCount = cartRepository.deleteAllOldCarts(threshold);
            log.info("Manual cleanup completed: deleted {} carts", deletedCount);

            // Логируем статистику после очистки
            cartMemoryMonitor.logCartStatistics();

        } catch (Exception e) {
            log.error("Error during manual cleanup", e);
        }
    }
}

//
///**
// * СЦЕНАРИИ УДАЛЕНИЯ КОРЗИН - ПОДРОБНЫЕ ПРИМЕРЫ
// */
//
//// ============================================================================
//// 🟢 КОРЗИНЫ, КОТОРЫЕ УДАЛЯЮТСЯ НЕМЕДЛЕННО (каждый час)
//// ============================================================================
//
//// СЦЕНАРИЙ 1: Пустая корзина
//Cart emptyCart = new Cart();
//emptyCart.setItems(new ArrayList<>()); // Пустой список товаров
//        emptyCart.setLastActivityAt(LocalDateTime.now().minusHours(25)); // Неактивна 25 часов
//// РЕЗУЛЬТАТ: ✅ УДАЛЯЕТСЯ НЕМЕДЛЕННО (пустая)
//
//// СЦЕНАРИЙ 2: Корзина без активности 7+ дней (даже с товарами)
//Cart oldInactiveCart = new Cart();
//oldInactiveCart.setItems(List.of(item1, item2)); // Есть товары
//        oldInactiveCart.setLastActivityAt(LocalDateTime.now().minusDays(8)); // Неактивна 8 дней
//// РЕЗУЛЬТАТ: ✅ УДАЛЯЕТСЯ НЕМЕДЛЕННО (старая без активности)
//
//// СЦЕНАРИЙ 3: Истекшая корзина
//Cart expiredCart = new Cart();
//expiredCart.setStatus(CartStatus.EXPIRED);
//expiredCart.setLastActivityAt(LocalDateTime.now().minusHours(25));
//// РЕЗУЛЬТАТ: ✅ УДАЛЯЕТСЯ НЕМЕДЛЕННО (истекшая)
//
//// ============================================================================
//// 🟡 КОРЗИНЫ, КОТОРЫЕ ПОМЕЧАЮТСЯ КАК ABANDONED (каждый час)
//// ============================================================================
//
//// СЦЕНАРИЙ 4: Корзина с товарами, неактивна 1-6 дней
//Cart recentInactiveCart = new Cart();
//recentInactiveCart.setItems(List.of(item1, item2)); // Есть товары
//        recentInactiveCart.setLastActivityAt(LocalDateTime.now().minusDays(3)); // Неактивна 3 дня
//// РЕЗУЛЬТАТ: 🟡 ПОМЕЧАЕТСЯ КАК ABANDONED (будет удалена через 1 день)
//
//// ============================================================================
//// 🟢 КОРЗИНЫ, КОТОРЫЕ НЕ ТРОГАЮТСЯ
//// ============================================================================
//
//// СЦЕНАРИЙ 5: Активная корзина с товарами
//Cart activeCart = new Cart();
//activeCart.setItems(List.of(item1, item2)); // Есть товары
//        activeCart.setLastActivityAt(LocalDateTime.now().minusHours(20)); // Активна 20 часов назад
//// РЕЗУЛЬТАТ: ⭐ НЕ ТРОГАЕТСЯ (еще не достигла порога 24 часа)
//
//// СЦЕНАРИЙ 6: Недавно активная корзина
//Cart recentCart = new Cart();
//recentCart.setItems(List.of(item1)); // Есть товары
//        recentCart.setLastActivityAt(LocalDateTime.now().minusMinutes(30)); // Активна 30 минут назад
//// РЕЗУЛЬТАТ: ⭐ НЕ ТРОГАЕТСЯ (свежая активность)
//
//// ============================================================================
//// 📋 ДЕТАЛЬНАЯ ЛОГИКА ОПРЕДЕЛЕНИЯ ДЕЙСТВИЙ
//// ============================================================================
//
//public class CartDeletionLogicExamples {
//
//    /**
//     * Метод для демонстрации логики принятия решений
//     */
//    public String determineCartAction(Cart cart) {
//        LocalDateTime now = LocalDateTime.now();
//
//        // Проверяем пустоту
//        boolean isEmpty = cart.getItems() == null || cart.getItems().isEmpty();
//        if (isEmpty) {
//            return "🗑️ УДАЛИТЬ НЕМЕДЛЕННО - корзина пустая";
//        }
//
//        // Проверяем истечение
//        if (cart.getStatus() == CartStatus.EXPIRED) {
//            return "🗑️ УДАЛИТЬ НЕМЕДЛЕННО - корзина истекла";
//        }
//
//        // Проверяем активность
//        LocalDateTime lastActivity = cart.getLastActivityAt();
//        if (lastActivity == null) {
//            return "🗑️ УДАЛИТЬ НЕМЕДЛЕННО - нет данных об активности";
//        }
//
//        long hoursInactive = ChronoUnit.HOURS.between(lastActivity, now);
//        long daysInactive = ChronoUnit.DAYS.between(lastActivity, now);
//
//        if (daysInactive >= 7) {
//            return String.format("🗑️ УДАЛИТЬ НЕМЕДЛЕННО - неактивна %d дней", daysInactive);
//        }
//
//        if (hoursInactive >= 24) {
//            return String.format("🟡 ПОМЕТИТЬ КАК ABANDONED - неактивна %d часов", hoursInactive);
//        }
//
//        return String.format("⭐ НЕ ТРОГАТЬ - активна %d часов назад", hoursInactive);
//    }
//
//    /**
//     * Примеры реальных ситуаций
//     */
//    public void realWorldExamples() {
//
//        // 👤 ПОЛЬЗОВАТЕЛЬ JOHN
//        // День 1: Добавил iPhone в корзину
//        // День 3: Добавил чехол в корзину
//        // День 10: Не заходил на сайт
//        // РЕЗУЛЬТАТ: Корзина будет удалена на 10-й день (7+ дней без активности)
//
//        // 👤 ПОЛЬЗОВАТЕЛЬ MARY
//        // День 1: Добавила платье в корзину
//        // День 2: Удалила платье из корзины (корзина стала пустой)
//        // День 3: Не заходила на сайт
//        // РЕЗУЛЬТАТ: Корзина будет удалена на 3-й день (пустая + неактивная 24+ часа)
//
//        // 👤 ПОЛЬЗОВАТЕЛЬ ALEX
//        // День 1: Добавил книгу в корзину
//        // День 2: Каждый день заходит и смотрит корзину
//        // День 30: Все еще периодически заходит
//        // РЕЗУЛЬТАТ: Корзина НЕ удаляется (регулярная активность)
//
//        // 👤 ПОЛЬЗОВАТЕЛЬ ANNA
//        // День 1: Зашла на сайт, ничего не добавила
//        // День 2: Не заходила
//        // РЕЗУЛЬТАТ: Пустая корзина удалится через 25 часов после создания
//    }
//}
//
//// ============================================================================
//// 🕒 ВРЕМЕННАЯ ШКАЛА УДАЛЕНИЯ
//// ============================================================================
//
///**
// * ВРЕМЕННАЯ ШКАЛА ЖИЗНИ КОРЗИНЫ:
// *
// * 📅 День 0 (0 часов): Корзина создана
// *
// * 🕐 Через 1 час: Если пустая → УДАЛЯЕТСЯ
// *
// * 🕐 Через 25 часов:
// *    - Если пустая → УДАЛЯЕТСЯ
// *    - Если с товарами → ПОМЕЧАЕТСЯ КАК ABANDONED
// *
// * 🕕 Через 2 дня (48 часов):
// *    - ABANDONED корзины → УДАЛЯЮТСЯ (если старше 1 дня)
// *
// * 🕘 Через 7 дней:
// *    - Любые корзины без активности → УДАЛЯЮТСЯ НЕМЕДЛЕННО
// *
// * 🗓️ Через 30 дней:
// *    - ВСЕ корзины → ПРИНУДИТЕЛЬНОЕ УДАЛЕНИЕ
// */