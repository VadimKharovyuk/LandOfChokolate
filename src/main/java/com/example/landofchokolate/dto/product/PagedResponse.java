package com.example.landofchokolate.dto.product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private PageMetadata metadata;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageMetadata {
        private int page;           // Текущая страница (0-based)
        private int size;           // Размер страницы
        private long totalElements; // Общее количество элементов
        private int totalPages;     // Общее количество страниц
        private boolean first;      // Первая страница?
        private boolean last;       // Последняя страница?
        private boolean hasNext;    // Есть следующая страница?
        private boolean hasPrevious; // Есть предыдущая страница?
    }

    // Конструктор для удобства создания из Spring Page
    public PagedResponse(List<T> content, org.springframework.data.domain.Page<?> page) {
        this.content = content;
        this.metadata = new PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}