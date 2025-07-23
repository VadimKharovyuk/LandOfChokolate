package com.example.landofchokolate.dto.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDto {

    @NotBlank(message = "Название продукта не может быть пустым")
    @Size(min = 2, max = 100, message = "Название должно содержать от 2 до 100 символов")
    private String name;

    @NotNull(message = "Цена не может быть пустой")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @Digits(integer = 8, fraction = 2, message = "Цена должна иметь максимум 8 цифр до запятой и 2 после")
    private BigDecimal price;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Max(value = 999999, message = "Количество не может превышать 999999")
    private Integer stockQuantity;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    @NotNull(message = "Бренд обязателен")
    private Long brandId;

    // Новое изображение (опционально при обновлении)
    private MultipartFile image;

    private Boolean isRecommendation = false;

    // Флаг для удаления текущего изображения
    private Boolean removeCurrentImage = false;
}