package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.visitor.GeoLocationInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
public class GeoLocationService {

    private final RestTemplate restTemplate;
    private final Map<String, GeoLocationInfo> cache = new ConcurrentHashMap<>();
    private final long CACHE_TTL = 24 * 60 * 60 * 1000; // 24 часа
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    public GeoLocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 🌐 Получение геолокации с кешированием
     */
    public GeoLocationInfo getLocationByIp(String ipAddress) {
        if (isLocalIp(ipAddress)) {
            return createLocalLocationInfo();
        }

        // Проверяем кеш
        GeoLocationInfo cached = getCachedLocation(ipAddress);
        if (cached != null) {
            log.debug("Using cached geo location for IP: {}", ipAddress);
            return cached;
        }

        try {
            String url = "http://ip-api.com/json/" + ipAddress + "?fields=status,country,city,isp,query";

            log.debug("Getting geo location for IP: {}", ipAddress);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                GeoLocationInfo info = new GeoLocationInfo();
                info.setCountry((String) response.get("country"));
                info.setCity((String) response.get("city"));
                info.setIsp((String) response.get("isp"));

                // Кешируем результат
                cacheLocation(ipAddress, info);

                log.debug("Geo location found: {} - {}", info.getCountry(), info.getCity());
                return info;
            } else {
                log.warn("Failed to get geo location for IP: {} - Status: {}",
                        ipAddress, response != null ? response.get("status") : "null response");
            }

        } catch (Exception e) {
            log.error("Error getting geo location for IP {}: {}", ipAddress, e.getMessage());
        }

        return null;
    }

    /**
     * 💾 Простое кеширование в памяти
     */
    private GeoLocationInfo getCachedLocation(String ipAddress) {
        Long timestamp = cacheTimestamps.get(ipAddress);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_TTL) {
            return cache.get(ipAddress);
        }

        // Удаляем устаревший кеш
        cache.remove(ipAddress);
        cacheTimestamps.remove(ipAddress);
        return null;
    }

    private void cacheLocation(String ipAddress, GeoLocationInfo info) {
        cache.put(ipAddress, info);
        cacheTimestamps.put(ipAddress, System.currentTimeMillis());

        // Ограничиваем размер кеша
        if (cache.size() > 10000) {
            cleanOldCache();
        }
    }

    private void cleanOldCache() {
        long now = System.currentTimeMillis();
        cacheTimestamps.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > CACHE_TTL) {
                cache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    private boolean isLocalIp(String ip) {
        if (ip == null || ip.isEmpty()) return true;

        return ip.equals("127.0.0.1") ||
                ip.equals("0:0:0:0:0:0:0:1") ||
                ip.equals("::1") ||
                ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.equals("localhost");
    }

    private GeoLocationInfo createLocalLocationInfo() {
        GeoLocationInfo info = new GeoLocationInfo();
        info.setCountry("Ukraine");
        info.setCity("Kyiv");
        info.setIsp("Local Development");
        return info;
    }
}