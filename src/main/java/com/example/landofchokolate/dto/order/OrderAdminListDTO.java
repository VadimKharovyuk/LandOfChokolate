package com.example.landofchokolate.dto.order;

import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAdminListDTO {

    private Long id;

    private String customerName;

    private String email;

    private String phoneNumber;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private DeliveryMethod deliveryMethod;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Дополнительные поля для удобства отображения
    private int itemsCount; // Количество товаров в заказе

    private String paymentMethod; // Способ оплаты (если есть связанный Payment)

    private String paymentStatus; // Статус оплаты

    // Методы для форматирования данных в шаблонах
    public String getFormattedAmount() {
        return totalAmount != null ? totalAmount + " грн" : "0 грн";
    }

    public String getStatusDisplayName() {
        if (status == null) return "Невідомо";

        return status.getDescription();
    }

    public String getDeliveryMethodDisplayName() {
        if (deliveryMethod == null) return "Не вказано";

        return deliveryMethod.getDescription();
    }

    public String getDeliveryMethodIcon() {
        if (deliveryMethod == null) return "fas fa-question";

        return switch (deliveryMethod) {
            case DELIVERY -> "fas fa-truck";
            case PICKUP -> "fas fa-store";
            default -> "fas fa-question";
        };
    }

    public String getDeliveryMethodBadgeClass() {
        if (deliveryMethod == null) return "badge-secondary";

        return switch (deliveryMethod) {
            case DELIVERY -> "badge-info";
            case PICKUP -> "badge-warning";
            default -> "badge-secondary";
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";

        return switch (status) {
            case NEW -> "badge-primary";
            case PAID -> "badge-success";
            case CANCELLED -> "badge-danger";
            case COMPLETED -> "badge-success";
            case READY_FOR_PICKUP -> "badge-warning";
            default -> "badge-secondary";
        };
    }
}