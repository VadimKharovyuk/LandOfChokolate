package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.novaposhta.TrackingInfo;
import com.example.landofchokolate.dto.order.CreateOrderRequest;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.exception.EmptyCartException;
import com.example.landofchokolate.exception.OrderCreationException;
import com.example.landofchokolate.exception.OrderNotFoundException;
import com.example.landofchokolate.service.CartService;
import com.example.landofchokolate.service.OrderService;
import com.example.landofchokolate.service.PoshtaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final PoshtaService poshtaService;




    @GetMapping
    public String showOrderForm(Model model, HttpSession session) {
        if (cartService.isCartEmpty(session)) {
            return "redirect:/cart";
        }

        CartDto cartDto = cartService.getCartDto(session);
        model.addAttribute("cart", cartDto);

        model.addAttribute("orderRequest", new CreateOrderRequest());

        return "client/order/form";
    }

    @PostMapping
    public String createOrder(@Valid @ModelAttribute("orderRequest") CreateOrderRequest orderRequest,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            CartDto cartDto = cartService.getCartDto(session);
            model.addAttribute("cart", cartDto);
            return "client/order/form";
        }

        try {
            OrderDTO createdOrder = orderService.createOrder(orderRequest, session);

            // ✅ Специальное сообщение для Nova Poshta
            if (createdOrder.getDeliveryMethod() == DeliveryMethod.NOVA_POSHTA) {
                if (createdOrder.getTrackingNumber() != null && !createdOrder.getTrackingNumber().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Заказ успешно создан! Номер для отслеживания Nova Poshta: " + createdOrder.getTrackingNumber());
                } else {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Заказ успешно создан! Накладная Nova Poshta будет создана позже.");
                    redirectAttributes.addFlashAttribute("warningMessage",
                            "Не удалось создать накладную Nova Poshta автоматически. Свяжитесь с нами для уточнения.");
                }
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Заказ успешно создан!");
            }

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

            // ✅ Добавляем информацию о Nova Poshta
            if (orderDTO.getDeliveryMethod() == DeliveryMethod.NOVA_POSHTA) {
                model.addAttribute("isNovaPoshta", true);
                if (orderDTO.getTrackingNumber() != null && !orderDTO.getTrackingNumber().trim().isEmpty()) {
                    model.addAttribute("hasTrackingNumber", true);
                }
            }

            return "client/order/details";
        } catch (OrderNotFoundException e) {
            return "redirect:/";
        }
    }

    // ✅ ЕДИНСТВЕННЫЙ МЕТОД: API для AJAX отслеживания
    @GetMapping("/track/{trackingNumber}")
    @ResponseBody
    public ResponseEntity<?> trackPackage(@PathVariable String trackingNumber) {
        try {
            // Убираем пробелы и проверяем формат
            String cleanTrackingNumber = trackingNumber.replaceAll("\\s+", "");

            if (cleanTrackingNumber.length() != 14) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Неправильний формат номеру відстеження (потрібно 14 цифр)"));
            }

            TrackingInfo trackingInfo = poshtaService.trackDelivery(cleanTrackingNumber);
            return ResponseEntity.ok(trackingInfo);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Не вдалося отримати інформацію про посилку: " + e.getMessage()));
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

