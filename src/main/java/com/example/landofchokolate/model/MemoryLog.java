package com.example.landofchokolate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "memory_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "render_plan", nullable = false, length = 50)
    private String renderPlan;

    @Column(name = "memory_limit_mb", nullable = false)
    private Long memoryLimitMb;

    @Column(name = "heap_used_mb", nullable = false)
    private Long heapUsedMb;

    @Column(name = "heap_max_mb", nullable = false)
    private Long heapMaxMb;

    @Column(name = "non_heap_used_mb", nullable = false)
    private Long nonHeapUsedMb;

    @Column(name = "total_used_mb", nullable = false)
    private Long totalUsedMb;

    @Column(name = "remaining_mb", nullable = false)
    private Long remainingMb;

    @Column(name = "usage_percentage", nullable = false)
    private Double usagePercentage;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemoryStatus status;

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical;

    @Column(name = "is_warning", nullable = false)
    private Boolean isWarning;

    @Column(name = "warning_threshold_mb", nullable = false)
    private Long warningThresholdMb;

    @Column(name = "critical_threshold_mb", nullable = false)
    private Long criticalThresholdMb;

    @Column(name = "render_price", nullable = false)
    private Integer renderPrice;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "jvm_flags", columnDefinition = "TEXT")
    private String jvmFlags;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum MemoryStatus {
        OPTIMAL,    // < 80%
        WARNING,    // 80-90%
        CRITICAL    // > 90%
    }
}