package com.example.landofchokolate.dto.brend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandDto {
    @NotBlank(message = "Название бренда не может быть пустым")
    @Size(min = 2, max = 100, message = "Название должно содержать от 2 до 100 символов")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 10, max = 1000, message = "Описание должно содержать от 10 до 1000 символов")
    private String description;

    private MultipartFile image;
}