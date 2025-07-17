package com.example.landofchokolate.mapper;

import com.example.landofchokolate.dto.category.CategoryEditData;
import com.example.landofchokolate.dto.category.CategoryPublicDto;
import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.category.CreateCategoryDto;
import com.example.landofchokolate.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

        // 🆕 Добавляем поддержку топовых категорий
        category.setIsFeatured(createCategoryDto.getIsFeatured());

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

        // 🆕 Обновляем статус топовой категории
        existingCategory.setIsFeatured(dto.getIsFeatured());

        log.debug("Updated category entity with name: {}", dto.getName());
    }

    /**
     * Преобразует Entity в ResponseDto
     */
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

        // Добавляем новые поля
        dto.setSlug(category.getSlug());
        dto.setImageUrl(category.getImageUrl());
        dto.setImageId(category.getImageId());
        dto.setMetaTitle(category.getMetaTitle());
        dto.setMetaDescription(category.getMetaDescription());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // 🆕 Добавляем только isFeatured (без featuredOrder)
        dto.setIsFeatured(category.getIsFeatured());

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
     * Преобразует Entity в PublicDto (для публичного API) с ценовой информацией
     */
    public CategoryPublicDto toPublicDto(Category category, BigDecimal minPrice, BigDecimal maxPrice, Integer productCount) {
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

        // 🆕 Ценовая информация
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);
        dto.setProductsCount(productCount);

        // Генерируем строку с диапазоном цен
        dto.setPriceRange(generatePriceRange(minPrice, maxPrice));

        log.debug("Mapped Category entity to PublicDto: id={}, slug={}, minPrice={}, productCount={}",
                category.getId(), category.getSlug(), minPrice, productCount);
        return dto;
    }
    /**
     * Генерирует строку с диапазоном цен
     */
    private String generatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null) {
            return "Товары отсутствуют";
        }

        if (maxPrice == null || minPrice.equals(maxPrice)) {
            return String.format("от %.0f грн", minPrice);
        }

        return String.format("от %.0f до %.0f грн", minPrice, maxPrice);
    }


    /**
     * Преобразует CategoryResponseDto в CategoryEditData для редактирования
     */
    public CategoryEditData toCategoryEditData(CategoryResponseDto category) {
        if (category == null) {
            log.warn("CategoryResponseDto is null, returning null");
            return null;
        }

        //Данные для редактирования (поля формы)
        CreateCategoryDto editDto = new CreateCategoryDto();
        editDto.setName(category.getName());
        editDto.setShortDescription(category.getShortDescription());
        editDto.setIsActive(category.getIsActive());
        editDto.setMetaDescription(category.getMetaDescription());
        editDto.setMetaTitle(category.getMetaTitle());
        // 🆕 Добавляем поддержку топовых категорий
        editDto.setIsFeatured(category.getIsFeatured());

     //Данные для отображения (только показать)
        CategoryEditData editData = new CategoryEditData();
        editData.setCategoryDto(editDto);
        editData.setCurrentImageUrl(category.getImageUrl());


        log.debug("Mapped CategoryResponseDto to CategoryEditData: id={}, name={}, hasImage={}",
                category.getId(), category.getName(), category.getImageUrl() != null);

        return editData;
    }
}

