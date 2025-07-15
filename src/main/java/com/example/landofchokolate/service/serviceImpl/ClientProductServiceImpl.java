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

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientProductServiceImpl implements ClientProductService {

    private final ProductRepository productRepository;
    private static final int PAGE_SIZE_LARGE = 6;



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

        // Вычисляем данные для навигации
        int currentPageNumber = productDtoPage.getNumber() + 1; // +1 для отображения (1-based)
        int totalPages = productDtoPage.getTotalPages();

        return ProductListResponseDto.builder()
                .products(productDtoPage.getContent())
                .totalCount((int) productDtoPage.getTotalElements())
                .hasNext(productDtoPage.hasNext())
                .hasPrevious(productDtoPage.hasPrevious())
                .currentPage(currentPageNumber)
                .pageSize(productDtoPage.getSize())
                // Новые поля для полной навигации:
                .totalPages(totalPages)
                .nextPage(productDtoPage.hasNext() ? currentPageNumber + 1 : null)
                .previousPage(productDtoPage.hasPrevious() ? currentPageNumber - 1 : null)
                .pageNumbers(generatePageNumbers(currentPageNumber, totalPages))
                .build();
    }

    // Вспомогательный метод для генерации списка номеров страниц
    private List<Integer> generatePageNumbers(int currentPage, int totalPages) {
        List<Integer> pageNumbers = new ArrayList<>();

        if (totalPages <= 7) {
            // Если страниц мало, показываем все
            for (int i = 1; i <= totalPages; i++) {
                pageNumbers.add(i);
            }
        } else {
            // Показываем страницы вокруг текущей (как в Google)
            int start = Math.max(1, currentPage - 2);
            int end = Math.min(totalPages, currentPage + 2);

            // Всегда показываем первую страницу
            if (start > 1) {
                pageNumbers.add(1);
                if (start > 2) {
                    pageNumbers.add(-1); // -1 означает "..."
                }
            }

            // Добавляем страницы вокруг текущей
            for (int i = start; i <= end; i++) {
                pageNumbers.add(i);
            }

            // Всегда показываем последнюю страницу
            if (end < totalPages) {
                if (end < totalPages - 1) {
                    pageNumbers.add(-1); // -1 означает "..."
                }
                pageNumbers.add(totalPages);
            }
        }

        return pageNumbers;
    }
}