package com.example.landofchokolate.config;
import com.example.landofchokolate.service.VisitorTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class VisitorTrackingInterceptor implements HandlerInterceptor {

    private final VisitorTrackingService visitorTrackingService;

    public VisitorTrackingInterceptor(VisitorTrackingService visitorTrackingService) {
        this.visitorTrackingService = visitorTrackingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        log.info("🔥 VisitorTrackingInterceptor сработал для: {}", requestURI);

        try {
            // ✅ ИСПОЛЬЗУЕМ НОВЫЙ метод с передачей request
            visitorTrackingService.logVisit(requestURI, request);
        } catch (Exception e) {
            log.error("❌ Ошибка в visitor tracking: {}", e.getMessage());
            // Не блокируем запрос при ошибке
        }

        return true;
    }
}