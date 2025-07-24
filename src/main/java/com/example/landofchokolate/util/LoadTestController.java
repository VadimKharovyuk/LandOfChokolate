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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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
}