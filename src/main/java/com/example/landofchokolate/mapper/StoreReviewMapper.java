package com.example.landofchokolate.mapper;
import com.example.landofchokolate.dto.storeReview.CreateStoreReviewDTO;
import com.example.landofchokolate.dto.storeReview.StoreReviewResponseDTO;
import com.example.landofchokolate.model.StoreReview;
import org.springframework.stereotype.Component;

@Component
public class StoreReviewMapper {

    // Конвертация DTO в Entity
    public StoreReview toEntity(CreateStoreReviewDTO dto) {
        StoreReview review = new StoreReview();
        review.setName(dto.getName());
        review.setComment(dto.getComment());
        review.setRating(dto.getRating());
        return review;
    }

    // Конвертация Entity в Response DTO
    public StoreReviewResponseDTO toResponseDTO(StoreReview entity) {
        return new StoreReviewResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getComment(),
                entity.getRating(),
                entity.getCreated()
        );
    }
}

