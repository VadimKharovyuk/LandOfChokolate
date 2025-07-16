package com.example.landofchokolate.dto.brend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandFilterDto {
    private Long id;
    private String name;
}