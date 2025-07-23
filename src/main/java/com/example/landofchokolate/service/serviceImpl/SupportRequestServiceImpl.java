package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.supportRequestDto.CreateSupportRequestDto;
import com.example.landofchokolate.dto.supportRequestDto.SupportRequestResponseDto;
import com.example.landofchokolate.mapper.SupportRequestMapper;
import com.example.landofchokolate.model.SupportRequest;
import com.example.landofchokolate.repository.SupportRequestRepository;
import com.example.landofchokolate.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportRequestServiceImpl implements SupportRequestService {
    private final SupportRequestRepository supportRequestRepository ;
    private final SupportRequestMapper supportRequestMapper ;


    @Override
    public SupportRequestResponseDto createSupport(CreateSupportRequestDto createSupportRequestDto) {

        SupportRequest supportRequest = supportRequestMapper.toEntity(createSupportRequestDto);

        // Сохранение
        SupportRequest saved = supportRequestRepository.save(supportRequest);

        SupportRequestResponseDto responseDto = supportRequestMapper.toDto(saved);

        return responseDto;
    }

    @Override
    public SupportRequestResponseDto getById(Long id) {

        SupportRequest supportRequest = supportRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support request not found with ID: " + id));

        return supportRequestMapper.toDto(supportRequest);
    }

    @Override
    public PagedResponse<SupportRequestResponseDto> getAllSupport(Pageable pageable) {

        // Получение Page<SupportRequest>
        Page<SupportRequest> supportRequestPage = supportRequestRepository.findAll(pageable);

        // Маппинг в DTO
        List<SupportRequestResponseDto> content = supportRequestPage.getContent()
                .stream()
                .map(supportRequestMapper::toDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, supportRequestPage);
    }

    @Override
    public void deleteById(Long id) {
        supportRequestRepository.deleteById(id);
    }
}

