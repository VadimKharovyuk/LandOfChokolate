package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.category.CategoryPublicDto;
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
        category.setShortDescription(createCategoryDto.getShortDescription());
        category.setMetaTitle(createCategoryDto.getMetaTitle());
        category.setMetaDescription(createCategoryDto.getMetaDescription());
        category.setIsActive(createCategoryDto.getIsActive());

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
        existingCategory.setShortDescription(dto.getShortDescription());
        existingCategory.setMetaTitle(dto.getMetaTitle());
        existingCategory.setMetaDescription(dto.getMetaDescription());
        existingCategory.setIsActive(dto.getIsActive());

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
        dto.setShortDescription(category.getShortDescription());

        // 🆕 Добавляем новые поля
        dto.setSlug(category.getSlug());
        dto.setImageUrl(category.getImageUrl());
        dto.setImageId(category.getImageId());
        dto.setMetaTitle(category.getMetaTitle());
        dto.setMetaDescription(category.getMetaDescription());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());



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



    /**
     * Преобразует Entity в PublicDto (для публичного API)
     */
    public CategoryPublicDto toPublicDto(Category category) {
        if (category == null) {
            log.warn("Category entity is null, returning null");
            return null;
        }

        CategoryPublicDto dto = new CategoryPublicDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setShortDescription(category.getShortDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());

        // 🆕 Добавляем SEO поля
        dto.setMetaTitle(category.getMetaTitle());
        dto.setMetaDescription(category.getMetaDescription());

        log.debug("Mapped Category entity to PublicDto: id={}, slug={}",
                category.getId(), category.getSlug());
        return dto;
    }

        /**
         * Преобразует список Entity в список PublicDto
         */
        public List<CategoryPublicDto> toPublicDtoList(List<Category> categories) {
            if (categories == null || categories.isEmpty()) {
                log.debug("Categories list is null or empty, returning empty list");
                return new ArrayList<>();
            }

            List<CategoryPublicDto> publicDtos = categories.stream()
                    .filter(category -> category.getIsActive() != null && category.getIsActive()) // только активные
                    .map(this::toPublicDto)
                    .collect(Collectors.toList());

            log.debug("Mapped {} active categories to PublicDto list", publicDtos.size());
            return publicDtos;
        }
    }

