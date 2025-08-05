package com.example.landofchokolate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private VisitorTrackingInterceptor visitorTrackingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("🔧 Регистрируем все interceptors");

        // ✅ Админ интерцептор
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")  // Защищаем админ раздел
                .excludePathPatterns(
                        "/admin/login",            // Страница логина
                        "/admin/logout",           // Выход
                        "/admin/assets/**",        // Статические файлы
                        "/admin/css/**",           // CSS файлы
                        "/admin/js/**",            // JS файлы
                        "/admin/images/**"         // Изображения
                );

        // ✅ Трекинг посетителей - ТОЛЬКО важные страницы
        registry.addInterceptor(visitorTrackingInterceptor)
                .addPathPatterns(
                        "/",                    // Главная
                        "/products/**",         // Товары
                        "/categories/**",       // Категории
                        "/brands/**",          // Бренды
                        "/about",              // О нас
                        "/contact",            // Контакты
                        "/product/**"          // Отдельные товары
                );
    }
}