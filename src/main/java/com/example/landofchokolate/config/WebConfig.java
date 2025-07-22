package com.example.landofchokolate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")  // Защищаем ваш основной админ раздел
                .excludePathPatterns(
                        "/admin/login",            // Страница логина
                        "/admin/logout",           // Выход
                        "/admin/assets/**",        // Статические файлы
                        "/admin/css/**",           // CSS файлы
                        "/admin/js/**",            // JS файлы
                        "/admin/images/**"         // Изображения
                );
    }
}