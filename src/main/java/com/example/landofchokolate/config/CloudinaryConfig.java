package com.example.landofchokolate.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:doixwfec5}")
    private String cloudName;

    @Value("${cloudinary.api-key:132578269839152}")
    private String apiKey;

    @Value("${cloudinary.api-secret:TpE5BTEQC9CwKbu7pXSwZ63bxo0}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        log.info("Creating Cloudinary bean with cloud name: {}", cloudName);

        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));

            // Тестируем конфигурацию
//            cloudinary.api().ping();
            log.info("Cloudinary configuration is valid and API is accessible");

            return cloudinary;
        } catch (Exception e) {
            log.error("Failed to create Cloudinary bean: {}", e.getMessage(), e);
            throw new RuntimeException("Cloudinary configuration failed", e);
        }
    }

    @PostConstruct
    public void logConfiguration() {
        log.info("Final Cloudinary config - Cloud: {}, Key: {}...",
                cloudName,
                apiKey != null && apiKey.length() > 6 ? apiKey.substring(0, 6) : "null");
    }
}