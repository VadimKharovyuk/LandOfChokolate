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
     * Экстренное удаление всех корзин старше определенного времени (независимо от статуса)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.createdAt < :threshold")
    int deleteAllOldCarts(@Param("threshold") LocalDateTime threshold);


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
     * Получить среднее количество товаров в корзинах
     */
    @Query("SELECT AVG(SIZE(c.items)) FROM Cart c WHERE c.status = 'ACTIVE'")
    Double getAverageItemsPerCart();


    // ===== Методы для поиска и фильтрации =====

    /**
     * Найти корзину с элементами и продуктами (улучшенная версия)
     * FETCH JOIN загружает все связанные данные в одном запросе
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.items ci " +
            "LEFT JOIN FETCH ci.product p " +
            "WHERE c.cartUuid = :cartUuid AND c.status = :status")
    Optional<Cart> findByCartUuidAndStatusWithItems(@Param("cartUuid") String cartUuid,
                                                    @Param("status") CartStatus status);




    boolean existsByCartUuidAndStatus(String newCartUuid, CartStatus cartStatus);



    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id IN " +
            "(SELECT c.id FROM Cart c WHERE c.createdAt < :threshold)")
    void deleteCartItemsForOldCarts(@Param("threshold") LocalDateTime threshold);
}