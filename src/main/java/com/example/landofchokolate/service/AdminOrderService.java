package com.example.landofchokolate.service;
import com.example.landofchokolate.dto.order.OrderAdminListDTO;
import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.enums.OrderStatus;
import org.springframework.data.domain.Pageable;


public interface AdminOrderService {

    /**
     * Получить заказ для админа по ID
     */
    OrderAdminListDTO getOrderForAdmin(Long orderId);

    /**
     * Получить все заказы с пагинацией
     */
    PagedResponse<OrderAdminListDTO> getAllOrdersForAdmin(Pageable pageable);



    /**
     * Удалить заказ (для админа)
     */
    void deleteOrderForAdmin(Long orderId);

    /**
     * Обновить статус заказа
     */
    OrderAdminListDTO updateOrderStatus(Long orderId, OrderStatus newStatus);
}