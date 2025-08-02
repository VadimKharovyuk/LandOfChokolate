//package com.example.landofchokolate.mapper;
//
//import com.example.landofchokolate.dto.order.OrderDTO;
//import com.example.landofchokolate.dto.order.OrderItemDTO;
//import com.example.landofchokolate.model.Order;
//import com.example.landofchokolate.model.OrderItem;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.stream.Collectors;
//
//@Component
//public class OrderMapper {
//
//    public OrderDTO toDTO(Order order) {
//        if (order == null) return null;
//
//        return OrderDTO.builder()
//                .id(order.getId())
//                .totalAmount(order.getTotalAmount())
//                .email(order.getEmail())
//                .phoneNumber(order.getPhoneNumber())
//                .customerName(order.getCustomerName())
//                .someNotes(order.getSomeNotes())
//                .status(order.getStatus())
//                .deliveryMethod(order.getDeliveryMethod())
//                .createdAt(order.getCreatedAt())
//                .updatedAt(order.getUpdatedAt())
//                .orderItems(order.getOrderItems() != null ?
//                        order.getOrderItems().stream()
//                                .map(this::toOrderItemDTO)
//                                .collect(Collectors.toList()) :
//                        new ArrayList<>())
//                .build();
//    }
//
//    private OrderItemDTO toOrderItemDTO(OrderItem item) {
//        return OrderItemDTO.builder()
//                .id(item.getId())
//                .productId(item.getProduct().getId())
//                .productName(item.getProduct().getName())
//                .productImageUrl(item.getProduct().getImageUrl())
//                .quantity(item.getQuantity())
//                .priceAtOrder(item.getPriceAtOrder())
//                .subtotal(item.getSubtotal())
//                .build();
//    }
//}
package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.dto.order.OrderItemDTO;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        if (order == null) return null;

        return OrderDTO.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .customerName(order.getCustomerName())
                .someNotes(order.getSomeNotes())
                .status(order.getStatus())
                .deliveryMethod(order.getDeliveryMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .trackingNumber(order.getTrackingNumber())
                .recipientCityRef(order.getRecipientCityRef())
                .recipientAddress(order.getRecipientAddressRef())
                .recipientFirstName(order.getRecipientFirstName())
                .recipientLastName(order.getRecipientLastName())
                .contactRecipientRef(order.getContactRecipientRef())
                .recipientPhone(order.getRecipientPhone())
                .deliveryStatus(order.getDeliveryStatus())
                .orderItems(order.getOrderItems() != null ?
                        order.getOrderItems().stream()
                                .map(this::toOrderItemDTO)
                                .collect(Collectors.toList()) :
                        new ArrayList<>())
                .build();
    }

    // ✅ Добавить обратное преобразование для создания заказов
    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) return null;

        return Order.builder()
                .id(orderDTO.getId())
                .totalAmount(orderDTO.getTotalAmount())
                .email(orderDTO.getEmail())
                .phoneNumber(orderDTO.getPhoneNumber())
                .customerName(orderDTO.getCustomerName())
                .someNotes(orderDTO.getSomeNotes())
                .status(orderDTO.getStatus())
                .deliveryMethod(orderDTO.getDeliveryMethod())
                .trackingNumber(orderDTO.getTrackingNumber())
                .recipientCityRef(orderDTO.getRecipientCityRef())
                .recipientAddressRef(orderDTO.getRecipientAddress())
                .recipientFirstName(orderDTO.getRecipientFirstName())
                .recipientLastName(orderDTO.getRecipientLastName())
                .contactRecipientRef(orderDTO.getContactRecipientRef())
                .recipientPhone(orderDTO.getRecipientPhone())
                .deliveryStatus(orderDTO.getDeliveryStatus())
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .subtotal(item.getSubtotal())
                .build();
    }
}