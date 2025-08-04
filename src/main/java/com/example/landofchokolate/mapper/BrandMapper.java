package com.example.landofchokolate.mapper;
import com.example.landofchokolate.dto.brend.BrandClientDto;
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
        brand.setShortDescription(createBrandDto.getShortDescription());

        return brand;
    }

    // Метод для обновления существующей entity
    public void updateEntityFromDto(CreateBrandDto dto, Brand existingBrand) {
        if (dto == null || existingBrand == null) {
            return;
        }

        existingBrand.setName(dto.getName());
        existingBrand.setDescription(dto.getDescription());
        existingBrand.setMetaDescription(dto.getMetaDescription());
        existingBrand.setMetaTitle(dto.getMetaTitle());
        existingBrand.setShortDescription(dto.getShortDescription());
    }

    // Конвертация для админской панели (полная информация)
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
        dto.setShortDescription(brand.getShortDescription());

        return dto;
    }

    // 🆕 Конвертация для клиентской части (без технической информации)
    public BrandClientDto toClientDto(Brand brand) {
        if (brand == null) {
            return null;
        }

        return new BrandClientDto(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getShortDescription(),     // ✅ 4-й параметр
                brand.getImageUrl(),             // ✅ 5-й параметр
                brand.getSlug(),                 // ✅ 6-й параметр
                brand.getMetaTitle(),            // ✅ 7-й параметр
                brand.getMetaDescription()       // ✅ 8-й параметр
        );
    }

    // Списки для админской панели
    public List<BrandResponseDto> toResponseDtoList(List<Brand> brands) {
        if (brands == null || brands.isEmpty()) {
            return new ArrayList<>();
        }

        return brands.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 🆕 Списки для клиентской части
    public List<BrandClientDto> toClientDtoList(List<Brand> brands) {
        if (brands == null || brands.isEmpty()) {
            return new ArrayList<>();
        }

        return brands.stream()
                .map(this::toClientDto)
                .collect(Collectors.toList());
    }
}