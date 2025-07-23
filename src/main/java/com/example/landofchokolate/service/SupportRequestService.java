package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.supportRequestDto.CreateSupportRequestDto;
import com.example.landofchokolate.dto.supportRequestDto.SupportRequestResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupportRequestService {


    SupportRequestResponseDto createSupport(CreateSupportRequestDto createSupportRequestDto);
    SupportRequestResponseDto getById(Long id);

    // ИСПРАВЛЕНО: убрал List<> внутри PagedResponse
    PagedResponse<SupportRequestResponseDto> getAllSupport(Pageable pageable);



    void deleteById(Long id);
}
