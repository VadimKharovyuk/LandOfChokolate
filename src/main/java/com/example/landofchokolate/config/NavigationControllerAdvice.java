package com.example.landofchokolate.config;

import com.example.landofchokolate.dto.category.CategoryNavDto;
import com.example.landofchokolate.service.CartService;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class NavigationControllerAdvice {
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final CategoryService categoryService;


    /**
     * Добавляет список категорий для мобильной навигации во все представления
     */
    @ModelAttribute("navigationCategories")
    public List<CategoryNavDto> addNavigationCategories() {
        return categoryService.getNavigationCategories(20);
    }


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
    public Integer getFavoritesCount(HttpSession session) {
     try {
         return wishlistService.getWishlistItemCount(session);
     }catch (Exception e) {
         log.warn("Ошибка при получении количества товаров в избраном: {}", e.getMessage());

     }
        return 0;
    }




}