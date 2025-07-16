package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.product.ProductListResponseDto;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ClientProductService {

    /**
     * Получить все товары без фильтров
     */
    ProductListResponseDto getAllProducts(Pageable pageable);

    /**
     * Получить товары с фильтрами
     */
    ProductListResponseDto getAllProductsWithFilters(Pageable pageable,
                                                     String searchName,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice,
                                                     Long categoryId,
                                                     Long brandId,
                                                     String stockStatus);
}