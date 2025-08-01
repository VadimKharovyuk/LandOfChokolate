package com.example.landofchokolate.service.serviceImpl;
import com.example.landofchokolate.config.NovaPoshtaConfig;
import com.example.landofchokolate.dto.novaposhta.*;
import com.example.landofchokolate.dto.order.OrderDTO;
import com.example.landofchokolate.enums.DeliveryMethod;
import com.example.landofchokolate.service.PoshtaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class NovaPoshtaService implements PoshtaService {

    private final NovaPoshtaConfig config;
    private final RestTemplate restTemplate;

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
                    log.info("✅ Emergency loaded {} cities", cities.size());

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
                log.debug("Loading cities page {} with limit {}", page, limit);

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
                            log.info("Background loading: page {}, total cities: {}", page, allCities.size());
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
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(request);

            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(config.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "LandOfChokolate/1.0")
                    .timeout(java.time.Duration.ofSeconds(30))
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

    @Override
    public TrackingInfo trackDelivery(String trackingNumber) {
        log.info("Tracking delivery: {}", trackingNumber);

        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            log.warn("Tracking number is empty");
            return null;
        }

        try {
            Map<String, Object> methodProperties = new HashMap<>();
            Map<String, Object> documents = new HashMap<>();
            documents.put("DocumentNumber", trackingNumber);
            documents.put("Phone", "");

            methodProperties.put("Documents", Arrays.asList(documents));

            NovaPoshtaRequest request = new NovaPoshtaRequest(
                    config.getApiKey(),
                    "TrackingDocument",
                    "getStatusDocuments",
                    methodProperties
            );

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
    private Map<String, Object> buildDeliveryProperties(OrderDTO order) {
        Map<String, Object> properties = new HashMap<>();

        properties.put("PayerType", "Sender");
        properties.put("PaymentMethod", "NonCash");
        properties.put("DateTime", "");
        properties.put("CargoType", "Cargo");
        properties.put("Weight", "1");
        properties.put("ServiceType", "WarehouseWarehouse");
        properties.put("SeatsAmount", "1");
        properties.put("Description", "Заказ шоколада #" + order.getId());
        properties.put("Cost", order.getTotalAmount().toString());

        // Получаем из конфигурации
        properties.put("CitySender", config.getCitySender());           // Ref города отправителя — откуда посылка уходит (например, Киев)
        properties.put("SenderAddress", config.getSenderAddress());     // Ref склада или отделения отправителя в этом городе
        properties.put("Sender", config.getSenderRef());                // Ref отправителя (юридического лица или физлица, зарегистрированного в Новой Почте)
        properties.put("ContactSender", config.getContactSenderRef());  // Ref контактного лица отправителя (человека, который будет заниматься отправкой)
        properties.put("SendersPhone", "+380000000000");                // Телефон отправителя для связи (можно использовать корпоративный номер)

        properties.put("CityRecipient", "");
        properties.put("RecipientAddress", "");
        properties.put("ContactRecipient", "");
        properties.put("RecipientsPhone", order.getPhoneNumber());

        properties.put("NewAddress", "1");
        properties.put("RecipientCityName", "");
        properties.put("RecipientArea", "");
        properties.put("RecipientAreaRegions", "");
        properties.put("RecipientHouse", "");
        properties.put("RecipientFlat", "");
        properties.put("RecipientName", order.getCustomerName());

        return properties;
    }

//    private Map<String, Object> buildDeliveryProperties(OrderDTO order) {
//        Map<String, Object> properties = new HashMap<>();
//
//        properties.put("PayerType", "Sender");
//        properties.put("PaymentMethod", "NonCash");
//        properties.put("DateTime", "");
//        properties.put("CargoType", "Cargo");
//        properties.put("Weight", "1");
//        properties.put("ServiceType", "WarehouseWarehouse");
//        properties.put("SeatsAmount", "1");
//        properties.put("Description", "Заказ шоколада #" + order.getId());
//        properties.put("Cost", order.getTotalAmount().toString());
//
//        // Заглушки — использовать реальные данные в продакшене!
//        properties.put("CitySender", "8d5a980d-391c-11dd-90d9-001a92567626"); // Киев
//        properties.put("SenderAddress", "1ec09d88-e1c2-11e3-8c4a-0050568002cf"); // Адрес склада в Киеве
//        properties.put("Sender", "00000000-0000-0000-0000-000000000001"); // Заглушка SenderRef
//        properties.put("ContactSender", "00000000-0000-0000-0000-000000000002"); // Заглушка ContactSenderRef
//        properties.put("SendersPhone", "+380000000000");
//
//        properties.put("CityRecipient", "");
//        properties.put("RecipientAddress", "");
//        properties.put("ContactRecipient", "");
//        properties.put("RecipientsPhone", order.getPhoneNumber());
//
//        properties.put("NewAddress", "1");
//        properties.put("RecipientCityName", "");
//        properties.put("RecipientArea", "");
//        properties.put("RecipientAreaRegions", "");
//        properties.put("RecipientHouse", "");
//        properties.put("RecipientFlat", "");
//        properties.put("RecipientName", order.getCustomerName());
//
//        return properties;
//    }
//
//    private Map<String, Object> buildDeliveryProperties(OrderDTO order) {
//        Map<String, Object> properties = new HashMap<>();
//
//        properties.put("PayerType", "Sender");
//        properties.put("PaymentMethod", "NonCash");
//        properties.put("DateTime", "");
//        properties.put("CargoType", "Cargo");
//        properties.put("Weight", "1");
//        properties.put("ServiceType", "WarehouseWarehouse");
//        properties.put("SeatsAmount", "1");
//        properties.put("Description", "Заказ шоколада #" + order.getId());
//        properties.put("Cost", order.getTotalAmount().toString());
//
//        properties.put("CitySender", "8d5a980d-391c-11dd-90d9-001a92567626");
//        properties.put("SenderAddress", "1ec09d88-e1c2-11e3-8c4a-0050568002cf");
//        properties.put("ContactSender", "");
//        properties.put("SendersPhone", "+380000000000");
//
//        properties.put("CityRecipient", "");
//        properties.put("RecipientAddress", "");
//        properties.put("ContactRecipient", "");
//        properties.put("RecipientsPhone", order.getPhoneNumber());
//
//        properties.put("NewAddress", "1");
//        properties.put("RecipientCityName", "");
//        properties.put("RecipientArea", "");
//        properties.put("RecipientAreaRegions", "");
//        properties.put("RecipientHouse", "");
//        properties.put("RecipientFlat", "");
//        properties.put("RecipientName", order.getCustomerName());
//
//        return properties;
//    }

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