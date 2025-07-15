package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.product.ProductListDto;
import com.example.landofchokolate.dto.product.ProductListResponseDto;
import com.example.landofchokolate.repository.ProductRepository;
import com.example.landofchokolate.service.ClientProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientProductServiceImpl implements ClientProductService {

    private final ProductRepository productRepository;
    private static final int PAGE_SIZE_LARGE = 50;

    @Override
    @Transactional(readOnly = true)
    public ProductListResponseDto getAllProducts(Pageable pageable) {
        log.info("Getting products page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());

        // Создаем новый Pageable с фиксированным размером
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                PAGE_SIZE_LARGE,
                pageable.getSort()
        );

        // Получаем DTO напрямую из репозитория - один запрос!
        Page<ProductListDto> productDtoPage = productRepository.findAllProductListDto(fixedPageable);

        // Создаем и возвращаем объект ProductListResponseDto с информацией о пагинации
        return ProductListResponseDto.builder()
                .products(productDtoPage.getContent())
                .totalCount((int) productDtoPage.getTotalElements())
                .hasNext(productDtoPage.hasNext())
                .hasPrevious(productDtoPage.hasPrevious())
                .currentPage(productDtoPage.getNumber() + 1)  // currentPage (1-based)
                .pageSize(productDtoPage.getSize())
                .build();
    }
}