package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.product.ProductListResponseDto;
import org.springframework.data.domain.Pageable;

public interface ClientProductService {

    ProductListResponseDto getAllProducts(Pageable pageable);
}
