package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.order.OrderAdminListDTO;
import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.enums.OrderStatus;
import com.example.landofchokolate.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;


    @GetMapping
    public String orderList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        log.info("Getting orders list - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        try {
            // Создаем Pageable объект
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Получаем заказы через сервис
            PagedResponse<OrderAdminListDTO> orders = adminOrderService.getAllOrdersForAdmin(pageable);

            // Добавляем данные в модель
            model.addAttribute("orders", orders);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);

            // Добавляем енумы для использования в шаблоне
            model.addAttribute("allStatuses", OrderStatus.values());

            return "admin/order/list";

        } catch (Exception e) {
            log.error("Error getting orders list", e);
            model.addAttribute("errorMessage", "Ошибка при загрузке списка заказов");
            return "admin/order/list";
        }
    }


    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        log.info("Getting order details for id: {}", orderId);

        try {
            OrderAdminListDTO orderDetail = adminOrderService.getOrderForAdmin(orderId);

            if (orderDetail == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Заказ не найден");
                return "redirect:/admin/orders";
            }

            model.addAttribute("order", orderDetail);
            model.addAttribute("allStatuses", OrderStatus.values());

            return "admin/order/detail";

        } catch (Exception e) {
            log.error("Error getting order details for id: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке заказа");
            return "redirect:/admin/orders";
        }
    }

    /**
     * Обновление статуса заказа
     */
    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("status") OrderStatus newStatus,
            RedirectAttributes redirectAttributes) {

        log.info("Updating order status for id: {} to status: {}", orderId, newStatus);

        try {
            OrderAdminListDTO updatedOrder = adminOrderService.updateOrderStatus(orderId, newStatus);

            if (updatedOrder != null) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Статус заказа успешно обновлен на: " + newStatus.getDescription());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Заказ не найден");
            }

        } catch (IllegalArgumentException e) {
            log.error("Order not found: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Заказ не найден");

        } catch (Exception e) {
            log.error("Error updating order status for id: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении статуса заказа");
        }

        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * Альтернативный метод обновления статуса (как в оригинальном коде)
     */
    @PostMapping("/update")
    public String updateOrder(
            @RequestParam Long orderId,
            @RequestParam OrderStatus orderStatus,
            RedirectAttributes redirectAttributes) {

        log.info("Updating order status for id: {} to status: {}", orderId, orderStatus);

        try {
            adminOrderService.updateOrderStatus(orderId, orderStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус заказа успешно обновлен");

        } catch (Exception e) {
            log.error("Error updating order status for id: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении заказа");
        }

        return "redirect:/admin/orders";
    }

    /**
     * Удаление заказа
     */
    @PostMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        log.info("Deleting order with id: {}", orderId);

        try {
            adminOrderService.deleteOrderForAdmin(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Заказ успешно удален");

        } catch (IllegalArgumentException e) {
            log.error("Order not found for deletion: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Заказ не найден");

        } catch (Exception e) {
            log.error("Error deleting order with id: {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении заказа");
        }

        return "redirect:/admin/orders";
    }

    /**
     * Альтернативный метод удаления (как в оригинальном коде)
     */
    @PostMapping("/delete/{orderId}")
    public String deleteOrderAlt(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        return deleteOrder(orderId, redirectAttributes);
    }

    /**
     * AJAX endpoint для быстрого обновления статуса
     */
    @PostMapping("/{orderId}/quick-status-update")
    @ResponseBody
    public String quickStatusUpdate(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        try {
            OrderAdminListDTO updatedOrder = adminOrderService.updateOrderStatus(orderId, status);
            return updatedOrder != null ? "success" : "not_found";
        } catch (Exception e) {
            log.error("Quick status update failed for order: {}", orderId, e);
            return "error";
        }
    }
}