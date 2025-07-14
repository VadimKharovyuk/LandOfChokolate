package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.subscription.SubscriptionResponse;
import com.example.landofchokolate.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {
    public SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .email(subscription.getEmail())
                .active(subscription.isActive())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}