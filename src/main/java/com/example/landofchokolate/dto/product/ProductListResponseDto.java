package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponseDto {
    private List<ProductListDto> products;
    private Integer totalCount;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer currentPage;
    private Integer pageSize;
}