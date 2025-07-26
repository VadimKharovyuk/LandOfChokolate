package com.example.landofchokolate.dto.category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryNavDto {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;



}