package com.example.landofchokolate.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryListPublicDto {
    private List<CategoryPublicDto> categories;


    // Основная информация
    private Integer totalCount;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;

    // Навигация
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer nextPage;
    private Integer previousPage;
    private List<Integer> pageNumbers;

    // 🆕 Дополнительные удобства (опционально)
    private Integer startItem;          // номер первого элемента на странице
    private Integer endItem;            // номер последнего элемента на странице
    private String sortBy;              // текущая сортировка
    private String sortDirection;       // направление сортировки

    // 🆕 Удобные методы
    public boolean isEmpty() {
        return categories == null || categories.isEmpty();
    }

    public Integer getDisplayCurrentPage() {
        return currentPage + 1; // для отображения с 1, а не с 0
    }

}
