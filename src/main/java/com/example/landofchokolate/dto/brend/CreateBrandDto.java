package com.example.landofchokolate.dto.brend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandDto {
    @NotBlank(message = "Название бренда не может быть пустым")
    @Size(min = 2, max = 100, message = "Название должно содержать от 2 до 100 символов")
    private String name;
}