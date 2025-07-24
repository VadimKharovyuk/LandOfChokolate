package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.order.OrderAdminListDTO;
import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.enums.OrderStatus;
import com.example.landofchokolate.mapper.OrderAdminMapper;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.repository.OrderRepository;
import com.example.landofchokolate.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderAdminMapper orderAdminMapper;

    @Override
    public OrderAdminListDTO getOrderForAdmin(Long orderId) {
        log.debug("Getting order for admin with id: {}", orderId);

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Order not found with id: {}", orderId);
            return null;
        }

        return orderAdminMapper.toAdminListDTO(order);
    }

    @Override
    public PagedResponse<OrderAdminListDTO> getAllOrdersForAdmin(Pageable pageable) {
        log.debug("Getting all orders for admin with pageable: {}", pageable);

        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderAdminListDTO> orderDTOs = orderPage.getContent()
                .stream()
                .map(orderAdminMapper::toAdminListDTO)
                .toList();

        return new PagedResponse<>(orderDTOs, orderPage);
    }


    @Override
    @Transactional
    public void deleteOrderForAdmin(Long orderId) {
        log.info("Deleting order with id: {}", orderId);

        if (!orderRepository.existsById(orderId)) {
            log.warn("Attempted to delete non-existent order with id: {}", orderId);
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }

        try {
            orderRepository.deleteById(orderId);
            log.info("Successfully deleted order with id: {}", orderId);
        } catch (Exception e) {
            log.error("Error deleting order with id: {}", orderId, e);
            throw new RuntimeException("Failed to delete order with id: " + orderId, e);
        }
    }

    @Override
    @Transactional
    public OrderAdminListDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order status for order id: {} to status: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);
        log.info("Successfully updated order status from {} to {} for order id: {}",
                oldStatus, newStatus, orderId);

        return orderAdminMapper.toAdminListDTO(savedOrder);
    }

    @Override
    public Long getTotalOrdersCount() {
        log.debug("Getting total orders count");
        return orderRepository.count();
    }

    @Override
    public Long getTodayOrdersCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return orderRepository.countByCreatedAtAfter(startOfDay);
    }


}