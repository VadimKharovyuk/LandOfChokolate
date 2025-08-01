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

    @Value("${novaposhta.sender-ref:}")
    private String senderRef;

    @Value("${novaposhta.contact-sender-ref:}")
    private String contactSenderRef;

    @Value("${novaposhta.city-sender:}")
    private String citySender;

    @Value("${novaposhta.sender-address:}")
    private String senderAddress;

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Потрібен API ключ Нова Пошта! Встановіть змінну оточення NOVAPOSHTA_API_KEY або властивість novaposhta.api.key");
        }
        log.info("Конфігурація Нова Пошта завантажена:");
        log.info("- URL API: {}", apiUrl);
        log.info("- API ключ: {}...", apiKey.substring(0, Math.min(6, apiKey.length())) + "****");
        log.info("- Sender Ref: {}", senderRef != null && !senderRef.isEmpty() ? senderRef : "<не встановлено>");
        log.info("- Contact Sender Ref: {}", contactSenderRef != null && !contactSenderRef.isEmpty() ? contactSenderRef : "<не встановлено>");
        log.info("- City Sender Ref: {}", citySender != null && !citySender.isEmpty() ? citySender : "<не встановлено>");
        log.info("- Sender Address Ref: {}", senderAddress != null && !senderAddress.isEmpty() ? senderAddress : "<не встановлено>");
    }
}

