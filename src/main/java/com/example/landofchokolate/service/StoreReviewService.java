package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.storeReview.CreateStoreReviewDTO;
import com.example.landofchokolate.dto.storeReview.StoreReviewResponseDTO;
import org.springframework.data.domain.Pageable;


public interface StoreReviewService {
    StoreReviewResponseDTO createReview(CreateStoreReviewDTO dto);

    void deleteReview(Long id) ;

    // Пагинированный метод для получения всех отзывов
    PagedResponse<StoreReviewResponseDTO> getAllReviews(Pageable pageable);

}
