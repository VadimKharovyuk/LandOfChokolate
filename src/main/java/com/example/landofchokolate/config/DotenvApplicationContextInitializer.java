package com.example.landofchokolate.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Загружаем ТОЛЬКО .env файл
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Создаем карту свойств ТОЛЬКО из .env
            Map<String, Object> dotenvProperties = new HashMap<>();

            System.out.println("=== Loading .env file ===");
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                dotenvProperties.put(key, value);

                // Также устанавливаем как системные свойства
                System.setProperty(key, value);

                // Логируем ТОЛЬКО переменные из .env (скрывая секретные значения)
                if (key.contains("PASSWORD") || key.contains("SECRET") || key.contains("KEY")) {
                    System.out.println("🔑 .env: " + key + " = [HIDDEN]");
                } else {
                    System.out.println("🔑 .env: " + key + " = " + value);
                }
            });

            // Добавляем в environment Spring с высоким приоритетом
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", dotenvProperties));

            System.out.println("✅ Successfully loaded " + dotenvProperties.size() + " properties from .env file");
            System.out.println("=== End .env loading ===");

        } catch (Exception e) {
            System.err.println("❌ Failed to load .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}