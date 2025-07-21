package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.card.CartDto;
import com.example.landofchokolate.dto.card.CartItemDto;
import com.example.landofchokolate.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Отображение страницы корзины
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCartDto(session);
        Integer cartItemCount = cartService.getCartItemCount(session);
        BigDecimal cartTotal = cartService.getCartTotal(session);

        model.addAttribute("cart", cart);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("cartTotal", cartTotal);

        return "client/cart/view";
    }

    /**
     * Добавление товара в корзину (AJAX)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.addProduct(session, productId, quantity);

            // Возвращаем обновленную информацию о корзине
            Integer cartItemCount = cartService.getCartItemCount(session);
            BigDecimal cartTotal = cartService.getCartTotal(session);

            response.put("success", true);
            response.put("message", "Товар добавлен в корзину");
            response.put("cartItemCount", cartItemCount);
            response.put("cartTotal", cartTotal);

            log.info("Товар {} добавлен в корзину, количество: {}", productId, quantity);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("Ошибка добавления товара в корзину: {}", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    /**
     * Обновление количества товара в корзине (AJAX)
     * ИСПРАВЛЕНО: Правильная работа с CartServiceDatabaseImpl
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Валидация входных данных
            if (productId == null || quantity == null) {
                response.put("success", false);
                response.put("message", "Некорректные параметры");
                return ResponseEntity.badRequest().body(response);
            }

            if (quantity < 0) {
                response.put("success", false);
                response.put("message", "Количество не может быть отрицательным");
                return ResponseEntity.badRequest().body(response);
            }

            // Обновляем количество в корзине
            cartService.updateQuantity(session, productId, quantity);

            // Получаем обновленную информацию о корзине
            CartDto cart = cartService.getCartDto(session);
            Integer cartItemCount = cartService.getCartItemCount(session);
            BigDecimal cartTotal = cartService.getCartTotal(session);

            // Находим обновленный товар для возврата его новой стоимости
            BigDecimal itemTotal = BigDecimal.ZERO;

            if (cart != null && cart.getItems() != null) {
                itemTotal = cart.getItems().stream()
                        .filter(item -> item.getProduct() != null &&
                                item.getProduct().getId() != null &&
                                item.getProduct().getId().equals(productId))
                        .map(CartItemDto::getTotalPrice)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
            }

            response.put("success", true);
            response.put("message", "Количество обновлено");
            response.put("cartItemCount", cartItemCount != null ? cartItemCount : 0);
            response.put("cartTotal", cartTotal != null ? cartTotal : BigDecimal.ZERO);
            response.put("itemTotal", itemTotal);

            log.info("Количество товара {} обновлено до: {}", productId, quantity);

        } catch (RuntimeException e) {
            // Обработка бизнес-ошибок (недостаток товара на складе и т.д.)
            response.put("success", false);
            response.put("message", e.getMessage());
            log.warn("Ошибка обновления количества товара {}: {}", productId, e.getMessage());

        } catch (Exception e) {
            // Обработка системных ошибок
            response.put("success", false);
            response.put("message", "Произошла внутренняя ошибка сервера");
            log.error("Системная ошибка при обновлении количества товара {}: {}", productId, e.getMessage(), e);
        }

        return ResponseEntity.ok(response);
    }


    /**
     * Удаление товара из корзины (AJAX)
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeProduct(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.removeProduct(session, productId);

            // Возвращаем обновленную информацию о корзине
            Integer cartItemCount = cartService.getCartItemCount(session);
            BigDecimal cartTotal = cartService.getCartTotal(session);
            boolean isEmpty = cartService.isCartEmpty(session);

            response.put("success", true);
            response.put("message", "Товар удален из корзины");
            response.put("cartItemCount", cartItemCount);
            response.put("cartTotal", cartTotal);
            response.put("isEmpty", isEmpty);

            log.info("Товар {} удален из корзины", productId);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("Ошибка удаления товара из корзины: {}", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Очистка корзины (AJAX)
     */
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.clearCart(session);

            response.put("success", true);
            response.put("message", "Корзина очищена");
            response.put("cartItemCount", 0);
            response.put("cartTotal", BigDecimal.ZERO);
            response.put("isEmpty", true);

            log.info("Корзина очищена");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("Ошибка очистки корзины: {}", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    /**
     * Получение информации о корзине (AJAX)
     * Полезно для обновления счетчиков на странице
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartInfo(HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Integer cartItemCount = cartService.getCartItemCount(session);
            BigDecimal cartTotal = cartService.getCartTotal(session);
            boolean isEmpty = cartService.isCartEmpty(session);

            response.put("success", true);
            response.put("cartItemCount", cartItemCount);
            response.put("cartTotal", cartTotal);
            response.put("isEmpty", isEmpty);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("Ошибка получения информации о корзине: {}", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}