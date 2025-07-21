package com.example.landofchokolate.service.payment;

import com.example.landofchokolate.enums.PaymentStatus;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;
import com.example.landofchokolate.repository.OrderRepository;
import com.example.landofchokolate.repository.PaymentRepository;
import com.example.landofchokolate.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class LiqPayPaymentServiceImpl implements PaymentService {

    @Value("${liqpay.public_key}")
    private String publicKey;

    @Value("${liqpay.private_key}")
    private String privateKey;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository; // Добавляем OrderRepository

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Payment processPayment(Order order, String returnUrl, String cancelUrl) {
        try {
            // Проверяем, есть ли уже платеж для этого заказа
            Payment existingPayment = getPaymentByOrder(order);

            if (existingPayment != null) {
                // Если платеж уже существует и не завершен, возвращаем его
                if (existingPayment.getStatus() == PaymentStatus.PENDING ||
                        existingPayment.getStatus() == PaymentStatus.PROCESSING) {
                    return existingPayment;
                }

                // Если платеж завершен, создаем новый (для повторной оплаты)
                if (existingPayment.getStatus() == PaymentStatus.COMPLETED) {
                    return existingPayment; // Заказ уже оплачен
                }
            }

            // Создаем новый объект платежа
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .type(order.getPayment() != null ? order.getPayment().getType() : null)
                    .status(PaymentStatus.PENDING)
                    .build();

            payment = paymentRepository.save(payment);

            // Сохраняем транзакцию ID как уникальный идентификатор
            payment.setTransactionId(payment.getId().toString());

            return paymentRepository.save(payment);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании платежа", e);
        }
    }

    @Override
    public Payment completePayment(String paymentId, String payerId) {
        try {
            // Ищем платеж по ID
            Long id = Long.parseLong(paymentId);
            Payment payment = paymentRepository.findById(id).orElse(null);

            if (payment != null && payment.getStatus() != PaymentStatus.COMPLETED) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());

                // Если передан transaction ID от LiqPay, сохраняем его
                if (payerId != null && !payerId.isEmpty()) {
                    payment.setTransactionId(payerId);
                }

                return paymentRepository.save(payment);
            }

            return payment; // Возвращаем платеж даже если он уже завершен
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public String getSignature(String data) {
        try {
            String signString = privateKey + data + privateKey;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(signString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании подписи", e);
        }
    }

    @Override
    public boolean processLiqPayCallback(Map<String, Object> paymentData) {
        return false;
    }

    // Дополнительные методы для работы с LiqPay

    /**
     * Проверка подписи от LiqPay
     */
    public boolean verifySignature(String data, String signature) {
        String expectedSignature = getSignature(data);
        return expectedSignature.equals(signature);
    }

    /**
     * Декодирование данных от LiqPay
     */
    public Map<String, Object> decodeData(String data) {
        try {
            String decodedData = new String(
                    Base64.getDecoder().decode(data),
                    StandardCharsets.UTF_8
            );
            return objectMapper.readValue(decodedData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при декодировании данных", e);
        }
    }

    /**
     * Получение данных для формы оплаты
     */
    public Map<String, String> getPaymentForm(Order order, String resultUrl, String serverUrl) {
        try {
            System.out.println("=== СОЗДАНИЕ ФОРМЫ LIQPAY ===");
            System.out.println("Public Key: " + publicKey);
            System.out.println("Private Key: " + (privateKey != null ? privateKey.substring(0, 10) + "..." : "null"));

            // Перезагружаем заказ с orderItems для корректного подсчета
            Order fullOrder = orderRepository.findByIdWithItems(order.getId()).orElse(order);

            Map<String, Object> params = new HashMap<>();
            params.put("action", "pay");
            params.put("amount", fullOrder.getTotalAmount().doubleValue());
            params.put("currency", "UAH");

            // Создаем более информативное описание с количеством товаров
            String description = "Заказ продуктов #" + fullOrder.getId();
            if (fullOrder.getOrderItems() != null && !fullOrder.getOrderItems().isEmpty()) {
                description += " (" + fullOrder.getOrderItems().size() + " товаров)";
            }
            params.put("description", description);

            params.put("order_id", fullOrder.getId().toString());
            params.put("version", "3");
            params.put("public_key", publicKey);
            params.put("result_url", resultUrl);
            params.put("server_url", serverUrl);
            params.put("language", "ru");

            System.out.println("Параметры LiqPay:");
            System.out.println("  - amount: " + params.get("amount"));
            System.out.println("  - description: " + params.get("description"));
            System.out.println("  - order_id: " + params.get("order_id"));
            System.out.println("  - result_url: " + params.get("result_url"));
            System.out.println("  - server_url: " + params.get("server_url"));

            String jsonString = objectMapper.writeValueAsString(params);
            System.out.println("JSON: " + jsonString);

            String data = Base64.getEncoder().encodeToString(
                    jsonString.getBytes(StandardCharsets.UTF_8));
            System.out.println("Base64 Data: " + data);

            String signature = getSignature(data);
            System.out.println("Signature: " + signature);

            Map<String, String> formData = new HashMap<>();
            formData.put("data", data);
            formData.put("signature", signature);
            formData.put("public_key", publicKey);

            System.out.println("✅ Форма создана успешно");
            return formData;
        } catch (Exception e) {
            System.out.println("❌ ОШИБКА при создании формы:");
            e.printStackTrace();
            throw new RuntimeException("Ошибка при создании формы оплаты", e);
        }
    }

    /**
     * Поиск платежа по заказу
     */
    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrderId(order.getId()).orElse(null);
    }

    /**
     * Проверка статуса платежа для заказа
     */
    public boolean isOrderPaid(Long orderId) {
        return paymentRepository.existsCompletedPaymentForOrder(orderId);
    }

    /**
     * Маппинг статусов LiqPay в наши статусы
     */
    public PaymentStatus mapLiqPayStatus(String liqPayStatus) {
        if (liqPayStatus == null) {
            return PaymentStatus.PENDING;
        }

        switch (liqPayStatus.toLowerCase()) {
            case "success":
                return PaymentStatus.COMPLETED;
            case "failure":
            case "error":
                return PaymentStatus.FAILED;
            case "reversed":
                return PaymentStatus.REFUNDED;
            case "processing":
                return PaymentStatus.PROCESSING;
            case "sandbox":
                return PaymentStatus.COMPLETED; // Для тестового режима
            default:
                return PaymentStatus.PENDING;
        }
    }

    /**
     * Обновление статуса платежа (используется в контроллере)
     */
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment != null) {
            payment.setStatus(status);
            if (transactionId != null) {
                payment.setTransactionId(transactionId);
            }
            if (status == PaymentStatus.COMPLETED) {
                payment.setPaidAt(LocalDateTime.now());
            }
            return paymentRepository.save(payment);
        }
        return null;
    }
}