package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.VisitorLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorLogRepository  extends JpaRepository<VisitorLog, Long> {

    Long countByVisitTimeAfter(LocalDateTime after);

    @Query("SELECT CASE WHEN COUNT(v2) = 0 THEN 0.0 " +
            "ELSE (COUNT(v) * 100.0 / COUNT(v2)) END " +
            "FROM VisitorLog v, VisitorLog v2 WHERE v.isMobile = true")
    Double getMobilePercentage();
    @Query("SELECT v.country, COUNT(v) FROM VisitorLog v " +
            "WHERE v.country IS NOT NULL " +
            "GROUP BY v.country ORDER BY COUNT(v) DESC")
    List<Object[]> getTopCountries(Pageable pageable);

    @Query("SELECT v.requestedUrl, COUNT(v) FROM VisitorLog v " +
            "WHERE v.requestedUrl IS NOT NULL " +
            "GROUP BY v.requestedUrl ORDER BY COUNT(v) DESC")
    List<Object[]> getTopPages(Pageable pageable);

    // Остальные методы без изменений
    Long countByVisitTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM VisitorLog v " +
            "WHERE v.visitTime BETWEEN :start AND :end")
    Long countUniqueVisitorsByTimeBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT v.requestedUrl, COUNT(v) FROM VisitorLog v " +
            "WHERE v.visitTime BETWEEN :start AND :end AND v.requestedUrl IS NOT NULL " +
            "GROUP BY v.requestedUrl ORDER BY COUNT(v) DESC")
    List<Object[]> getTopPagesByTimeBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            Pageable pageable);

    @Query("SELECT v.country, COUNT(v) FROM VisitorLog v " +
            "WHERE v.visitTime BETWEEN :start AND :end AND v.country IS NOT NULL " +
            "GROUP BY v.country ORDER BY COUNT(v) DESC")
    List<Object[]> getTopCountriesByTimeBetween(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                Pageable pageable);





    // Все посещения по IP (сортировка по времени)
    List<VisitorLog> findByIpAddressOrderByVisitTimeDesc(String ipAddress);

    // Количество посещений по IP
    Long countByIpAddress(String ipAddress);

    // Посещения IP за период
    List<VisitorLog> findByIpAddressAndVisitTimeBetweenOrderByVisitTimeDesc(
            String ipAddress, LocalDateTime start, LocalDateTime end);

    // Уникальные страницы по IP
    @Query("SELECT DISTINCT v.requestedUrl FROM VisitorLog v WHERE v.ipAddress = :ipAddress")
    List<String> findUniquePagesByIp(@Param("ipAddress") String ipAddress);

    // Самые посещаемые страницы для IP
    @Query("SELECT v.requestedUrl, COUNT(v) FROM VisitorLog v " +
            "WHERE v.ipAddress = :ipAddress " +
            "GROUP BY v.requestedUrl ORDER BY COUNT(v) DESC")
    List<Object[]> getTopPagesByIp(@Param("ipAddress") String ipAddress, Pageable pageable);



    // 🔍 Для поиска IP
    @Query("SELECT DISTINCT v.ipAddress FROM VisitorLog v WHERE v.ipAddress LIKE :pattern ORDER BY v.ipAddress")
    List<String> findDistinctIpAddressesStartingWith(@Param("pattern") String pattern);

    // 👥 Топ IP за период
    @Query("SELECT v.ipAddress, COUNT(v), MAX(v.country), MAX(v.city) FROM VisitorLog v " +
            "WHERE v.visitTime BETWEEN :start AND :end " +
            "GROUP BY v.ipAddress ORDER BY COUNT(v) DESC")
    List<Object[]> getTopIpsByTimeBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          Pageable pageable);

    // 📊 Количество уникальных IP за период
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM VisitorLog v " +
            "WHERE v.visitTime BETWEEN :start AND :end")
    Long countDistinctIpsByTimeBetween(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}

