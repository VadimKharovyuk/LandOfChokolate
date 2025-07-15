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

    // Добавить для полной навигации:
    private Integer totalPages;        // Общее количество страниц
    private Integer nextPage;          // Номер следующей страницы (если есть)
    private Integer previousPage;      // Номер предыдущей страницы (если есть)
    private List<Integer> pageNumbers; // Список номеров страниц для отображения
}