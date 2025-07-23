package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.supportRequestDto.CreateSupportRequestDto;
import com.example.landofchokolate.dto.supportRequestDto.SupportRequestResponseDto;
import org.springframework.data.domain.Pageable;

public interface SupportRequestService {


    SupportRequestResponseDto createSupport(CreateSupportRequestDto createSupportRequestDto);
    SupportRequestResponseDto getById(Long id);
    PagedResponse<SupportRequestResponseDto> getAllSupport(Pageable pageable);
    void deleteById(Long id);
}
