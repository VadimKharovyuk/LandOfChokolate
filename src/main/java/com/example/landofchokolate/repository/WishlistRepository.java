
// Репозиторий WishlistRepository
package com.example.landofchokolate.repository;

import com.example.landofchokolate.enums.WishlistStatus;
import com.example.landofchokolate.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Найти wishlist по UUID и статусу с загрузкой items (FETCH JOIN)
     */
    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.items i LEFT JOIN FETCH i.product " +
            "WHERE w.wishlistUuid = :wishlistUuid AND w.status = :status")
    Optional<Wishlist> findByWishlistUuidAndStatusWithItems(
            @Param("wishlistUuid") String wishlistUuid,
            @Param("status") WishlistStatus status);

    /**
     * Найти wishlist по UUID и статусу
     */
    Optional<Wishlist> findByWishlistUuidAndStatus(String wishlistUuid, WishlistStatus status);

    /**
     * Найти все активные wishlist с истекшим сроком
     */
    @Query("SELECT w FROM Wishlist w WHERE w.status = :status AND w.expiresAt < :currentTime")
    List<Wishlist> findExpiredWishlists(
            @Param("status") WishlistStatus status,
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * Найти wishlist по IP адресу (для анонимных пользователей)
     */
    @Query("SELECT w FROM Wishlist w WHERE w.ipAddress = :ipAddress AND w.status = :status " +
            "ORDER BY w.lastActivityAt DESC")
    List<Wishlist> findByIpAddressAndStatus(
            @Param("ipAddress") String ipAddress,
            @Param("status") WishlistStatus status);

    /**
     * Подсчитать количество активных wishlist
     */
    long countByStatus(WishlistStatus status);

    /**
     * Найти wishlist с неактивностью больше указанного периода
     */
    @Query("SELECT w FROM Wishlist w WHERE w.status = :status AND w.lastActivityAt < :beforeTime")
    List<Wishlist> findInactiveWishlists(
            @Param("status") WishlistStatus status,
            @Param("beforeTime") LocalDateTime beforeTime);

    /**
     * Удалить истекшие wishlist
     */
    void deleteByStatusAndExpiresAtBefore(WishlistStatus status, LocalDateTime beforeTime);

    /**
     * Найти wishlist, содержащие определенный товар
     */
    @Query("SELECT DISTINCT w FROM Wishlist w JOIN w.items i " +
            "WHERE i.product.id = :productId AND w.status = :status")
    List<Wishlist> findByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") WishlistStatus status);
}
