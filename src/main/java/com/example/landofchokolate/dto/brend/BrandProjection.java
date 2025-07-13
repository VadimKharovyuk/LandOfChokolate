package com.example.landofchokolate.dto.brend;

import java.time.LocalDateTime;

// Интерфейс проекции
public interface BrandProjection {
    Long getId();
    String getName();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}