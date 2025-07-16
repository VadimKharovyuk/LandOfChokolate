package com.example.landofchokolate.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryDto {

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 2, max = 50, message = "Название должно содержать от 2 до 50 символов")
    private String name;

    @NotBlank(message = "Описание  не может быть пустым")
    @Size(min = 2, max = 90, message = "Описание должно содержать от 2 до 90 символов")
    private String  shortDescription;

}
