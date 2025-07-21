package com.example.landofchokolate.repository;

import com.example.landofchokolate.enums.PaymentStatus;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Найти платеж по ID транзакции
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Найти все платежи по заказу
     */
    List<Payment> findByOrder(Order order);

    /**
     * Найти платежи по статусу
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Найти платежи в определенном статусе за период
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Найти просроченные неоплаченные платежи
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :expiredDate")
    List<Payment> findExpiredPendingPayments(@Param("expiredDate") LocalDateTime expiredDate);

    /**
     * Найти платеж по заказу
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Существует ли успешный платеж для заказа
     */
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.order.id = :orderId AND p.status = 'COMPLETED'")
    boolean existsCompletedPaymentForOrder(@Param("orderId") Long orderId);


}