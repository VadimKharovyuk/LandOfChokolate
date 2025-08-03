package com.example.landofchokolate.service.serviceImpl;
import com.example.landofchokolate.config.NovaPoshtaConfig;
import com.example.landofchokolate.dto.novaposhta.*;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.service.PoshtaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Primary
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheManager = "novaPoshtaCacheManager")
public class NovaPoshtaService implements PoshtaService {

    private final NovaPoshtaConfig config;
    private final RestTemplate restTemplate;
    private  final ObjectMapper objectMapper;

    // Кэш для городов
    private volatile List<City> cachedCities = new ArrayList<>();
    private final AtomicBoolean isLoadingCities = new AtomicBoolean(false);
    private final AtomicBoolean citiesLoaded = new AtomicBoolean(false);

    // Кэш популярных городов для быстрого старта
    private volatile List<City> popularCities = new ArrayList<>();
    private final AtomicBoolean popularCitiesLoaded = new AtomicBoolean(false);

    @PostConstruct
    public void initService() {
        log.info("NovaPoshtaService initialized - background loading will start after app startup");

        // Запускаем загрузку популярных городов асинхронно ПОСЛЕ старта приложения
        CompletableFuture.runAsync(() -> {
            try {
                // Небольшая задержка чтобы приложение успело стартовать
                Thread.sleep(2000);
                preloadPopularCitiesAsync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Popular cities preloading interrupted");
            }
        });
    }

    /**
     * ✅ БЫСТРАЯ ЗАГРУЗКА: Только популярные города (топ 50)
     */
    @Async
    public void preloadPopularCitiesAsync() {
        if (popularCitiesLoaded.get()) {
            return;
        }

        log.info("Loading popular cities for quick start...");

        try {
            // Загружаем только первую страницу с самыми популярными городами
            Map<String, Object> methodProperties = new HashMap<>();
            methodProperties.put("Page", "1");
            methodProperties.put("Limit", "50"); // Только топ-50 городов
            methodProperties.put("FindByString", ""); // Пустой поиск = популярные города

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "Address",
                    "getCities",
                    methodProperties
            );

            String responseBody = makeDirectHttpRequest(request);

            if (responseBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                NovaPoshtaResponse<City> response = mapper.readValue(responseBody,
                        new TypeReference<NovaPoshtaResponse<City>>() {});

                if (response.isSuccess() && response.getData() != null) {
                    popularCities = new ArrayList<>(response.getData());
                    popularCitiesLoaded.set(true);

                    log.info("✅ Loaded {} popular cities for quick search", popularCities.size());

                    // Теперь запускаем полную загрузку в фоне
                    startFullCitiesLoadingAsync();
                }
            }
        } catch (Exception e) {
            log.error("Error loading popular cities", e);
        }
    }

    /**
     * ✅ ПОЛНАЯ ЗАГРУЗКА: Все города в фоне с низким приоритетом
     */
    @Async
    public void startFullCitiesLoadingAsync() {
        if (isLoadingCities.get() || citiesLoaded.get()) {
            return;
        }

        // Устанавливаем низкий приоритет потока
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        log.info("Starting full cities loading in background...");
        loadAllCitiesInternal();
    }

    @Override
    public List<City> getCities() {
        log.debug("Getting cities - full loaded: {}, popular loaded: {}, loading: {}",
                citiesLoaded.get(), popularCitiesLoaded.get(), isLoadingCities.get());

        // Если полный кэш готов - возвращаем его
        if (citiesLoaded.get() && !cachedCities.isEmpty()) {
            log.debug("Returning {} cached cities", cachedCities.size());
            return cachedCities;
        }

        // Если есть популярные города - возвращаем их
        if (popularCitiesLoaded.get() && !popularCities.isEmpty()) {
            log.debug("Returning {} popular cities while full loading continues", popularCities.size());

            // Если полная загрузка еще не началась - запускаем
            if (!isLoadingCities.get()) {
                startFullCitiesLoadingAsync();
            }

            return popularCities;
        }

        // В крайнем случае загружаем первую страницу синхронно
        return loadFirstPageQuickly();
    }

    /**
     * ✅ ЭКСТРЕННАЯ ЗАГРУЗКА: Только первая страница синхронно
     */
    private List<City> loadFirstPageQuickly() {
        log.info("Emergency loading: first page of cities");

        try {
            Map<String, Object> methodProperties = new HashMap<>();
            methodProperties.put("Page", "1");
            methodProperties.put("Limit", "100");

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "Address",
                    "getCities",
                    methodProperties
            );

            String responseBody = makeDirectHttpRequest(request);

            if (responseBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                NovaPoshtaResponse<City> response = mapper.readValue(responseBody,
                        new TypeReference<NovaPoshtaResponse<City>>() {});

                if (response.isSuccess() && response.getData() != null) {
                    List<City> cities = response.getData();


                    // Запускаем фоновую загрузку если еще не запущена
                    if (!isLoadingCities.get()) {
                        startFullCitiesLoadingAsync();
                    }

                    return cities;
                }
            }
        } catch (Exception e) {
            log.error("Error in emergency cities loading", e);
        }

        return Collections.emptyList();
    }

    /**
     * ✅ ВНУТРЕННИЙ МЕТОД: Загрузка всех городов с оптимизацией
     */
    private void loadAllCitiesInternal() {
        if (!isLoadingCities.compareAndSet(false, true)) {
            return; // Уже загружается
        }

        List<City> allCities = new ArrayList<>();

        try {
            int page = 1;
            int limit = 500;
            boolean hasMoreData = true;

            while (hasMoreData && page <= 50) { // Ограничиваем максимум 50 страниц

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

                if (responseBody != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    NovaPoshtaResponse<City> response = mapper.readValue(responseBody,
                            new TypeReference<NovaPoshtaResponse<City>>() {});

                    if (response.isSuccess() && response.getData() != null) {
                        List<City> pageCities = response.getData();
                        allCities.addAll(pageCities);

                        // Обновляем кэш по ходу загрузки (thread-safe)
                        cachedCities = new ArrayList<>(allCities);

                        if (page % 5 == 0) { // Логируем каждые 5 страниц

                        }

                        if (pageCities.size() < limit) {
                            hasMoreData = false;
                        } else {
                            page++;
                        }

                        // Увеличиваем задержку чтобы не перегружать API
                        Thread.sleep(100);

                    } else {
                        log.warn("Page {} returned success=false", page);
                        hasMoreData = false;
                    }
                } else {
                    log.warn("Empty response for page {}", page);
                    hasMoreData = false;
                }
            }

            cachedCities = allCities;
            citiesLoaded.set(true);

            log.info("✅ Background cities loading completed: {} cities total", allCities.size());

        } catch (Exception e) {
            log.error("Error during background cities loading", e);
        } finally {
            isLoadingCities.set(false);
        }
    }

    /**
     * ✅ HTTP клиент с таймаутами
     */
    private String makeDirectHttpRequest(NovaPoshtaRequest request) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)  // Следовать редиректам
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(request);

            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(config.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "LandOfChokolate/1.0")
                    .timeout(java.time.Duration.ofSeconds(50))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            java.net.http.HttpResponse<String> httpResponse = client.send(httpRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

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


    @Override
    public List<Department> getDepartments(String cityRef) {
        log.info("Fetching departments for city: {}", cityRef);

        if (cityRef == null || cityRef.trim().isEmpty()) {
            log.warn("City reference is empty");
            return Collections.emptyList();
        }

        try {
            Map<String, Object> methodProperties = new HashMap<>();
            methodProperties.put("CityRef", cityRef);
            methodProperties.put("Page", "1");
            methodProperties.put("Limit", "0");
            methodProperties.put("Language", "ua");

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "Address",
                    "getWarehouses",
                    methodProperties
            );

            NovaPoshtaResponse<Department> response = sendRequest(request,
                    new ParameterizedTypeReference<NovaPoshtaResponse<Department>>() {});

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
            Map<String, Object> methodProperties = buildDeliveryProperties(order);

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "InternetDocument",
                    "save",
                    methodProperties
            );

            // Логируем JSON запроса
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("Sending request to NovaPoshta: {}", requestJson);

            NovaPoshtaResponse<CreateDeliveryResponse> response = sendRequest(request,
                    new ParameterizedTypeReference<NovaPoshtaResponse<CreateDeliveryResponse>>() {});

            // Логируем JSON ответа
            String responseJson = objectMapper.writeValueAsString(response);
            log.info("Response from NovaPoshta: {}", responseJson);

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


    @Override
    @Cacheable(value = "novaPoshtaTracking", key = "#trackingNumber")
    public TrackingInfo trackDelivery(String trackingNumber) {
        log.info("🚀 Tracking delivery: {}", trackingNumber);

        // Валидация
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер відстеження не може бути порожнім");
        }

        String cleanTrackingNumber = trackingNumber.replaceAll("\\s+", "");
        if (cleanTrackingNumber.length() != 14 || !cleanTrackingNumber.matches("\\d{14}")) {
            throw new IllegalArgumentException("Неправильний формат номеру відстеження");
        }

        try {
            // Формируем JSON запрос вручную
            String requestJson = String.format("""
            {
                "apiKey": "%s",
                "modelName": "TrackingDocument",
                "calledMethod": "getStatusDocuments",
                "methodProperties": {
                    "Documents": [
                        {
                            "DocumentNumber": "%s",
                            "Phone": ""
                        }
                    ]
                }
            }
            """, config.getApiKey(), cleanTrackingNumber);

            log.debug("📤 Sending tracking request");

            // Прямой HTTP запрос без использования sendRequest
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "LandOfChokolate/1.0");

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            // Получаем сырой JSON ответ
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                log.error("❌ Empty response from Nova Poshta");
                throw new RuntimeException("Отримано порожню відповідь від Nova Пошта");
            }

            log.debug("📥 Response received: {} chars", responseBody.length());

            // Парсим JSON вручную, избегая проблемных полей
            JsonNode rootNode = objectMapper.readTree(responseBody);

            boolean success = rootNode.path("success").asBoolean(false);
            log.debug("✅ API success: {}", success);

            if (!success) {
                JsonNode errorsNode = rootNode.path("errors");
                if (errorsNode.isArray() && errorsNode.size() > 0) {
                    String errorMessage = errorsNode.get(0).asText("Невідома помилка");
                    log.error("❌ Nova Poshta error: {}", errorMessage);
                    throw new RuntimeException(errorMessage);
                }
                throw new RuntimeException("Nova Пошта повернула помилку");
            }

            JsonNode dataNode = rootNode.path("data");
            if (!dataNode.isArray() || dataNode.size() == 0) {
                log.warn("⚠️ No data found for: {}", cleanTrackingNumber);
                throw new RuntimeException("Посилку з номером " + cleanTrackingNumber + " не знайдено");
            }

            // Извлекаем данные посылки
            JsonNode trackingData = dataNode.get(0);
            log.debug("📦 Processing tracking data");

            // Создаем TrackingInfo объект вручную
            TrackingInfo trackingInfo = buildTrackingInfo(trackingData);

            log.info("✅ Successfully tracked: {} - Status: {}",
                    cleanTrackingNumber, trackingInfo.getStatus());

            return trackingInfo;

        } catch (JsonProcessingException e) {
            log.error("❌ JSON error for: " + cleanTrackingNumber, e);
            throw new RuntimeException("Помилка обробки відповіді від Nova Пошта");
        } catch (RestClientException e) {
            log.error("❌ Network error for: " + cleanTrackingNumber, e);
            if (e.getMessage().contains("timeout") || e.getMessage().contains("Connection")) {
                throw new RuntimeException("Проблеми з підключенням до Nova Пошта");
            }
            throw new RuntimeException("Помилка зв'язку з Nova Пошта");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error for: " + cleanTrackingNumber, e);
            throw new RuntimeException("Неочікувана помилка: " + e.getMessage());
        }
    }

    // Метод для создания TrackingInfo из JsonNode
    private TrackingInfo buildTrackingInfo(JsonNode trackingData) {
        TrackingInfo trackingInfo = new TrackingInfo();

        // Основные поля
        trackingInfo.setNumber(safeGetString(trackingData, "Number"));
        trackingInfo.setStatusCode(safeGetString(trackingData, "StatusCode"));
        trackingInfo.setDateCreated(safeGetString(trackingData, "DateCreated"));
        trackingInfo.setStatus(safeGetString(trackingData, "Status"));

        // География
        trackingInfo.setCitySender(safeGetString(trackingData, "CitySender"));
        trackingInfo.setCityRecipient(safeGetString(trackingData, "CityRecipient"));

        // Адреса (приоритет детальным адресам)
        String senderAddr = safeGetString(trackingData, "WarehouseSenderAddress");
        if (isEmpty(senderAddr)) {
            senderAddr = safeGetString(trackingData, "WarehouseSender");
        }
        trackingInfo.setSenderAddress(senderAddr);

        String recipientAddr = safeGetString(trackingData, "WarehouseRecipientAddress");
        if (isEmpty(recipientAddr)) {
            recipientAddr = safeGetString(trackingData, "WarehouseRecipient");
        }
        trackingInfo.setRecipientAddress(recipientAddr);

        // Получатель
        trackingInfo.setRecipientFullName(safeGetString(trackingData, "RecipientFullName"));

        // Финансы
        trackingInfo.setDocumentCost(safeGetString(trackingData, "DocumentCost"));
        trackingInfo.setAnnouncedPrice(safeGetString(trackingData, "AnnouncedPrice"));

        // Даты
        trackingInfo.setScheduledDeliveryDate(safeGetString(trackingData, "ScheduledDeliveryDate"));
        trackingInfo.setActualDeliveryDate(safeGetString(trackingData, "ActualDeliveryDate"));
        trackingInfo.setDateScan(safeGetString(trackingData, "DateScan"));
        trackingInfo.setDateMoving(safeGetString(trackingData, "DateMoving"));
        trackingInfo.setTrackingUpdateDate(safeGetString(trackingData, "TrackingUpdateDate"));

        // Платежи
        trackingInfo.setPaymentMethod(safeGetString(trackingData, "PaymentMethod"));
        trackingInfo.setPayerType(safeGetString(trackingData, "PayerType"));

        // Груз
        trackingInfo.setCargoDescriptionString(safeGetString(trackingData, "CargoDescriptionString"));
        trackingInfo.setCargoType(safeGetString(trackingData, "CargoType"));

        // Контакты
        trackingInfo.setPhoneSender(safeGetString(trackingData, "PhoneSender"));
        trackingInfo.setPhoneRecipient(safeGetString(trackingData, "PhoneRecipient"));

        // Склады
        trackingInfo.setWarehouseSender(safeGetString(trackingData, "WarehouseSender"));
        trackingInfo.setWarehouseRecipient(safeGetString(trackingData, "WarehouseRecipient"));

        // Вес и количество
        JsonNode weightNode = trackingData.path("DocumentWeight");
        if (!weightNode.isMissingNode()) {
            trackingInfo.setDocumentWeight(String.valueOf(weightNode.asInt(0)));
        }

        trackingInfo.setSeatsAmount(safeGetString(trackingData, "SeatsAmount"));
        trackingInfo.setServiceType(safeGetString(trackingData, "ServiceType"));

        return trackingInfo;
    }

    // Вспомогательные методы
    private String safeGetString(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        String value = fieldNode.asText();
        return isEmpty(value) ? null : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty() || "null".equals(value) || "".equals(value);
    }



    private Map<String, Object> buildDeliveryProperties(OrderDTO order) {
        Map<String, Object> properties = new HashMap<>();

        // ✅ Валидация обязательных полей
        validateOrderForDelivery(order);

        // ✅ ИСПРАВЛЕНО: Основные параметры отправления
        properties.put("PayerType", "Recipient");      // Получатель платит ✅
        properties.put("PaymentMethod", "Cash");       // Наличные при получении ✅
        properties.put("DateTime", "");
        properties.put("CargoType", "Parcel");         // ✅ ВАЖНО: Parcel вместо Cargo!
        properties.put("Weight", "1");
        properties.put("ServiceType", "WarehouseWarehouse");
        properties.put("SeatsAmount", "1");
        properties.put("Description", "Заказ шоколада #" + order.getId());
        properties.put("Cost", order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0");

        // ✅ Отправитель из конфигурации
        properties.put("CitySender", config.getCitySender());
        properties.put("SenderAddress", config.getSenderAddress());
        properties.put("Sender", config.getSenderRef());
        properties.put("ContactSender", config.getContactSenderRef());
        properties.put("SendersPhone", formatPhoneNumber(config.getSenderPhone()));

        // ✅ Получатель из заказа
        properties.put("CityRecipient", order.getRecipientCityRef());
        properties.put("RecipientAddress", order.getRecipientAddress());

        // Формируем полное имя получателя
        String recipientName = buildRecipientName(order);
        properties.put("RecipientName", recipientName);

        // Телефон получателя
        String recipientPhone = order.getRecipientPhone() != null && !order.getRecipientPhone().trim().isEmpty()
                ? order.getRecipientPhone()
                : order.getPhoneNumber();
        properties.put("RecipientsPhone", formatPhoneNumber(recipientPhone));

        // ✅ ИСПРАВЛЕНО: Контакт получателя для частных лиц
        properties.put("ContactRecipient", "");        // Пустой для автоматического создания
        properties.put("NewAddress", "1");             // Создать новый адрес
        properties.put("RecipientType", "PrivatePerson"); // ✅ ДОБАВЛЕНО: Тип получателя

        // Дополнительные поля (обычно пустые для склад-склад)
        properties.put("RecipientCityName", "");
        properties.put("RecipientArea", "");
        properties.put("RecipientAreaRegions", "");
        properties.put("RecipientHouse", "");
        properties.put("RecipientFlat", "");

        log.info("✅ Built delivery properties for order {}: Recipient={}, PayerType=Recipient, PaymentMethod=Cash",
                order.getId(), recipientName);

        return properties;
    }

    /**
     * Валидация заказа перед созданием накладной
     */
    private void validateOrderForDelivery(OrderDTO order) {
        if (order.getRecipientCityRef() == null || order.getRecipientCityRef().trim().isEmpty()) {
            throw new IllegalArgumentException("Не вказано місто отримувача для доставки Новою Поштою");
        }

        if (order.getRecipientAddress() == null || order.getRecipientAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Не вказано відділення отримувача для доставки Новою Поштою");
        }

        // Проверяем телефон
        String phone = order.getRecipientPhone() != null ? order.getRecipientPhone() : order.getPhoneNumber();
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Не вказано телефон отримувача для доставки Новою Поштою");
        }
    }

    /**
     * Формирует полное имя получателя
     */
    private String buildRecipientName(OrderDTO order) {
        StringBuilder fullName = new StringBuilder();

        if (order.getRecipientFirstName() != null && !order.getRecipientFirstName().trim().isEmpty()) {
            fullName.append(order.getRecipientFirstName().trim());
        }

        if (order.getRecipientLastName() != null && !order.getRecipientLastName().trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(order.getRecipientLastName().trim());
        }

        // Если имя получателя не указано - используем имя заказчика
        if (fullName.length() == 0 && order.getCustomerName() != null && !order.getCustomerName().trim().isEmpty()) {
            fullName.append(order.getCustomerName().trim());
        }

        // В крайнем случае - дефолтное значение
        if (fullName.length() == 0) {
            fullName.append("Покупець");
        }

        return fullName.toString();
    }

    /**
     * Форматирует номер телефона для Nova Poshta
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "+380000000000"; // Дефолтный номер
        }

        // Убираем все лишние символы
        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        // Если номер начинается с 380, добавляем +
        if (cleanPhone.startsWith("380") && !cleanPhone.startsWith("+380")) {
            cleanPhone = "+" + cleanPhone;
        }

        // Если номер начинается с 0, заменяем на +380
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "+38" + cleanPhone;
        }

        return cleanPhone;
    }

    private <T> NovaPoshtaResponse<T> sendRequest(NovaPoshtaRequest request,
                                                  ParameterizedTypeReference<NovaPoshtaResponse<T>> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "LandOfChokolate/1.0");

            HttpEntity<NovaPoshtaRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Sending request to Nova Poshta API: {}", request.getCalledMethod());

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