package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.category.CreateCategoryDto;

import java.util.List;

public interface CategoryService {

    /**
     * Создает новую категорию
     */
    CategoryResponseDto createCategory(CreateCategoryDto createCategoryDto);

    /**
     * Обновляет существующую категорию
     */
    CategoryResponseDto updateCategory(Long id, CreateCategoryDto updateCategoryDto);

    /**
     * Удаляет категорию по ID
     */
    void deleteCategory(Long id);

    /**
     * Получает категорию по ID
     */
    CategoryResponseDto getCategoryById(Long id);

    /**
     * Получает все категории
     */
    List<CategoryResponseDto> getAllCategories();

    /**
     * Поиск категорий по имени (содержит подстроку, игнорируя регистр)
     */
    List<CategoryResponseDto> getCategoriesByName(String name);


     void generateMissingSlugForAllCategories() ;
}