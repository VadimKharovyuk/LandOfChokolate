package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.dto.order.CreateOrderRequest;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.exception.EmptyCartException;
import com.example.landofchokolate.exception.OrderCreationException;
import com.example.landofchokolate.exception.OrderNotFoundException;
import com.example.landofchokolate.mapper.OrderMapper;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.OrderItem;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.repository.OrderItemRepository;
import com.example.landofchokolate.repository.OrderRepository;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.CartService;
import com.example.landofchokolate.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, HttpSession session) {
        log.info("Creating order for customer: {}", request.getCustomerName());

        // Шаг 1: Получаем корзину из сессии
        CartDto cartDto = cartService.getCartDto(session);

        if (cartService.isCartEmpty(session)) {
            throw new EmptyCartException("Корзина пуста. Добавьте товары перед оформлением заказа");
        }

        // Шаг 2: Создаем заказ
        Order order = createOrderEntity(request);

        // Шаг 3: Создаем OrderItems из CartItems
        BigDecimal totalAmount = createOrderItemsFromCart(order, cartDto);
        order.setTotalAmount(totalAmount);

        // Шаг 4: Сохраняем заказ
        Order savedOrder = orderRepository.save(order);

        // Шаг 5: Очищаем корзину
        cartService.clearCart(session);

        return orderMapper.toDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByPhoneNumber(String phoneNumber) {

        List<Order> orders = orderRepository.findByPhoneNumberOrderByCreatedAtDesc(phoneNumber);

        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ с ID " + orderId + " не найден"));
        return orderMapper.toDTO(order);
    }


    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ с ID " + orderId + " не найден"));
    }



    private Order createOrderEntity(CreateOrderRequest request) {
        return Order.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .customerName(request.getCustomerName())
                .someNotes(request.getSomeNotes())
                .deliveryMethod(request.getDeliveryMethod())
                .orderItems(new ArrayList<>())
                .build();
    }

    private BigDecimal createOrderItemsFromCart(Order order, CartDto cartDto) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemDto cartItem : cartDto.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new OrderCreationException(
                            "Товар с ID " + cartItem.getProduct().getId() + " не найден"));

            // Проверяем доступность товара
            validateProductAvailability(product, cartItem.getQuantity());

            // Создаем OrderItem
            OrderItem orderItem = createOrderItem(order, product, cartItem);
            order.getOrderItems().add(orderItem);

            // Добавляем к общей сумме
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        return totalAmount;
    }

    private void validateProductAvailability(Product product, Integer quantity) {
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new OrderCreationException("Товар '" + product.getName() + "' больше не доступен");
        }

        if (product.getStockQuantity() < quantity) {
            throw new OrderCreationException(
                    "Недостаточно товара '" + product.getName() + "' на складе. " +
                            "Доступно: " + product.getStockQuantity() + ", запрошено: " + quantity);
        }
    }

    private OrderItem createOrderItem(Order order, Product product, CartItemDto cartItem) {
        BigDecimal subtotal = cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        // Опционально: уменьшить остаток на складе
         product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
         productRepository.save(product);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPriceAtOrder(cartItem.getPriceAtTime());
        orderItem.setSubtotal(subtotal);

        return orderItem;
    }
}
