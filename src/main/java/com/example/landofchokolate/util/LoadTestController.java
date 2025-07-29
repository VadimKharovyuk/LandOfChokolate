package com.example.landofchokolate.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class LoadTestController {

    private static final Logger log = LoggerFactory.getLogger(LoadTestController.class);

    // Константы для URL
    private static final String BASE_URL = "http://localhost:2217";
    private static final String ADD_TO_CART_URL = BASE_URL + "/cart/add";

    // Параметры нагрузочного тестирования
    private static final int USERS = 100;
    private static final int THREADS = 200;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/load-test/start")
    public ResponseEntity<String> startLoadTest() {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < USERS; i++) {
            executor.submit(() -> {
                try {
                    // Получение сессии с правильными заголовками
                    HttpHeaders getHeaders = new HttpHeaders();
                    getHeaders.set("User-Agent", "LoadTest-Client");
                    HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);

                    ResponseEntity<String> response = restTemplate.exchange(
                            BASE_URL,
                            HttpMethod.GET,
                            getEntity,
                            String.class
                    );

                    String sessionId = extractSessionId(response.getHeaders());
                    log.debug("Получен sessionId: {}", sessionId);

                    if (sessionId != null) {
                        // Небольшая задержка для инициализации сессии
                        Thread.sleep(100);

                        // Добавление в корзину
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Cookie", "JSESSIONID=" + sessionId);
                        headers.set("User-Agent", "LoadTest-Client");
                        headers.set("Content-Type", "application/x-www-form-urlencoded");

                        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                        body.add("productId", "1");
                        body.add("quantity", "1");

                        HttpEntity<MultiValueMap<String, String>> request =
                                new HttpEntity<>(body, headers);

                        ResponseEntity<String> cartResponse = restTemplate.postForEntity(ADD_TO_CART_URL, request, String.class);

                        if (cartResponse.getStatusCode().is2xxSuccessful()) {
                            log.info("Товар успешно добавлен в корзину [сессия {}]", sessionId);
                        } else {
                            log.error("Ошибка добавления товара: {} для сессии: {}", cartResponse.getStatusCode(), sessionId);
                        }
                    } else {
                        log.error("Не удалось получить sessionId из ответа");
                    }

                } catch (Exception e) {
                    log.error("Ошибка в нагрузочном тесте", e);
                }
            });
        }

        // Корректное завершение executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok("Нагрузочный тест запущен для " + USERS + " пользователей");
    }

    private String extractSessionId(HttpHeaders headers) {
        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith("JSESSIONID")) {
                    String sessionId = cookie.split("=")[1].split(";")[0];
                    log.debug("Извлечен sessionId: {}", sessionId);
                    return sessionId;
                }
            }
        }
        log.warn("JSESSIONID не найден в headers: {}", headers.get("Set-Cookie"));
        return null;
    }

    @GetMapping("/test-session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testSession(HttpSession session, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        response.put("sessionId", session.getId());
        response.put("cartUuid", session.getAttribute("cartUuid"));
        response.put("isNew", session.isNew());

        // Логируем все cookie
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies()).forEach(cookie ->
                    log.info("Test Cookie: {} = {}", cookie.getName(), cookie.getValue())
            );
        }

        return ResponseEntity.ok(response);
    }


    // Добавьте эти методы в ваш LoadTestController

    @PostMapping("/load-test/homepage/{users}")
    public ResponseEntity<String> testHomePage(@PathVariable int users) {
        // Валидация количества пользователей
        if (users <= 0 || users > 200) {
            return ResponseEntity.badRequest()
                    .body("Количество пользователей должно быть от 1 до 200");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(users, 50));
        final String HOMEPAGE_URL = "https://landofchokolate.onrender.com/";

        log.info("Запуск нагрузочного теста главной страницы для {} пользователей", users);

        for (int i = 0; i < users; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    // Настройка заголовков для реалистичного запроса
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (LoadTest-Client-" + userId + ")");
                    headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    headers.set("Accept-Language", "uk,en;q=0.5");
                    headers.set("Accept-Encoding", "gzip, deflate");
                    headers.set("Connection", "keep-alive");

                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    long startTime = System.currentTimeMillis();

                    ResponseEntity<String> response = restTemplate.exchange(
                            HOMEPAGE_URL,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

                    long responseTime = System.currentTimeMillis() - startTime;

                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Пользователь {} - Успешно загрузил главную страницу за {} мс",
                                userId, responseTime);
                    } else {
                        log.error("Пользователь {} - Ошибка загрузки: {}",
                                userId, response.getStatusCode());
                    }

                    // Небольшая задержка между запросами для имитации реального поведения
                    Thread.sleep(100 + (int)(Math.random() * 200)); // 100-300ms

                } catch (Exception e) {
                    log.error("Пользователь {} - Ошибка при загрузке главной страницы: {}",
                            userId, e.getMessage());
                }
            });
        }

        // Корректное завершение executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                log.warn("Тест был принудительно остановлен по таймауту");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok("Нагрузочный тест главной страницы запущен для " + users + " пользователей");
    }

    // Быстрые методы для часто используемых значений
    @PostMapping("/load-test/homepage-50")
    public ResponseEntity<String> testHomePage50() {
        return testHomePage(50);
    }

    @PostMapping("/load-test/homepage-60")
    public ResponseEntity<String> testHomePage60() {
        return testHomePage(60);
    }

    @PostMapping("/load-test/homepage-80")
    public ResponseEntity<String> testHomePage80() {
        return testHomePage(80);
    }

    // Метод для получения статистики нагрузки
    @GetMapping("/load-test/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLoadTestStats() {
        Map<String, Object> stats = new HashMap<>();

        // Получаем информацию о системе
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        stats.put("timestamp", System.currentTimeMillis());
        stats.put("memory", Map.of(
                "total_mb", totalMemory / 1024 / 1024,
                "used_mb", usedMemory / 1024 / 1024,
                "free_mb", freeMemory / 1024 / 1024,
                "usage_percent", (usedMemory * 100) / totalMemory
        ));

        stats.put("available_processors", runtime.availableProcessors());

        return ResponseEntity.ok(stats);
    }


    // Добавьте эти методы в ваш LoadTestController

// ============================================================================
// ТЕСТИРОВАНИЕ СТРАНИЦЫ КАТЕГОРИЙ
// ============================================================================

    @PostMapping("/load-test/categories/{users}")
    public ResponseEntity<String> testCategoriesPage(@PathVariable int users) {
        if (users <= 0 || users > 200) {
            return ResponseEntity.badRequest()
                    .body("Количество пользователей должно быть от 1 до 200");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(users, 50));
        final String CATEGORIES_URL = "https://landofchokolate.onrender.com/categories";

        log.info("Запуск нагрузочного теста страницы категорий для {} пользователей", users);

        for (int i = 0; i < users; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (LoadTest-Categories-" + userId + ")");
                    headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    headers.set("Accept-Language", "uk,en;q=0.5");

                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    long startTime = System.currentTimeMillis();

                    ResponseEntity<String> response = restTemplate.exchange(
                            CATEGORIES_URL,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

                    long responseTime = System.currentTimeMillis() - startTime;

                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Пользователь {} - Категории загружены за {} мс", userId, responseTime);
                    } else {
                        log.error("Пользователь {} - Ошибка загрузки категорий: {}", userId, response.getStatusCode());
                    }

                    Thread.sleep(150 + (int)(Math.random() * 250)); // 150-400ms

                } catch (Exception e) {
                    log.error("Пользователь {} - Ошибка при загрузке категорий: {}", userId, e.getMessage());
                }
            });
        }

        shutdownExecutor(executor);
        return ResponseEntity.ok("Тест страницы категорий запущен для " + users + " пользователей");
    }

// ============================================================================
// ТЕСТИРОВАНИЕ СТРАНИЦЫ ВСЕХ ПРОДУКТОВ
// ============================================================================

    @PostMapping("/load-test/products-all/{users}")
    public ResponseEntity<String> testAllProductsPage(@PathVariable int users) {
        if (users <= 0 || users > 200) {
            return ResponseEntity.badRequest()
                    .body("Количество пользователей должно быть от 1 до 200");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(users, 40)); // Меньше потоков для БД
        final String PRODUCTS_URL = "https://landofchokolate.onrender.com/product/all";

        log.info("Запуск нагрузочного теста всех продуктов для {} пользователей", users);

        for (int i = 0; i < users; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (LoadTest-Products-" + userId + ")");
                    headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    headers.set("Accept-Language", "uk,en;q=0.5");

                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    long startTime = System.currentTimeMillis();

                    ResponseEntity<String> response = restTemplate.exchange(
                            PRODUCTS_URL,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

                    long responseTime = System.currentTimeMillis() - startTime;

                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Пользователь {} - Все продукты загружены за {} мс", userId, responseTime);
                    } else {
                        log.error("Пользователь {} - Ошибка загрузки продуктов: {}", userId, response.getStatusCode());
                    }

                    Thread.sleep(200 + (int)(Math.random() * 300)); // 200-500ms (больше для БД)

                } catch (Exception e) {
                    log.error("Пользователь {} - Ошибка при загрузке продуктов: {}", userId, e.getMessage());
                }
            });
        }

        shutdownExecutor(executor);
        return ResponseEntity.ok("Тест страницы всех продуктов запущен для " + users + " пользователей");
    }
    // ============================================================================
// ТЕСТИРОВАНИЕ КОНКРЕТНОГО ПРОДУКТА
// ============================================================================

    @PostMapping("/load-test/product-ferrero/{users}")
    public ResponseEntity<String> testFerreroProductPage(@PathVariable int users) {
        if (users <= 0 || users > 200) {
            return ResponseEntity.badRequest()
                    .body("Количество пользователей должно быть от 1 до 200");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(users, 40));
        final String FERRERO_URL = "https://landofchokolate.onrender.com/product/ferrero-rocher";

        log.info("Запуск нагрузочного теста страницы Ferrero Rocher для {} пользователей", users);

        for (int i = 0; i < users; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (LoadTest-Ferrero-" + userId + ")");
                    headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    headers.set("Accept-Language", "uk,en;q=0.5");
                    headers.set("Cache-Control", "no-cache");

                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    long startTime = System.currentTimeMillis();

                    ResponseEntity<String> response = restTemplate.exchange(
                            FERRERO_URL,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

                    long responseTime = System.currentTimeMillis() - startTime;

                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Пользователь {} - Ferrero Rocher загружен за {} мс", userId, responseTime);
                    } else {
                        log.error("Пользователь {} - Ошибка загрузки Ferrero: {}", userId, response.getStatusCode());
                    }

                    Thread.sleep(100 + (int)(Math.random() * 200)); // 100-300ms

                } catch (Exception e) {
                    log.error("Пользователь {} - Ошибка при загрузке Ferrero: {}", userId, e.getMessage());
                }
            });
        }

        shutdownExecutor(executor);
        return ResponseEntity.ok("Тест страницы Ferrero Rocher запущен для " + users + " пользователей");
    }
    @PostMapping("/load-test/complex/{users}")
    public ResponseEntity<String> testComplexUserJourney(@PathVariable int users) {
        if (users <= 0 || users > 100) {
            return ResponseEntity.badRequest()
                    .body("Для комплексного теста максимум 100 пользователей");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(users, 30));

        log.info("Запуск комплексного теста пользовательского пути для {} пользователей", users);

        for (int i = 0; i < users; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    simulateUserJourney(userId);
                } catch (Exception e) {
                    log.error("Пользователь {} - Ошибка в комплексном тесте: {}", userId, e.getMessage());
                }
            });
        }

        shutdownExecutor(executor);
        return ResponseEntity.ok("Комплексный тест запущен для " + users + " пользователей");
    }

// ============================================================================
// БЫСТРЫЕ МЕТОДЫ ДЛЯ POSTMAN
// ============================================================================

    @PostMapping("/load-test/categories-50")
    public ResponseEntity<String> testCategories50() { return testCategoriesPage(50); }

    @PostMapping("/load-test/categories-80")
    public ResponseEntity<String> testCategories80() { return testCategoriesPage(80); }

    @PostMapping("/load-test/products-all-40")
    public ResponseEntity<String> testProductsAll40() { return testAllProductsPage(40); }

    @PostMapping("/load-test/products-all-60")
    public ResponseEntity<String> testProductsAll60() { return testAllProductsPage(60); }

    @PostMapping("/load-test/ferrero-50")
    public ResponseEntity<String> testFerrero50() { return testFerreroProductPage(50); }

    @PostMapping("/load-test/ferrero-80")
    public ResponseEntity<String> testFerrero80() { return testFerreroProductPage(80); }

    @PostMapping("/load-test/complex-30")
    public ResponseEntity<String> testComplex30() { return testComplexUserJourney(30); }

// ============================================================================
// ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
// ============================================================================

    private void simulateUserJourney(int userId) throws InterruptedException {
        String baseUrl = "https://landofchokolate.onrender.com";
        String[] urls = {
                baseUrl + "/",                           // Главная
                baseUrl + "/categories",                 // Категории
                baseUrl + "/product/all",               // Все продукты
                baseUrl + "/product/ferrero-rocher"     // Конкретный продукт
        };

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (ComplexTest-" + userId + ")");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        for (int i = 0; i < urls.length; i++) {
            try {
                HttpEntity<String> entity = new HttpEntity<>(headers);
                long startTime = System.currentTimeMillis();

                ResponseEntity<String> response = restTemplate.exchange(
                        urls[i],
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                long responseTime = System.currentTimeMillis() - startTime;

                log.info("Пользователь {} - Шаг {} ({}) выполнен за {} мс",
                        userId, i + 1, urls[i], responseTime);

                // Пауза между страницами (имитация чтения)
                Thread.sleep(500 + (int)(Math.random() * 1000)); // 0.5-1.5 сек

            } catch (Exception e) {
                log.error("Пользователь {} - Ошибка на шаге {} ({}): {}",
                        userId, i + 1, urls[i], e.getMessage());
            }
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(8, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                log.warn("Тест был принудительно остановлен по таймауту");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}