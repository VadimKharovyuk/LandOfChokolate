package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.config.NovaPoshtaConfig;
import com.example.landofchokolate.dto.novaposhta.*;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.service.PoshtaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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


    /**
     * Получение списка всех городов Nova Poshta
     */
    @Override
    public List<City> getCities() {
        log.info("Fetching cities from Nova Poshta API");

        try {
            // Формируем запрос для получения городов
            Map<String, Object> methodProperties = new HashMap<>();
            methodProperties.put("Page", "1");
            methodProperties.put("Limit", "0"); // 0 = все города
            methodProperties.put("FindByString", ""); // пустая строка = все города

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "Address",
                    "getCities",
                    methodProperties
            );

            // Отправляем запрос
            NovaPoshtaResponse<City> response = sendRequest(request, new ParameterizedTypeReference<NovaPoshtaResponse<City>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("Successfully fetched {} cities", response.getData().size());
                return response.getData();
            } else {
                log.error("Failed to fetch cities. Errors: {}", response != null ? response.getErrors() : "null response");
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Error fetching cities from Nova Poshta API", e);
            return Collections.emptyList();
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