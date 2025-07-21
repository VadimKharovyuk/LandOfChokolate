package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.phoneNumber = :phone ORDER BY o.createdAt DESC")
    List<Order> findByPhoneNumberOrderByCreatedAtDesc(@Param("phone") String phone);

    /**
     * Найти заказ с загруженными orderItems и payment
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "LEFT JOIN FETCH o.payment " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
