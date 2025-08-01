package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.config.NovaPoshtaConfig;
import com.example.landofchokolate.dto.novaposhta.*;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.service.PoshtaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovaPoshtaService implements PoshtaService {

    private final NovaPoshtaConfig config;
    private final RestTemplate restTemplate;

    // ============================================================================
// ОБНОВИТЕ NovaPoshtaService - метод getCities()
// ============================================================================

    @Override
    public List<City> getCities() {
        log.info("Fetching ALL cities from Nova Poshta API with pagination");

        List<City> allCities = new ArrayList<>();
        int page = 1;
        int limit = 150; // Максимум записей за запрос
        boolean hasMoreData = true;

        try {
            while (hasMoreData) {
                log.info("Loading cities page {} with limit {}", page, limit);

                Map<String, Object> methodProperties = new HashMap<>();
                methodProperties.put("Page", String.valueOf(page));
                methodProperties.put("Limit", String.valueOf(limit));

                NovaPoshtaRequest request = new NovaPoshtaRequest(
                        config.getApiKey(),
                        "Address",
                        "getCities",
                        methodProperties
                );

                String responseBody = makeDirectHttpRequest(request);

                if (responseBody != null && !responseBody.trim().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        NovaPoshtaResponse<City> response = mapper.readValue(responseBody,
                                new TypeReference<NovaPoshtaResponse<City>>() {});

                        if (response.isSuccess() && response.getData() != null) {
                            List<City> pageCities = response.getData();
                            allCities.addAll(pageCities);

                            log.info("Page {}: loaded {} cities, total so far: {}",
                                    page, pageCities.size(), allCities.size());

                            // ✅ УСЛОВИЕ ОСТАНОВКИ: если получили меньше чем limit, значит это последняя страница
                            if (pageCities.size() < limit) {
                                hasMoreData = false;
                                log.info("Received {} cities (less than limit {}), stopping pagination",
                                        pageCities.size(), limit);
                            } else {
                                page++;

                                // ✅ ЗАЩИТА от бесконечного цикла
                                if (page > 100) {
                                    log.warn("Reached maximum pages (100), stopping to prevent infinite loop");
                                    hasMoreData = false;
                                }

                                // ✅ НЕБОЛЬШАЯ ЗАДЕРЖКА чтобы не перегружать API
                                Thread.sleep(100);
                            }
                        } else {
                            log.error("Page {}: Nova Poshta returned success=false. Errors: {}",
                                    page, response.getErrors());
                            hasMoreData = false;
                        }

                    } catch (Exception parseException) {
                        log.error("Failed to parse cities response for page {}: {}", page, parseException.getMessage());
                        hasMoreData = false;
                    }
                } else {
                    log.error("Empty response from Nova Poshta API for page {}", page);
                    hasMoreData = false;
                }
            }

            log.info("✅ Successfully loaded {} cities from {} pages", allCities.size(), page - 1);
            return allCities;

        } catch (Exception e) {
            log.error("Error fetching cities from Nova Poshta API", e);
            return allCities; // Возвращаем то что успели загрузить
        }
    }

    // ✅ Добавьте этот вспомогательный метод (если еще не добавили):
    private String makeDirectHttpRequest(NovaPoshtaRequest request) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(request);

            log.debug("Request body: {}", requestBody);

            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(config.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "LandOfChokolate/1.0")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            java.net.http.HttpResponse<String> httpResponse = client.send(httpRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            log.info("HTTP Status: {}, Response length: {}",
                    httpResponse.statusCode(),
                    httpResponse.body() != null ? httpResponse.body().length() : 0);

            if (httpResponse.statusCode() == 200) {
                return httpResponse.body();
            } else {
                log.error("HTTP error: {} - {}", httpResponse.statusCode(), httpResponse.body());
                return null;
            }

        } catch (Exception e) {
            log.error("Direct HTTP request failed", e);
            return null;
        }
    }


    // ✅ ДОБАВЬТЕ ЭТОТ ВСПОМОГАТЕЛЬНЫЙ МЕТОД:
    private String makeRawHttpRequest(NovaPoshtaRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "LandOfChokolate/1.0");

            HttpEntity<NovaPoshtaRequest> entity = new HttpEntity<>(request, headers);

            log.info("Making HTTP POST to: {}", config.getApiUrl());
            log.info("Request headers: {}", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("HTTP Status: {}", response.getStatusCode());
            log.info("Response headers: {}", response.getHeaders());

            return response.getBody();

        } catch (Exception e) {
            log.error("HTTP request failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение списка отделений по городу
     */
    @Override
    public List<Department> getDepartments(String cityRef) {
        log.info("Fetching departments for city: {}", cityRef);

        if (cityRef == null || cityRef.trim().isEmpty()) {
            log.warn("City reference is empty");
            return Collections.emptyList();
        }

        try {
            // Формируем запрос для получения отделений
            Map<String, Object> methodProperties = new HashMap<>();
            methodProperties.put("CityRef", cityRef);
            methodProperties.put("Page", "1");
            methodProperties.put("Limit", "0"); // 0 = все отделения
            methodProperties.put("Language", "ua"); // украинский язык

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "Address",
                    "getWarehouses",
                    methodProperties
            );

            // Отправляем запрос
            NovaPoshtaResponse<Department> response = sendRequest(request, new ParameterizedTypeReference<NovaPoshtaResponse<Department>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("Successfully fetched {} departments for city {}", response.getData().size(), cityRef);
                return response.getData();
            } else {
                log.error("Failed to fetch departments for city {}. Errors: {}", cityRef,
                        response != null ? response.getErrors() : "null response");
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Error fetching departments for city: " + cityRef, e);
            return Collections.emptyList();
        }
    }

    /**
     * Создание накладной (ТТН) на основе заказа
     */
    @Override
    public String createDelivery(OrderDTO order) {
        log.info("Creating delivery for order: {}", order.getId());

        if (order == null) {
            log.error("Order is null");
            return null;
        }

        if (order.getDeliveryMethod() != DeliveryMethod.NOVA_POSHTA) {
            log.warn("Order {} has delivery method {}, but Nova Poshta expected",
                    order.getId(), order.getDeliveryMethod());
            return null;
        }

        try {
            // Формируем параметры для создания накладной
            Map<String, Object> methodProperties = buildDeliveryProperties(order);

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "InternetDocument",
                    "save",
                    methodProperties
            );

            // Отправляем запрос
            NovaPoshtaResponse<CreateDeliveryResponse> response = sendRequest(request,
                    new ParameterizedTypeReference<NovaPoshtaResponse<CreateDeliveryResponse>>() {});

            if (response != null && response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                CreateDeliveryResponse deliveryResponse = response.getData().get(0);
                String ttnNumber = deliveryResponse.getIntDocNumber();
                log.info("Successfully created delivery. TTN: {}", ttnNumber);
                return ttnNumber;
            } else {
                log.error("Failed to create delivery for order {}. Errors: {}", order.getId(),
                        response != null ? response.getErrors() : "null response");
                return null;
            }

        } catch (Exception e) {
            log.error("Error creating delivery for order: " + order.getId(), e);
            return null;
        }
    }

    /**
     * Отслеживание посылки по номеру ТТН
     */
    @Override
    public TrackingInfo trackDelivery(String trackingNumber) {
        log.info("Tracking delivery: {}", trackingNumber);

        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            log.warn("Tracking number is empty");
            return null;
        }

        try {
            // Формируем запрос для отслеживания
            Map<String, Object> methodProperties = new HashMap<>();
            Map<String, Object> documents = new HashMap<>();
            documents.put("DocumentNumber", trackingNumber);
            documents.put("Phone", ""); // можно добавить телефон для дополнительной проверки

            methodProperties.put("Documents", Arrays.asList(documents));

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "TrackingDocument",
                    "getStatusDocuments",
                    methodProperties
            );

            // Отправляем запрос
            NovaPoshtaResponse<TrackingInfo> response = sendRequest(request,
                    new ParameterizedTypeReference<NovaPoshtaResponse<TrackingInfo>>() {});

            if (response != null && response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                TrackingInfo trackingInfo = response.getData().get(0);
                log.info("Successfully tracked delivery: {} - Status: {}", trackingNumber, trackingInfo.getStatus());
                return trackingInfo;
            } else {
                log.error("Failed to track delivery {}. Errors: {}", trackingNumber,
                        response != null ? response.getErrors() : "null response");
                return null;
            }

        } catch (Exception e) {
            log.error("Error tracking delivery: " + trackingNumber, e);
            return null;
        }
    }

    /**
     * Построение параметров для создания накладной
     */
    private Map<String, Object> buildDeliveryProperties(OrderDTO order) {
        Map<String, Object> properties = new HashMap<>();

        // Основные параметры отправления
        properties.put("PayerType", "Sender"); // Отправитель платит
        properties.put("PaymentMethod", "NonCash"); // Безнал
        properties.put("DateTime", ""); // Текущая дата
        properties.put("CargoType", "Cargo"); // Груз
        properties.put("Weight", "1"); // Вес по умолчанию 1 кг
        properties.put("ServiceType", "WarehouseWarehouse"); // Склад-склад
        properties.put("SeatsAmount", "1"); // Количество мест
        properties.put("Description", "Заказ шоколада #" + order.getId()); // Описание
        properties.put("Cost", order.getTotalAmount().toString()); // Оценочная стоимость

        // Отправитель (ваш магазин) - заполните своими данными
        properties.put("CitySender", "8d5a980d-391c-11dd-90d9-001a92567626"); // Киев (замените на ваш город)
        properties.put("SenderAddress", "1ec09d88-e1c2-11e3-8c4a-0050568002cf"); // Отделение отправителя
        properties.put("ContactSender", ""); // Ваш контакт в системе Nova Poshta
        properties.put("SendersPhone", "+380000000000"); // Ваш телефон

        // Получатель (из заказа)
        properties.put("CityRecipient", ""); // Нужно получить из UI
        properties.put("RecipientAddress", ""); // Нужно получить из UI
        properties.put("ContactRecipient", ""); // Создается автоматически или берется существующий
        properties.put("RecipientsPhone", order.getPhoneNumber());

        // Дополнительные параметры
        properties.put("NewAddress", "1"); // Создать новый адрес получателя если нужно
        properties.put("RecipientCityName", ""); // Название города получателя
        properties.put("RecipientArea", ""); // Область получателя
        properties.put("RecipientAreaRegions", ""); // Район
        properties.put("RecipientHouse", ""); // Дом
        properties.put("RecipientFlat", ""); // Квартира
        properties.put("RecipientName", order.getCustomerName());

        return properties;
    }

    /**
     * Универсальный метод для отправки запросов к Nova Poshta API
     */
    private <T> NovaPoshtaResponse<T> sendRequest(NovaPoshtaRequest request, ParameterizedTypeReference<NovaPoshtaResponse<T>> responseType) {
        try {
            // Настройка заголовков
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "LandOfChokolate/1.0");

            HttpEntity<NovaPoshtaRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Sending request to Nova Poshta API: {}", request.getCalledMethod());

            // Отправка запроса
            return restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    responseType
            ).getBody();

        } catch (RestClientException e) {
            log.error("REST client error while calling Nova Poshta API", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while calling Nova Poshta API", e);
            return null;
        }
    }
}