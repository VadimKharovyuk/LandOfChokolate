package com.example.landofchokolate.config;

import com.example.landofchokolate.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class NavigationControllerAdvice {
    private final CartService cartService;

    @ModelAttribute("currentURI")
    public String getCurrentURI(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest is null, returning default URI");
            return "/";
        }

        String uri = request.getRequestURI();
        if (uri == null || uri.isEmpty()) {
            log.warn("Request URI is null or empty, returning default URI");
            return "/";
        }

        log.debug("Current URI: {}", uri);
        return uri;
    }


    @ModelAttribute("cartCount")
    public Integer getCartCount(HttpSession session) {
        try {
            return cartService.getCartItemCount(session);
        } catch (Exception e) {
            log.warn("Ошибка при получении количества товаров в корзине: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Добавляет общую сумму корзины во все модели
     * Доступно в шаблонах как ${cartTotal}
     */
    @ModelAttribute("cartTotal")
    public BigDecimal getCartTotal(HttpSession session) {
        try {
            return cartService.getCartTotal(session);
        } catch (Exception e) {
            log.warn("Ошибка при получении общей суммы корзины: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @ModelAttribute("favoritesCount")
    public Integer getFavoritesCount(HttpServletRequest request) {
        // В будущем здесь можно получать количество из сессии или базы данных
        // Пример получения из сессии:
        // HttpSession session = request != null ? request.getSession(false) : null;
        // if (session != null) {
        //     Integer count = (Integer) session.getAttribute("favoritesCount");
        //     return count != null ? count : 0;
        // }

        return 0; // Заглушка для незарегистрированных пользователей
    }


}