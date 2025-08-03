package com.example.landofchokolate.dto.storeReview;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateStoreReviewDTO {

    @NotBlank(message = "Ім’я не може бути порожнім")
    @Size(min = 2, max = 100, message = "Ім’я повинно містити від 2 до 100 символів")
    private String name;

    @NotBlank(message = "Коментар не може бути порожнім")
    @Size(min = 10, max = 1000, message = "Коментар повинен містити від 10 до 1000 символів")
    private String comment;

    @NotNull(message = "Оцінка є обов’язковою")
    @Min(value = 1, message = "Оцінка повинна бути щонайменше 1 зірка")
    @Max(value = 5, message = "Оцінка повинна бути не більше 5 зірок")
    private Integer rating;
}
