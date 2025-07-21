package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.order.CreateOrderRequest;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.model.Order;
import jakarta.servlet.http.HttpSession;

import java.util.List;


public interface OrderService {
    OrderDTO createOrder(CreateOrderRequest createOrderRequest, HttpSession session);
    List<OrderDTO> getOrdersByPhoneNumber(String phoneNumber);
    OrderDTO getOrderById(Long orderId);
    Order findById(Long orderId);
}
