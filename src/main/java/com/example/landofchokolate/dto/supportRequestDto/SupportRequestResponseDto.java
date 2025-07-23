package com.example.landofchokolate.dto.supportRequestDto;

import com.example.landofchokolate.enums.SupportTopic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupportRequestResponseDto {

    private Long id;
    private String email;
    private String phoneNumber;
    private SupportTopic topic;
    private String message;
    private LocalDateTime createdAt;
}
