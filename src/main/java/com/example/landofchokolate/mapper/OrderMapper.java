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
                // ✅ Новые поля для Nova Poshta
                .trackingNumber(order.getTrackingNumber())
                .recipientCityRef(order.getRecipientCityRef())
                .recipientAddress(order.getRecipientAddressRef())
                .deliveryStatus(order.getDeliveryStatus())
                .orderItems(order.getOrderItems() != null ?
                        order.getOrderItems().stream()
                                .map(this::toOrderItemDTO)
                                .collect(Collectors.toList()) :
                        new ArrayList<>())
                .build();
    }

    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) return null;

        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setEmail(orderDTO.getEmail());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setCustomerName(orderDTO.getCustomerName());
        order.setSomeNotes(orderDTO.getSomeNotes());
        order.setStatus(orderDTO.getStatus());
        order.setDeliveryMethod(orderDTO.getDeliveryMethod());
        order.setCreatedAt(orderDTO.getCreatedAt());
        order.setUpdatedAt(orderDTO.getUpdatedAt());
        // ✅ Новые поля для Nova Poshta
        order.setTrackingNumber(orderDTO.getTrackingNumber());
        order.setRecipientCityRef(orderDTO.getRecipientCityRef());
        order.setRecipientAddressRef(orderDTO.getRecipientAddress());
        order.setDeliveryStatus(orderDTO.getDeliveryStatus());

        return order;
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