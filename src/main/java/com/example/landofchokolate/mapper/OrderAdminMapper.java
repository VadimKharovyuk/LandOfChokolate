package com.example.landofchokolate.mapper;
import com.example.landofchokolate.dto.order.OrderAdminListDTO;
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

        // Если у вас есть поле paymentMethod в модели Payment
        // return payment.getPaymentMethod();

        // Или если вы определяете способ оплаты по другим полям в Payment
        // Например, если есть поле paymentType или similar:
        // return getPaymentMethodDisplayName(payment.getPaymentType());

        // Пока что возвращаем заглушку, замените на вашу логику
        return "IBAN";
    }

    /**
     * Определить статус оплаты
     */
    private String getPaymentStatus(Order order) {
        Payment payment = order.getPayment();

        if (payment == null) {
            return determinePaymentStatusByOrderStatus(order.getStatus());
        }

        // Если у вас есть поле status в модели Payment
        // return payment.getStatus().getDescription();

        // Или логика на основе других полей Payment
        // if (payment.isPaid()) {
        //     return "Оплачено";
        // } else if (payment.isFailed()) {
        //     return "Помилка оплати";
        // } else {
        //     return "В очікуванні";
        // }

        // Пока что определяем статус оплаты на основе статуса заказа
        return determinePaymentStatusByOrderStatus(order.getStatus());
    }

    /**
     * Определить статус оплаты на основе статуса заказа
     */
    private String determinePaymentStatusByOrderStatus(com.example.landofchokolate.enums.OrderStatus orderStatus) {
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

    /**
     * Получить отображаемое название способа оплаты (если нужно)
     */
    private String getPaymentMethodDisplayName(String paymentType) {
        if (paymentType == null) {
            return "Не вказано";
        }

        return switch (paymentType.toUpperCase()) {
            case "IBAN" -> "Банківський переказ (IBAN)";
            case "CASH" -> "Готівка при отриманні";
            case "CARD" -> "Банківська картка";
            case "QRCODE" -> "QR-код оплата";
            case "PRIVATBANK" -> "ПриватБанк";
            case "MONOBANK" -> "МоноБанк";
            default -> paymentType;
        };
    }
}
