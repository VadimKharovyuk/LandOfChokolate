package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.order.OrderAdminListDTO;
import com.example.landofchokolate.enums.OrderStatus;
import com.example.landofchokolate.enums.PaymentStatus;
import com.example.landofchokolate.enums.PaymentType;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class OrderAdminMapper {

    public OrderAdminListDTO toAdminListDTO(Order order) {
        if (order == null) {
            return null;
        }

        return OrderAdminListDTO.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryMethod(order.getDeliveryMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .itemsCount(getItemsCount(order))
                .paymentMethod(getPaymentMethod(order))
                .paymentStatus(getPaymentStatus(order))
                .build();
    }

    /**
     * Получить количество товаров в заказе
     */
    private int getItemsCount(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return 0;
        }

        // Считаем общее количество товаров (учитываем quantity каждого item)
        return order.getOrderItems()
                .stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
    }

    /**
     * Определить способ оплаты
     */
    private String getPaymentMethod(Order order) {
        Payment payment = order.getPayment();

        if (payment == null) {
            return "Не вказано";
        }

        // Используем реальное поле type из модели Payment
        PaymentType paymentType = payment.getType();
        if (paymentType != null) {
            return paymentType.getDescription();
        }

        return "Не вказано";
    }

    /**
     * Определить статус оплаты
     */
    private String getPaymentStatus(Order order) {
        Payment payment = order.getPayment();

        if (payment == null) {
            return determinePaymentStatusByOrderStatus(order.getStatus());
        }

        // Используем реальное поле status из модели Payment (если есть)
        if (payment.getStatus() != null) {
            return getPaymentStatusDisplayName(payment.getStatus());
        }

        // Fallback - определяем статус по статусу заказа
        return determinePaymentStatusByOrderStatus(order.getStatus());
    }

    /**
     * Получить отображаемое название статуса оплаты
     */
    private String getPaymentStatusDisplayName(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            return "Невідомо";
        }

        return switch (paymentStatus) {
            case PENDING -> "Очікує оплати";
            case FAILED -> "Помилка оплати";
            case CANCELLED -> "Скасовано";
            case REFUNDED -> "Повернено";
            default -> paymentStatus.name();
        };
    }

    /**
     * Определить статус оплаты на основе статуса заказа
     */
    private String determinePaymentStatusByOrderStatus(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return "Невідомо";
        }

        return switch (orderStatus) {
            case NEW -> "Очікує оплати";
            case PAID -> "Оплачено";
            case CANCELLED -> "Скасовано";
            case COMPLETED -> "Оплачено";
            case READY_FOR_PICKUP -> "Оплачено";
            default -> "Невідомо";
        };
    }
}