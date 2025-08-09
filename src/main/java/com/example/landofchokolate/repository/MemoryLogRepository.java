package com.example.landofchokolate.repository;


import com.example.landofchokolate.model.MemoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemoryLogRepository extends JpaRepository<MemoryLog, Long> {

    /**
     * Найти логи за последние N часов
     */
    @Query("SELECT m FROM MemoryLog m WHERE m.timestamp >= :startTime ORDER BY m.timestamp DESC")
    List<MemoryLog> findByTimestampAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * Найти критические логи за период
     */
    @Query("SELECT m FROM MemoryLog m WHERE m.isCritical = true AND m.timestamp BETWEEN :startTime AND :endTime ORDER BY m.timestamp DESC")
    List<MemoryLog> findCriticalLogsBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);


    /**
     * Получить средний процент использования за период
     */
    @Query("SELECT AVG(m.usagePercentage) FROM MemoryLog m WHERE m.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageUsagePercentage(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * Количество критических событий за последние 24 часа
     */
    @Query("SELECT COUNT(m) FROM MemoryLog m WHERE m.isCritical = true AND m.timestamp >= :startTime")
    Long countCriticalEventsAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * Удалить логи старше указанного времени (для очистки)
     */
    void deleteByTimestampBefore(LocalDateTime timestamp);


}