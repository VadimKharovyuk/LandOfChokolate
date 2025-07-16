package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.product.ProductFilterDto;
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

import java.math.BigDecimal;
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
        return getAllProductsWithFilters(pageable, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponseDto getAllProductsWithFilters(Pageable pageable,
                                                            String searchName,
                                                            BigDecimal minPrice,
                                                            BigDecimal maxPrice,
                                                            Long categoryId,
                                                            Long brandId,
                                                            String stockStatus) {

        // Создаем объект фильтров
        ProductFilterDto filters = ProductFilterDto.builder()
                .searchName(searchName)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .categoryId(categoryId)
                .brandId(brandId)
                .stockStatus(stockStatus)
                .build();

        return getAllProductsWithFilters(pageable, filters);
    }

    // Приватный метод для обработки фильтрации
    private ProductListResponseDto getAllProductsWithFilters(Pageable pageable, ProductFilterDto filters) {
        log.info("Getting products page {} with filters: {}", pageable.getPageNumber(), filters);

        // Создаем новый Pageable с фиксированным размером
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                PAGE_SIZE_LARGE,
                pageable.getSort()
        );

        // Получаем данные с учетом фильтров
        Page<ProductListDto> productDtoPage = getFilteredProducts(fixedPageable, filters);

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
                .totalPages(totalPages)
                .nextPage(productDtoPage.hasNext() ? currentPageNumber + 1 : null)
                .previousPage(productDtoPage.hasPrevious() ? currentPageNumber - 1 : null)
                .pageNumbers(generatePageNumbers(currentPageNumber, totalPages))
                .build();
    }

    // Приватный метод для выбора правильного метода репозитория
    private Page<ProductListDto> getFilteredProducts(Pageable pageable, ProductFilterDto filters) {
        if (filters == null || !hasAnyFilters(filters)) {
            // Без фильтров - используем обычный метод
            log.debug("No filters applied, using standard query");
            return productRepository.findAllProductListDto(pageable);
        } else {
            // С фильтрами - используем фильтрованный запрос
            log.debug("Applying filters: {}", filters);
            return productRepository.findAllProductListDtoWithFilters(pageable, filters);
        }
    }

    // Приватный метод для проверки наличия фильтров
    private boolean hasAnyFilters(ProductFilterDto filters) {
        return (filters.getSearchName() != null && !filters.getSearchName().trim().isEmpty()) ||
                filters.getMinPrice() != null ||
                filters.getMaxPrice() != null ||
                filters.getCategoryId() != null ||
                filters.getBrandId() != null ||
                (filters.getStockStatus() != null && !filters.getStockStatus().trim().isEmpty());
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