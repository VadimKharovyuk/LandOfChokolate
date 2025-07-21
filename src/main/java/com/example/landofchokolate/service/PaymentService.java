package com.example.landofchokolate.service;

import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;

import java.util.Map;

public interface PaymentService {
    Payment processPayment(Order order, String returnUrl, String cancelUrl);
    Payment completePayment(String paymentId, String payerId);
    Payment getPaymentById(Long id);

    String getSignature(String data);

    boolean processLiqPayCallback(Map<String, Object> paymentData);
}
