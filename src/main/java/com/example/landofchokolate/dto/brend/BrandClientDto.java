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
    private String imageUrl;
    private String slug;


    private String metaTitle;
    private String metaDescription;
}
