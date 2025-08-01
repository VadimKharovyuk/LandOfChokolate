package com.example.landofchokolate.dto.novaposhta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    private String ref;
    private String description;
    private String cityRef;
    private String address;
}