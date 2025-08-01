package com.example.landofchokolate.dto.novaposhta;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// dto/novaposhta/City.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    @NotBlank
    private String ref;

    @NotBlank
    private String description;

    private String descriptionRu;
}
