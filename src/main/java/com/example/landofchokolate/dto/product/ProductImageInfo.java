package com.example.landofchokolate.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public  class ProductImageInfo {
    private Long id;
    private String imageUrl;
    private String imageId;
    private Integer sortOrder;
    private Boolean isMain;
    private String altText;
}
