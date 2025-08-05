//package com.example.landofchokolate.controller;
//
//import com.example.landofchokolate.model.VisitorLog;
//import com.example.landofchokolate.repository.VisitorLogRepository;
//import com.example.landofchokolate.service.VisitorTrackingService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//// 📊 Контроллер для просмотра аналитики посетителей
//@Controller
//@RequestMapping("/analytics")
//@Slf4j
//@RequiredArgsConstructor
//public class VisitorAnalyticsController {
//
//    private final VisitorTrackingService visitorTrackingService;
//    private final VisitorLogRepository visitorLogRepository;
//
//
//    @GetMapping
//    public String analyticsHome(Model model) {
//        try {
//
//            Map<String, Object> basicStats = visitorTrackingService.getBasicStats();
//
//            // ✅ ЗАЩИТА ОТ NULL
//            if (basicStats == null) {
//                basicStats = new HashMap<>();
//                basicStats.put("totalVisits", 0L);
//                basicStats.put("weeklyVisits", 0L);
//                basicStats.put("mobilePercentage", 0.0);
//                basicStats.put("topCountries", new ArrayList<>());
//                basicStats.put("topPages", new ArrayList<>());
//            }
//
//            model.addAttribute("stats", basicStats);
//            model.addAttribute("totalVisits", basicStats.get("totalVisits"));
//
//            // Последние посетители (20 записей)
//            PageRequest pageRequest = PageRequest.of(0, 20,
//                    Sort.by(Sort.Direction.DESC, "visitTime"));
//            Page<VisitorLog> recentVisitors = visitorLogRepository.findAll(pageRequest);
//            model.addAttribute("recentVisitors", recentVisitors.getContent());
//
//            // Статистика за сегодня
//            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
//            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
//
//            long todayVisits = visitorLogRepository.countByVisitTimeBetween(startOfDay, endOfDay);
//            long todayUnique = visitorLogRepository.countUniqueVisitorsByTimeBetween(startOfDay, endOfDay);
//
//            model.addAttribute("todayVisits", todayVisits);
//            model.addAttribute("todayUnique", todayUnique);
//
//            // Топ страницы за неделю
//            LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
//            List<Object[]> topPages = visitorLogRepository.getTopPagesByTimeBetween(
//                    weekAgo, LocalDateTime.now(), PageRequest.of(0, 10));
//            model.addAttribute("topPages", topPages);
//
//            // Топ страны
//            List<Object[]> topCountries = visitorLogRepository.getTopCountriesByTimeBetween(
//                    weekAgo, LocalDateTime.now(), PageRequest.of(0, 5));
//            model.addAttribute("topCountries", topCountries);
//
//
//        } catch (Exception e) {
//            log.error("Error loading analytics: {}", e.getMessage());
//            model.addAttribute("error", "Ошибка загрузки аналитики: " + e.getMessage());
//
//            // ✅ FALLBACK данные при ошибке
//            Map<String, Object> fallbackStats = new HashMap<>();
//            fallbackStats.put("totalVisits", 0L);
//            fallbackStats.put("weeklyVisits", 0L);
//            fallbackStats.put("mobilePercentage", 0.0);
//            fallbackStats.put("topCountries", new ArrayList<>());
//            fallbackStats.put("topPages", new ArrayList<>());
//
//            model.addAttribute("stats", fallbackStats);
//            model.addAttribute("totalVisits", 0L);
//            model.addAttribute("todayVisits", 0L);
//            model.addAttribute("todayUnique", 0L);
//            model.addAttribute("topPages", new ArrayList<>());
//            model.addAttribute("topCountries", new ArrayList<>());
//            model.addAttribute("recentVisitors", new ArrayList<>());
//        }
//
//        return "admin/analytics/dashboard";
//    }
//
//    /**
//     * 🌍 Детальная статистика по странам
//     */
//    @GetMapping("/countries")
//    public String countriesAnalytics(@RequestParam(defaultValue = "7") int days, Model model) {
//        try {
//            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
//
//            List<Object[]> countriesStats = visitorLogRepository.getTopCountriesByTimeBetween(
//                    startDate, LocalDateTime.now(), PageRequest.of(0, 50));
//
//            model.addAttribute("countriesStats", countriesStats);
//            model.addAttribute("selectedDays", days);
//
//            // Общее количество за период
//            long totalVisits = visitorLogRepository.countByVisitTimeAfter(startDate);
//            model.addAttribute("totalVisits", totalVisits);
//
//        } catch (Exception e) {
//            log.error("Error loading countries analytics: {}", e.getMessage());
//            model.addAttribute("error", "Ошибка загрузки статистики по странам");
//        }
//
//        return "admin/analytics/countries";
//    }
//
//}

package com.example.landofchokolate.controller;

import com.example.landofchokolate.model.VisitorLog;
import com.example.landofchokolate.repository.VisitorLogRepository;
import com.example.landofchokolate.service.VisitorTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 📊 Контроллер для просмотра аналитики посетителей
@Controller
@RequestMapping("/analytics")
@Slf4j
@RequiredArgsConstructor
public class VisitorAnalyticsController {

    private final VisitorTrackingService visitorTrackingService;
    private final VisitorLogRepository visitorLogRepository;

    @GetMapping
    public String analyticsHome(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size,
                                Model model) {
        try {
            Map<String, Object> basicStats = visitorTrackingService.getBasicStats();

            // ✅ ЗАЩИТА ОТ NULL
            if (basicStats == null) {
                basicStats = createEmptyStats();
            }

            model.addAttribute("stats", basicStats);
            model.addAttribute("totalVisits", basicStats.get("totalVisits"));

            // ✅ ПОСЛЕДНИЕ ПОСЕТИТЕЛИ С ПАГИНАЦИЕЙ (максимум 50 записей)
            int maxSize = Math.min(size, 50); // Ограничиваем до 50
            PageRequest pageRequest = PageRequest.of(page, maxSize,
                    Sort.by(Sort.Direction.DESC, "visitTime"));
            Page<VisitorLog> recentVisitors = visitorLogRepository.findAll(pageRequest);

            model.addAttribute("recentVisitors", recentVisitors.getContent());

            // ✅ ДАННЫЕ ПАГИНАЦИИ
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", maxSize);
            model.addAttribute("totalPages", Math.min(recentVisitors.getTotalPages(), (50 / maxSize) + 1)); // Ограничиваем показ до 50 записей
            model.addAttribute("totalElements", Math.min(recentVisitors.getTotalElements(), 50L));
            model.addAttribute("hasNext", recentVisitors.hasNext() && (page + 1) * maxSize < 50);
            model.addAttribute("hasPrevious", recentVisitors.hasPrevious());

            // Для удобства навигации
            model.addAttribute("nextPage", page + 1);
            model.addAttribute("prevPage", page - 1);

            // Статистика за сегодня
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            long todayVisits = visitorLogRepository.countByVisitTimeBetween(startOfDay, endOfDay);
            long todayUnique = visitorLogRepository.countUniqueVisitorsByTimeBetween(startOfDay, endOfDay);

            model.addAttribute("todayVisits", todayVisits);
            model.addAttribute("todayUnique", todayUnique);

            // Топ страницы за неделю
            LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
            List<Object[]> topPages = visitorLogRepository.getTopPagesByTimeBetween(
                    weekAgo, LocalDateTime.now(), PageRequest.of(0, 10));
            model.addAttribute("topPages", topPages);

            // Топ страны
            List<Object[]> topCountries = visitorLogRepository.getTopCountriesByTimeBetween(
                    weekAgo, LocalDateTime.now(), PageRequest.of(0, 5));
            model.addAttribute("topCountries", topCountries);

            log.info("Analytics page accessed successfully - page: {}, size: {}", page, maxSize);

        } catch (Exception e) {
            log.error("Error loading analytics: {}", e.getMessage());
            model.addAttribute("error", "Ошибка загрузки аналитики: " + e.getMessage());

            // ✅ FALLBACK данные при ошибке
            setFallbackData(model);
        }

        return "admin/analytics/dashboard";
    }

    // ✅ ДОПОЛНИТЕЛЬНЫЙ МЕТОД: Показать все последние посетители отдельно
    @GetMapping("/visitors")
    public String allVisitors(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "50") int size,
                              Model model) {
        try {
            int maxSize = Math.min(size, 100);

            PageRequest pageRequest = PageRequest.of(page, maxSize,
                    Sort.by(Sort.Direction.DESC, "visitTime"));
            Page<VisitorLog> visitors = visitorLogRepository.findAll(pageRequest);

            model.addAttribute("visitors", visitors.getContent());

            // Полная пагинация
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", maxSize);
            model.addAttribute("totalPages", visitors.getTotalPages());
            model.addAttribute("totalElements", visitors.getTotalElements());
            model.addAttribute("hasNext", visitors.hasNext());
            model.addAttribute("hasPrevious", visitors.hasPrevious());
            model.addAttribute("nextPage", page + 1);
            model.addAttribute("prevPage", page - 1);

            int startPage = Math.max(0, page - 2);
            int endPage = Math.min(visitors.getTotalPages() - 1, page + 2);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);

            // ✅ ДОБАВЛЯЕМ СТАТИСТИКУ ВРУЧНУЮ
            List<VisitorLog> currentPageVisitors = visitors.getContent();
            long mobileCount = currentPageVisitors.stream().filter(v -> v.getIsMobile() != null && v.getIsMobile()).count();
            long desktopCount = currentPageVisitors.stream().filter(v -> v.getIsMobile() != null && !v.getIsMobile()).count();
            long botCount = currentPageVisitors.stream().filter(v -> v.getIsBot() != null && v.getIsBot()).count();
            long uniqueIPs = currentPageVisitors.stream().map(VisitorLog::getIpAddress).distinct().count();

            model.addAttribute("mobileCount", mobileCount);
            model.addAttribute("desktopCount", desktopCount);
            model.addAttribute("botCount", botCount);
            model.addAttribute("uniqueIPCount", uniqueIPs);

            log.info("All visitors page accessed - page: {}, size: {}, total: {}",
                    page, maxSize, visitors.getTotalElements());

        } catch (Exception e) {
            log.error("Error loading all visitors: {}", e.getMessage());
            model.addAttribute("error", "Ошибка загрузки посетителей: " + e.getMessage());
            model.addAttribute("visitors", new ArrayList<>());
        }

        return "admin/analytics/all-visitors";
    }

    /**
     * 🌍 Детальная статистика по странам
     */
    @GetMapping("/countries")
    public String countriesAnalytics(@RequestParam(defaultValue = "7") int days, Model model) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);

            List<Object[]> countriesStats = visitorLogRepository.getTopCountriesByTimeBetween(
                    startDate, LocalDateTime.now(), PageRequest.of(0, 50));

            model.addAttribute("countriesStats", countriesStats);
            model.addAttribute("selectedDays", days);

            // Общее количество за период
            long totalVisits = visitorLogRepository.countByVisitTimeAfter(startDate);
            model.addAttribute("totalVisits", totalVisits);

        } catch (Exception e) {
            log.error("Error loading countries analytics: {}", e.getMessage());
            model.addAttribute("error", "Ошибка загрузки статистики по странам");
        }

        return "admin/analytics/countries";
    }

    /**
     * 🔍 Поиск и анализ по IP адресу
     */
    @GetMapping("/ip")
    public String ipAnalytics(@RequestParam(required = false) String ip, Model model) {
        try {
            // Очищаем IP от лишних пробелов
            String cleanIp = (ip != null) ? ip.trim() : null;
            model.addAttribute("searchIp", cleanIp);

            if (cleanIp != null && !cleanIp.isEmpty()) {
                log.info("🔍 Поиск по IP: {}", cleanIp);

                // Получаем все посещения по IP
                List<VisitorLog> ipVisits = visitorTrackingService.getPagesByIp(cleanIp);
                model.addAttribute("ipVisits", ipVisits);

                if (!ipVisits.isEmpty()) {
                    // Детальная статистика по IP
                    Map<String, Object> ipStats = visitorTrackingService.getIpStatistics(cleanIp);
                    model.addAttribute("ipStats", ipStats);

                    // Хронология последних 50 посещений
                    List<Map<String, Object>> timeline = visitorTrackingService.getIpTimeline(cleanIp, 50);
                    model.addAttribute("timeline", timeline);

                    log.info("📊 Найдено {} посещений для IP {}", ipVisits.size(), cleanIp);
                } else {
                    model.addAttribute("noDataMessage", "По IP адресу " + cleanIp + " не найдено посещений");
                    log.info("🔍 Посещения для IP {} не найдены", cleanIp);
                }
            }

        } catch (Exception e) {
            log.error("❌ Ошибка поиска по IP {}: {}", ip, e.getMessage());
            model.addAttribute("error", "Ошибка при поиске по IP: " + e.getMessage());
        }

        return "admin/analytics/ip-search";
    }

    /**
     * 🔍 API для автокомплита IP адресов
     */
    @GetMapping("/ip/suggest")
    @ResponseBody
    public List<String> suggestIpAddresses(@RequestParam String query) {
        try {
            if (query.length() < 3) {
                return new ArrayList<>();
            }

            // Получаем уникальные IP начинающиеся с запроса
            List<String> suggestions = visitorLogRepository.findDistinctIpAddressesStartingWith(query + "%");

            // Ограничиваем до 10 результатов
            return suggestions.stream().limit(10).toList();

        } catch (Exception e) {
            log.error("Error getting IP suggestions: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 📊 API для статистики по IP (AJAX)
     */
    @GetMapping("/ip/{ipAddress}/stats")
    @ResponseBody
    public Map<String, Object> getIpStatsApi(@PathVariable String ipAddress) {
        try {
            return visitorTrackingService.getIpStatistics(ipAddress);
        } catch (Exception e) {
            log.error("Error getting IP stats for {}: {}", ipAddress, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ошибка получения статистики");
            return error;
        }
    }

    /**
     * 👥 Топ IP адресов
     */
    @GetMapping("/top-ips")
    public String topIpAddresses(@RequestParam(defaultValue = "7") int days, Model model) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);

            // Получаем топ IP за период
            List<Object[]> topIps = visitorLogRepository.getTopIpsByTimeBetween(
                    startDate, LocalDateTime.now(), PageRequest.of(0, 50));

            model.addAttribute("topIps", topIps);
            model.addAttribute("selectedDays", days);

            // Общая статистика
            long totalVisits = visitorLogRepository.countByVisitTimeAfter(startDate);
            long uniqueIps = visitorLogRepository.countDistinctIpsByTimeBetween(startDate, LocalDateTime.now());

            model.addAttribute("totalVisits", totalVisits);
            model.addAttribute("uniqueIps", uniqueIps);

        } catch (Exception e) {
            log.error("Error loading top IPs: {}", e.getMessage());
            model.addAttribute("error", "Ошибка загрузки топа IP адресов");
        }

        return "admin/analytics/top-ips";
    }



    private Map<String, Object> createEmptyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", 0L);
        stats.put("weeklyVisits", 0L);
        stats.put("mobilePercentage", 0.0);
        stats.put("topCountries", new ArrayList<>());
        stats.put("topPages", new ArrayList<>());
        return stats;
    }

    private void setFallbackData(Model model) {
        model.addAttribute("stats", createEmptyStats());
        model.addAttribute("totalVisits", 0L);
        model.addAttribute("todayVisits", 0L);
        model.addAttribute("todayUnique", 0L);
        model.addAttribute("topPages", new ArrayList<>());
        model.addAttribute("topCountries", new ArrayList<>());
        model.addAttribute("recentVisitors", new ArrayList<>());
    }
}
