package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.subscription.CreateSubscriptionRequest;
import com.example.landofchokolate.dto.subscription.SubscriptionResponse;
import com.example.landofchokolate.exception.SubscriptionAlreadyExistsException;
import com.example.landofchokolate.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping()
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        try {
            log.info("Получен запрос на создание подписки для email: {}", request.getEmail());
            SubscriptionResponse subscription = subscriptionService.createSubscription(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (SubscriptionAlreadyExistsException e) {
            log.warn("Попытка создать дубликат подписки: {}", request.getEmail());
            throw e;
        }
    }
}
