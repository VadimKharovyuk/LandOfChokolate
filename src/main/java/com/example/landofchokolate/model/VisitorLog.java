package com.example.landofchokolate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "visitor_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌐 Основная информация о запросе
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "requested_url", columnDefinition = "TEXT")
    private String requestedUrl;

    @Column(name = "referer_url", columnDefinition = "TEXT")
    private String refererUrl;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    // 📱 Информация об устройстве (парсится из userAgent)
    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(name = "browser_name", length = 100)
    private String browserName;

    @Column(name = "is_mobile")
    private Boolean isMobile = false;

    // 🌍 Геолокация (базовая)
    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "isp", length = 200)
    private String isp;

    // ⏰ Время посещения
    @CreationTimestamp
    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime;

    // 📊 Базовая аналитика
    @Column(name = "is_bot")
    private Boolean isBot = false;

    @Column(name = "is_first_visit")
    private Boolean isFirstVisit = true;

    // 🛠️ Utility методы
    public boolean isFromUkraine() {
        return country != null &&
                (country.equalsIgnoreCase("Ukraine") ||
                        country.equalsIgnoreCase("Україна"));
    }

    public boolean isFromKyiv() {
        return city != null &&
                (city.equalsIgnoreCase("Kyiv") ||
                        city.equalsIgnoreCase("Kiev") ||
                        city.equalsIgnoreCase("Київ"));
    }

    public String getDeviceInfo() {
        return String.format("%s - %s (%s)",
                operatingSystem != null ? operatingSystem : "Unknown",
                browserName != null ? browserName : "Unknown",
                isMobile ? "Mobile" : "Desktop");
    }

    public String getLocationInfo() {
        if (city != null && country != null) {
            return String.format("%s, %s", city, country);
        } else if (country != null) {
            return country;
        } else {
            return "Unknown location";
        }
    }
}