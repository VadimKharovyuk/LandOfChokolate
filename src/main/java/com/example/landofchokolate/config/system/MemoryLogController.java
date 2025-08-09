package com.example.landofchokolate.config.system;

import com.example.landofchokolate.model.MemoryLog;
import com.example.landofchokolate.service.MemoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.render.memory.database.enabled", havingValue = "true")
public class MemoryLogController {

    private final MemoryLogService memoryLogService;


    /**
     * Получить последние логи памяти
     */
    @GetMapping("/logs")
    public ResponseEntity<List<MemoryLog>> getRecentLogs(
            @RequestParam(defaultValue = "24") int hours) {
        List<MemoryLog> logs = memoryLogService.getRecentLogs(hours);
        return ResponseEntity.ok(logs);
    }

    /**
     * Получить критические логи
     */
    @GetMapping("/logs/critical")
    public ResponseEntity<List<MemoryLog>> getCriticalLogs(
            @RequestParam(defaultValue = "24") int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();
        List<MemoryLog> criticalLogs = memoryLogService.getCriticalLogs(startTime, endTime);
        return ResponseEntity.ok(criticalLogs);
    }
}

