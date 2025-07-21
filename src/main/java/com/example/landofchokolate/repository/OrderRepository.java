package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.phoneNumber = :phone ORDER BY o.createdAt DESC")
    List<Order> findByPhoneNumberOrderByCreatedAtDesc(@Param("phone") String phone);
}
