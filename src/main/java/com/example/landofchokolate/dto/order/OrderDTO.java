package com.example.landofchokolate.dto.order;

import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private BigDecimal totalAmount;
    private String email;
    private String phoneNumber;
    private String customerName;
    private String someNotes;
    private OrderStatus status;
    private DeliveryMethod deliveryMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> orderItems;
}
