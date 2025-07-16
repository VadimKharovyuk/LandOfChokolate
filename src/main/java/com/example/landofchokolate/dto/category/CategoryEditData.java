package com.example.landofchokolate.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class CategoryEditData {
    private CreateCategoryDto categoryDto;
    private String currentImageUrl;
}
