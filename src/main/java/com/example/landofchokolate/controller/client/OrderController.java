package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.order.CreateOrderRequest;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.exception.EmptyCartException;
import com.example.landofchokolate.exception.OrderCreationException;
import com.example.landofchokolate.exception.OrderNotFoundException;
import com.example.landofchokolate.service.CartService;
import com.example.landofchokolate.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;


    @GetMapping
    public String showOrderForm(Model model, HttpSession session) {
        // Проверяем, что корзина не пуста
        if (cartService.isCartEmpty(session)) {
            return "redirect:/cart";
        }

        CartDto cartDto = cartService.getCartDto(session);
        model.addAttribute("cart", cartDto);
        model.addAttribute("orderRequest", new CreateOrderRequest());

        return "client/order/form";
    }


    @PostMapping
    public String createOrder(@Valid @ModelAttribute CreateOrderRequest orderRequest,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Если ошибки валидации - показываем форму снова
            CartDto cartDto = cartService.getCartDto(session);
            model.addAttribute("cart", cartDto);
            return "client/order/form";
        }

        try {
            OrderDTO createdOrder = orderService.createOrder(orderRequest, session);
            redirectAttributes.addFlashAttribute("successMessage", "Заказ успешно создан!");
            return "redirect:/order/" + createdOrder.getId();
        } catch (EmptyCartException | OrderCreationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            CartDto cartDto = cartService.getCartDto(session);
            model.addAttribute("cart", cartDto);
            return "client/order/form";
        }
    }

    @GetMapping("/{id}")
    public String showOrder(@PathVariable Long id, Model model) {
        try {
            OrderDTO orderDTO = orderService.getOrderById(id);
            model.addAttribute("orderDTO", orderDTO);
            return "client/order/details";
        } catch (OrderNotFoundException e) {
            return "redirect:/";
        }
    }

    // Показать форму поиска заказов
    @GetMapping("/info")
    public String showOrderSearchForm(Model model) {
        return "client/order/search";
    }

    @GetMapping("/info/{phone}")
    public String showOrderInfo(@PathVariable String phone, Model model) {
        log.info("Searching orders for phone: {}", phone);

        try {
            List<OrderDTO> orders = orderService.getOrdersByPhoneNumber(phone);
            log.info("Found {} orders", orders.size());

            model.addAttribute("searchPhone", phone);

            if (orders.isEmpty()) {
                model.addAttribute("message", "Заказы по номеру " + phone + " не найдены");
                model.addAttribute("messageType", "warning");
            } else {
                model.addAttribute("orders", orders);
                model.addAttribute("message", "Найдено заказов: " + orders.size());
                model.addAttribute("messageType", "success");
            }

            return "client/order/search";
        } catch (Exception e) {
            log.error("Error searching orders for phone: {}", phone, e);
            model.addAttribute("searchPhone", phone);
            model.addAttribute("message", "Ошибка: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "client/order/search";
        }
    }

    // Дополнительно: POST для поиска через форму
    @PostMapping("/info/search")
    public String searchOrders(@RequestParam String phone) {
        // Очистка номера телефона от лишних символов
        String cleanPhone = phone.replaceAll("[^+\\d]", "");
        return "redirect:/order/info/" + cleanPhone;
    }

}

