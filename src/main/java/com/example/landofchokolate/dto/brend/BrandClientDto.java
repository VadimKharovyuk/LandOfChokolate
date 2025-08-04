package com.example.landofchokolate.dto.brend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandClientDto {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;    // 4-й параметр
    private String imageUrl;            // 5-й параметр
    private String slug;                // 6-й параметр
    private String metaTitle;           // 7-й параметр
    private String metaDescription;     // 8-й параметр
}
