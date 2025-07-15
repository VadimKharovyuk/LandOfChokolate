package com.example.landofchokolate.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice
public class NavigationControllerAdvice {

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
    public Integer getCartCount(HttpServletRequest request) {
        // В будущем здесь можно получать количество из сессии или базы данных
        // Пример получения из сессии:
        // HttpSession session = request != null ? request.getSession(false) : null;
        // if (session != null) {
        //     Integer count = (Integer) session.getAttribute("cartCount");
        //     return count != null ? count : 0;
        // }

        return 0; // Заглушка для незарегистрированных пользователей
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