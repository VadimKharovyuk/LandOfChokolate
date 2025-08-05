package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.visitor.GeoLocationInfo;
import com.example.landofchokolate.model.VisitorLog;
import com.example.landofchokolate.repository.VisitorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@CacheConfig(cacheManager = "visitorAnalyticsCacheManager")
public class VisitorTrackingService {

    private final VisitorLogRepository visitorLogRepository;
    private final GeoLocationService geoLocationService;

    public VisitorTrackingService(VisitorLogRepository repository,
                                  GeoLocationService geoLocationService) {
        this.visitorLogRepository = repository;
        this.geoLocationService = geoLocationService;
    }
    @CacheEvict(value = {"visitorBasicStats", "visitorsList"}, allEntries = true)
    public void logVisit(String requestUrl, HttpServletRequest request) {
        try {
            VisitorLog visitorLog = new VisitorLog();

            // Базовая информация
            String ipAddress = getClientIpAddress(request);
            visitorLog.setIpAddress(ipAddress);
            visitorLog.setUserAgent(request.getHeader("User-Agent"));
            visitorLog.setRequestedUrl(requestUrl);
            visitorLog.setRefererUrl(request.getHeader("Referer"));
            visitorLog.setSessionId(request.getSession().getId());

            // Анализ User-Agent
            parseUserAgent(visitorLog);

            // Геолокация
            try {
                enrichWithGeoLocation(visitorLog);
            } catch (Exception e) {
                visitorLog.setCountry("Unknown");
                visitorLog.setCity("Unknown");
                visitorLog.setIsp("Unknown");
            }

            // Проверка на бота
            visitorLog.setIsBot(isBot(visitorLog.getUserAgent()));

            // Сохранение
            VisitorLog saved = visitorLogRepository.save(visitorLog);


        } catch (Exception e) {
            log.error("❌ Ошибка логирования: {}", e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr;
    }

    private void parseUserAgent(VisitorLog visitorLog ) {
        String userAgent = visitorLog.getUserAgent();
        if (userAgent == null) {
            return;
        }

        String ua = userAgent.toLowerCase();

        // Мобильное устройство
        boolean isMobile = ua.contains("mobile") || ua.contains("android") || ua.contains("iphone");
        visitorLog.setIsMobile(isMobile);

        // Операционная система
        if (ua.contains("windows")) {
            visitorLog.setOperatingSystem("Windows");
        } else if (ua.contains("mac")) {
            visitorLog.setOperatingSystem("macOS");
        } else if (ua.contains("linux")) {
            visitorLog.setOperatingSystem("Linux");
        } else if (ua.contains("android")) {
            visitorLog.setOperatingSystem("Android");
        } else if (ua.contains("ios") || ua.contains("iphone")) {
            visitorLog.setOperatingSystem("iOS");
        } else {
            visitorLog.setOperatingSystem("Unknown");
        }

        // Браузер
        if (ua.contains("chrome")) {
            visitorLog.setBrowserName("Chrome");
        } else if (ua.contains("firefox")) {
            visitorLog.setBrowserName("Firefox");
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            visitorLog.setBrowserName("Safari");
        } else if (ua.contains("edge")) {
            visitorLog.setBrowserName("Edge");
        } else {
            visitorLog.setBrowserName("Unknown");
        }
    }

    private void enrichWithGeoLocation(VisitorLog visitorLog) {
        try {
            GeoLocationInfo geoInfo = geoLocationService.getLocationByIp(visitorLog.getIpAddress());

            if (geoInfo != null) {
                visitorLog.setCountry(geoInfo.getCountry());
                visitorLog.setCity(geoInfo.getCity());
                visitorLog.setIsp(geoInfo.getIsp());
            } else {
                log.warn("⚠️ Геолокация вернула null");
                visitorLog.setCountry("Unknown");
                visitorLog.setCity("Unknown");
                visitorLog.setIsp("Unknown");
            }
        } catch (Exception e) {
            log.error("❌ Ошибка геолокации: {}", e.getMessage());
            throw e;
        }
    }

    private boolean isBot(String userAgent) {
        if (userAgent == null) return false;

        String ua = userAgent.toLowerCase();
        boolean isBot = ua.contains("bot") || ua.contains("crawler") ||
                ua.contains("spider") || ua.contains("googlebot");

        return isBot;
    }

    /**
     * 📊 Простая статистика
     */
    // 📊 Кешируем базовую статистику
    @Cacheable(value = "visitorBasicStats", key = "'basicStats'")
    public Map<String, Object> getBasicStats() {
        Map<String, Object> stats = new HashMap<>();

        try {

            Long totalVisits = visitorLogRepository.count();


            LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
            Long weeklyVisits = visitorLogRepository.countByVisitTimeAfter(weekAgo);

            stats.put("totalVisits", totalVisits);
            stats.put("weeklyVisits", weeklyVisits);

            // Безопасный расчет мобильного процента
            if (totalVisits > 0) {
                try {
                    Double mobilePercentage = visitorLogRepository.getMobilePercentage();
                    stats.put("mobilePercentage", mobilePercentage != null ? mobilePercentage : 0.0);
                } catch (Exception e) {
                    log.warn("⚠️ Ошибка расчета мобильного процента: {}", e.getMessage());
                    stats.put("mobilePercentage", 0.0);
                }
            } else {
                stats.put("mobilePercentage", 0.0);
            }

            try {
                stats.put("topCountries", visitorLogRepository.getTopCountries(PageRequest.of(0, 5)));
                stats.put("topPages", visitorLogRepository.getTopPages(PageRequest.of(0, 10)));
            } catch (Exception e) {
                log.warn("⚠️ Ошибка получения топов: {}", e.getMessage());
                stats.put("topCountries", new ArrayList<>());
                stats.put("topPages", new ArrayList<>());
            }
            log.info("📊 Базова статистика розрахована та збережена в кеш");
            return stats;

        } catch (Exception e) {
            log.error("❌ Ошибка в getBasicStats: {}", e.getMessage(), e);

            // Возвращаем пустую статистику
            stats.put("totalVisits", 0L);
            stats.put("weeklyVisits", 0L);
            stats.put("mobilePercentage", 0.0);
            stats.put("topCountries", new ArrayList<>());
            stats.put("topPages", new ArrayList<>());

            return stats;
        }
    }



    /**
     * 🔍 Получить все страницы посещенные конкретным IP
     */
    // 📄 Кешируем список по IP
    @Cacheable(value = "visitorIpStats", key = "#ipAddress + '_pages'")
    public List<VisitorLog> getPagesByIp(String ipAddress) {
        try {
            log.info("🔍 Получаем все страницы для IP: {}", ipAddress);

            List<VisitorLog> visits = visitorLogRepository.findByIpAddressOrderByVisitTimeDesc(ipAddress);

            log.info("📊 Найдено {} посещений для IP {} (сохранено в кеш)", visits.size(), ipAddress);
            return visits;

        } catch (Exception e) {
            log.error("❌ Ошибка получения страниц для IP {}: {}", ipAddress, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 📈 Статистика по IP с группировкой страниц
     */
    @Cacheable(value = "visitorIpStats", key = "#ipAddress")
    public Map<String, Object> getIpStatistics(String ipAddress) {
        try {
            Map<String, Object> stats = new HashMap<>();

            List<VisitorLog> allVisits = visitorLogRepository.findByIpAddressOrderByVisitTimeDesc(ipAddress);

            if (allVisits.isEmpty()) {
                stats.put("totalVisits", 0);
                stats.put("uniquePages", 0);
                stats.put("pageStats", new ArrayList<>());
                stats.put("firstVisit", null);
                stats.put("lastVisit", null);
                return stats;
            }

            // Базовая информация
            VisitorLog firstVisit = allVisits.get(allVisits.size() - 1); // Самый ранний
            VisitorLog lastVisit = allVisits.get(0); // Самый поздний

            stats.put("totalVisits", allVisits.size());
            stats.put("firstVisit", firstVisit.getVisitTime());
            stats.put("lastVisit", lastVisit.getVisitTime());
            stats.put("country", firstVisit.getCountry());
            stats.put("city", firstVisit.getCity());
            stats.put("isMobile", firstVisit.getIsMobile());
            stats.put("operatingSystem", firstVisit.getOperatingSystem());
            stats.put("browserName", firstVisit.getBrowserName());

            // Группировка по страницам
            Map<String, Long> pageVisits = allVisits.stream()
                    .collect(Collectors.groupingBy(
                            VisitorLog::getRequestedUrl,
                            Collectors.counting()
                    ));

            // Конвертируем в список с сортировкой
            List<Map<String, Object>> pageStats = pageVisits.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> pageInfo = new HashMap<>();
                        pageInfo.put("url", entry.getKey());
                        pageInfo.put("visits", entry.getValue());
                        return pageInfo;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("visits"), (Long) a.get("visits")))
                    .collect(Collectors.toList());

            stats.put("uniquePages", pageVisits.size());
            stats.put("pageStats", pageStats);
            log.info("🔍 Статистика для IP {} розрахована та збережена в кеш", ipAddress);
            return stats;

        } catch (Exception e) {
            log.error("❌ Ошибка получения статистики для IP {}: {}", ipAddress, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 🕐 Хронология посещений IP
     */
    @Cacheable(value = "visitorIpTimeline", key = "#ipAddress + '_' + #limit")
    public List<Map<String, Object>> getIpTimeline(String ipAddress, int limit) {
        try {
            List<VisitorLog> visits = visitorLogRepository.findByIpAddressOrderByVisitTimeDesc(ipAddress);
            log.info("🕐 Хронологія для IP {} (ліміт: {}) збережена в кеш", ipAddress, limit);
            return visits.stream()
                    .limit(limit)
                    .map(visit -> {
                        Map<String, Object> timeline = new HashMap<>();
                        timeline.put("visitTime", visit.getVisitTime());
                        timeline.put("requestedUrl", visit.getRequestedUrl());
                        timeline.put("refererUrl", visit.getRefererUrl());
                        timeline.put("userAgent", visit.getUserAgent());
                        timeline.put("sessionId", visit.getSessionId());
                        return timeline;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Ошибка получения хронологии для IP {}: {}", ipAddress, e.getMessage());
            return new ArrayList<>();
        }
    }
}