package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.supportRequestDto.CreateSupportRequestDto;
import com.example.landofchokolate.dto.supportRequestDto.SupportRequestResponseDto;
import com.example.landofchokolate.model.SupportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class SupportRequestMapper {

    public SupportRequest toEntity(CreateSupportRequestDto dto) {
        return SupportRequest.builder()
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .topic(dto.getTopic())
                .message(dto.getMessage())
                .build();
    }

    public SupportRequestResponseDto toDto(SupportRequest entity) {
        return SupportRequestResponseDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .topic(entity.getTopic())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
