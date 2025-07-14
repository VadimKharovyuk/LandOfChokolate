package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.Subscription;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.active = true")
    List<Subscription> findActiveSubscriptions();


    boolean existsByEmail(@NotBlank(message = "Email не может быть пустым") @Email(message = "Некорректный формат email") String email);
}
