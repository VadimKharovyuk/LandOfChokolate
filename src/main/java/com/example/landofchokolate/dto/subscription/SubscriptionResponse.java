package com.example.landofchokolate.dto.subscription;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {
    private Long id;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;
}
