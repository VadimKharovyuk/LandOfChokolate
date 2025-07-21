package com.example.landofchokolate.dto.order;

import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    private Long id;
    private String customerName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private DeliveryMethod deliveryMethod;
    private LocalDateTime createdAt;
    private Integer itemsCount;
}