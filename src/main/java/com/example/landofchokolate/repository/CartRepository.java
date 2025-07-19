package com.example.landofchokolate.repository;

import com.example.landofchokolate.enums.CartStatus;
import com.example.landofchokolate.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Найти корзину по UUID и статусу
     */
    Optional<Cart> findByCartUuidAndStatus(String cartUuid, CartStatus status);

    /**
     * Найти активную корзину по UUID
     */
    @Query("SELECT c FROM Cart c WHERE c.cartUuid = :cartUuid AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByUuid(@Param("cartUuid") String cartUuid);

    // ===== Методы для очистки и администрирования =====

    /**
     * Найти корзины для очистки (старые неактивные)
     */
    @Query("SELECT c FROM Cart c WHERE c.lastActivityAt < :threshold AND c.status = 'ACTIVE'")
    List<Cart> findCartsForCleanup(@Param("threshold") LocalDateTime threshold);

    /**
     * Удалить старые корзины с определенными статусами
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.createdAt < :threshold AND c.status IN ('EXPIRED', 'CONVERTED', 'ABANDONED')")
    int deleteOldCarts(@Param("threshold") LocalDateTime threshold);

    /**
     * Удалить заброшенные корзины старше определенного времени
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.status = 'ABANDONED' AND c.lastActivityAt < :threshold")
    int deleteAbandonedCarts(@Param("threshold") LocalDateTime threshold);

    /**
     * Экстренное удаление всех корзин старше определенного времени (независимо от статуса)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.createdAt < :threshold")
    int deleteAllOldCarts(@Param("threshold") LocalDateTime threshold);

    /**
     * Удалить пустые корзины старше определенного времени
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.lastActivityAt < :threshold AND " +
            "NOT EXISTS (SELECT 1 FROM CartItem ci WHERE ci.cart = c)")
    int deleteEmptyCarts(@Param("threshold") LocalDateTime threshold);

    // ===== Статистические методы =====

    /**
     * Подсчитать общее количество товаров в активных корзинах
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.items ci WHERE c.status = 'ACTIVE'")
    Long getTotalItemsInActiveCarts();

    /**
     * Подсчитать количество активных корзин
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.status = 'ACTIVE'")
    Long countActiveCarts();

    /**
     * Подсчитать количество непустых активных корзин
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Cart c JOIN c.items ci WHERE c.status = 'ACTIVE'")
    Long countActiveCartsWithItems();

    /**
     * Подсчитать количество корзин по статусам
     */
    @Query("SELECT c.status, COUNT(c) FROM Cart c GROUP BY c.status")
    List<Object[]> countCartsByStatus();

    /**
     * Получить среднее количество товаров в корзинах
     */
    @Query("SELECT AVG(SIZE(c.items)) FROM Cart c WHERE c.status = 'ACTIVE'")
    Double getAverageItemsPerCart();

    /**
     * Найти самые старые активные корзины
     */
    @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<Cart> findOldestActiveCarts();

    // ===== Методы для поиска и фильтрации =====

    /**
     * Найти корзину с элементами и продуктами (FETCH JOIN для избежания LazyInitializationException)
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product p " +
            "WHERE c.cartUuid = :cartUuid AND c.status = :status")
    Optional<Cart> findByCartUuidAndStatusWithItems(@Param("cartUuid") String cartUuid,
                                                    @Param("status") CartStatus status);
}