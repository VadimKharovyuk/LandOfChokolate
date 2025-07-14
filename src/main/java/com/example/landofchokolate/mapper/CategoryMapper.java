package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.category.CreateCategoryDto;
import com.example.landofchokolate.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CategoryMapper {

    /**
     * Преобразует CreateCategoryDto в Entity
     */
    public Category toEntity(CreateCategoryDto createCategoryDto) {
        if (createCategoryDto == null) {
            log.warn("CreateCategoryDto is null, returning null");
            return null;
        }

        Category category = new Category();
        category.setName(createCategoryDto.getName());

        log.debug("Mapped CreateCategoryDto to Category: {}", createCategoryDto.getName());
        return category;
    }

    /**
     * Обновляет существующую Entity из DTO
     */
    public void updateEntityFromDto(CreateCategoryDto dto, Category existingCategory) {
        if (dto == null || existingCategory == null) {
            log.warn("DTO or existing category is null, skipping update");
            return;
        }

        existingCategory.setName(dto.getName());

        log.debug("Updated category entity with name: {}", dto.getName());
    }

    /**
     * Преобразует Entity в ResponseDto
     */
    public CategoryResponseDto toResponseDto(Category category) {
        if (category == null) {
            log.warn("Category entity is null, returning null");
            return null;
        }

        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());

        log.debug("Mapped Category entity to ResponseDto: id={}, name={}",
                category.getId(), category.getName());
        return dto;
    }

    /**
     * Преобразует список Entity в список ResponseDto
     */
    public List<CategoryResponseDto> toResponseDtoList(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            log.debug("Categories list is null or empty, returning empty list");
            return new ArrayList<>();
        }

        List<CategoryResponseDto> responseDtos = categories.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        log.debug("Mapped {} categories to ResponseDto list", responseDtos.size());
        return responseDtos;
    }

}