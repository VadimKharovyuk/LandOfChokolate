package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.brend.CreateBrandDto;
import com.example.landofchokolate.model.Brand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BrandMapper {

    public Brand toEntity(CreateBrandDto createBrandDto) {
        if (createBrandDto == null) {
            return null;
        }

        Brand brand = new Brand();
        brand.setName(createBrandDto.getName());
        brand.setDescription(createBrandDto.getDescription());
        brand.setSlug(createBrandDto.getSlug());

        brand.setMetaDescription(createBrandDto.getMetaDescription());
        brand.setMetaTitle(createBrandDto.getMetaTitle());

        return brand;
    }

    // Новый метод для обновления существующей entity
    public void updateEntityFromDto(CreateBrandDto dto, Brand existingBrand) {
        if (dto == null || existingBrand == null) {
            return;
        }

        existingBrand.setName(dto.getName());
        existingBrand.setDescription(dto.getDescription());

        existingBrand.setMetaDescription(dto.getMetaDescription());
        existingBrand.setMetaTitle(dto.getMetaTitle());

    }

    public BrandResponseDto toResponseDto(Brand brand) {
        if (brand == null) {
            return null;
        }

        BrandResponseDto dto = new BrandResponseDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setDescription(brand.getDescription());
        dto.setImageUrl(brand.getImageUrl());
        dto.setSlug(brand.getSlug());

        dto.setMetaDescription(brand.getMetaDescription());
        dto.setMetaTitle(brand.getMetaTitle());


        dto.setCreatedAt(brand.getCreatedAt());
        dto.setUpdatedAt(brand.getUpdatedAt());
        return dto;
    }

    public List<BrandResponseDto> toResponseDtoList(List<Brand> brands) {
        if (brands == null || brands.isEmpty()) {
            return new ArrayList<>();
        }

        return brands.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}