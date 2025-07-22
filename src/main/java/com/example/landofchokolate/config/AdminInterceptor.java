package com.example.landofchokolate.config;

import com.example.landofchokolate.enums.AdminType;
import com.example.landofchokolate.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        log.debug("AdminInterceptor processing URI: {}", uri);

        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("adminUser");
            if (user != null && user.getAdminType() != AdminType.NONE) {

                // Проверка прав на редактирование для VIEWER
                if (isEditOperation(uri) && user.getAdminType() == AdminType.VIEWER) {
                    log.warn("VIEWER user {} attempted to access edit operation: {}", user.getUsername(), uri);
                    response.sendRedirect("/admin/access-denied");
                    return false;
                }

                // Проверка прав на создание пользователей (только для SUPER_ADMIN)
                if (isUserManagementOperation(uri) && user.getAdminType() != AdminType.SUPER_ADMIN) {
                    log.warn("User {} with role {} attempted to access user management: {}",
                            user.getUsername(), user.getAdminType(), uri);
                    response.sendRedirect("/admin/access-denied");
                    return false;
                }

                return true;
            }
        }

        log.debug("No valid admin session found, redirecting to login");
        response.sendRedirect("/admin/login");
        return false;
    }

    private boolean isEditOperation(String uri) {
        return uri.contains("/edit") ||
                uri.contains("/update") ||
                uri.contains("/delete") ||
                uri.contains("/create") ||
                (uri.contains("/save") && !uri.contains("/create-user"));
    }

    private boolean isUserManagementOperation(String uri) {
        return uri.contains("/create-user") ||
                uri.contains("/delete-user") ||
                uri.contains("/edit-user");
    }
}