package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.novaposhta.City;
import com.example.landofchokolate.dto.novaposhta.Department;
import com.example.landofchokolate.service.PoshtaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/novaposhta")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NovaPoshtaController {

    private final PoshtaService poshtaService;


    @GetMapping("/cities")
    public ResponseEntity<Map<String, Object>> getCities() {
        log.info("API: Getting ALL cities from Nova Poshta with pagination");

        try {
            List<City> cities = poshtaService.getCities();

            Map<String, Object> response = new HashMap<>();

            if (cities == null || cities.isEmpty()) {
                log.warn("API: No cities received from Nova Poshta service");
                response.put("success", false);
                response.put("message", "Не вдалося завантажити міста Nova Poshta");
                response.put("data", List.of());
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            // ✅ СТАТИСТИКА по типам населенных пунктов
            Map<String, Long> byType = cities.stream()
                    .collect(Collectors.groupingBy(
                            city -> city.getSettlementTypeDescription() != null ?
                                    city.getSettlementTypeDescription() : "Невідомо",
                            Collectors.counting()
                    ));

            // ✅ СТАТИСТИКА по доставке
            long withDelivery1 = cities.stream().mapToLong(c -> "1".equals(c.getDelivery1()) ? 1 : 0).sum();
            long withDelivery3 = cities.stream().mapToLong(c -> "1".equals(c.getDelivery3()) ? 1 : 0).sum();
            long withBranches = cities.stream().mapToLong(c -> "1".equals(c.getIsBranch()) ? 1 : 0).sum();

            log.info("API: Successfully returning {} cities", cities.size());
            log.info("API: By type: {}", byType);
            log.info("API: With delivery1: {}, delivery3: {}, branches: {}",
                    withDelivery1, withDelivery3, withBranches);

            response.put("success", true);
            response.put("message", "Всі міста успішно завантажено");
            response.put("data", cities);
            response.put("count", cities.size());
            response.put("statistics", Map.of(
                    "byType", byType,
                    "withDelivery1", withDelivery1,
                    "withDelivery3", withDelivery3,
                    "withBranches", withBranches
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("API: Error getting cities", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Помилка сервера при завантаженні міст");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("data", List.of());
            errorResponse.put("count", 0);

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Получение отделений Nova Poshta по городу
     * GET /api/novaposhta/departments/{cityRef}
     */
    @GetMapping("/departments/{cityRef}")
    public ResponseEntity<Map<String, Object>> getDepartments(@PathVariable String cityRef) {
        log.info("API: Getting departments for city: {}", cityRef);

        Map<String, Object> response = new HashMap<>();

        // Валидация входных данных
        if (cityRef == null || cityRef.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Не вказано код міста");
            response.put("data", List.of());
            response.put("count", 0);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<Department> departments = poshtaService.getDepartments(cityRef);

            if (departments == null || departments.isEmpty()) {
                log.warn("API: No departments found for city: {}", cityRef);
                response.put("success", false);
                response.put("message", "Відділень не знайдено для цього міста");
                response.put("data", List.of());
                response.put("count", 0);
                response.put("cityRef", cityRef);
                return ResponseEntity.ok(response);
            }

            log.info("API: Successfully returning {} departments for city {}", departments.size(), cityRef);

            response.put("success", true);
            response.put("message", "Відділення успішно завантажено");
            response.put("data", departments);
            response.put("count", departments.size());
            response.put("cityRef", cityRef);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("API: Error getting departments for city: " + cityRef, e);

            response.put("success", false);
            response.put("message", "Помилка сервера при завантаженні відділень");
            response.put("error", e.getMessage());
            response.put("data", List.of());
            response.put("count", 0);
            response.put("cityRef", cityRef);

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Простой тест работы API
     * GET /api/novaposhta/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("API: Test endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Nova Poshta API працює!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }


    /**
     * Информация об API
     * GET /api/novaposhta/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.info("API: Info endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Nova Poshta API налаштовано");
        response.put("hasService", poshtaService != null);
        response.put("endpoints", Map.of(
                "cities", "GET /api/novaposhta/cities - Отримати список міст",
                "departments", "GET /api/novaposhta/departments/{cityRef} - Отримати відділення по місту",
                "test", "GET /api/novaposhta/test - Тест роботи API",
                "info", "GET /api/novaposhta/info - Інформація про API"
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Получение конкретного города по ref
     * GET /api/novaposhta/city/{cityRef}
     */
    @GetMapping("/city/{cityRef}")
    public ResponseEntity<Map<String, Object>> getCity(@PathVariable String cityRef) {
        log.info("API: Getting city info for ref: {}", cityRef);

        Map<String, Object> response = new HashMap<>();

        if (cityRef == null || cityRef.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Не вказано код міста");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<City> cities = poshtaService.getCities();

            City foundCity = cities.stream()
                    .filter(city -> cityRef.equals(city.getRef()))
                    .findFirst()
                    .orElse(null);

            if (foundCity != null) {
                response.put("success", true);
                response.put("message", "Місто знайдено");
                response.put("data", foundCity);
            } else {
                response.put("success", false);
                response.put("message", "Місто не знайдено");
                response.put("data", null);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("API: Error getting city info for ref: " + cityRef, e);

            response.put("success", false);
            response.put("message", "Помилка сервера при пошуку міста");
            response.put("error", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Поиск городов по названию
     * GET /api/novaposhta/cities/search?query=Київ
     */
    @GetMapping("/cities/search")
    public ResponseEntity<Map<String, Object>> searchCities(
            @RequestParam(required = false, defaultValue = "") String query) {

        log.info("API: Searching cities with query: '{}'", query);

        Map<String, Object> response = new HashMap<>();

        try {
            List<City> allCities = poshtaService.getCities();

            if (allCities == null || allCities.isEmpty()) {
                response.put("success", false);
                response.put("message", "Міста не завантажені");
                response.put("data", List.of());
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            List<City> filteredCities;

            // ✅ ИСПРАВЛЕНО: обрабатываем пустой запрос для предзагрузки
            if (query == null || query.trim().isEmpty() || "all".equals(query.trim().toLowerCase())) {
                // Возвращаем все города для предзагрузки
                filteredCities = allCities;
                log.info("Returning all {} cities for preloading", filteredCities.size());
            } else if (query.trim().length() < 2) {
                response.put("success", false);
                response.put("message", "Запит має містити мінімум 2 символи");
                response.put("data", List.of());
                response.put("count", 0);
                return ResponseEntity.badRequest().body(response);
            } else {
                // Фильтруем по запросу
                String searchQuery = query.trim().toLowerCase();
                filteredCities = allCities.stream()
                        .filter(city ->
                                city.getDescription().toLowerCase().contains(searchQuery) ||
                                        (city.getDescriptionRu() != null &&
                                                city.getDescriptionRu().toLowerCase().contains(searchQuery))
                        )
                        .limit(50) // Ограничиваем результат
                        .toList();
            }

            response.put("success", true);
            response.put("message", String.format("Знайдено %d міст за запитом '%s'",
                    filteredCities.size(), query));
            response.put("data", filteredCities);
            response.put("count", filteredCities.size());
            response.put("query", query);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("API: Error searching cities with query: " + query, e);

            response.put("success", false);
            response.put("message", "Помилка сервера при пошуку міст");
            response.put("error", e.getMessage());
            response.put("data", List.of());
            response.put("count", 0);

            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * ✅ Альтернативный эндпоинт с дефисом для совместимости с JS
     */
    @GetMapping("/warehouses")
    public ResponseEntity<?> getWarehouses(@RequestParam String cityRef) {
        log.info("API: Getting warehouses (alias) for city: {}", cityRef);
        return getDepartments(cityRef);
    }

    /**
     * ✅ Упрощенный поиск городов без обертки для JS
     */
    @GetMapping("/cities/search/simple")
    public ResponseEntity<List<City>> searchCitiesSimple(@RequestParam(required = false, defaultValue = "") String query) {
        log.debug("API: Simple search for cities with query: '{}'", query);

        try {
            List<City> allCities = poshtaService.getCities();

            if (allCities == null || allCities.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            if (query == null || query.trim().isEmpty()) {
                // Возвращаем все города для предзагрузки
                return ResponseEntity.ok(allCities);
            }

            String searchQuery = query.trim().toLowerCase();
            List<City> filteredCities = allCities.stream()
                    .filter(city ->
                            city.getDescription().toLowerCase().contains(searchQuery) ||
                                    (city.getDescriptionRu() != null && city.getDescriptionRu().toLowerCase().contains(searchQuery))
                    )
                    .limit(50)
                    .toList();

            return ResponseEntity.ok(filteredCities);

        } catch (Exception e) {
            log.error("API: Error in simple search for cities", e);
            return ResponseEntity.ok(List.of());
        }
    }
}