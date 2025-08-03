package com.example.landofchokolate.dto.storeReview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StoreReviewResponseDTO {

    private Long id;
    private String name;
    private String comment;
    private Integer rating;
    private LocalDateTime created;

    // Удобный метод для отображения звезд
    public String getStarsDisplay() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }
}