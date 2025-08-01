package com.example.landofchokolate.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Getter
@Slf4j
@Configuration
public class NovaPoshtaConfig {

    @Value("${novaposhta.api.key:}")
    private String apiKey;

    @Value("${novaposhta.api.url:https://api.novaposhta.ua/v2.0/json/}")
    private String apiUrl;

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Nova Poshta API key is required! Set NOVAPOSHTA_API_KEY environment variable or novaposhta.api.key property");
        }
        log.info("Nova Poshta configuration loaded:");
        log.info("- API URL: {}", apiUrl);
        log.info("- API key: {}...", apiKey.substring(0, Math.min(6, apiKey.length())) + "****");
    }

}

