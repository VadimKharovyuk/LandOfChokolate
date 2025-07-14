package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.subscription.CreateSubscriptionRequest;
import com.example.landofchokolate.dto.subscription.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);


    List<SubscriptionResponse> getAllActiveSubscriptions();


    void deleteSubscription(Long id);
}
